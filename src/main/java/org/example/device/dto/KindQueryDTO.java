package org.example.device.dto;

import lombok.Data;
import org.example.device.enums.TimeUnitEnum;

import java.math.BigDecimal;

@Data
public class KindQueryDTO {

    private String name;

    /**
     * 收费金额下限
     */
    private BigDecimal chargeMin;

    /**
     * 收费金额上限
     */
    private BigDecimal chargeMax;

    private TimeUnitEnum timeUnit;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
