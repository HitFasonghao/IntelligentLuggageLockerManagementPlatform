package org.example.device.vo;

import lombok.Data;
import org.example.device.enums.TimeUnitEnum;

import java.math.BigDecimal;

@Data
public class CabinetKindVO {

    private Integer kindId;

    private String name;

    private String description;

    /**
     * 该种类寄存柜数量
     */
    private Long cabinetCount;

    private BigDecimal charge;

    private TimeUnitEnum timeUnit;
}
