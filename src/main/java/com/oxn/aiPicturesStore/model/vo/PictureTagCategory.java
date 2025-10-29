package com.oxn.aiPicturesStore.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PictureTagCategory {

    List<String> tagList;
    List<String> categoryList;
    Map categoryMap;
    Map tagMap;
}
