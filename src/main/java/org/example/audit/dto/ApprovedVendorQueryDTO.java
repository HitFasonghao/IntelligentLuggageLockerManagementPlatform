package org.example.audit.dto;

import lombok.Data;

@Data
public class ApprovedVendorQueryDTO {
    private String companyName;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
