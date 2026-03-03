package org.example.auth.vo;

import lombok.Data;

/**
 * @author fasonghao
 */
@Data
public class CaptchaVO {

    private String captchaId;

    private String imageBase64;
}
