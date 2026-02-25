package org.example.po;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.example.enums.VendorUserStatusEnum;

import java.time.LocalDateTime;

/**
 * 厂商用户表实体类
 * 对应表：vendor_users
 * @author fasonghao
 */
@Data
@TableName("vendor_users")
public class VendorUserPO {

    /**
     * 厂商管理员标识（主键）
     */
    @TableId(type = IdType.AUTO)
    private Integer vendorUserId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 电话
     */
    private String phone;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 账号状态：active-启用，locked-锁定，inactive-未启用
     */
    private VendorUserStatusEnum status;

    /**
     * 连续登录失败次数
     */
    private Integer failedLoginNumber;

    /**
     * 最后一次登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后一次密码修改时间
     */
    private LocalDateTime passwordChangedTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 最后修改时间
     */
    private LocalDateTime updatedTime;

}