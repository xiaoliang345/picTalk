package com.oxn.aiPicturesStore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxn.aiPicturesStore.model.dto.post.PostAddRequest;
import com.oxn.aiPicturesStore.model.entity.Post;
import com.oxn.aiPicturesStore.model.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
* @author 34576
* @description 针对表【post】的数据库操作Service
* @createDate 2025-10-28 09:43:49
*/
public interface PostService extends IService<Post> {

    /**
     * 添加帖子
     * @param postAddRequest
     * @param LongUser
     */
    long addPost(PostAddRequest postAddRequest, User LongUser);
}
