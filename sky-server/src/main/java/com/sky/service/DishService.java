package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {


    //新增菜品和口味
    public void saveWithFlavor(DishDTO dish);


    PageResult pageQuery(DishPageQueryDTO queryDTO);

    void delete(List<Long> ids);


    DishVO getByIdWithF(Long id);

    void updateWithF(DishDTO dish);

    /**
     * 根据分类id查询菜品列表
     * @param categoryId
     * @return
     */
    List<Dish> list(Long categoryId);

    /**
     * 启用禁用菜品
     * @param ids
     * @param status
     */
    void changeStatus(Integer ids, Long status);
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
