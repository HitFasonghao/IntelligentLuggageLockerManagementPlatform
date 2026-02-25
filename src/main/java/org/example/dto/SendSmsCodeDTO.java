package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.example.enums.SmsCodePurposeEnum;

/**
 * @author fasonghao
 */
@Data
public class SendSmsCodeDTO {
    // 手机号
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    //验证码用途
    @NotNull(message = "验证码用途不能为空或无效")
    private SmsCodePurposeEnum purpose;
}
