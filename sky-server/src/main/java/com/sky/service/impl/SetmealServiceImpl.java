package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SetmealServiceImpl implements SetmealService {

//    @Override
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;


    @Transactional
    public void addStemeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        
        // 先插入套餐，获取生成的id
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();
        
        // 处理套餐菜品关系
        List<SetmealDish> dishes = setmealDTO.getSetmealDishes();
        if (dishes != null && dishes.size() > 0) {
            dishes.forEach(dish -> {
                dish.setSetmealId(setmealId);
            });
            setmealDishMapper.insertStemealDish(dishes);
        }
    }

//    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.page(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());


    }

//    @Override
    @Transactional
    public void delete(List<Long> ids) {
        for (Long l : ids) {
            Setmeal setmeal = setmealMapper.getById(l);
            if(setmeal.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        List<Long> dishIds = setmealDishMapper.getSetmealDishIdsBySetmealId(ids);
        if (dishIds != null && dishIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        for (Long id : ids) {
            setmealMapper.delete(id);
            setmealDishMapper.deleteSD(id);
        }

    }

//    @Override
    public SetmealDTO getById(Long id) {
        // 1. 查询套餐基本信息
        Setmeal setmeal = setmealMapper.getById(id);
        
        // 2. 查询套餐包含的菜品列表
        List<SetmealDish> setmealDishes = setmealDishMapper.getById(id);
        
        // 3. 组装SetmealDTO
        SetmealDTO setmealDTO = new SetmealDTO();
        BeanUtils.copyProperties(setmeal, setmealDTO);
        setmealDTO.setSetmealDishes(setmealDishes);  // 手动设置菜品列表
        
        return setmealDTO;
    }

//    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        // 1. 更新套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        
        // 2. 删除原有的套餐菜品关系
        setmealDishMapper.deleteSD(setmealDTO.getId());
        
        // 3. 插入新的套餐菜品关系
        List<SetmealDish> dishes = setmealDTO.getSetmealDishes();
        if (dishes != null && dishes.size() > 0) {
            dishes.forEach(dish -> {
                dish.setSetmealId(setmealDTO.getId());
            });
            setmealDishMapper.insertStemealDish(dishes);
        }
    }

//    @Override
    public void changeStatus(Integer status, Long id) {
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(status == StatusConstant.ENABLE){
            //select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if(dishList != null && dishList.size() > 0){
                dishList.forEach(dish -> {
                    if(StatusConstant.DISABLE == dish.getStatus()){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
            Setmeal setmeal = Setmeal.builder()
                    .id(id).status(status).build();
            setmealMapper.update(setmeal);
        }
    }
}
