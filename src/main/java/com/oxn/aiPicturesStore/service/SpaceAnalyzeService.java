package com.oxn.aiPicturesStore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxn.aiPicturesStore.model.dto.space.SpaceAddRequest;
import com.oxn.aiPicturesStore.model.dto.space.SpaceQueryRequest;
import com.oxn.aiPicturesStore.model.dto.space.analyze.SpaceAnalyzeRequest;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 34576
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-10-10 20:26:22
*/
public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 检查用户空间分析权限
     * @param spaceAnalyzeRequest
     * @param loginUser
     */
    void checkSpaceAnalyzeAuch(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser);

    /**
     * 填充查询条件
     * @param spaceAnalyzeRequest
     * @param queryWrapper
     */
    void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper);
}
