package com.oxn.aiPicturesStore.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

@Data
public class InviteUserRequest implements Serializable {

    /**
     * 空间 ID
     */
    Long spaceId;

    /**
     * 用户 ID
     */
    Long userId;
}
