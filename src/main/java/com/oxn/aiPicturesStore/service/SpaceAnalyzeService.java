package com.oxn.aiPicturesStore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxn.aiPicturesStore.model.dto.space.SpaceAddRequest;
import com.oxn.aiPicturesStore.model.dto.space.SpaceQueryRequest;
import com.oxn.aiPicturesStore.model.dto.space.analyze.*;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.SpaceVO;
import com.oxn.aiPicturesStore.model.vo.analyze.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 34576
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-10-10 20:26:22
 */
public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 检查用户空间分析权限
     *
     * @param spaceAnalyzeRequest
     * @param loginUser
     */
    void checkSpaceAnalyzeAuch(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser);

    /**
     * 填充查询条件
     *
     * @param spaceAnalyzeRequest
     * @param queryWrapper
     */
    void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper);

    /**
     * 查询空间使用情况
     *
     * @param spaceUsageAnalyzeRequest
     * @param loginUser
     * @return
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 获取对应分类的图片数量
     *
     * @param spaceCategoryAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 获取对应标签的图片数量
     *
     * @param spaceTagAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片各个大小数量
     *
     * @param spaceSizeAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 根据用户进行查询
     *
     * @param spaceUserAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 根据空间排名查询
     *
     * @param spaceRankAnalyzeRequest
     * @param loginUser
     */
    List<Space> getSpaceRank(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
