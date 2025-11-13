package com.oxn.aiPicturesStore.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.oxn.aiPicturesStore.model.entity.Post;
import com.oxn.aiPicturesStore.model.vo.CommentVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class PostVO implements Serializable {
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
    
    @TableField("is_top")
    private Integer isTop = 0;

    @TableField(exist = false)
    private List<Map<String, String>> imageUrls; // 非数据库字段，用于返回图片列表

    // 非数据库字段，用于返回用户信息
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userAvatar;

    @TableField(exist = false)
    private List<CommentVO> comments;

    private static final long serialVersionUID = 1L;

}