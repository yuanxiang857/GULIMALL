package com.yuanxiang.gulimall.auth.vo;
/**
 * Copyright 2021 bejson.com 
 */

import lombok.Data;

/**
 * Auto-generated: 2021-04-10 11:31:45
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */

@Data
public class SocialUserVo {

    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;


}
