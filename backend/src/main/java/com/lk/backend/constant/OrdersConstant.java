package com.lk.backend.constant;

/**
 * 订单常量
 * @Author : lk
 * @create 2023/10/15
 */
public interface OrdersConstant {
    /**
     * 回调地址(本地部署时需要内网穿透)
     */
    String NOTIFYURL = "http://6ytvug.natappfree.cc/api/alipay/notify";

    /**
     * 未支付
     */
    String UNPAID = "unpaid";

    //  region 权限

    /**
     * 支付中
     */
    String PAYING = "paying";

    /**
     * 成功
     */
    String SUCCEED = "succeed";

    /**
     * 失败
     */
    String FAILED = "failed";
}
