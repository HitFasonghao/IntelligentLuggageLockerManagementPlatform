package org.example.auth.vo;

import lombok.Data;

/**
 * 厂商简要信息VO（用于切换厂商）
 * @author fasonghao
 */
@Data
public class VendorSimpleVO {
    private Integer vendorId;
    private String companyName;
    private String shortName;
    private Boolean isCurrent;
}
