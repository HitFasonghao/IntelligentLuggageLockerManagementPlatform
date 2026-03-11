package org.example.device.vo;

import lombok.Data;
import org.example.device.enums.CabinetStatusEnum;

@Data
public class CabinetVO {

    private Integer cabinetId;

    private String deviceId;

    private Integer number;

    private CabinetStatusEnum status;

    /**
     * 是否已分配柜群（true=已分配，false=待分配）
     */
    private Boolean assigned;

    /**
     * 寄存柜种类名称
     */
    private String kindName;

    /**
     * 柜群名称
     */
    private String clusterName;
}
