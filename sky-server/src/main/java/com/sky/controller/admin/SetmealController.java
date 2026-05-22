package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;


    @PostMapping
    @ApiOperation("新增分类")
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐{}", setmealDTO);
        setmealService.addStemeal(setmealDTO);
        return Result.success();
    }


    @GetMapping("/page")
    @ApiOperation("分页查询")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("分页查询{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("删除")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除{}",ids);
        setmealService.delete(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("getById")
    public Result<SetmealDTO> getById(@PathVariable Long id) {
        log.info("查询id{}",id);
        SetmealDTO setmealDTO =  setmealService.getById(id);
        return Result.success(setmealDTO);
    }


    @PutMapping
    @ApiOperation("修改菜品")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改菜品");
        setmealService.update(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result changeStatus(@PathVariable Integer status,Long id) {
        log.info("更改状态");
        setmealService.changeStatus(status,id);
        return Result.success();
    }
}
