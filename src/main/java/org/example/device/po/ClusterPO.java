package org.example.device.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.example.device.enums.ClusterStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("clusters")
public class ClusterPO {

    @TableId(value = "cluster_id", type = IdType.AUTO)
    private Integer clusterId;

    @TableField("vendor_id")
    private Integer vendorId;

    @TableField("name")
    private String name;

    @TableField("location")
    private String location;

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("dimension")
    private BigDecimal dimension;

    @TableField("status")
    private ClusterStatusEnum status;

    @TableField("description")
    private String description;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
