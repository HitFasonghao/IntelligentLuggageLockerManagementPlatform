package org.example.device.service;

import org.example.auth.vo.HttpResponseVO;
import org.example.device.dto.CreateKindDTO;
import org.example.device.dto.KindQueryDTO;
import org.example.device.dto.UpdateKindDTO;
import org.example.device.vo.CabinetKindVO;

import java.util.Map;

public interface CabinetKindService {

    HttpResponseVO<String> createKind(CreateKindDTO dto);

    HttpResponseVO<Map<String, Object>> listKinds(KindQueryDTO queryDTO);

    HttpResponseVO<String> updateKind(UpdateKindDTO dto);

    HttpResponseVO<String> deleteKind(Integer kindId);
}
