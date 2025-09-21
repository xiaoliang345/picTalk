package com.oxn.aiPicturesStore.model.dto;

import lombok.Data;

@Data
public class UserLoginRequest {

    private String userAccount;
    private String userPassword;
}
