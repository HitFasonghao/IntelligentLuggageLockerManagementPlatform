package org.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @author fasonghao
 */
@Data
public class ConfirmOldPhoneDTO {

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$",message = "短信验证码必须为6位数字")
    private String code;

}
