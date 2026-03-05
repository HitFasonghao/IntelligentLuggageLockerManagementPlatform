package org.example.auth.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.example.auth.enums.PcRoleTypeEnum;

import java.time.LocalDateTime;

/**
 * PC端页面权限（菜单按钮）表
 * 对应表：pc_permission
 * @author fasonghao
 */
@Data
@TableName("pc_permission")
public class PcPermissionPO {

    /**
     * 权限标识
     */
    @TableId(value = "permission_id", type = IdType.AUTO) // 自增主键
    private Integer id;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 编码
     */
    @TableField("code")
    private String code;

    /**
     * 类型（MENU:菜单，BUTTON:按钮）
     */
    @TableField("type")
    private PcRoleTypeEnum type;

    /**
     * 父级标识
     */
    @TableField("parent_id")
    private Integer parentId;

    /**
     * 路由路径
     */
    @TableField("path")
    private String path;

    /**
     * 路由重定向地址
     */
    @TableField("redirect")
    private String redirect;

    /**
     * 菜单图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 前端组件路径
     */
    @TableField("component")
    private String component;

    /**
     * 页面布局类型
     */
    @TableField("layout")
    private String layout;

    /**
     * 是否缓存页面
     */
    @TableField("keep_alive")
    private Boolean keepAlive;

    /**
     * 预留字段，对应按钮的请求类型，暂无用
     */
    @TableField("method")
    private String method;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 是否显示
     */
    @TableField("show_status")
    private Boolean show;

    /**
     * 是否启用
     */
    @TableField("enable_status")
    private Boolean enable;

    /**
     * 排序号
     */
    @TableField("sort")
    private Integer order;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createdTime;

    /**
     * 最后修改时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE) // 插入/更新时自动填充
    private LocalDateTime updatedTime;

}
