package org.example.auth.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * @author fasonghao
 */
@Component // 必须加这个注解，让Spring管理
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 严格判断，如果字段本身已经有值（手动设置了），则不覆盖
        if (this.getFieldValByName("createdTime", metaObject) == null) {
            this.setFieldValByName("createdTime", LocalDateTime.now(), metaObject);
        }
        if (this.getFieldValByName("updatedTime", metaObject) == null) {
            this.setFieldValByName("updatedTime", LocalDateTime.now(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updatedTime", LocalDateTime.now(), metaObject);
    }
}