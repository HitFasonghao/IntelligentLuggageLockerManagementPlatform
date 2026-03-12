package org.example.audit.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.example.audit.enums.VendorStatusEnum;

import java.time.LocalDateTime;

/**
 * 厂商表
 * 对应表：vendors
 * @author fasonghao
 */
@Data
@TableName("vendors")
public class VendorPO {

    @TableId(value = "vendor_id", type = IdType.AUTO)
    private Integer vendorId;

    @TableField("company_name")
    private String companyName;

    @TableField("short_name")
    private String shortName;

    @TableField("license_no")
    private String licenseNo;

    @TableField("license_image")
    private String licenseImage;

    @TableField("legal_person")
    private String legalPerson;

    @TableField("legal_person_id")
    private String legalPersonId;

    @TableField("contact_person")
    private String contactPerson;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("contact_email")
    private String contactEmail;

    @TableField("company_address")
    private String companyAddress;

    @TableField("website")
    private String website;

    @TableField("introduction")
    private String introduction;

    @TableField("business_scope")
    private String businessScope;

    @TableField("api_endpoint")
    private String apiEndpoint;

    @TableField("vendor_access_token")
    private String vendorAccessToken;

    @TableField("platform_access_token")
    private String platformAccessToken;

    @TableField("status")
    private VendorStatusEnum status;

    @TableField("submitted_time")
    private LocalDateTime submittedTime;

    @TableField("reviewed_time")
    private LocalDateTime reviewedTime;

    @TableField("approved_time")
    private LocalDateTime approvedTime;

    @TableField("effective_from")
    private LocalDateTime effectiveFrom;

    @TableField("effective_to")
    private LocalDateTime effectiveTo;

    @TableField("admin_id")
    private Integer adminId;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
