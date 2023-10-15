package com.lk.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lk.backend.model.entity.Orders;
import com.lk.backend.service.OrdersService;
import com.lk.backend.mapper.OrdersMapper;
import org.springframework.stereotype.Service;

/**
* @author k
* @description 针对表【orders(充值订单表)】的数据库操作Service实现
* @createDate 2023-10-14 21:32:17
*/
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{

}




