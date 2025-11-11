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

    /**
     * 帖子标题/内容
     */
    String searchText;

    /**
     * 开始时间
     */
    Data startTime;

    /**
     * 结束时间
     */
    Data endTime;

}
