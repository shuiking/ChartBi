package com.lk.backend.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.lk.backend.config.AlipayTemplate;
import com.lk.backend.constant.OrdersConstant;
import com.lk.backend.model.dto.order.OrderAddRequest;
import com.lk.backend.model.entity.Orders;
import com.lk.backend.model.entity.User;
import com.lk.backend.model.vo.PayVo;
import com.lk.backend.service.CreditService;
import com.lk.backend.service.OrdersService;
import com.lk.backend.service.UserService;
import com.lk.common.api.ErrorCode;
import com.lk.common.exception.ThrowUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付接口
 * @Author : lk
 * @create 2023/10/15
 */
@RestController
@RequestMapping("/alipay")
public class AliPayController {
    @Resource
    private OrdersService ordersService;

    @Resource
    private CreditService creditService;

    @Resource
    private UserService userService;

    @Resource
    private AlipayTemplate alipayTemplate;

    @ResponseBody
    @GetMapping(value = "/pay",produces = "text/html")
    public String pay(OrderAddRequest aliPay) throws AlipayApiException {
        //1. 插入数据库订单消息
        User loginUser = userService.getLoginUser();

        Orders orders = new Orders();
        orders.setSubject(aliPay.getSubject());
        orders.setTotalAmount(aliPay.getTotalAmount());
        orders.setUserId(loginUser.getId());
        boolean result = ordersService.save(orders);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR,"订单保存错误");
        PayVo payVo = new PayVo();
        payVo.setAlipayTradeNo(orders.getId().toString());
        payVo.setTotal_amount(orders.getTotalAmount().toString());
        payVo.setSubject(orders.getSubject());

        return alipayTemplate.pay(payVo);
    }

    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) throws Exception {
        // 判断状态是否为成功
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {

            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();

            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            String outTradeNo = params.get("out_trade_no");
            String buyerId = params.get("buyer_id");
            String alipayTradeNo = params.get("trade_no");
            // 给金额转型
            String[] total_amounts = params.get("total_amount").split("\\.");
            Integer totalAmount = Integer.valueOf(total_amounts[0]);

            String sign = params.get("sign");
            String content = AlipaySignature.getSignCheckContentV1(params);
            boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, alipayTemplate.getAlipayPublicKey(), "UTF-8"); // 验证签名
            // 支付宝验签
            if (checkSignature) {
                // 查询订单并更新状态并加积分
                // 查询当前订单消息
                Orders orders = ordersService.getById(outTradeNo);
                orders.setTradeStatus(OrdersConstant.SUCCEED);
                orders.setAlipayTradeNo(alipayTradeNo);
                orders.setBuyerId(buyerId);
                boolean result = ordersService.updateById(orders);
                ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR,"订单更新错误");
                //根据充值金额增加积分
                result = creditService.updateCredits(orders.getUserId(), 100L * totalAmount);
                ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR,"积分更新错误");
            }
        }
        return "success";
    }
}
