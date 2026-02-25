package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author fasonghao
 */
@Data
public class VendorUserInfoVO {
    private String username;

    private String email;

    private String phone;

    private String realName;

    private String status;

    private LocalDateTime passwordChangedTime;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
