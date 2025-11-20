package com.oxn.aiPicturesStore.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class IniteInfoVO implements Serializable {

    /**
     * 空间 ID
     */
    Long spaceId;

    /**
     * 空间名称
     */
    String spaceName;

    /**
     * 用户 ID
     */
    Long userId;

    /**
     * 用户名称
     */
    String userName;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime expireTime;

}