package org.example.auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新权限（菜单/按钮）DTO
 * @author fasonghao
 */
@Data
public class UpdatePermissionDTO {

    /**
     * 名称
     */
    @Size(max = 50, message = "名称长度不能超过50")
    private String name;

    /**
     * 编码
     */
    @Size(max = 100, message = "编码长度不能超过100")
    private String code;

    /**
     * 父级标识
     */
    private Integer parentId;

    /**
     * 路由路径
     */
    @Size(max = 200, message = "路由路径长度不能超过200")
    private String path;

    /**
     * 路由重定向地址
     */
    @Size(max = 200, message = "重定向地址长度不能超过200")
    private String redirect;

    /**
     * 菜单图标
     */
    @Size(max = 100, message = "图标长度不能超过100")
    private String icon;

    /**
     * 前端组件路径
     */
    @Size(max = 200, message = "组件路径长度不能超过200")
    private String component;

    /**
     * 页面布局类型
     */
    @Size(max = 50, message = "布局类型长度不能超过50")
    private String layout;

    /**
     * 是否缓存页面
     */
    private Boolean keepAlive;

    /**
     * 是否显示
     */
    private Boolean show;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 排序号
     */
    private Integer order;
}
