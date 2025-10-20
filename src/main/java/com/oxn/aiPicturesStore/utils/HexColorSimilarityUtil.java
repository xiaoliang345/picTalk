package com.oxn.aiPicturesStore.utils;

import java.awt.Color;

/**
 * 十六进制颜色相似度计算工具类
 * 支持基于RGB和HSV两种方式计算相似度，返回0-100分（分数越高越相似）
 */
public class HexColorSimilarityUtil {


    /**
     * 基于RGB颜色空间的欧氏距离计算相似度
     * 原理：将RGB三通道视为三维空间，计算两点距离，距离越小越相似
     * @param hexColor1 十六进制颜色1
     * @param hexColor2 十六进制颜色2
     * @return 相似度评分（0-100）
     */
    public static int calculateSimilarityByRGB(String hexColor1, String hexColor2) {
        // 解析十六进制颜色为RGB值
        int[] rgb1 = parseHexToRGB(hexColor1);
        int[] rgb2 = parseHexToRGB(hexColor2);

        // 计算RGB三通道的差值平方和
        int rDiff = rgb1[0] - rgb2[0];
        int gDiff = rgb1[1] - rgb2[1];
        int bDiff = rgb1[2] - rgb2[2];
        double distance = Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);

        // RGB最大理论距离（当两颜色为#000000和#FFFFFF时）
        double maxDistance = Math.sqrt(255*255 + 255*255 + 255*255); // ≈441.67

        // 转换为相似度评分（距离越小，评分越高）
        return (int) ((1 - distance / maxDistance) * 100);
    }

    /**
     * 基于HSV颜色空间的差异度计算相似度
     * 原理：人眼对色调（H）、饱和度（S）、明度（V）的敏感度不同，加权计算差异
     * @param hexColor1 十六进制颜色1
     * @param hexColor2 十六进制颜色2
     * @return 相似度评分（0-100）
     */
    public static int calculateSimilarityByHSV(String hexColor1, String hexColor2) {
        // 解析十六进制颜色为HSV值（H:0-360, S:0-1, V:0-1）
        float[] hsv1 = parseHexToHSV(hexColor1);
        float[] hsv2 = parseHexToHSV(hexColor2);

        // 色调（H）差异：考虑环形特性（如350°和10°的差异是20°，而非340°）
        float hDiff = Math.abs(hsv1[0] - hsv2[0]);
        hDiff = Math.min(hDiff, 360 - hDiff); // 取最小角度差

        // 饱和度（S）和明度（V）差异（线性差异）
        float sDiff = Math.abs(hsv1[1] - hsv2[1]);
        float vDiff = Math.abs(hsv1[2] - hsv2[2]);

        // 加权计算总差异（人眼对色调更敏感，权重更高）
        // 权重参考：H:0.5, S:0.2, V:0.3（可根据需求调整）
        double totalDiff = (hDiff / 360) * 0.5 + sDiff * 0.2 + vDiff * 0.3;

        // 转换为相似度评分（总差异越小，评分越高）
        return (int) ((1 - totalDiff) * 100);
    }

    /**
     * 将十六进制颜色字符串解析为RGB数组
     * @param hexColor 十六进制颜色（#RRGGBB 或 RRGGBB）
     * @return RGB数组 [r, g, b]（每个值0-255）
     * @throws IllegalArgumentException 若格式无效
     */
    private static int[] parseHexToRGB(String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) {
            throw new IllegalArgumentException("颜色字符串不能为空");
        }

        // 处理前缀：移除 # 或 0x
        String cleanHex = hexColor.replaceAll("^(#|0x)", "");

        // 处理5位或6位的情况（5位时高位补零，视为6位）
        int length = cleanHex.length();
        if (length != 5 && length != 6) {
            throw new IllegalArgumentException("无效的十六进制颜色长度：" + hexColor + "，请使用5位或6位格式（如0x4c205或#FF0000）");
        }
        // 5位时在开头补一个0，转为6位（例如 4c205 → 04c205）
        if (length == 5) {
            cleanHex = "0" + cleanHex;
        }

        // 校验是否为合法的十六进制字符
        if (!cleanHex.matches("[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException("包含非十六进制字符：" + hexColor);
        }

        // 解析RGB值（前2位R，中间2位G，后2位B）
        try {
            int r = Integer.parseInt(cleanHex.substring(0, 2), 16);
            int g = Integer.parseInt(cleanHex.substring(2, 4), 16);
            int b = Integer.parseInt(cleanHex.substring(4, 6), 16);
            return new int[]{r, g, b};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("解析颜色失败：" + hexColor, e);
        }
    }

    /**
     * 将十六进制颜色字符串解析为HSV数组
     * @param hexColor 十六进制颜色（#RRGGBB 或 RRGGBB）
     * @return HSV数组 [h, s, v]（h:0-360, s:0-1, v:0-1）
     */
    private static float[] parseHexToHSV(String hexColor) {
        int[] rgb = parseHexToRGB(hexColor);
        // 使用Java内置Color类转换RGB到HSV
        float[] hsv = new float[3];
        Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsv);
        // HSB中的H范围是0-1，转换为0-360
        hsv[0] *= 360;
        return hsv;
    }
}