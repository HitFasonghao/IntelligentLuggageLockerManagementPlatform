package org.example.auth.mapper;

import org.example.auth.po.PcPermissionPO;
import org.example.auth.po.PlatformAdminPO;
import org.example.auth.po.VendorUserPO;
import org.example.auth.vo.AdminInfoVO;
import org.example.auth.vo.PcPermissionVO;
import org.example.auth.vo.VendorUserInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * @author fasonghao
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface MapStructMapper {

    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "realName", target = "realName")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "passwordChangedTime", target = "passwordChangedTime")
    @Mapping(source = "createdTime", target = "createdTime")
    @Mapping(source = "updatedTime", target = "updatedTime")
    VendorUserInfoVO vendorUserToInfoVO(VendorUserPO vendorUser);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "realName", target = "realName")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "isActive", target = "isActive")
    @Mapping(source = "isSuperAdmin", target = "isSuperAdmin")
    @Mapping(source = "createdTime", target = "createdTime")
    @Mapping(source = "updatedTime", target = "updatedTime")
    AdminInfoVO adminToInfoVO(PlatformAdminPO platformAdmin);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "code", target = "code")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "parentId", target = "parentId")
    @Mapping(source = "path", target = "path")
    @Mapping(source = "redirect", target = "redirect")
    @Mapping(source = "icon", target = "icon")
    @Mapping(source = "component", target = "component")
    @Mapping(source = "layout", target = "layout")
    @Mapping(source = "keepAlive", target = "keepAlive")
    @Mapping(source = "method", target = "method")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "showStatus", target = "show")
    @Mapping(source = "enableStatus", target = "enable")
    @Mapping(source = "sort", target = "order")
    PcPermissionVO pcPermissionPoToVo(PcPermissionPO pcPermission);
}
