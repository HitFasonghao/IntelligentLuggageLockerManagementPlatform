package org.example.auth.mapper;

import org.example.auth.po.PlatformAdminPO;
import org.example.auth.po.VendorUserPO;
import org.example.auth.vo.AdminInfoVO;
import org.example.auth.vo.VendorUserInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author fasonghao
 */
@Mapper(componentModel = "spring")
public interface MapStructMapper {

    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "realName", target = "realName")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "passwordChangedTime", target = "passwordChangedTime")
    @Mapping(source = "createdTime", target = "createdTime")
    @Mapping(source = "updatedTime", target = "updatedTime")
    VendorUserInfoVO vendorUserToInfoVO(VendorUserPO vendorUser);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "realName", target = "realName")
    @Mapping(source = "isActive", target = "isActive")
    @Mapping(source = "isSuperAdmin", target = "isSuperAdmin")
    @Mapping(source = "createdTime", target = "createdTime")
    @Mapping(source = "updatedTime", target = "updatedTime")
    AdminInfoVO adminToInfoVO(PlatformAdminPO platformAdmin);

}
