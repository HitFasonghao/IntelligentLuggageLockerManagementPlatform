package org.example.audit.vo;

import lombok.Data;
import org.example.audit.enums.AuditNodeTypeEnum;

/**
 * 审核流程节点VO
 * @author fasonghao
 */
@Data
public class AuditNodeVO {

    private Integer auditNodeId;
    private String name;
    private AuditNodeTypeEnum type;
    private Integer order;
    private Boolean autoPass;
    private Integer timeoutHours;
    private Boolean isActive;
}
