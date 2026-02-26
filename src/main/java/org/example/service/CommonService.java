package org.example.service;

import org.example.dto.CountIsExistDTO;

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
