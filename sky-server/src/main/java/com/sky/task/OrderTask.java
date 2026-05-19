package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder(){
        log.info("定时处理超时订单{}", LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-15);

        List<Orders> list = orderMapper.getByStatusTime(Orders.PENDING_PAYMENT, localDateTime);

        if(list != null && list.size() > 0){
            for(Orders order : list){
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时自动取消");
                order.setCheckoutTime(localDateTime);
                orderMapper.update(order);
            }
        }

    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("订单派送完成{}", LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-60);
        List<Orders> list = orderMapper.getByStatusTime(Orders.DELIVERY_IN_PROGRESS, localDateTime);
        if(list != null && list.size() > 0){
            for(Orders order : list){
                order.setStatus(Orders.COMPLETED);
                order.setCancelReason("订单派送完成");
                orderMapper.update(order);
            }
        }

    }


}
