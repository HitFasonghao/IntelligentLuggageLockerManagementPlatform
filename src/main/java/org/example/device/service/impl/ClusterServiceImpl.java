package org.example.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.vo.HttpResponseVO;
import org.example.device.dto.ClusterQueryDTO;
import org.example.device.dto.CreateClusterDTO;
import org.example.device.dto.UpdateCabinetStatusDTO;
import org.example.device.enums.CabinetStatusEnum;
import org.example.device.mapper.CabinetKindMapper;
import org.example.device.mapper.CabinetMapper;
import org.example.device.mapper.ClusterMapper;
import org.example.device.po.CabinetKindPO;
import org.example.device.po.CabinetPO;
import org.example.device.po.ClusterPO;
import org.example.device.service.ClusterService;
import org.example.device.vo.CabinetVO;
import org.example.device.vo.ClusterDetailVO;
import org.example.device.vo.ClusterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClusterServiceImpl implements ClusterService {

    @Autowired
    private ClusterMapper clusterMapper;

    @Autowired
    private CabinetMapper cabinetMapper;

    @Autowired
    private CabinetKindMapper cabinetKindMapper;

    @Override
    public HttpResponseVO<String> createCluster(CreateClusterDTO dto) {
        ClusterPO po = new ClusterPO();
        po.setVendorId(getVendorId());
        po.setName(dto.getName());
        po.setLocation(dto.getLocation());
        po.setLongitude(dto.getLongitude());
        po.setDimension(dto.getDimension());
        po.setDescription(dto.getDescription());
        po.setStatus(org.example.device.enums.ClusterStatusEnum.FORBIDDEN);
        clusterMapper.insert(po);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("添加柜群成功")
                .build();
    }

    @Override
    public HttpResponseVO<Map<String, Object>> listClusters(ClusterQueryDTO queryDTO) {
        LambdaQueryWrapper<ClusterPO> wrapper = Wrappers.lambdaQuery();

        // 限制只查询当前厂商的柜群
        wrapper.eq(ClusterPO::getVendorId, getVendorId());

        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(ClusterPO::getName, queryDTO.getName());
        }
        if (StringUtils.hasText(queryDTO.getLocation())) {
            wrapper.like(ClusterPO::getLocation, queryDTO.getLocation());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(ClusterPO::getStatus, queryDTO.getStatus());
        }

        Page<ClusterPO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<ClusterPO> resultPage = clusterMapper.selectPage(page, wrapper);

        List<ClusterVO> voList = resultPage.getRecords().stream().map(po -> {
            ClusterVO vo = new ClusterVO();
            vo.setClusterId(po.getClusterId());
            vo.setName(po.getName());
            vo.setLocation(po.getLocation());
            vo.setLongitude(po.getLongitude());
            vo.setDimension(po.getDimension());
            vo.setStatus(po.getStatus());
            vo.setDescription(po.getDescription());
            vo.setCreatedTime(po.getCreatedTime());
            vo.setUpdatedTime(po.getUpdatedTime());
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
    public HttpResponseVO<ClusterDetailVO> getClusterDetail(Integer clusterId) {
        ClusterPO po = clusterMapper.selectById(clusterId);
        if (po == null) {
            return HttpResponseVO.<ClusterDetailVO>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("柜群不存在")
                    .build();
        }

        // 查询该柜群下的所有寄存柜
        List<CabinetPO> cabinetList = cabinetMapper.selectList(
                Wrappers.<CabinetPO>lambdaQuery().eq(CabinetPO::getClusterId, clusterId)
        );

        // 预加载种类名称
        Map<Integer, String> kindNameMap = cabinetKindMapper.selectList(null)
                .stream()
                .collect(Collectors.toMap(CabinetKindPO::getKindId, CabinetKindPO::getName));

        List<CabinetVO> cabinetVOList = cabinetList.stream().map(c -> {
            CabinetVO vo = new CabinetVO();
            vo.setCabinetId(c.getCabinetId());
            vo.setDeviceId(c.getDeviceId());
            vo.setNumber(c.getNumber());
            vo.setStatus(c.getStatus());
            vo.setAssigned(c.getClusterId() != null);
            vo.setKindName(c.getKindId() != null ? kindNameMap.get(c.getKindId()) : null);
            vo.setClusterName(po.getName());
            return vo;
        }).collect(Collectors.toList());

        ClusterDetailVO detailVO = new ClusterDetailVO();
        detailVO.setClusterId(po.getClusterId());
        detailVO.setName(po.getName());
        detailVO.setLocation(po.getLocation());
        detailVO.setLongitude(po.getLongitude());
        detailVO.setDimension(po.getDimension());
        detailVO.setStatus(po.getStatus());
        detailVO.setDescription(po.getDescription());
        detailVO.setCreatedTime(po.getCreatedTime());
        detailVO.setUpdatedTime(po.getUpdatedTime());
        detailVO.setCabinetCount((long) cabinetVOList.size());
        detailVO.setCabinets(cabinetVOList);

        return HttpResponseVO.<ClusterDetailVO>builder()
                .data(detailVO)
                .code(HttpStatusConstants.SUCCESS)
                .msg("查询成功")
                .build();
    }

    @Override
    public HttpResponseVO<String> updateCabinetStatus(UpdateCabinetStatusDTO dto) {
        CabinetPO cabinet = cabinetMapper.selectById(dto.getCabinetId());
        if (cabinet == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("寄存柜不存在")
                    .build();
        }

        CabinetStatusEnum currentStatus = cabinet.getStatus();
        CabinetStatusEnum targetStatus = dto.getStatus();

        // 禁用操作：只能在free状态下执行，opening/using状态不允许
        if (targetStatus == CabinetStatusEnum.FORBIDDEN) {
            if (currentStatus == CabinetStatusEnum.OPENING || currentStatus == CabinetStatusEnum.USING) {
                return HttpResponseVO.<String>builder()
                        .code(HttpStatusConstants.ERROR)
                        .msg("寄存柜正在使用中，无法禁用")
                        .build();
            }
            if (currentStatus == CabinetStatusEnum.FORBIDDEN) {
                return HttpResponseVO.<String>builder()
                        .code(HttpStatusConstants.ERROR)
                        .msg("寄存柜已处于禁用状态")
                        .build();
            }
        }

        // 启用操作：只能在forbidden状态下执行
        if (targetStatus == CabinetStatusEnum.FREE) {
            if (currentStatus != CabinetStatusEnum.FORBIDDEN) {
                return HttpResponseVO.<String>builder()
                        .code(HttpStatusConstants.ERROR)
                        .msg("只能启用处于禁用状态的寄存柜")
                        .build();
            }
        }

        cabinet.setStatus(targetStatus);
        cabinetMapper.updateById(cabinet);

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg(targetStatus == CabinetStatusEnum.FORBIDDEN ? "禁用成功" : "启用成功")
                .build();
    }

    @Override
    public HttpResponseVO<String> removeCabinetFromCluster(Integer cabinetId) {
        CabinetPO cabinet = cabinetMapper.selectById(cabinetId);
        if (cabinet == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("寄存柜不存在")
                    .build();
        }

        if (cabinet.getStatus() != CabinetStatusEnum.FORBIDDEN) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("只能删除处于禁用状态的寄存柜")
                    .build();
        }

        cabinetMapper.update(null, Wrappers.<CabinetPO>lambdaUpdate()
                .eq(CabinetPO::getCabinetId, cabinetId)
                .set(CabinetPO::getClusterId, null)
                .set(CabinetPO::getStatus, CabinetStatusEnum.FORBIDDEN));

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("删除成功")
                .build();
    }

    private Integer getVendorId() {
        PcUserInfo userInfo = UserContext.get();
        return userInfo.getVendorId();
    }
}
