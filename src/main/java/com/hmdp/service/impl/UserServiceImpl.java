package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {

        //校验手机号

        if (RegexUtils.isPhoneInvalid(phone)) {
            //判断是否符合
            return Result.fail("手机号格式错误");
        }
        //符合生成验证码
        String code = RandomUtil.randomNumbers(6);


        //保存验证码到session   set key value ex 120
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY +phone,code,RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        //发送验证码


        log.debug("发送成功:{}",code);

        //返回ok
        return Result.ok();
    }

//    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {

        //校验手机号

        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        //校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null||!cacheCode.toString().equals(code)) {
            //不一致报错
            return Result.fail("验证码错误");
        }
        //一致，查询手机号用户

        User user = query().eq("phone", phone).one();

        //用户存在？
        if (user == null) {
            //不存在，创建并保存
            user = createUserWithPhone(phone);
        }


        //保存信息到redis1.token 令牌 2.user-hash 3.存储

        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        String tokenKey =  RedisConstants.LOGIN_CODE_KEY + token;
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(), CopyOptions.create()
                .setIgnoreNullValue(true).setFieldValueEditor((fieldName,fieldValue)->fieldValue.toString()));



        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        stringRedisTemplate.expire(tokenKey,RedisConstants.CACHE_SHOP_TTL,TimeUnit.MINUTES);
        return Result.ok(token);

    }

    private User createUserWithPhone(String phone) {
        User user = new User().setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));

        save(user);
        return user;
    }
}
