package com.oxn.aiPicturesStore.model.dto.post;

import com.oxn.aiPicturesStore.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class PostQueryRequest extends PageRequest implements Serializable {

    /**
     * 用户id
     */
    Long userId;
}
