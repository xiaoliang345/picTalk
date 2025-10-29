package com.oxn.aiPicturesStore.constant;


import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

public interface PictureConstant {
    //文件类型
    List<String> FILE_TYPE = Arrays.asList("image/jpg", "image/jpeg", "image/png", "image/gif", "image/webp");
    //标签
    List<String> tagList=Arrays.asList("治愈","高清","可爱","性感","风景","动漫");
    //分类
    List<String> categoryList=Arrays.asList("电脑壁纸","手机壁纸","头像");

    Map<String, String> categoryMap = new LinkedHashMap<>() {{
        put("电脑壁纸", "fluent-color:image-28");
        put("手机壁纸", "fluent-color:phone-16");
        put("头像", "twemoji:person");
    }};

    Map<String, String> tagMap = new LinkedHashMap<>() {{
        put("治愈", "success");
        put("高清", "processing");
        put("可爱", "magenta");
        put("性感", "red");
        put("风景", "orange");
        put("动漫", "geekblue");
    }};


    //批量抓取地址
    String batchUrl="https://cn.bing.com/images/search?q=%s";
}
