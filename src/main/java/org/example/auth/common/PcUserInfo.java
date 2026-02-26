package org.example.auth.common;

import lombok.Data;
import org.example.auth.enums.PcUserIdentityEnum;

import java.util.List;

/**
 * @author fasonghao
 */
@Data
public class PcUserInfo {
    //管理员id
    private Integer userId;

    //管理员身份
    private PcUserIdentityEnum role;

    //平台管理员权限列表
    private List<Integer> permissions;

    //厂商用户关联厂商
    private List<Integer> vendorIds;
}
