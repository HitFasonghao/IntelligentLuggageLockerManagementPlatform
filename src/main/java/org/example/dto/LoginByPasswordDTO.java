package org.example.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author fasonghao
 */
@Data
public class LoginByPasswordDTO {
    //用户名
    @NotBlank(message = "用户名不能为空")
    @Size(min = 10,max = 30,message = "用户名长度必须在10到30个字符之间")
    private String username;

    //密码
    @NotBlank(message = "密码不能为空")
    @Size(min = 6,max = 20,message = "密码长度必须在6到20个字符之间")
    private String password;
}
