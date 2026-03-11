package org.example.device.controller;

import jakarta.validation.Valid;
import org.example.auth.vo.HttpResponseVO;
import org.example.device.dto.ClusterQueryDTO;
import org.example.device.dto.CreateClusterDTO;
import org.example.device.dto.UpdateCabinetStatusDTO;
import org.example.device.service.ClusterService;
import org.example.device.vo.ClusterDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cluster")
public class ClusterController {

    @Autowired
    private ClusterService clusterService;

    @PostMapping("/create")
    public HttpResponseVO<String> createCluster(@RequestBody @Valid CreateClusterDTO dto) {
        return clusterService.createCluster(dto);
    }

    @GetMapping("/list")
    public HttpResponseVO<Map<String, Object>> listClusters(ClusterQueryDTO queryDTO) {
        return clusterService.listClusters(queryDTO);
    }

    @GetMapping("/detail/{clusterId}")
    public HttpResponseVO<ClusterDetailVO> getClusterDetail(@PathVariable Integer clusterId) {
        return clusterService.getClusterDetail(clusterId);
    }

    @PutMapping("/cabinet/status")
    public HttpResponseVO<String> updateCabinetStatus(@RequestBody @Valid UpdateCabinetStatusDTO dto) {
        return clusterService.updateCabinetStatus(dto);
    }

    @DeleteMapping("/cabinet/{cabinetId}")
    public HttpResponseVO<String> removeCabinetFromCluster(@PathVariable Integer cabinetId) {
        return clusterService.removeCabinetFromCluster(cabinetId);
    }
}
