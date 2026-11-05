package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.SHOP_TYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    private final StringRedisTemplate StringRedisTemplate;


    public ShopTypeServiceImpl(RedisTemplate<Object, Object> redisTemplate, StringRedisTemplate stringRedisTemplate, ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
        this.StringRedisTemplate = stringRedisTemplate;

    }

    @Override
    public Result queryTypeList() {
        //1.从redis查询商铺类型列表
        String shopTypeListJson = (String) StringRedisTemplate.opsForValue().get(SHOP_TYPE_KEY);
        //2.判断是否存在
        if (StrUtil.isNotBlank(shopTypeListJson)) {
            //3.存在，直接返回
            List<ShopType> shopTypeList = JSONUtil.toList(shopTypeListJson, ShopType.class);
            return Result.ok(shopTypeList);
        }
        //4.不存在，从数据库查询
        List<ShopType> shopTypeList = list();
        //判断数据库是否存在
        if (shopTypeList == null||shopTypeList.isEmpty()) {
             StringRedisTemplate.opsForValue().set(SHOP_TYPE_KEY, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.fail("类型不存在");
        }

        StringRedisTemplate.opsForValue().set(SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopTypeList), RedisConstants.CACHE_SHOP_TTL, TimeUnit.SECONDS);
        return Result.ok(shopTypeList);
    }
}
