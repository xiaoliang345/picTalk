package com.oxn.aiPicturesStore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("post_image")
public class PostImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("post_id")
    private Long postId;
    @TableField("image_url")
    private String imageUrl;
    @TableField("thumbnail_url")
    private String  thumbnailUrl;
    private Integer sort = 0;
}