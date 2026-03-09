package org.example.audit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 最终审批 DTO
 * @author fasonghao
 */
@Data
public class FinalApprovalDTO {

    /** 是否批准 */
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    /** 审批意见 */
    private String notes;

    /** 生效日期 */
    private LocalDateTime effectiveFrom;

    /** 失效日期 */
    private LocalDateTime effectiveTo;
}
