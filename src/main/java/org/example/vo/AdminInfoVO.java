package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author fasonghao
 */
@Data
public class AdminInfoVO {

    private String username;

    private String realName;

    private String email;

    private String phone;

    private Boolean isSuperAdmin;

    private Boolean isActive;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
