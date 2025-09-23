package com.oxn.aiPicturesStore.model.dto.user;

import lombok.Data;

@Data
public class UserRegisterRequest {

    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
