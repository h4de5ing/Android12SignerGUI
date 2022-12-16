package org.example.api.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseVersion {
    public ResponseVersion(Object message) {
        this.data = message;
    }

    public String code = "200";
    public Object data;
    public String message = "请求成功";
}
