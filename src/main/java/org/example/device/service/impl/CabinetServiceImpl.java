package org.example.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.audit.mapper.VendorMapper;
import org.example.audit.po.VendorPO;
import org.example.auth.common.PcUserInfo;
import org.example.auth.common.UserContext;
import org.example.auth.constants.HttpStatusConstants;
import org.example.auth.vo.HttpResponseVO;
import org.example.device.dto.AssignClusterDTO;
import org.example.device.dto.AssignKindDTO;
import org.example.device.dto.CabinetQueryDTO;
import org.example.device.enums.CabinetStatusEnum;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CabinetServiceImpl implements CabinetService {

    @Autowired
    private CabinetMapper cabinetMapper;

    @Autowired
    private CabinetKindMapper cabinetKindMapper;

    @Autowired
    private ClusterMapper clusterMapper;

    @Autowired
    private VendorMapper vendorMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public HttpResponseVO<String> syncCabinets() {
        Integer vendorId = getVendorId();

        // 获取厂商信息
        VendorPO vendor = vendorMapper.selectById(vendorId);
        if (vendor == null) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商信息不存在")
                    .build();
        }

        String apiEndpoint = vendor.getApiEndpoint();
        String accessToken = vendor.getVendorAccessToken();
        if (!StringUtils.hasText(apiEndpoint) || !StringUtils.hasText(accessToken)) {
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商接口地址或访问Token未配置")
                    .build();
        }

        // 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        int pageSize = 1000;
        int pageNum = 1;
        int insertedCount = 0;

        try {
            while (true) {
                String url = apiEndpoint + "/api/getDevices?pageNum=" + pageNum + "&pageSize=" + pageSize;

                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<>() {}
                );

                Map<String, Object> body = response.getBody();
                if (body == null || !Integer.valueOf(1200).equals(body.get("code"))) {
                    log.error("同步设备失败，远程接口返回异常: {}", body);
                    return HttpResponseVO.<String>builder()
                            .code(HttpStatusConstants.ERROR)
                            .msg("远程接口调用失败")
                            .build();
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                @SuppressWarnings("unchecked")
                List<String> deviceIds = data != null ? (List<String>) data.get("deviceIds") : null;

                if (deviceIds == null || deviceIds.isEmpty()) {
                    break;
                }

                // 用这批deviceId去数据库查已存在的
                List<CabinetPO> existList = cabinetMapper.selectList(
                        Wrappers.<CabinetPO>lambdaQuery()
                                .eq(CabinetPO::getVendorId, vendorId)
                                .in(CabinetPO::getDeviceId, deviceIds)
                                .select(CabinetPO::getDeviceId)
                );
                Set<String> existDeviceIds = new HashSet<>();
                for (CabinetPO c : existList) {
                    existDeviceIds.add(c.getDeviceId());
                }

                // 筛出新设备，批量插入
                List<CabinetPO> toInsert = new ArrayList<>();
                for (String deviceId : deviceIds) {
                    if (!existDeviceIds.contains(deviceId)) {
                        CabinetPO po = new CabinetPO();
                        po.setVendorId(vendorId);
                        po.setDeviceId(deviceId);
                        po.setStatus(CabinetStatusEnum.FREE);
                        toInsert.add(po);
                    }
                }
                if (!toInsert.isEmpty()) {
                    cabinetMapper.insert(toInsert);
                    insertedCount += toInsert.size();
                }

                // 本页数据不足pageSize，说明已经是最后一页
                if (deviceIds.size() < pageSize) {
                    break;
                }
                pageNum++;
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("同步设备异常：无法连接厂商接口 {}", apiEndpoint, e);
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商接口连接失败，请检查接口地址配置是否正确（" + apiEndpoint + "）")
                    .build();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("同步设备异常：厂商接口返回 {}", e.getStatusCode(), e);
            String detail;
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                detail = "厂商接口拒绝访问，请检查访问Token配置是否正确";
            } else {
                detail = "厂商接口请求失败（HTTP " + e.getStatusCode().value() + "）";
            }
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg(detail)
                    .build();
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("同步设备异常：厂商接口服务端错误 {}", e.getStatusCode(), e);
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("厂商接口服务端异常（HTTP " + e.getStatusCode().value() + "），请联系厂商排查")
                    .build();
        } catch (Exception e) {
            log.error("同步设备异常", e);
            return HttpResponseVO.<String>builder()
                    .code(HttpStatusConstants.ERROR)
                    .msg("同步设备异常: " + e.getMessage())
                    .build();
        }

        return HttpResponseVO.<String>builder()
                .code(HttpStatusConstants.SUCCESS)
                .msg("同步完成，新增 " + insertedCount + " 台设备")
                .build();
    }

    @Override
    public HttpResponseVO<Map<String, Object>> listCabinets(CabinetQueryDTO queryDTO) {
        Integer vendorId = getVendorId();
        LambdaQueryWrapper<CabinetPO> wrapper = Wrappers.lambdaQuery();

        // 限制只查询当前厂商的寄存柜
        wrapper.eq(CabinetPO::getVendorId, vendorId);

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
        Integer vendorId = getVendorId();
        LambdaQueryWrapper<CabinetKindPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CabinetKindPO::getVendorId, vendorId);

        List<KindOptionVO> list = cabinetKindMapper.selectList(wrapper)
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
        Integer vendorId = getVendorId();
        LambdaQueryWrapper<ClusterPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ClusterPO::getVendorId, vendorId);

        List<ClusterOptionVO> list = clusterMapper.selectList(wrapper)
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

    private Integer getVendorId() {
        PcUserInfo userInfo = UserContext.get();
        return userInfo.getVendorId();
    }
}
