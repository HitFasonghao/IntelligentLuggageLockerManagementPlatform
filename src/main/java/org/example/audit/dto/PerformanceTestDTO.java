package org.example.audit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 性能测试审核 DTO
 * @author fasonghao
 */
@Data
public class PerformanceTestDTO {

    /** 是否通过 */
    @NotNull(message = "测试结果不能为空")
    private Boolean passed;

    /** 测试反馈 */
    private String notes;
}
