package org.example.service;

import org.example.vo.HttpResponseVO;
import org.example.vo.VendorUserInfoVO;

/**
 * @author fasonghao
 */
public interface VendorUserService {
    /**
     * 查询账号信息
     */
    HttpResponseVO<VendorUserInfoVO> queryVendorUserInfo();
}
