package com.yuanxiang.gulimall.cart.controller;

import com.yuanxiang.common.constant.AuthServerConstant;
import com.yuanxiang.gulimall.cart.Interceptor.CartInterceptor;
import com.yuanxiang.gulimall.cart.service.CartService;
import com.yuanxiang.gulimall.cart.vo.Cart;
import com.yuanxiang.gulimall.cart.vo.CartItem;
import com.yuanxiang.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;


@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItems() {
        List<CartItem> items = cartService.getCurrentUserCartItems();
        return items;
    }
    /**
     * 删除购物项
     * @param skuId
     * @return
     */
    @GetMapping("/deleteItem")
    public String checkItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }
    /**
     * 勾选购物项
     * @param skuId
     * @param checked
     * @return
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("checked") Integer checked) {
        cartService.checkItem(skuId, checked);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 改变商品的数量
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.countItem(skuId, num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }
    /**
     * 浏览器中有一个user-key：一个月后过期，用于表示用户的身份
     * 如果第一次使用jd 的购物车功能，都会给一个临时的身份
     * 浏览器以后保存，每次访问都会带上这个cookie
     * <p>
     * 登录。session有
     * 没登陆：按照cookie里面的user-key来做
     * 第一次：如果没有临时用户，帮忙创建一个临时用户。
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * RedirectAttributes ra.addFlashAttribute将数据放在session里面可以在页面取出，但是只能一次
     *                    ra.addAttribute("skuId", skuId)；将数据放到url后面
     * @return
     */
    @GetMapping("/addCartItem")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes ra) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        ra.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 返回到购物车页面，只是取值，防止反复刷新后增加商品数量
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,Model model){
        CartItem cartItem=cartService.getCartItem(skuId);
        model.addAttribute("cartItem",cartItem);
        return "success";
    }
}
