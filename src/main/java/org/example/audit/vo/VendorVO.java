package org.example.audit.vo;

import lombok.Data;
import org.example.audit.enums.VendorStatusEnum;

import java.time.LocalDateTime;

/**
 * 厂商信息VO
 * @author fasonghao
 */
@Data
public class VendorVO {

    private Integer vendorId;
    private String companyName;
    private String shortName;
    private String licenseNo;
    private String licenseImage;
    private String legalPerson;
    private String legalPersonId;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String companyAddress;
    private String website;
    private String introduction;
    private String businessScope;
    private String apiEndpoint;
    private String vendorAccessToken;
    private String platformAccessToken;
    private VendorStatusEnum status;
    private LocalDateTime submittedTime;
    private LocalDateTime reviewedTime;
    private LocalDateTime approvedTime;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private Integer adminId;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
