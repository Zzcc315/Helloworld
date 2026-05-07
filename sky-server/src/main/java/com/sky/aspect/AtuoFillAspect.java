package com.sky.aspect;


//自定义切面实现自动填充

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AtuoFillAspect {

    //切入点

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

//    前置通知为公共字段复制
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始填充");
        //获取当前被拦截方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autoFill =  signature.getMethod().getAnnotation(AutoFill.class);//方法注解对象
        OperationType operationType = autoFill.value();//数据库操作类型

        //获取当前被拦截方法的参数-实体对象

       Object[] args =  joinPoint.getArgs();
       if(args.length==0 ||args ==null){
           return;
       }
       Object entity =  args[0];
        //为实体对象的公共属性赋值准备数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //根据当前操作类型，为对应属性通过反射赋值
        if(operationType == OperationType.INSERT){
            //为四个公共字段复制
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (operationType == OperationType.UPDATE) {
            //为两个
            try {
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        }
    }

