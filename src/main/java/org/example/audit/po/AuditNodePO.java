package org.example.audit.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.example.audit.enums.AuditNodeTypeEnum;

/**
 * 审核流程节点表
 * 对应表：audit_nodes
 * @author fasonghao
 */
@Data
@TableName("audit_nodes")
public class AuditNodePO {

    @TableId(value = "audit_node_id", type = IdType.AUTO)
    private Integer auditNodeId;

    @TableField("name")
    private String name;

    @TableField("type")
    private AuditNodeTypeEnum type;

    @TableField("`order`")
    private Integer order;

    @TableField("auto_pass")
    private Boolean autoPass;

    @TableField("timeout_hours")
    private Integer timeoutHours;

    @TableField("is_active")
    private Boolean isActive;
}
