package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SetmealService {


    void addStemeal(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void delete(List<Long> id);

    SetmealDTO getById(Long id);

    void update(SetmealDTO setmealDTO);

    void changeStatus(Integer status, Long id);
}
