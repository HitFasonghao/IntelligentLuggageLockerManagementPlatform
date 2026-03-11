package org.example.device.dto;

import lombok.Data;
import org.example.device.enums.ClusterStatusEnum;

@Data
public class ClusterQueryDTO {

    private String name;

    private String location;

    private ClusterStatusEnum status;

    private Integer pageNum = 1;

    private Integer pageSize = 12;
}
