package com.yuanxianggulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

//先不用springboot测试
//@SpringBootTest
class GulimallMemberApplicationTests {

	@Test
	void contextLoads() {
		String s = DigestUtils.md5Hex("123456");
		System.out.println(s);
	}

	@Test
	void test() {

	}

}
