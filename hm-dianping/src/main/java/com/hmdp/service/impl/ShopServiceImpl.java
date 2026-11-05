package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    private final RedisTemplate<Object, Object> redisTemplate;

    public ShopServiceImpl(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Result queryById(Long id) {
        //1.从redis查询商铺缓存
        //解决序列化问题
        //互斥锁解决缓存击穿问题
        Shop shop = queryWithMutex(id);
        //7.返回
        return Result.ok(shop);
    }
    /**
     * 通过互斥锁解决缓存击穿问题
     * @param id 商铺id
     * @return 商铺信息
     */
    public Shop queryWithMutex(Long id) {
        //1.从redis查询商铺缓存
        //解决序列化问题
        String shopJson = (String) redisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        //2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //3.存在，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        //判断命中的是否是空值
        if (shopJson != null) {
            return null;

        }
        //4.实现缓存重建
        //4.1获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean isLock = trylock(lockKey);
            //4.2判断是否获取成功
            if (!isLock) {
                //4.3失败，休眠并重试
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            //4.4成功，根据id查询数据库
            shop = getById(id);
            if (shop == null) {
                //5.不存在，返回错误信息
                //将空值写入redis，防止缓存穿透
                redisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            //6.存在，写入redis，返回信息
            redisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, shop, 30 + RandomUtil.randomInt(10), TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //7.释放锁
            unlock(lockKey);
        }
        return shop;
    }
/**
     * 通过互斥锁解决缓存击穿问题
     * @param id 商铺id
     * @return 商铺信息
     */
    public Shop queryWithPassThrough(Long id) {
        //1.从redis查询商铺缓存
        //解决序列化问题
        String shopJson = (String) redisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        //2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //3.存在，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        //判断命中的是否是空值
        if (shopJson != null) {
            return null;

        }
        //4.不存在，根据id查询数据库
        Shop shop = getById(id);
        if (shop == null) {
            //5.不存在，返回错误信息
            //将空值写入redis，防止缓存穿透
            redisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        //6.存在，写入redis，返回信息
        // 设置随机过期时间，防止大量key同时过期（缓存雪崩）
        redisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, shop, 30 + RandomUtil.randomInt(10), TimeUnit.MINUTES);
        return shop;
    }
    /**
     * 尝试获取锁
     * @param key 锁的键
     * @return 是否获取成功
     */
    public boolean trylock(String key) {
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }
    /**
     * 释放锁
     * @param key 锁的键
     */
    public void unlock(String key) {
        redisTemplate.delete(key);
    }


    @Transactional
    @Override
    public Result updateShop(Shop shop) {
        //非空判断
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺ID不能为空");
        }

        //1.更新数据库
        updateById(shop);
        //2.删除缓存
        redisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }
}
