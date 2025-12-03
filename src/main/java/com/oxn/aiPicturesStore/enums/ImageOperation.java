package com.oxn.aiPicturesStore.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

//图片状态枚举
@Getter
public enum ImageOperation {

    CREATE("CREATE", 0),

    EDIT("EDIT", 1);

    private final String status;
    private final int value;

    ImageOperation(String status, int value) {
        this.status = status;
        this.value = value;
    }

    public static ImageOperation getEnumByValue(int value) {
        if(ObjUtil.isEmpty(value))return null;
        for (ImageOperation valueEnum : ImageOperation.values()) {
            if (valueEnum.value == value) {
                return valueEnum;
            }
        }
        return null;
    }
}
