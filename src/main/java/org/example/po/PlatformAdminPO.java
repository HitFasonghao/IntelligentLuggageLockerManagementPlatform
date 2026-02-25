package org.example.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 平台管理员表实体类
 * 对应表：platform_admins
 * @author fasonghao
 */
@Data
@TableName("platform_admins")
public class PlatformAdminPO {

    /**
     * 管理员标识（主键）
     */
    @TableId(type = IdType.AUTO)
    private Integer adminId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 电话
     */
    private String phone;

    /**
     * 是否为超级管理员
     */
    private Boolean isSuperAdmin;

    /**
     * 是否被启用
     */
    private Boolean isActive;

    /**
     * 最近一次登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 最近修改时间
     */
    private LocalDateTime updatedTime;
}
