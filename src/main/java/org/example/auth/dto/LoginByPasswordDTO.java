package org.example.auth.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author fasonghao
 */
@Data
public class LoginByPasswordDTO {
    //用户名
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4,max = 30,message = "用户名长度必须在4到30个字符之间")
    private String username;

    //密码
    @NotBlank(message = "密码不能为空")
    @Size(min = 6,max = 20,message = "密码长度必须在6到20个字符之间")
    private String password;

    @NotBlank(message = "验证码id无效")
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "captchaId 格式无效，必须是标准的 UUID 格式"
    )
    private String captchaId;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]{4}$", message = "验证码格式错误，应为4位字母或数字")
    private String code;
}
