package org.example.device.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.example.device.enums.CabinetStatusEnum;

/**
 * @author fasonghao
 */
@Data
@TableName("cabinets")
public class CabinetPO {

    @TableId(value = "cabinet_id", type = IdType.AUTO)
    private Integer cabinetId;

    @TableField("vendor_id")
    private Integer vendorId;

    @TableField("device_id")
    private String deviceId;

    @TableField("number")
    private Integer number;

    @TableField("status")
    private CabinetStatusEnum status;

    @TableField("kind_id")
    private Integer kindId;

    @TableField("cluster_id")
    private Integer clusterId;
}
