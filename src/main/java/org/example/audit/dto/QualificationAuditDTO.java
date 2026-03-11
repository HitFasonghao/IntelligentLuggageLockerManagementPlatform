package org.example.audit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 资质审核 DTO
 * @author fasonghao
 */
@Data
public class QualificationAuditDTO {

    /** 是否通过 */
    @NotNull(message = "审核结果不能为空")
    private Boolean passed;

    /** 审核意见 */
    private String notes;
}
