package com.oxn.aiPicturesStore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxn.aiPicturesStore.model.dto.space.SpaceAddRequest;
import com.oxn.aiPicturesStore.model.dto.space.SpaceQueryRequest;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.SpaceVO;
import com.oxn.aiPicturesStore.model.vo.UserLoginVo;

import javax.servlet.http.HttpServletRequest;

/**
* @author 34576
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-10-10 20:26:22
*/
public interface SpaceService extends IService<Space> {

    /**
     * 添加空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 获取查询条件
     *
     *
     * */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取空间(单条脱敏）
     * @param space
     * @param httpServletRequest
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest httpServletRequest);

    /**
     * 获取空间(多条脱敏）
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVO(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 校验空间
     * @param space
     * @param add
     */
    void validSpace(Space space,Boolean add);

    /**
     * 根据空间类型来构建对象
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);
}
