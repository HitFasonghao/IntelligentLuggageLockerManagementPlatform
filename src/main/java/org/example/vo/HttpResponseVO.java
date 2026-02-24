package org.example.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author fasonghao
 */
@Data
@Builder
public class HttpResponseVO<T> {
    private T data;

    private int code;

    private String msg;
}
