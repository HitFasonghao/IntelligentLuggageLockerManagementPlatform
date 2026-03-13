package org.example.device.service;

import org.example.auth.vo.HttpResponseVO;
import org.example.device.dto.AssignNumberDTO;
import org.example.device.dto.ClusterQueryDTO;
import org.example.device.dto.CreateClusterDTO;
import org.example.device.dto.UpdateCabinetStatusDTO;
import org.example.device.vo.ClusterDetailVO;

import java.util.Map;

public interface ClusterService {

    HttpResponseVO<String> createCluster(CreateClusterDTO dto);

    HttpResponseVO<Map<String, Object>> listClusters(ClusterQueryDTO queryDTO);

    HttpResponseVO<ClusterDetailVO> getClusterDetail(Integer clusterId);

    HttpResponseVO<String> updateCabinetStatus(UpdateCabinetStatusDTO dto);

    HttpResponseVO<String> removeCabinetFromCluster(Integer cabinetId);

    HttpResponseVO<String> assignNumber(AssignNumberDTO dto);
}
