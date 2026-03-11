package org.example.device.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.example.device.enums.TimeUnitEnum;

import java.math.BigDecimal;

@Data
@TableName("cabinet_kinds")
public class CabinetKindPO {

    @TableId(value = "kind_id", type = IdType.AUTO)
    private Integer kindId;

    @TableField("vendor_id")
    private Integer vendorId;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("charge")
    private BigDecimal charge;

    @TableField("time_unit")
    private TimeUnitEnum timeUnit;
}
