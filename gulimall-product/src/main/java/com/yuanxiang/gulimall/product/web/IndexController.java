package com.yuanxiang.gulimall.product.web;

import com.yuanxiang.gulimall.product.entity.CategoryEntity;
import com.yuanxiang.gulimall.product.service.CategoryService;
import com.yuanxiang.gulimall.product.vo.Catalog2Vo;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    RedissonClient redisson;
    @Autowired
    CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        //查出所有一级分类
        List<CategoryEntity> entities = categoryService.getLevel1Categorys();
        model.addAttribute("categories", entities);
        return "index";
    }

    //返回数据而不是跳转页面
    @ResponseBody
    @RequestMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        Map<String, List<Catalog2Vo>> catelogJson = categoryService.getCatalogJson();
        return catelogJson;
    }

    @ResponseBody
    @RequestMapping("/hello")
    public String hello() {
        RLock lock = redisson.getLock("my-lock");
        //加锁
        lock.lock();//阻塞式等待
//        lock.lock(10, TimeUnit.SECONDS);//锁到了之后不会自动续期   如果我们传递了锁的超时时间，就发送给redis执行脚本 如果我们未指定锁的超时时间，使用默认的3*1000；
        //只要占锁成功，就会启动一个定时任务。1/3的时间续机也就是10s
        //锁的自动续期，如果业务时间长，运行旗舰自动给续上新的30s。所自动过期被删掉
        //加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，也会在30s后默认删除
        //最佳实战
//        lock.lock(30, TimeUnit.SECONDS);
        try {
            System.out.println("加锁成功" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //此处即使锁被中断、
            System.out.println("释放锁..." + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }
    /**
     * 读写锁 rw-lock
     * 保证读的数据是最新数据
     * 修改期间，写锁是一个排他锁（互斥锁、独享锁），读锁是一个共享锁
     * 写锁如果没释放，读不了，必须等待写锁释放。
     * 读+读 相当于无锁，并发读，只会在redis中记录好，所有当前的读锁。他们都会加锁成功。
     * 写+读：等待写锁释放
     * 写+写：阻塞方式
     * 读+写：等待读锁释放
     * 只要有写的存在，都必须等待
     */

    /**
     * 信号量测试，进行限流操作
     */
    @RequestMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore rSemaphore = redisson.getSemaphore("park");
        rSemaphore.acquire();//拿一个信号 占一个车位 以后的秒杀服务？
//        boolean b = rSemaphore.tryAcquire();
        return "ok";
    }

    @ResponseBody
    @RequestMapping("/go")
    public String go() {
        RSemaphore rSemaphore = redisson.getSemaphore("park");
        rSemaphore.release();
        return "ok";
    }
    /**
     * 闭锁
     * 放假锁门，
     * 必须走完才能锁大门
     */


}
