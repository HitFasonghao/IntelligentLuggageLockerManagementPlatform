package org.example.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.vo.HttpResponseVO;
import org.example.device.dto.CreateKindDTO;
import org.example.device.dto.KindQueryDTO;
import org.example.device.dto.UpdateKindDTO;
import org.example.device.mapper.CabinetKindMapper;
import org.example.device.mapper.CabinetMapper;
import org.example.device.po.CabinetKindPO;
import org.example.device.po.CabinetPO;
import org.example.device.service.CabinetKindService;
import org.example.device.vo.CabinetKindVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CabinetKindServiceImpl implements CabinetKindService {

    @Autowired
    private CabinetKindMapper cabinetKindMapper;

    @Autowired
    private CabinetMapper cabinetMapper;

    @Override
    public HttpResponseVO<String> createKind(CreateKindDTO dto) {
        CabinetKindPO po = new CabinetKindPO();
        po.setVendorId(getVendorId());
        po.setName(dto.getName());
        po.setDescription(dto.getDescription());
        po.setCharge(dto.getCharge().setScale(2, RoundingMode.HALF_UP));
        po.setTimeUnit(dto.getTimeUnit());
        cabinetKindMapper.insert(po);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("创建成功")
                .build();
    }

    @Override
    public HttpResponseVO<Map<String, Object>> listKinds(KindQueryDTO queryDTO) {
        LambdaQueryWrapper<CabinetKindPO> wrapper = Wrappers.lambdaQuery();

        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(CabinetKindPO::getName, queryDTO.getName());
        }
        if (queryDTO.getChargeMin() != null) {
            wrapper.ge(CabinetKindPO::getCharge, queryDTO.getChargeMin());
        }
        if (queryDTO.getChargeMax() != null) {
            wrapper.le(CabinetKindPO::getCharge, queryDTO.getChargeMax());
        }
        if (queryDTO.getTimeUnit() != null) {
            wrapper.eq(CabinetKindPO::getTimeUnit, queryDTO.getTimeUnit());
        }

        Page<CabinetKindPO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<CabinetKindPO> resultPage = cabinetKindMapper.selectPage(page, wrapper);

        // 统计每个种类的寄存柜数量
        Map<Integer, Long> countMap = new HashMap<>();
        if (!resultPage.getRecords().isEmpty()) {
            List<Integer> kindIds = resultPage.getRecords().stream()
                    .map(CabinetKindPO::getKindId)
                    .collect(Collectors.toList());
            for (Integer kindId : kindIds) {
                Long count = cabinetMapper.selectCount(
                        Wrappers.<CabinetPO>lambdaQuery().eq(CabinetPO::getKindId, kindId)
                );
                countMap.put(kindId, count);
            }
        }

        List<CabinetKindVO> voList = resultPage.getRecords().stream().map(po -> {
            CabinetKindVO vo = new CabinetKindVO();
            vo.setKindId(po.getKindId());
            vo.setName(po.getName());
            vo.setDescription(po.getDescription());
            vo.setCabinetCount(countMap.getOrDefault(po.getKindId(), 0L));
            vo.setCharge(po.getCharge());
            vo.setTimeUnit(po.getTimeUnit());
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", resultPage.getTotal());
        result.put("pageNum", resultPage.getCurrent());
        result.put("pageSize", resultPage.getSize());

        return HttpResponseVO.<Map<String, Object>>builder()
                .data(result)
                .code(HttpStatusConstants.SUCCESS)
                .msg("查询成功")
                .build();
    }

    @Override
    public HttpResponseVO<String> updateKind(UpdateKindDTO dto) {
        CabinetKindPO po = cabinetKindMapper.selectById(dto.getKindId());
        if (po == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("种类不存在")
                    .build();
        }

        po.setName(dto.getName());
        po.setDescription(dto.getDescription());
        po.setCharge(dto.getCharge().setScale(2, RoundingMode.HALF_UP));
        po.setTimeUnit(dto.getTimeUnit());
        cabinetKindMapper.updateById(po);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("修改成功")
                .build();
    }

    @Override
    public HttpResponseVO<String> deleteKind(Integer kindId) {
        CabinetKindPO po = cabinetKindMapper.selectById(kindId);
        if (po == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("种类不存在")
                    .build();
        }

        // 检查是否有寄存柜使用该种类
        Long count = cabinetMapper.selectCount(
                Wrappers.<CabinetPO>lambdaQuery().eq(CabinetPO::getKindId, kindId)
        );
        if (count > 0) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("该种类下还有 " + count + " 个寄存柜，无法删除")
                    .build();
        }

        cabinetKindMapper.deleteById(kindId);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("删除成功")
                .build();
    }

    /**
     * 获取当前用户的厂商id（暂时硬编码，后续从UserContext中获取）
     */
    private Integer getVendorId() {
        // TODO: 从UserContext获取当前用户关联的vendorId
        return 11;
    }
}
