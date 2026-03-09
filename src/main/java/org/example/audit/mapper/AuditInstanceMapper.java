package org.example.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.audit.po.AuditInstancePO;

/**
 * @author fasonghao
 */
@Mapper
public interface AuditInstanceMapper extends BaseMapper<AuditInstancePO> {
}
