package org.example.auth.init;

import org.example.auth.constants.Constants;
import org.example.auth.mapper.MapStructMapper;
import org.example.auth.mapper.PcPermissionMapper;
import org.example.auth.mapper.PcRoleMapper;
import org.example.auth.po.PcPermissionPO;
import org.example.auth.po.PcRolePO;
import org.example.auth.util.JsonUtil;
import org.example.auth.vo.PcPermissionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目启动之初，加载静态数据到Redis中
 * @author fasonghao
 */
@Component
public class StaticDataLoader implements CommandLineRunner {
    @Autowired
    private PcPermissionMapper pcPermissionMapper;

    @Autowired
    private PcRoleMapper pcRoleMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MapStructMapper mapStructMapper;

    @Override
    public void run(String... args) throws Exception {
        //加载角色在前端对应的权限列表到Redis中
        //获取角色列表
        List<PcRolePO> pcRoles=pcRoleMapper.selectList(null);
        for(PcRolePO pcRole:pcRoles){
            //查询每个角色的权限列表
            List<PcPermissionPO> pcPermissions =pcPermissionMapper.queryByRoleId(pcRole.getRoleId());

            Map<Integer,List<PcPermissionVO>> chrildrenMap=new HashMap<>();
            Map<Integer, PcPermissionVO> permissionVoMap=new HashMap<>();
            List<PcPermissionVO> result=new ArrayList<>();

            for(PcPermissionPO pcPermission : pcPermissions){
                PcPermissionVO pcPermissionVO= mapStructMapper.pcPermissionPoToVo(pcPermission);
                permissionVoMap.put(pcPermissionVO.getId(),pcPermissionVO);
                if(pcPermissionVO.getParentId()!=null){
                    //有父级
                    List<PcPermissionVO> temp=chrildrenMap.getOrDefault(pcPermissionVO.getParentId(),new ArrayList<>());
                    temp.add(pcPermissionVO);
                    chrildrenMap.put(pcPermissionVO.getParentId(),temp);
                }else{
                    //无父级
                    result.add(pcPermissionVO);
                }
            }
            //为有子菜单或按钮的设置children
            for(Map.Entry<Integer,List<PcPermissionVO>> entry : chrildrenMap.entrySet() ){
                permissionVoMap.get(entry.getKey()).setChildren(entry.getValue());
            }

            //序列化为json
            String value= JsonUtil.toString(result);
            //写入Redis
            redisTemplate.opsForValue().set(Constants.ROLE_PERMISSION_PREFIX+pcRole.getCode(),value);
            System.out.println(pcRole.getName()+"对应权限列表已加载成功：" + value);
        }

    }
}