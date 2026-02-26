package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author fasonghao
 */
@Data
public class UpdateVendorUserDTO {

    //用户名
    @Size(min = 4,max = 30,message = "用户名长度必须在4到30个字符之间")
    private String username;

    //邮箱
    @Size(max = 100,message = "邮箱长度不能超过100个字符")
    private String email;

    //姓名
    @Size(max = 50,message = "姓名长度不能超过50个字符")
    private String realName;

}
