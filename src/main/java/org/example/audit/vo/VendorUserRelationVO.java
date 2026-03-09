package org.example.audit.vo;

import lombok.Data;

/**
 * 厂商用户关联VO
 * @author fasonghao
 */
@Data
public class VendorUserRelationVO {

    private Integer vendorUserRelationId;
    private Integer vendorUserId;
    private Integer vendorId;
    private Boolean isMain;
    /** 关联用户的用户名 */
    private String username;
    /** 关联用户的真实姓名 */
    private String realName;
    /** 关联用户的手机号 */
    private String phone;
}
