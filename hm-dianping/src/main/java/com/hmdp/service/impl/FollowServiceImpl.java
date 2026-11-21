package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private IUserService UserService;

    @Override
    public Result follow(Long followUserId, Boolean isfollow) {

        //获取登录用户
        Long userId = UserHolder.getUser().getId();
        String key = "follows:"+userId;
        //判断是关注还是取关
        if (isfollow){
            //关注，新增数据
            Follow follow = new Follow();
            follow.setFollowUserId(followUserId);
            follow.setUserId(userId);
            boolean isSuccess = save(follow);
            if (isSuccess){
                //把关注用户的id存入redis的set集合中
            stringRedisTemplate.opsForSet().add(key,followUserId.toString());
            }
        }else {
            //取关 删除 delete from tb_follow where user_id = ? and follow_user_id = ?
           boolean isSuccess =  remove(new QueryWrapper<Follow>()
                    .eq("user_id",userId).eq("follow_user_id",followUserId));
            if (isSuccess){
                //把关注用户的id从redis中移除
                stringRedisTemplate.opsForSet().remove(key,followUserId.toString());
            }
        }
    return null;

    }

    @Override
    public Result isfollow(Long id) {
        //获取登录用户
        Long userId = UserHolder.getUser().getId();
        //查询是否关注
        Integer count = query().eq("follow_user_id", id).eq("user_id", userId).count();
        //判断
        if (count>0){
            return Result.ok(true);
        }
        return null;
    }

    @Override
    public Result followCommons(Long id) {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        //求交集
        String key2 = "follows:" + id;
        Set<String> common = stringRedisTemplate.opsForSet().intersect(key, key2);
        if (common == null || common.isEmpty()) {
            //无交集
            return Result.ok(Collections.emptyList());
        }
        //解析id集合
        List<Long> ids = common.stream().map(Long::valueOf).collect(Collectors.toList());
        //查询用户
        List<UserDTO> users =  UserService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(users);
    }
}
