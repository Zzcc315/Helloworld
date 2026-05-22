package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> getSetmealDishIdsBySetmealId(List<Long> dishIds);




    void insertStemealDish(List<SetmealDish> dishes);

    @Delete("delete from setmeal_dish where id = #{id}")
    void deleteSD(Long id);

    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getById(Long id);
}
