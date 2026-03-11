package org.example.device.config;

import com.baomidou.mybatisplus.annotation.EnumValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 通用枚举转换器工厂，支持将请求参数字符串转换为带有 @EnumValue 注解的枚举
 */
@Component
public class EnumConverterFactory implements ConverterFactory<String, Enum<?>> {

    @Override
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return source -> {
            // 先尝试通过 @EnumValue 字段匹配
            for (Field field : targetType.getDeclaredFields()) {
                if (field.isAnnotationPresent(EnumValue.class)) {
                    field.setAccessible(true);
                    for (T enumConstant : targetType.getEnumConstants()) {
                        try {
                            if (source.equals(String.valueOf(field.get(enumConstant)))) {
                                return enumConstant;
                            }
                        }
                        catch (IllegalAccessException ignored) {
                        }
                    }
                }
            }
            // 兜底：按枚举名称匹配
            for (T enumConstant : targetType.getEnumConstants()) {
                if (enumConstant.name().equalsIgnoreCase(source)) {
                    return enumConstant;
                }
            }
            return null;
        };
    }
}
