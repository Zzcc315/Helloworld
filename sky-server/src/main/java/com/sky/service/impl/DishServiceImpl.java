package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {


    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

//    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dish) {

        Dish dish1 = new Dish();
        BeanUtils.copyProperties(dish, dish1);
        dishMapper.insert(dish1);

        Long dishId = dish1.getId();
        List<DishFlavor> flavors = dish.getFlavors();
        if (flavors != null && flavors.size() > 0) {

            flavors.forEach(flavor -> {
                flavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

//    @Override
    public PageResult pageQuery(DishPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(queryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

//    @Override
    @Transactional
    public void delete(List<Long> ids) {
        for (Long id : ids) {
            Dish dish =  dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw  new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        List<Long> setmealids = setmealDishMapper.getSetmealDishIdsBySetmealId(ids);
        if (setmealids != null && setmealids.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for (Long id : ids) {
            dishMapper.delete(id);
            dishFlavorMapper.deleteByDishId(id);
        }


    }

//    @Override


    public DishVO getByIdWithF(Long id) {
        Dish dish =  dishMapper.getById(id);
        List<DishFlavor> dishFlavors= dishFlavorMapper.getFlavorById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

//    @Override
    public void updateWithF(DishDTO dish) {
        Dish dish1 = new Dish();
        BeanUtils.copyProperties(dish, dish1);
        dishMapper.updateDish(dish1);
        dishFlavorMapper.deleteByDishId(dish.getId());
        List<DishFlavor> flavors = dish.getFlavors();
        if (flavors != null && flavors.size() > 0) {

            flavors.forEach(flavor -> {
                flavor.setDishId(dish.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

//    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

//    @Override
    public void changeStatus(Integer status, Long id) {

            Dish dish = Dish.builder()
                    .id(id)
                    .status(status)
                    .build();
            dishMapper.updateDish(dish);
    }
}
