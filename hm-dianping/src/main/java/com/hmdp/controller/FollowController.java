package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.IFollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/follow")
public class FollowController {
    @Resource
    private IFollowService followService;

/**
 * 更新用户关注状态的接口
 * @param followUserId 被关注用户的ID
 * @param isfollow 关注状态，true表示关注，false表示取消关注
 * @return 返回操作结果
 */
    @PutMapping("/{id}/{isfollow}")
    public Result follow( Long followUserId,@PathVariable("isfollow") Boolean isfollow){
        return followService.follow(followUserId,isfollow);
    }
    @GetMapping("/or/not/{id}")
    public Result follow(@PathVariable("id") Long id ){
        return followService.isfollow(id);
    }

}
