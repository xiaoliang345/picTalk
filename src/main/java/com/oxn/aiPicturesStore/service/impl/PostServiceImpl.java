package com.oxn.aiPicturesStore.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.model.dto.post.PostAddRequest;
import com.oxn.aiPicturesStore.model.entity.Post;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.service.PostService;
import com.oxn.aiPicturesStore.mapper.PostMapper;
import org.springframework.stereotype.Service;

/**
* @author 34576
* @description 针对表【post】的数据库操作Service实现
* @createDate 2025-10-28 09:43:49
*/
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
    implements PostService{

    @Override
    public long addPost(PostAddRequest postAddRequest, User LongUser) {
        Post post = new Post();
        BeanUtil.copyProperties(postAddRequest,post);
        Long userId = post.getUser_id();
        if(userId==null){
            post.setUser_id(LongUser.getId());
        }
        else{
            if(userId!=LongUser.getId()){
                throw new BusinessException(StatusCode.NO_AUTH_ERROR);
            }
        }
        String imageUrl = post.getImage_url();
        if(imageUrl==null){

        }
        return 0;
    }
}




