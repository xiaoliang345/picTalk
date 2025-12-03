package com.oxn.aiPicturesStore.enums;

import lombok.Getter;

@Getter
public enum SpaceLevelEnum {


    COMMON("普通版",0,300,300*1024*1024),
    PROFESSIONAL("专业版",1,1000,1000*1024*1024),
    FLAGSHIP("旗舰版",2,3000,3000*1024*1024);



    private final String text;
    private final Integer value;
    private final Integer maxCount;
    private final long maxSize;

    SpaceLevelEnum(String text, int value, int maxCount, int maxSize) {
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }


    public static SpaceLevelEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for(SpaceLevelEnum anEnum : values()){
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
