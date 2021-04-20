package com.yuanxiang.gulimall.cart.service.imp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yuanxiang.common.utils.R;
import com.yuanxiang.gulimall.cart.Interceptor.CartInterceptor;
import com.yuanxiang.gulimall.cart.feign.ProductFeignService;
import com.yuanxiang.gulimall.cart.service.CartService;
import com.yuanxiang.gulimall.cart.vo.Cart;
import com.yuanxiang.gulimall.cart.vo.CartItem;
import com.yuanxiang.gulimall.cart.vo.SkuInfoVo;
import com.yuanxiang.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImp implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService feignService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    private final String CART_PREFIX = "gulimall:cart:";

    @Override


    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //如果购物车中没有此商品
        String res = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> skuInfo = CompletableFuture.runAsync(() -> {
                //1、远程查询当前需要添加的商品信息
                R info = feignService.info(skuId);
                SkuInfoVo data = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setSkuId(skuId);
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setPrice(data.getPrice());
                cartItem.setTitle(data.getSkuTitle());
            }, threadPoolExecutor);
            //2、远程查询sku的组合信息
            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValue = feignService.getSkuSaleAttrValue(skuId);
                cartItem.setSkuAttr(skuSaleAttrValue);
            }, threadPoolExecutor);
            CompletableFuture.allOf(skuInfo, voidCompletableFuture).get();
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), s);
            return cartItem;
        } else {
            //如果购物车中有此商品 则只是修改它的数量
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String s = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(s, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            //1、登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            //临时购物车有数据，需要进行合并操作
            String tempCartKey=CART_PREFIX+userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(tempCartKey);
            if (cartItems != null) {
                for (CartItem cartItem : cartItems) {
                    addToCart(cartItem.getSkuId(), cartItem.getCount());
                }
                clearCart(tempCartKey);
            }
            List<CartItem> items = getCartItems(cartKey);
            cart.setItems(items);
        } else {
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    /**
     * 清空购物车
     * @param cartKey
     */
    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer checked) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(checked == 1 ? true : false);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        //1、获得当前的购物车
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(res, CartItem.class);
        cartItem.setCount(num);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getCurrentUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        }else {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            //获取所有被选中的购物项 同时要进行价格的更新
            List<CartItem> collect = cartItems.stream().filter(item -> item.getCheck()).map(item->{
                R price=feignService.getPrice(item.getSkuId());
                String data = (String)price.get("data");
                item.setPrice(new BigDecimal(data));
                 return item;
            }).collect(Collectors.toList());
            return collect;
        }
    }

    /*
    获取当前使用的购物车
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        //决定用那个购物车
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);//绑定一个key的hash操作
        return operations;
    }

    /*
    获取购物车中的购物项
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItem> collect = values.stream().map(o -> {
                String s = (String) o;
                CartItem cartItem = JSON.parseObject(s, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

}
