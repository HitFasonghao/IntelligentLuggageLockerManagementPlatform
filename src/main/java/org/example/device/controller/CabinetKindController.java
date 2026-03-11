package org.example.device.controller;

import jakarta.validation.Valid;
import org.example.auth.vo.HttpResponseVO;
import org.example.device.dto.CreateKindDTO;
import org.example.device.dto.KindQueryDTO;
import org.example.device.dto.UpdateKindDTO;
import org.example.device.service.CabinetKindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cabinetKind")
public class CabinetKindController {

    @Autowired
    private CabinetKindService cabinetKindService;

    @PostMapping("/create")
    public HttpResponseVO<String> createKind(@RequestBody @Valid CreateKindDTO dto) {
        return cabinetKindService.createKind(dto);
    }

    @GetMapping("/list")
    public HttpResponseVO<Map<String, Object>> listKinds(KindQueryDTO queryDTO) {
        return cabinetKindService.listKinds(queryDTO);
    }

    @PutMapping("/update")
    public HttpResponseVO<String> updateKind(@RequestBody @Valid UpdateKindDTO dto) {
        return cabinetKindService.updateKind(dto);
    }

    @DeleteMapping("/delete/{kindId}")
    public HttpResponseVO<String> deleteKind(@PathVariable Integer kindId) {
        return cabinetKindService.deleteKind(kindId);
    }
}
