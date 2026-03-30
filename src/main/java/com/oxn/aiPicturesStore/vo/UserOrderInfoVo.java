package com.oxn.aiPicturesStore.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserOrderInfoVo {
    /**
     * 用户id
     */
    private Integer user_id;

    /**
     * 用户名
     */
    private String user_name;

    /**
     * 订单ID
     */
    private Integer order_id;

    /**
     * 下单时间
     */
    private Date order_time;

    /**
     * 订单总金额
     */
    private BigDecimal total_amount;

    /**
     * 订单状态（0：未支付 1：已支付 2：已发货 3：已完成）
     */
    private Integer status;
}
