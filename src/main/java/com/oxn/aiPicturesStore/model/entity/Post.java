package com.oxn.aiPicturesStore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    @TableField("like_count")
    private Integer likeCount = 0;
    
    /**
     * 是否置顶 0-不置顶 1-置顶
     */
    @TableField("is_top")
    private Integer isTop = 0;

    
    // 非数据库字段，用于返回用户信息
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String userAvatar;
}