package com.oxn.aiPicturesStore.controller;

import com.oxn.aiPicturesStore.common.BaseResponse;
import com.oxn.aiPicturesStore.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainController {

    @GetMapping("/health")
    public BaseResponse<String> testHealth(){
        return ResultUtils.success("ok");
    }
}
