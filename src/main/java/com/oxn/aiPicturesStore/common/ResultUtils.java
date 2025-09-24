package com.oxn.aiPicturesStore.common;

import com.oxn.aiPicturesStore.enums.StatusCode;

public class ResultUtils {

    /**
     * 成功
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, data, "ok");
    }

    /**
     * 失败
     *
     * @param statusCode 状态码
     * @return 响应
     */
    public static BaseResponse<?> error(StatusCode statusCode) {
        return new BaseResponse<>(statusCode);
    }

    /**
     * 失败
     *
     * @param code    状态码
     * @param message 错误信息
     * @return 响应
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败
     *
     * @param statusCode 状态码 
     * @return 响应
     */
    public static BaseResponse<?> error(StatusCode statusCode, String message) {
        return new BaseResponse<>(statusCode.getCode(), null, message);
    }
}
