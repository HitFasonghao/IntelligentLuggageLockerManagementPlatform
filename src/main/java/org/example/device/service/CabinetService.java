package org.example.device.service;

import org.example.auth.vo.HttpResponseVO;
import org.example.device.dto.AssignClusterDTO;
import org.example.device.dto.AssignKindDTO;
import org.example.device.dto.CabinetQueryDTO;
import org.example.device.vo.ClusterOptionVO;
import org.example.device.vo.KindOptionVO;

import java.util.List;
import java.util.Map;

public interface CabinetService {

    /**
     * 同步所有寄存柜（暂为空实现）
     */
    HttpResponseVO<String> syncCabinets();

    /**
     * 分页查询寄存柜列表
     */
    HttpResponseVO<Map<String, Object>> listCabinets(CabinetQueryDTO queryDTO);

    /**
     * 分配寄存柜种类
     */
    HttpResponseVO<String> assignKind(AssignKindDTO dto);

    /**
     * 分配柜群
     */
    HttpResponseVO<String> assignCluster(AssignClusterDTO dto);

    /**
     * 获取所有寄存柜种类选项
     */
    HttpResponseVO<List<KindOptionVO>> listKindOptions();

    /**
     * 获取所有柜群选项
     */
    HttpResponseVO<List<ClusterOptionVO>> listClusterOptions();
}
