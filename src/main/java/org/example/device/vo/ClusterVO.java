package org.example.device.vo;

import lombok.Data;
import org.example.device.enums.ClusterStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ClusterVO {

    private Integer clusterId;

    private String name;

    private String location;

    private BigDecimal longitude;

    private BigDecimal dimension;

    private ClusterStatusEnum status;

    private String description;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
