package org.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.auth.po.PcPermissionPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author fasonghao
 */
@Mapper
public interface PcPermissionMapper extends BaseMapper<PcPermissionPO> {
    //根据角色id联表查询角色对应的权限列表
    List<PcPermissionPO> queryByRoleId(Integer roleId);
}
