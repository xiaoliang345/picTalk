package com.oxn.aiPicturesStore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oxn.aiPicturesStore.model.entity.FileShare;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface FileShareMapper extends BaseMapper<FileShare> {


    // 查询所有已过期记录（用于定时清理）
    @Select("SELECT * FROM file_share WHERE expires_at < #{now}")
    List<FileShare> selectExpiredShares(@Param("now") LocalDateTime now);
    
    // 根据取件码统计数量
    @Select("SELECT COUNT(*) FROM file_share WHERE share_code = #{shareCode}")
    int selectCountByShareCode(@Param("shareCode") String shareCode);
}