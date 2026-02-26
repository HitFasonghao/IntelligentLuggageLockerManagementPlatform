package org.example.auth.vo;

import lombok.Data;

/**
 * @author fasonghao
 */
@Data
public class AccessTokenVO {
    private String token;

    public AccessTokenVO(String token){
        this.token=token;
    }
}
