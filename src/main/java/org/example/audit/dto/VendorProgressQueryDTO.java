package org.example.audit.dto;

import lombok.Data;
import org.example.audit.enums.AuditRecordResultEnum;

@Data
public class VendorProgressQueryDTO {
    private String companyName;
    private AuditRecordResultEnum result;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
