package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.Result;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderContorller")
@RequestMapping("/admin/order")
@Api(tags = "管理端接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation("订单搜索")
    @GetMapping("/conditionSearch")
    public Result<PageResult> search( OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索{}",ordersPageQueryDTO);
        PageResult pageResult = orderService.search(ordersPageQueryDTO);
        return Result.success(pageResult);

    }

    @ApiOperation("数量统计")
    @GetMapping("/statistics")
    public Result count(){
        log.info("数量统计");
        OrderStatisticsVO orderStatisticsVO =  orderService.count();
        return Result.success(orderStatisticsVO);
    }

    @ApiOperation("订单详细")
    @GetMapping("/details/{id}")
    public Result<OrderVO> Detial(@PathVariable Long id){
        log.info("订单详细");
        OrderVO orderVO = orderService.Detail(id);
        return Result.success(orderVO);
    }


    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }


    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        orderService.admincancel(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable("id") Long id) {
        orderService.delivery(id);
        return Result.success();
    }
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id) {
        orderService.complete(id);
        return Result.success();
    }

}
