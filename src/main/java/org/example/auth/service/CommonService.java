package org.example.auth.service;

import org.example.auth.dto.CountIsExistDTO;

/**
 * @author fasonghao
 */
public interface CommonService {

    /**
     * 判断平台管理员账号是否存在
     */
    boolean isExistAdmin(CountIsExistDTO countIsExistDTO);

    /**
     * 判断厂商用户是否存在
     */
    boolean isExistVendorUser(CountIsExistDTO countIsExistDTO);
}
