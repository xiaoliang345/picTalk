package com.oxn.aiPicturesStore.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

//图片状态枚举
@Getter
public enum PictureReviewStatusEnum {

    REVIEWING("REVIEWING", 0),

    PASS("PASS", 1),

    REJECT("REJECT", 2);

    private final String status;
    private final int value;

    PictureReviewStatusEnum(String status, int value) {
        this.status = status;
        this.value = value;
    }

    public static PictureReviewStatusEnum getEnumByValue(int value) {
        if(ObjUtil.isEmpty(value))return null;
        for (PictureReviewStatusEnum valueEnum : PictureReviewStatusEnum.values()) {
            if (valueEnum.value == value) {
                return valueEnum;
            }
        }
        return null;
    }
}
