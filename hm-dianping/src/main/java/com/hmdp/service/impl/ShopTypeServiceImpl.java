package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
        //查redis
        String key = "cache:shop:type:list";
        List<ShopType> shopList = new ArrayList<>();
        Set<String> jsonSet = stringRedisTemplate.opsForZSet().range(key, 0, -1);
        if (jsonSet != null && !jsonSet.isEmpty()) {
            for (String json : jsonSet) {
                ShopType shopType = JSONUtil.toBean(json, ShopType.class);
                shopList.add(shopType);
            }
            return Result.ok(shopList);
        }
        //查数据库
        shopList = query().orderByAsc("sort").list();
        if (shopList == null || shopList.size() == 0) {
            return Result.fail("nonono");
        }
        //添加到redis中
        for (ShopType type : shopList) {
            stringRedisTemplate.opsForZSet().add(key,JSONUtil.toJsonStr(type), type.getSort());
        }
        return Result.ok(shopList);
    }


}
