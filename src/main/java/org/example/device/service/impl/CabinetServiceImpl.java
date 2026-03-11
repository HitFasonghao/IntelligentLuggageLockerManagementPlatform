package org.example.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.vo.HttpResponseVO;
import org.example.device.dto.AssignClusterDTO;
import org.example.device.dto.AssignKindDTO;
import org.example.device.dto.CabinetQueryDTO;
import org.example.device.mapper.CabinetKindMapper;
import org.example.device.mapper.CabinetMapper;
import org.example.device.mapper.ClusterMapper;
import org.example.device.po.CabinetKindPO;
import org.example.device.po.CabinetPO;
import org.example.device.po.ClusterPO;
import org.example.device.service.CabinetService;
import org.example.device.vo.CabinetVO;
import org.example.device.vo.ClusterOptionVO;
import org.example.device.vo.KindOptionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CabinetServiceImpl implements CabinetService {

    @Autowired
    private CabinetMapper cabinetMapper;

    @Autowired
    private CabinetKindMapper cabinetKindMapper;

    @Autowired
    private ClusterMapper clusterMapper;

    @Override
    public HttpResponseVO<String> syncCabinets() {
        // TODO: 后续实现同步逻辑
        return HttpResponseVO.<String>builder()
                .data(null)
                .code(HttpStatusConstants.SUCCESS)
                .msg("同步接口暂未实现")
                .build();
    }

    @Override
    public HttpResponseVO<Map<String, Object>> listCabinets(CabinetQueryDTO queryDTO) {
        LambdaQueryWrapper<CabinetPO> wrapper = Wrappers.lambdaQuery();

        // 设备id模糊匹配
        if (StringUtils.hasText(queryDTO.getDeviceId())) {
            wrapper.like(CabinetPO::getDeviceId, queryDTO.getDeviceId());
        }

        // 是否分配：依据cluster_id是否为空
        if (queryDTO.getAssigned() != null) {
            if (queryDTO.getAssigned()) {
                wrapper.isNotNull(CabinetPO::getClusterId);
            } else {
                wrapper.isNull(CabinetPO::getClusterId);
            }
        }

        // 种类筛选
        if (queryDTO.getKindId() != null) {
            wrapper.eq(CabinetPO::getKindId, queryDTO.getKindId());
        }

        // 柜群筛选
        if (queryDTO.getClusterId() != null) {
            wrapper.eq(CabinetPO::getClusterId, queryDTO.getClusterId());
        }

        // 分页查询
        Page<CabinetPO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<CabinetPO> resultPage = cabinetMapper.selectPage(page, wrapper);

        // 预加载种类和柜群名称映射
        Map<Integer, String> kindNameMap = cabinetKindMapper.selectList(null)
                .stream()
                .collect(Collectors.toMap(CabinetKindPO::getKindId, CabinetKindPO::getName));

        Map<Integer, String> clusterNameMap = clusterMapper.selectList(null)
                .stream()
                .collect(Collectors.toMap(ClusterPO::getClusterId, ClusterPO::getName));

        // PO -> VO
        List<CabinetVO> voList = resultPage.getRecords().stream().map(po -> {
            CabinetVO vo = new CabinetVO();
            vo.setCabinetId(po.getCabinetId());
            vo.setDeviceId(po.getDeviceId());
            vo.setNumber(po.getNumber());
            vo.setStatus(po.getStatus());
            vo.setAssigned(po.getClusterId() != null);
            vo.setKindName(po.getKindId() != null ? kindNameMap.get(po.getKindId()) : null);
            vo.setClusterName(po.getClusterId() != null ? clusterNameMap.get(po.getClusterId()) : null);
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
    public HttpResponseVO<String> assignKind(AssignKindDTO dto) {
        CabinetPO cabinet = cabinetMapper.selectById(dto.getCabinetId());
        if (cabinet == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("寄存柜不存在")
                    .build();
        }

        // 已分配柜群的不允许操作
        if (cabinet.getClusterId() != null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("该寄存柜已分配柜群，无法修改种类")
                    .build();
        }

        // 校验种类是否存在
        CabinetKindPO kind = cabinetKindMapper.selectById(dto.getKindId());
        if (kind == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("寄存柜种类不存在")
                    .build();
        }

        cabinet.setKindId(dto.getKindId());
        cabinetMapper.updateById(cabinet);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("分配种类成功")
                .build();
    }

    @Override
    public HttpResponseVO<String> assignCluster(AssignClusterDTO dto) {
        CabinetPO cabinet = cabinetMapper.selectById(dto.getCabinetId());
        if (cabinet == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("寄存柜不存在")
                    .build();
        }

        // 已分配柜群的不允许操作
        if (cabinet.getClusterId() != null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("该寄存柜已分配柜群，无法重复分配")
                    .build();
        }

        // 必须先分配种类
        if (cabinet.getKindId() == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("请先设置寄存柜种类")
                    .build();
        }

        // 校验柜群是否存在
        ClusterPO cluster = clusterMapper.selectById(dto.getClusterId());
        if (cluster == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("柜群不存在")
                    .build();
        }

        cabinet.setClusterId(dto.getClusterId());
        cabinetMapper.updateById(cabinet);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("分配柜群成功")
                .build();
    }

    @Override
    public HttpResponseVO<List<KindOptionVO>> listKindOptions() {
        List<KindOptionVO> list = cabinetKindMapper.selectList(null)
                .stream()
                .map(po -> {
                    KindOptionVO vo = new KindOptionVO();
                    vo.setKindId(po.getKindId());
                    vo.setName(po.getName());
                    return vo;
                })
                .collect(Collectors.toList());

        return HttpResponseVO.<List<KindOptionVO>>builder()
                .data(list)
                .code(HttpStatusConstants.SUCCESS)
                .msg("查询成功")
                .build();
    }

    @Override
    public HttpResponseVO<List<ClusterOptionVO>> listClusterOptions() {
        List<ClusterOptionVO> list = clusterMapper.selectList(null)
                .stream()
                .map(po -> {
                    ClusterOptionVO vo = new ClusterOptionVO();
                    vo.setClusterId(po.getClusterId());
                    vo.setName(po.getName());
                    return vo;
                })
                .collect(Collectors.toList());

        return HttpResponseVO.<List<ClusterOptionVO>>builder()
                .data(list)
                .code(HttpStatusConstants.SUCCESS)
                .msg("查询成功")
                .build();
    }
}
