package org.example.audit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.audit.enums.TestResultEnum;

/**
 * 技术测试审核 DTO
 * @author fasonghao
 */
@Data
public class TechTestAuditDTO {

    /** 测试结果：passed / failed */
    @NotNull(message = "测试结果不能为空")
    private TestResultEnum testResult;

    /** 测试反馈 */
    private String testNotes;

    /** API检验结果（JSON） */
    private Object apiValidationResult;

    /** 性能测试结果（JSON） */
    private Object performanceResult;
}
