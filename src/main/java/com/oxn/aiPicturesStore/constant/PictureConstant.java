package com.oxn.aiPicturesStore.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface PictureConstant {
    //文件类型
    List<String> FILE_TYPE = Arrays.asList("image/jpg", "image/jpeg", "image/png", "image/gif", "image/webp");
    //标签
    List<String> tagList=Arrays.asList("治愈","高清","可爱","性感");
    //分类
    List<String> categorieList=Arrays.asList("壁纸","头像","动漫","风景","表情包");

    //批量抓取地址
    String batchUrl="https://cn.bing.com/images/search?q=%s";
}
