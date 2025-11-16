package com.oxn.aiPicturesStore.utils;

import com.oxn.aiPicturesStore.mapper.FileShareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * 随机验证码生成工具类（6位，大小写字母 + 数字）
 */
@Component
public class RandomCodeUtils {

    private final FileShareMapper fileShareMapper;

    // 字符集：0-9, A-Z, a-z（共 62 个字符）
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 10; // 最大尝试次数
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public RandomCodeUtils(FileShareMapper fileShareMapper) {
        this.fileShareMapper = fileShareMapper;
    }

    /**
     * 生成 6 位随机验证码（大小写字母 + 数字），并确保在数据库中唯一
     *
     * @return 6位字符串，如 "aB3xK9"
     */
    public String generateUniqueCode() {
        Set<String> generatedCodes = new HashSet<>();
        
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
            
            String code = sb.toString();
            
            // 避免在同一次生成过程中产生重复码
            if (generatedCodes.contains(code)) {
                continue;
            }
            
            generatedCodes.add(code);
            
            // 检查数据库中是否已存在该码
            if (fileShareMapper.selectCountByShareCode(code) == 0) {
                return code;
            }
        }
        
        // 如果达到最大尝试次数仍未生成唯一码，则抛出异常
        throw new RuntimeException("无法在" + MAX_ATTEMPTS + "次尝试内生成唯一的提取码");
    }

    // 可选：提供自定义长度的方法
    public String generateCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}