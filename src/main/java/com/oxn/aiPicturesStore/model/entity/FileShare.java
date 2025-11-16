package com.oxn.aiPicturesStore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import groovy.transform.Field;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_share")
public class FileShare {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("share_code")
    private String shareCode;

    @TableField("file_name")
    private String fileName;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("ip_address")
    private String ipAddress;
}