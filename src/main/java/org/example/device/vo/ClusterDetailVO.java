package org.example.device.vo;

import lombok.Data;
import org.example.device.enums.ClusterStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ClusterDetailVO {

    private Integer clusterId;

    private String name;

    private String location;

    private BigDecimal longitude;

    private BigDecimal dimension;

    private ClusterStatusEnum status;

    private String description;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    /**
     * 该柜群的寄存柜数量
     */
    private Long cabinetCount;

    /**
     * 该柜群的所有寄存柜
     */
    private List<CabinetVO> cabinets;
}
