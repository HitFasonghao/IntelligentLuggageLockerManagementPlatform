package org.example.audit.vo;

import lombok.Data;
import org.example.audit.enums.VendorStatusEnum;

import java.time.LocalDateTime;

/**
 * 厂商列表项VO（管理员审核列表展示用）
 * @author fasonghao
 */
@Data
public class VendorListVO {

    private Integer vendorId;
    private String companyName;
    private String shortName;
    private String contactPerson;
    private String contactPhone;
    private VendorStatusEnum status;
    private Integer currentRound;
    private LocalDateTime submittedTime;
    private LocalDateTime createdTime;
}
