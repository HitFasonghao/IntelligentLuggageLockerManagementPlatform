package org.example.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.audit.po.VendorAuditRecordPO;

/**
 * @author fasonghao
 */
@Mapper
public interface VendorAuditRecordMapper extends BaseMapper<VendorAuditRecordPO> {
}
