package com.oxn.aiPicturesStore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oxn.aiPicturesStore.model.dto.spaceuser.SpaceUserAddRequest;
import com.oxn.aiPicturesStore.model.dto.spaceuser.SpaceUserQueryRequest;
import com.oxn.aiPicturesStore.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.IniteInfoVO;
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
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest, User loginUser);

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
    void validSpaceUser(SpaceUser spaceUser, Boolean add, User loginUser);

    /**
     * 邀请用户加入空间
     * @param spaceId
     * @param loginUser
     * @return
     */
    Long inviteUser(Long spaceId,User loginUser);

    /**
     * 创建邀请链接
     * @param spaceId
     * @param loginUser
     */
    String createIniteLink(Long spaceId, User loginUser);

    /**
     * 获取邀请信息
     * @param inviteCode
     * @return
     */
    IniteInfoVO getInviteInfo(String inviteCode);

    /**
     * 接受邀请
     * @param inviteCode
     * @param loginUser
     * @return
     */
    boolean acceptInvite(String inviteCode, User loginUser);
}
