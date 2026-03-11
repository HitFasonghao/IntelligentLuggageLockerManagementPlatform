package org.example.device.controller;

import jakarta.validation.Valid;
import org.example.auth.vo.HttpResponseVO;
import org.example.device.dto.AssignClusterDTO;
import org.example.device.dto.AssignKindDTO;
import org.example.device.dto.CabinetQueryDTO;
import org.example.device.service.CabinetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.device.vo.ClusterOptionVO;
import org.example.device.vo.KindOptionVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cabinet")
public class CabinetController {

    @Autowired
    private CabinetService cabinetService;

    /**
     * 同步所有寄存柜（空接口，后续实现）
     */
    @PostMapping("/sync")
    public HttpResponseVO<String> syncCabinets() {
        return cabinetService.syncCabinets();
    }

    /**
     * 分页查询寄存柜列表
     */
    @GetMapping("/list")
    public HttpResponseVO<Map<String, Object>> listCabinets(CabinetQueryDTO queryDTO) {
        return cabinetService.listCabinets(queryDTO);
    }

    /**
     * 分配寄存柜种类
     */
    @PutMapping("/assignKind")
    public HttpResponseVO<String> assignKind(@RequestBody @Valid AssignKindDTO dto) {
        return cabinetService.assignKind(dto);
    }

    /**
     * 分配柜群
     */
    @PutMapping("/assignCluster")
    public HttpResponseVO<String> assignCluster(@RequestBody @Valid AssignClusterDTO dto) {
        return cabinetService.assignCluster(dto);
    }

    /**
     * 获取所有寄存柜种类选项（用于下拉选择）
     */
    @GetMapping("/kindOptions")
    public HttpResponseVO<List<KindOptionVO>> listKindOptions() {
        return cabinetService.listKindOptions();
    }

    /**
     * 获取所有柜群选项（用于下拉选择）
     */
    @GetMapping("/clusterOptions")
    public HttpResponseVO<List<ClusterOptionVO>> listClusterOptions() {
        return cabinetService.listClusterOptions();
    }
}
