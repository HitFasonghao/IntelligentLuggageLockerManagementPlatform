package org.example.audit.mapper;

import org.example.audit.po.*;
import org.example.audit.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * 审核模块 MapStruct 映射器
 * @author fasonghao
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuditMapStructMapper {

    VendorVO vendorPoToVo(VendorPO po);

    VendorAuditRecordVO auditRecordPoToVo(VendorAuditRecordPO po);

    AuditNodeVO auditNodePoToVo(AuditNodePO po);

    AuditTaskVO auditTaskPoToVo(AuditTaskPO po);
}
