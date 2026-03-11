package org.example.device.mapper;

import org.example.device.po.CabinetPO;
import org.example.device.vo.CabinetVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeviceMapStructMapper {

    CabinetVO cabinetPoToVo(CabinetPO po);
}
