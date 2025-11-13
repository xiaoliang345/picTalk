package com.oxn.aiPicturesStore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_like")
public class UserLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("target_id")
    private Long targetId;
    @TableField("target_type")
    private Integer targetType; // 1: post, 2: comment
    @TableField("create_time")
    private LocalDateTime createTime;
}