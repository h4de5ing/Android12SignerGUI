package org.example.api.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse {

    public SuccessResponse(String message) {
        this.data = message;
    }

    public String code = "200";
    public Object data;
    public String message = "请求成功";
}
