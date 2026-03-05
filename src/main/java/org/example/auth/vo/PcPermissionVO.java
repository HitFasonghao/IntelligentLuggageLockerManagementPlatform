package org.example.auth.vo;

import lombok.Data;
import org.example.auth.enums.PcRoleTypeEnum;

import java.util.List;

/**
 * @author fasonghao
 */
@Data
public class PcPermissionVO {

    /**
     * 权限标识
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 类型（MENU:菜单，BUTTON:按钮）
     */
    private PcRoleTypeEnum type;

    /**
     * 父级标识
     */
    private Integer parentId;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 路由重定向地址
     */
    private String redirect;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 页面布局类型
     */
    private String layout;

    /**
     * 是否缓存页面
     */
    private Boolean keepAlive;

    /**
     * 预留字段，对应按钮的请求类型，暂无用
     */
    private String method;

    /**
     * 描述
     */
    private String description;

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

    /**
     * 子菜单/按钮
     */
    private List<PcPermissionVO> children;
}
