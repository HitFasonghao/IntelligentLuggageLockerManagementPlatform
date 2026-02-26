package org.example.service;

import org.example.dto.CountIsExistDTO;
import org.example.dto.UpdateAdminDTO;
import org.example.vo.AdminInfoVO;
import org.example.vo.HttpResponseVO;
import org.springframework.stereotype.Service;

/**
 * @author fasonghao
 */
@Service
public interface AdminInfoService {

    /**
     * 查询账号状态
     */
    HttpResponseVO<AdminInfoVO> queryAdminInfo();

    /**
     * 修改账号状态
     */
    HttpResponseVO<String> updateAdminInfo(UpdateAdminDTO updateVendorUserDTO);
}
