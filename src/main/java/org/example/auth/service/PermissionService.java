package org.example.auth.service;

import org.example.auth.dto.AddPermissionDTO;
import org.example.auth.dto.UpdatePermissionDTO;
import org.example.auth.vo.HttpResponseVO;
import org.example.auth.vo.PcPermissionVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 资源管理（菜单/按钮权限）服务接口
 * @author fasonghao
 */
@Service
public interface PermissionService {

    /**
     * 获取菜单树
     */
    HttpResponseVO<List<PcPermissionVO>> getMenuTree();

    /**
     * 获取指定菜单下的按钮列表
     */
    HttpResponseVO<List<PcPermissionVO>> getButtons(Integer parentId);

    /**
     * 新增权限（菜单/按钮）
     */
    HttpResponseVO<String> addPermission(AddPermissionDTO addPermissionDTO);

    /**
     * 更新权限（菜单/按钮）
     */
    HttpResponseVO<String> updatePermission(Integer id, UpdatePermissionDTO updatePermissionDTO);

    /**
     * 删除权限（菜单/按钮）
     */
    HttpResponseVO<String> deletePermission(Integer id);

    /**
     * 验证菜单路径是否已存在
     */
    HttpResponseVO<Boolean> validateMenuPath(String path);
}
