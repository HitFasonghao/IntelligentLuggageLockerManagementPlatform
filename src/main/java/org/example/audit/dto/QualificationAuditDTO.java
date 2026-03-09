package org.example.audit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.audit.enums.AuditResultEnum;

/**
 * 资质审核 DTO
 * @author fasonghao
 */
@Data
public class QualificationAuditDTO {

    /** 审核结果：pass / fail */
    @NotNull(message = "审核结果不能为空")
    private AuditResultEnum auditResult;

    /** 审核意见 */
    private String auditNotes;
}
