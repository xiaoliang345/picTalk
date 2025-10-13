package com.oxn.aiPicturesStore.model.dto.space;

import lombok.Data;

import java.io.Serializable;

//用户修改空间
@Data
public class SpaceEditRequest implements Serializable {

    /**
     * 空间 id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    private static final long serialVersionUID = 1L;
}
