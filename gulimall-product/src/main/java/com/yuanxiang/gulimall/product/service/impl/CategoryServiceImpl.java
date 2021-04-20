package com.yuanxiang.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yuanxiang.gulimall.product.service.CategoryBrandRelationService;
import com.yuanxiang.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.common.utils.Query;

import com.yuanxiang.gulimall.product.dao.CategoryDao;
import com.yuanxiang.gulimall.product.entity.CategoryEntity;
import com.yuanxiang.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        System.out.println("--------------");
        //2、组成父子的树形结构

        //2、1找到所有的一级分类(lambda表达式、Stream流函数)
        List<CategoryEntity> list = entities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildrens(menu, entities));
                    return menu;
                }).sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                }).collect(Collectors.toList());
        return list;
    }

    /**
     * 查出父分类的ID的数组
     *
     * @param CatalogId
     * @return
     */

    @Override
    public Long[] findCatalogPath(Long CatalogId) {
        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(CatalogId, paths);

        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    public List<Long> findParentPath(Long CatalogId, List<Long> paths) {
        //1、收集当前节点的ID
        paths.add(CatalogId);
        CategoryEntity byId = this.getById(CatalogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }


    //递归获得子菜单
    public List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> categoryEntity.getParentCid() == root.getCatId())
                //1、找到子菜单
                .map(categoryEntity -> {
                    categoryEntity.setChildren(getChildrens(categoryEntity, all));
                    return categoryEntity;
                }).sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                }).collect(Collectors.toList());

        return children;
    }

    @Override
    public void removeMenusById(List<Long> aslist) {
        //TODO 1、检查当前删除的菜单，是否被其他的地方所引用  涉及到物理删除与逻辑删除
        baseMapper.deleteBatchIds(aslist);
    }

    //@CacheEvict值的是缓存一致性模式中的失效模式、当成功修改数据后，缓存就会失效。
    //@CacheEvict(evict="category",allEntries=true)//删除分区的缓存
    @Caching(evict = {
            @CacheEvict(value="category",key="'getLevel1Categorys'"),
            @CacheEvict(value="category",key="'getCatalogJson'")
    })
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    //要进行缓存的分区
    /**
     * 默认的机制
     * 1、如果缓存中有、方法不再调用
     * 2、key默认自动生成。缓存的名字：SimpleKey
     * 3、缓存value的值。默认使用jdk序列化机制，将序列化后的数据存到redis
     * 4、默认ttl时间 -1
     * 自定义的操作
     * 1、指定缓存的key   前面的value指的是分组名字 spEL语法
     * 2、指定缓存的ttl时间 配置文件中修改
     * 3、将数据保存为JSON格式
     * springcache的不足
     * 1、读模式：
     * 缓存穿透：查询一个null数据：缓存空数据:cache-null-values=true
     * 缓存击穿：大量并发查一个正好过期的数据：加锁 默认没有加锁; syne=true 加锁解决击穿
     * 缓存雪崩：大量的key同时过期：解决：加随机时间，spring.cache.redis.time-to-live
     * 2、写模式：（缓存与数据库一致）
     * 读写加锁
     * 引入Canal，感知mysql的更新去更新数据库
     * 读多写多，直接去数据库查询
     * 总结：
     *  常规数据（读多写少，即时性，一致性要求不高的数据）；完全可以使用spring-cache；
     *  特殊数据：特殊设计
     *
     * @return
     */
    @Cacheable(value={"category"},key="#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("GetCategories...");
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }


    @Cacheable(value={"category"},key = "#root.method.name")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        System.out.println("查询了数据库");
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //1、查出所有的一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(entities, 0L);
        //2、封装数据
        Map<String, List<Catalog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类、查到这个一级分类的二级分类 二级分类的父分类ID正是一级分类
            List<CategoryEntity> categoryEntities = getParent_cid(entities, v.getCatId());
            //封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //当前二级分类的三级分类
                    List<CategoryEntity> level3Catalog = getParent_cid(entities, l2.getCatId());
                    if (level3Catalog != null) {
                        List<Catalog2Vo.Catalog3Vo> collect = level3Catalog.stream().map(l3 -> {
                            //封装三级分类
                            Catalog2Vo.Catalog3Vo Catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return Catalog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(collect);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        return parent_cid;
    }

    //TODO 产生堆外内存溢出、OutOfDirectMemoryError
    //SpringBoot2.0以后默认使用lettuce作为操作redis 的客户端。它使用netty进行网络通信
    //2、lettuce的bug导致netty堆外内存溢出
    //3、可以通过-Dio.netty.maxDirectMemory进行设置
    //4、不能通过-Dio.netty.maxDirectMemory只去调大堆外内存
    //)1、升级lettuce客户端 2、切换使用jedis
    public Map<String, List<Catalog2Vo>> getCatalogJson2() {
        /**
         * 1、看那个结果缓存：缓存穿透
         * 2、设置过期时间（加随机值）：解决缓存雪崩
         * 3、加锁：解决缓存击穿
         */
        //序列化与反序列化
        //1、加入缓存逻辑，缓存中存的数据是json字符串
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {//查询一个一定不存在的数据、此处可能发生缓存穿透、高并发同时查询null
            //2、缓存中没有，查询数据库
            System.out.println("缓存不命中，查询数据库");
            Map<String, List<Catalog2Vo>> CatalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();
//            //3、查到的数据再放入库存中，将对象转为json放在缓存中
//            String s = JSON.toJSONString(CatalogJsonFromDb);
//            stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
            return CatalogJsonFromDb;
        }
        System.out.println("缓存命中，直接返回");
        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });//匿名内部类
        return result;
    }

    /**
     * 缓存数据的一致性问题
     * 1、双写模式--同时修改缓存
     * 2、失效模式--使缓存失效、读数据要重新读缓存.
     * 我们系统的一致性解决方案
     * 1、缓存的所有数据都有过期时间，数据过期下一次查询触发主动更新
     * 2、读写数据的时候，加上分布式的读写锁
     * 经常读，经常写。
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        RLock lock = redissonClient.getLock("CatalogJson-lock");
        lock.lock();
        Map<String, List<Catalog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        //1、占分布式锁
        String s = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", s, 300, TimeUnit.SECONDS);//使用原子命令进行占锁
        if (lock) {
//            stringRedisTemplate.expire("lock", 30, TimeUnit.SECONDS);//设置锁的过期时间
            System.out.println("获取分布式锁成功");
            Map<String, List<Catalog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                //lua脚本解锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), s);
            }
//            String lockValue = stringRedisTemplate.opsForValue().get("s");
//            if (lockValue != null) {
//                //因为网络问题  可能会删除其他人的锁
//                stringRedisTemplate.delete("lock");
//            }
            return dataFromDb;
        } else {
            System.out.println("获取分布式锁失败，等待重试");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();//自旋
        }
    }

    private Map<String, List<Catalog2Vo>> getDataFromDb() {
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });//匿名内部类
            return result;
        }
        System.out.println("查询了数据库");

        List<CategoryEntity> entities = baseMapper.selectList(null);

        //1、查出所有的一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(entities, 0L);
        //2、封装数据
        Map<String, List<Catalog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类、查到这个一级分类的二级分类 二级分类的父分类ID正是一级分类
            List<CategoryEntity> categoryEntities = getParent_cid(entities, v.getCatId());
            //封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //当前二级分类的三级分类
                    List<CategoryEntity> level3Catalog = getParent_cid(entities, l2.getCatId());
                    if (level3Catalog != null) {
                        List<Catalog2Vo.Catalog3Vo> collect = level3Catalog.stream().map(l3 -> {
                            //封装三级分类
                            Catalog2Vo.Catalog3Vo Catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return Catalog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(collect);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        //3、查到的数据再放入库存中，将对象转为json放在缓存中
        //此处是为了释放锁之前就保存数据  防止第二个保存到时候出问题
        String s = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
        return parent_cid;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDb() {
        //springboot中所有组件在容器中都是单例的
        //双重检验
        //TODO  分布式的情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {//本地锁
            return getDataFromDb();
        }

    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> entities, Long parentCid) {
//        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> collect = entities.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        return collect;
    }
}