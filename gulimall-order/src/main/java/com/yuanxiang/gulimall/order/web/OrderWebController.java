package com.yuanxiang.gulimall.order.web;

import com.yuanxiang.gulimall.order.service.OrderService;
import com.yuanxiang.gulimall.order.vo.OrderConfirmVo;
import com.yuanxiang.gulimall.order.vo.OrderSubmitVo;
import com.yuanxiang.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    /**
     * 提交下单的功能
     *
     * @param vo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
        if (responseVo.getCode() == 0) {
            model.addAttribute("submitOrderResp", responseVo);
            //下单成功到支付选择页
            return "pay";
        } else {
            //下单失败回到订单确认页重新确认订单信息
            String msg="下单失败";
            switch (responseVo.getCode()){
                case 1:   msg+=":订单信息过期，请刷新后提交"; break;
                case 2:   msg+=":订单价格发生变化，请确认后条件";break;
                case 3:   msg+=":库存锁定失败，商品库存不足";break;
            }
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
