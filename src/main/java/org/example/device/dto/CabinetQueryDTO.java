package org.example.device.dto;

import lombok.Data;

@Data
public class CabinetQueryDTO {

    /**
     * 设备id（模糊匹配）
     */
    private String deviceId;

    /**
     * 是否已分配：true=已分配，false=待分配，null=全部
     */
    private Boolean assigned;

    /**
     * 寄存柜种类id
     */
    private Integer kindId;

    /**
     * 柜群id
     */
    private Integer clusterId;

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 12;
}
