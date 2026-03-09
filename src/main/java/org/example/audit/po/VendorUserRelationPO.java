package org.example.audit.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 厂商用户与厂商关联表
 * 对应表：vendor_user_relation
 * @author fasonghao
 */
@Data
@TableName("vendor_user_relation")
public class VendorUserRelationPO {

    @TableId(value = "vendor_user_relation_id", type = IdType.AUTO)
    private Integer vendorUserRelationId;

    @TableField("vendor_user_id")
    private Integer vendorUserId;

    @TableField("vendor_id")
    private Integer vendorId;

    @TableField("is_main")
    private Boolean isMain;
}
