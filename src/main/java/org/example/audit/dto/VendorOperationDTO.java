package org.example.audit.dto;

import lombok.Data;

/**
 * 厂商管理操作 DTO（暂停/恢复/封禁）
 * @author fasonghao
 */
@Data
public class VendorOperationDTO {
    /** 操作说明 */
    private String notes;
}
