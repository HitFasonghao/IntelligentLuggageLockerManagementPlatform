package org.example.auth.controller;

import jakarta.validation.Valid;
import org.example.auth.dto.AddPermissionDTO;
import org.example.auth.dto.UpdatePermissionDTO;
import org.example.auth.service.PermissionService;
import org.example.auth.vo.HttpResponseVO;
import org.example.auth.vo.PcPermissionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源管理模块（菜单/按钮权限）
 * @author fasonghao
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取菜单树
     */
    @GetMapping("/menu/tree")
    public HttpResponseVO<List<PcPermissionVO>> getMenuTree() {
        return permissionService.getMenuTree();
    }

    /**
     * 获取指定菜单下的按钮列表
     */
    @GetMapping("/button/{parentId}")
    public HttpResponseVO<List<PcPermissionVO>> getButtons(@PathVariable Integer parentId) {
        return permissionService.getButtons(parentId);
    }

    /**
     * 新增权限（菜单/按钮）
     */
    @PostMapping
    public HttpResponseVO<String> addPermission(@RequestBody @Valid AddPermissionDTO addPermissionDTO) {
        return permissionService.addPermission(addPermissionDTO);
    }

    /**
     * 更新权限（菜单/按钮）
     */
    @PatchMapping("/{id}")
    public HttpResponseVO<String> updatePermission(@PathVariable Integer id,
                                                   @RequestBody @Valid UpdatePermissionDTO updatePermissionDTO) {
        return permissionService.updatePermission(id, updatePermissionDTO);
    }

    /**
     * 删除权限（菜单/按钮）
     */
    @DeleteMapping("/{id}")
    public HttpResponseVO<String> deletePermission(@PathVariable Integer id) {
        return permissionService.deletePermission(id);
    }

    /**
     * 验证菜单路径是否已存在
     */
    @GetMapping("/menu/validate")
    public HttpResponseVO<Boolean> validateMenuPath(@RequestParam String path) {
        return permissionService.validateMenuPath(path);
    }
}
