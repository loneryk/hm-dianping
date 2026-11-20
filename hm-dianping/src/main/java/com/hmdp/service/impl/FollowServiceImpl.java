package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.UserHolder;
import org.springframework.stereotype.Service;

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

    @Override
    public Result follow(Long followUserId, Boolean isfollow) {
        //获取登录用户
        Long userId = UserHolder.getUser().getId();
        //判断是关注还是取关
        if (isfollow){
            //关注，新增数据
            Follow follow = new Follow();
            follow.setFollowUserId(followUserId);
            follow.setUserId(userId);
            save(follow);
        }else {
            //取关 删除 delete from tb_follow where user_id = ? and follow_user_id = ?
            remove(new QueryWrapper<Follow>()
                    .eq("user_id",userId).eq("follow_user_id",followUserId));

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
}
