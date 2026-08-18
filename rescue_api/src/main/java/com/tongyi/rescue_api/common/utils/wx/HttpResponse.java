package com.tongyi.rescue_api.common.utils.wx;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.CaseInsensitiveMap;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class HttpResponse implements Serializable {

    private String body;
    private int status;
    private Map<String, List<String>> headers;

    public String getHeader(String name) {
        List<String> values = this.headerList(name);
        return CollectionUtil.isEmpty(values) ? null : values.get(0);
    }

    private List<String> headerList(String name) {
        if (StrUtil.isBlank(name)) {
            return null;
        } else {
            CaseInsensitiveMap<String, List<String>> headersIgnoreCase = new CaseInsensitiveMap<>(getHeaders());
            return headersIgnoreCase.get(name.trim());
        }
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "body='" + body + '\'' +
                ", status=" + status +
                ", headers=" + headers +
                '}';
    }
}
