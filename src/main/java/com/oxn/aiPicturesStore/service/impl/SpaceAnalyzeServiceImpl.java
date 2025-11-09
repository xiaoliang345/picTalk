package com.oxn.aiPicturesStore.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.mapper.SpaceMapper;
import com.oxn.aiPicturesStore.model.dto.space.analyze.*;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.analyze.*;
import com.oxn.aiPicturesStore.service.PictureService;
import com.oxn.aiPicturesStore.service.SpaceAnalyzeService;
import com.oxn.aiPicturesStore.service.SpaceService;
import com.oxn.aiPicturesStore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author 34576
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-10-10 20:26:22
 */
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {

    @Autowired
    private UserService userService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private PictureService pictureService;

    public void checkSpaceAnalyzeAuch(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        if (queryAll || queryPublic) {
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), StatusCode.NO_AUTH_ERROR, "没有权限");
        } else {
            ThrowUtils.throwIf(spaceId == null, StatusCode.PARAMS_ERROR, "空间id为空");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, StatusCode.PARAMS_ERROR, "空间不存在");
            //spaceService.chechUserHasAuth(loginUser, space);
        }
    }

    @Override
    public void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        if (queryAll) {
            return;
        }
        if (queryPublic) {
            queryWrapper.isNull("spaceId");
            return;
        }
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(StatusCode.PARAMS_ERROR, "未指定查询范围");
    }

    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
        //查询全部空间或者公共空间
        if (spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()) {
            checkSpaceAnalyzeAuch(spaceUsageAnalyzeRequest, loginUser);
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, queryWrapper);
            List<Object> objects = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long usedSize = objects.stream().mapToLong(item -> (Long) item).sum();
            long usedCount = objects.size();
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            return spaceUsageAnalyzeResponse;
        } else {
            checkSpaceAnalyzeAuch(spaceUsageAnalyzeRequest, loginUser);
            Space space = spaceService.getById(spaceUsageAnalyzeRequest.getSpaceId());
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(space.getTotalSize());
            spaceUsageAnalyzeResponse.setUsedCount(space.getTotalCount());
            spaceUsageAnalyzeResponse.setMaxSize(space.getMaxSize());
            spaceUsageAnalyzeResponse.setMaxCount(space.getMaxCount());
            Double round = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            //空间使用容量和数量占比
            spaceUsageAnalyzeResponse.setSizeUsageRatio(round);
            round = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            spaceUsageAnalyzeResponse.setCountUsageRatio(round);
            return spaceUsageAnalyzeResponse;

        }
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        checkSpaceAnalyzeAuch(spaceCategoryAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        queryWrapper.select("category", "count(*) as count", "sum(picSize) as totalSize")
                .groupBy("category");
        List<SpaceCategoryAnalyzeResponse> list;
        list = pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(item -> {
            String category = item.get("category").toString();
            Long count = ((Number) item.get("count")).longValue();
            Long totalSize = ((Number) item.get("totalSize")).longValue();
            return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        checkSpaceAnalyzeAuch(spaceTagAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);

        queryWrapper.select("tags")
                .isNotNull("tags")
                .ne("tags", "");

        List<String> objects = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream().map(Object::toString).collect(Collectors.toList());

        // 创建 ObjectMapper 实例
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Long> result = (Map<String, Long>) objects.stream()
                .map(s -> {
                    try {
                        return objectMapper.readValue(s, new TypeReference<List<String>>() {
                        });
                    } catch (Exception e) {
                        return Collections.emptyList();
                    }
                })
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return result.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        checkSpaceAnalyzeAuch(spaceSizeAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);

        queryWrapper.select("picSize");
        List<Long> objects = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream().map(object -> (Long) object).collect(Collectors.toList());
        //定义大小范围map
        Map<String, Long> sizeRangeMap = new HashMap<>();
        sizeRangeMap.put("0MB~0.5MB", objects.stream().filter(object -> object < 512 * 1024L).count());
        sizeRangeMap.put("0.5MB~1MB", objects.stream().filter(object -> object >= 512 * 1024L && object < 1024 * 1024L).count());
        sizeRangeMap.put("1MB~1.5MB", objects.stream().filter(object -> object >= 1024 * 1024L && object < 1024L * 1024 * 1.5).count());
        sizeRangeMap.put("1.5MB~2MB", objects.stream().filter(object -> object >= 1024L * 1024 * 1.5 && object < 1024 * 1024 * 2L).count());
        List<SpaceSizeAnalyzeResponse> collect = sizeRangeMap.entrySet().stream().map(entry ->
                new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        checkSpaceAnalyzeAuch(spaceUserAnalyzeRequest, loginUser);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);

        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(userId != null, "userId", userId);

        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        ThrowUtils.throwIf(timeDimension == null, StatusCode.PARAMS_ERROR, "时间维度不能为空");
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE(createTime) AS period,COUNT(*) AS count");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(createTime, 1) AS period,COUNT(*) AS count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') AS period,COUNT(*) AS count");
                break;
            default:
                throw new BusinessException(StatusCode.PARAMS_ERROR,"时间维度错误");
        }

        queryWrapper.groupBy("period");

        List<SpaceUserAnalyzeResponse> collect = pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(map -> {
            String period = map.get("period").toString();
            Long count = (Long) map.get("count");
            return new SpaceUserAnalyzeResponse(period, count);
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<Space> getSpaceRank(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        userService.isAdmin(loginUser);
        Integer topN = spaceRankAnalyzeRequest.getTopN();
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "totalSize", "totalCount")
                .orderByDesc("totalSize")
                .last("limit " + topN);
        List<Space> list = spaceService.list(queryWrapper);
        return list;
    }


}




