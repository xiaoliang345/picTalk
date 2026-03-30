package com.oxn.aiPicturesStore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/null-pointer")
    public String nullPointer() {
        String str = null;
        return str.toString();
    }
}
