package org.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.dto.AddPermissionDTO;
import org.example.auth.dto.UpdatePermissionDTO;
import org.example.auth.enums.PcRoleTypeEnum;
import org.example.auth.mapper.MapStructMapper;
import org.example.auth.mapper.PcPermissionMapper;
import org.example.auth.po.PcPermissionPO;
import org.example.auth.service.PermissionService;
import org.example.auth.vo.HttpResponseVO;
import org.example.auth.vo.PcPermissionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资源管理（菜单/按钮权限）服务实现
 * @author fasonghao
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PcPermissionMapper pcPermissionMapper;

    @Autowired
    private MapStructMapper mapStructMapper;

    /**
     * 获取菜单树（仅菜单类型，按sort排序，构建树形结构）
     */
    @Override
    public HttpResponseVO<List<PcPermissionVO>> getMenuTree() {
        // 查询所有菜单类型的权限
        LambdaQueryWrapper<PcPermissionPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PcPermissionPO::getType, PcRoleTypeEnum.MENU);
        wrapper.orderByAsc(PcPermissionPO::getSort);
        List<PcPermissionPO> allMenus = pcPermissionMapper.selectList(wrapper);

        // PO转VO
        List<PcPermissionVO> allMenuVOs = allMenus.stream()
                .map(mapStructMapper::pcPermissionPoToVo)
                .collect(Collectors.toList());

        // 构建树形结构
        List<PcPermissionVO> tree = buildTree(allMenuVOs);

        return HttpResponseVO.<List<PcPermissionVO>>builder()
                .data(tree)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取菜单树成功")
                .build();
    }

    /**
     * 获取指定菜单下的按钮列表
     */
    @Override
    public HttpResponseVO<List<PcPermissionVO>> getButtons(Integer parentId) {
        LambdaQueryWrapper<PcPermissionPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PcPermissionPO::getType, PcRoleTypeEnum.BUTTON);
        wrapper.eq(PcPermissionPO::getParentId, parentId);
        wrapper.orderByAsc(PcPermissionPO::getSort);
        List<PcPermissionPO> buttons = pcPermissionMapper.selectList(wrapper);

        List<PcPermissionVO> buttonVOs = buttons.stream()
                .map(mapStructMapper::pcPermissionPoToVo)
                .collect(Collectors.toList());

        return HttpResponseVO.<List<PcPermissionVO>>builder()
                .data(buttonVOs)
                .code(HttpStatusConstants.SUCCESS)
                .msg("获取按钮列表成功")
                .build();
    }

    /**
     * 新增权限（菜单/按钮）
     */
    @Override
    public HttpResponseVO<String> addPermission(AddPermissionDTO dto) {
        // 校验编码唯一性
        LambdaQueryWrapper<PcPermissionPO> codeWrapper = Wrappers.lambdaQuery();
        codeWrapper.eq(PcPermissionPO::getCode, dto.getCode());
        if (pcPermissionMapper.selectCount(codeWrapper) > 0) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("编码已存在")
                    .build();
        }

        PcPermissionPO po = new PcPermissionPO();
        po.setName(dto.getName());
        po.setCode(dto.getCode());
        po.setType(dto.getType());
        po.setParentId(dto.getParentId());
        po.setPath(dto.getPath());
        po.setRedirect(dto.getRedirect());
        po.setIcon(dto.getIcon());
        po.setComponent(dto.getComponent());
        po.setLayout(dto.getLayout());
        po.setKeepAlive(dto.getKeepAlive());
        po.setShowStatus(dto.getShow());
        po.setEnableStatus(dto.getEnable());
        po.setSort(dto.getOrder());

        int result = pcPermissionMapper.insert(po);
        if (result > 0) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("新增成功")
                    .build();
        }
        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.ERROR)
                .msg("新增失败")
                .build();
    }

    /**
     * 更新权限（菜单/按钮）
     */
    @Override
    public HttpResponseVO<String> updatePermission(Integer id, UpdatePermissionDTO dto) {
        // 检查权限是否存在
        PcPermissionPO existing = pcPermissionMapper.selectById(id);
        if (existing == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("权限不存在")
                    .build();
        }

        // 如果修改了编码，校验唯一性
        if (dto.getCode() != null && !dto.getCode().equals(existing.getCode())) {
            LambdaQueryWrapper<PcPermissionPO> codeWrapper = Wrappers.lambdaQuery();
            codeWrapper.eq(PcPermissionPO::getCode, dto.getCode());
            if (pcPermissionMapper.selectCount(codeWrapper) > 0) {
                return HttpResponseVO.<String>builder()
                        .code(HttpStatusConstants.ERROR)
                        .msg("编码已存在")
                        .build();
            }
        }

        // 动态更新字段
        LambdaUpdateWrapper<PcPermissionPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PcPermissionPO::getId, id);

        if (dto.getName() != null) {
            wrapper.set(PcPermissionPO::getName, dto.getName());
        }
        if (dto.getCode() != null) {
            wrapper.set(PcPermissionPO::getCode, dto.getCode());
        }
        if (dto.getParentId() != null) {
            wrapper.set(PcPermissionPO::getParentId, dto.getParentId());
        }
        if (dto.getPath() != null) {
            wrapper.set(PcPermissionPO::getPath, dto.getPath());
        }
        if (dto.getRedirect() != null) {
            wrapper.set(PcPermissionPO::getRedirect, dto.getRedirect());
        }
        if (dto.getIcon() != null) {
            wrapper.set(PcPermissionPO::getIcon, dto.getIcon());
        }
        if (dto.getComponent() != null) {
            wrapper.set(PcPermissionPO::getComponent, dto.getComponent());
        }
        if (dto.getLayout() != null) {
            wrapper.set(PcPermissionPO::getLayout, dto.getLayout());
        }
        if (dto.getKeepAlive() != null) {
            wrapper.set(PcPermissionPO::getKeepAlive, dto.getKeepAlive());
        }
        if (dto.getShow() != null) {
            wrapper.set(PcPermissionPO::getShowStatus, dto.getShow());
        }
        if (dto.getEnable() != null) {
            wrapper.set(PcPermissionPO::getEnableStatus, dto.getEnable());
        }
        if (dto.getOrder() != null) {
            wrapper.set(PcPermissionPO::getSort, dto.getOrder());
        }

        int result = pcPermissionMapper.update(null, wrapper);
        if (result > 0) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.SUCCESS)
                    .msg("更新成功")
                    .build();
        }
        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.ERROR)
                .msg("更新失败")
                .build();
    }

    /**
     * 删除权限（菜单/按钮），同时删除其子级
     */
    @Override
    public HttpResponseVO<String> deletePermission(Integer id) {
        PcPermissionPO existing = pcPermissionMapper.selectById(id);
        if (existing == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("权限不存在")
                    .build();
        }

        // 递归收集所有要删除的id（自身 + 所有子级）
        List<Integer> idsToDelete = new ArrayList<>();
        collectChildIds(id, idsToDelete);

        pcPermissionMapper.deleteBatchIds(idsToDelete);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("删除成功")
                .build();
    }

    /**
     * 验证菜单路径是否已存在
     */
    @Override
    public HttpResponseVO<Boolean> validateMenuPath(String path) {
        LambdaQueryWrapper<PcPermissionPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PcPermissionPO::getPath, path);
        wrapper.eq(PcPermissionPO::getType, PcRoleTypeEnum.MENU);
        boolean exists = pcPermissionMapper.selectCount(wrapper) > 0;

        return HttpResponseVO.<Boolean>builder()
                .data(exists)
                .code(HttpStatusConstants.SUCCESS)
                .msg(exists ? "路径已存在" : "路径可用")
                .build();
    }

    /**
     * 构建树形结构
     */
    private List<PcPermissionVO> buildTree(List<PcPermissionVO> allMenus) {
        // 按parentId分组
        Map<Integer, List<PcPermissionVO>> parentMap = allMenus.stream()
                .filter(menu -> menu.getParentId() != null)
                .collect(Collectors.groupingBy(PcPermissionVO::getParentId));

        // 为每个菜单设置children
        for (PcPermissionVO menu : allMenus) {
            List<PcPermissionVO> children = parentMap.get(menu.getId());
            if (children != null) {
                menu.setChildren(children);
            }
        }

        // 返回顶级菜单（parentId为null的）
        return allMenus.stream()
                .filter(menu -> menu.getParentId() == null)
                .collect(Collectors.toList());
    }

    /**
     * 递归收集自身及所有子级的id
     */
    private void collectChildIds(Integer id, List<Integer> ids) {
        ids.add(id);
        LambdaQueryWrapper<PcPermissionPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PcPermissionPO::getParentId, id);
        List<PcPermissionPO> children = pcPermissionMapper.selectList(wrapper);
        for (PcPermissionPO child : children) {
            collectChildIds(child.getId(), ids);
        }
    }
}
