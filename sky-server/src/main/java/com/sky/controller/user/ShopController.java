package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@Slf4j
@Api(value = "店铺相关接口")
@RequestMapping("/user/shop")
public class ShopController {


    @Autowired
    private RedisTemplate redisTemplate;



    public static final String KEY = "SHOP_STATUS";
/*

    @ApiOperation("设置店铺营业状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置营业状态{}",status==1?"营业中":"大洋中");
        redisTemplate.opsForValue().set("shop_status",status);


        return Result.success();
    }*/

    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取营业状态为{}",status == 1?"营业中":"大洋中");
        return Result.success(status);
    }

}
