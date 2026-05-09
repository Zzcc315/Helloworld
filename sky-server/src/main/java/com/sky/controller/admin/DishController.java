package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.lettuce.core.cluster.event.RedirectionEventSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

//菜品管理
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;



    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dish) {
        log.info("新增菜品{}", dish);
        dishService.saveWithFlavor(dish);
        String key = "dish_" + dish.getCategoryId();
        cleanCache(key);

        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页")
    public Result<PageResult> page(DishPageQueryDTO queryDTO) {
        log.info("菜品分页{}", queryDTO);
        PageResult pageResult =  dishService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }


    @DeleteMapping
    @ApiOperation("批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除{}",ids);
        dishService.delete(ids);
        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("查询id{}",id);
        DishVO dishVO =  dishService.getByIdWithF(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dish) {
        log.info("修改菜品{}",dish);
        dishService.updateWithF(dish);
        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId) {
        log.info("根据分类id查询菜品:{}", categoryId);
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用菜品")
    public Result changeStatus(@PathVariable("status") Integer status, Long id) {
        log.info("启用禁用菜品: status={}, ids={}", status, id);
        dishService.changeStatus(status, id);
        cleanCache("dish_*");
        return Result.success();
    }
    private void cleanCache(String pattern) {
        Set key = redisTemplate.keys(pattern);
        redisTemplate.delete(key);
    }
}
