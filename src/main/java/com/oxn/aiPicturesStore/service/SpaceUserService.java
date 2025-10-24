package com.oxn.aiPicturesStore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oxn.aiPicturesStore.model.dto.spaceuser.SpaceUserAddRequest;
import com.oxn.aiPicturesStore.model.dto.spaceuser.SpaceUserQueryRequest;
import com.oxn.aiPicturesStore.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxn.aiPicturesStore.model.vo.SpaceUserVO;

import java.util.List;

/**
* @author 34576
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-10-24 13:36:56
*/
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间成员
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 获取查询条件
     *
     *
     * */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间(单条脱敏）
     * @param spaceUser
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser);

    /**
     * 获取空间用户(多条脱敏）
     *
     * @param spacePage
     * @return
     */
    List<SpaceUserVO> getSpaceUserVO(List<SpaceUser> spacePage);

    /**
     * 校验空间成员
     * @param spaceUser
     * @param add
     */
    void validSpaceUser(SpaceUser spaceUser,Boolean add);
}
