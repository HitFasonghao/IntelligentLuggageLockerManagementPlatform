package org.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author fasonghao
 */
@Data
public class UpdatePasswordDTO {

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$",message = "短信验证码必须为6位数字")
    private String code;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6,max = 20,message = "密码长度必须在6到20个字符之间")
    private String password;
}
