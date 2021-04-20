package com.yuanxiang.gulimall.product;

//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClient;
//import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuanxiang.gulimall.product.dao.AttrGroupDao;
import com.yuanxiang.gulimall.product.dao.SkuSaleAttrValueDao;
import com.yuanxiang.gulimall.product.entity.BrandEntity;
import com.yuanxiang.gulimall.product.service.BrandService;
import com.yuanxiang.gulimall.product.service.CategoryService;
import com.yuanxiang.gulimall.product.vo.SkuItemSaleAttr;
import com.yuanxiang.gulimall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallProductApplicationTests {


	@Autowired
	BrandService brandService;

	@Autowired
	CategoryService categoryService;
	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Autowired
	RedissonClient redisson;
	@Autowired
	AttrGroupDao attrGroupDao;
	@Autowired
	SkuSaleAttrValueDao skuSaleAttrValueDao;

	@Test
	public void test12(){
		List<SpuItemAttrGroupVo> attrGroupVos = attrGroupDao.getAttrGroupWithAttrsBySpuId(24L, 225L);
		System.out.println(attrGroupVos);
	}

	@Test
	public void test13() {
		List<SkuItemSaleAttr> attrs = skuSaleAttrValueDao.getSaleAttrVos(24L);
		System.out.println(attrs);
	}
	@Test
	void testRedis() {
		ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
		ops.set("hello", "world" + UUID.randomUUID().toString());
		String hello = ops.get("hello");
		String catalogJSON = ops.get("catalogJSON");
		System.out.println(hello);
		System.out.println(catalogJSON);
	}

	@Test
	public void redisson() {
		System.out.println(redisson);
	}

	@Test
	void test1() {
		Long[] paths = categoryService.findCatalogPath(231L);
		log.info("完整路径:{}", Arrays.asList(paths));
	}

	@Test
	void contextLoads() {
//		BrandEntity brandEntity = new BrandEntity();
//		brandEntity.setBrandId(1L);
//		brandEntity.setDescript("华为");
//		brandService.updateById(brandEntity);

		List<BrandEntity> list=brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
//		list.forEach((item)->{
//			System.out.println(item);
//		});
		for (BrandEntity item : list) {
			System.out.println(item);
		}
	}
	@Test
	void contextLoads1() {
		String s = DigestUtils.md5Hex("123456");
		System.out.println(s);

		String s1 = Md5Crypt.apr1Crypt("123456".getBytes());
		System.out.println(s1);
	}

	//测试文件上传
//	@Test
//	void testFileUpload() {
//		// Endpoint以杭州为例，其它Region请按实际情况填写。
////		String endpoint = "oss-cn-beijing.aliyuncs.com";
////// 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录RAM控制台创建RAM账号。
////		String accessKeyId = "LTAI5tRKPMQh7rPLWGEEnpgZ";
////		String accessKeySecret = "w1t8pwEHAz4umUqpZcAPmXYcPBEBtG";
//		String bucketName = "gulimall-yuanxiang-hello";
////// <yourObjectName>上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
//		String objectName = "C:/Users/10457/Pictures/Saved Pictures/test.jpg";
//
//// 创建OSSClient实例。
////		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//// 上传文件到指定的存储空间（bucketName）并将其保存为指定的文件名称（objectName）。
//		String content = "Hello OSS";
//		ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content.getBytes()));
//
//		System.out.println("上传成功");
//// 关闭OSSClient。
//		ossClient.shutdown();
//	}

}
