package org.example.auth.vo;

import lombok.Data;
import org.example.auth.enums.VendorUserStatusEnum;

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

    private VendorUserStatusEnum status;

    private LocalDateTime passwordChangedTime;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
