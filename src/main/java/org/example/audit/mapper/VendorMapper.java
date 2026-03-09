package org.example.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.audit.po.VendorPO;

/**
 * @author fasonghao
 */
@Mapper
public interface VendorMapper extends BaseMapper<VendorPO> {
}
