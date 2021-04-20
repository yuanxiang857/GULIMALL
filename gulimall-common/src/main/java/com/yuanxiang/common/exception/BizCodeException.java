package com.yuanxiang.common.exception;

/**
 * 11:商品
 * 12：订单
 * 13：购物车
 * 14：物流
 * 15：用户
 * 21:库存
 */
public enum  BizCodeException {
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    TO_MANY_REQUEST(10002,"请求流量过大"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    USER_EXIST_EXCEPTION(15001,"用户存在异常"),
    PHONE_EXIST_EXCEPTION(15002, "手机号存在异常"),
    LOINGACCT_PASSWORD_UNVALID_EXCEPTION(15003, "账号或密码错误"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足");


    private int code;
    private String msg;

    BizCodeException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
