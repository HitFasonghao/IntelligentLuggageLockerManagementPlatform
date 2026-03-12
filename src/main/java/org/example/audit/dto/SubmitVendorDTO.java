package org.example.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 厂商入驻申请/保存草稿 DTO
 * @author fasonghao
 */
@Data
public class SubmitVendorDTO {

    /** 厂商ID（编辑已有草稿时传入，新建时为null） */
    private Integer vendorId;

    @NotBlank(message = "公司全称不能为空")
    @Size(max = 100, message = "公司全称长度不能超过100")
    private String companyName;

    @Size(max = 50, message = "简称长度不能超过50")
    private String shortName;

    @Size(max = 50, message = "营业执照号长度不能超过50")
    private String licenseNo;

    @Size(max = 500, message = "营业执照照片URL长度不能超过500")
    private String licenseImage;

    @Size(max = 50, message = "法定代表人长度不能超过50")
    private String legalPerson;

    @Size(max = 30, message = "法人身份证号长度不能超过30")
    private String legalPersonId;

    @Size(max = 50, message = "联系人姓名长度不能超过50")
    private String contactPerson;

    @Size(max = 20, message = "联系人电话长度不能超过20")
    private String contactPhone;

    @Size(max = 100, message = "联系人邮箱长度不能超过100")
    private String contactEmail;

    @Size(max = 300, message = "公司地址长度不能超过300")
    private String companyAddress;

    @Size(max = 200, message = "官网长度不能超过200")
    private String website;

    private String introduction;

    private String businessScope;

    @Size(max = 500, message = "API接口地址长度不能超过500")
    private String apiEndpoint;

    @Size(max = 500, message = "厂商系统访问token长度不能超过500")
    private String vendorAccessToken;

}
