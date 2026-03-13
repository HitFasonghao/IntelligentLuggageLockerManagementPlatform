package org.example.audit.service;

import org.example.audit.vo.VendorUserRelationVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 厂商用户关联服务接口
 * @author fasonghao
 */
@Service
public interface VendorUserRelationService {

    /** 获取厂商下的用户列表 */
    HttpResponseVO<List<VendorUserRelationVO>> getVendorUsers();

    /** 添加厂商用户（通过用户名） */
    HttpResponseVO<String> addVendorUser(String username);

    /** 移除厂商用户 */
    HttpResponseVO<String> removeVendorUser(Integer vendorUserRelationId);
}
