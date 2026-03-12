package org.example.auth.service;

import org.example.auth.dto.UpdateAdminDTO;
import org.example.auth.vo.AdminInfoVO;
import org.example.auth.vo.HttpResponseVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 更换头像
     */
    HttpResponseVO<String> updateAvatar(MultipartFile file);
}
