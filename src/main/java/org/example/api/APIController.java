package org.example.api;

import org.example.api.bean.RequestVersion;
import org.example.api.bean.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/index")
public class APIController {
    @RequestMapping("/")
    public ResponseEntity<Response> index() {
        Response response = new Response();
        response.setCode("200");
        response.setMessage("APP ota 接口正常运行中...");
        response.setData("");
        return new ResponseEntity(response, HttpStatus.LOOP_DETECTED);
    }

    @RequestMapping("/getVersion")
    public String getVersion(@Validated RequestVersion requestVersion) {
        System.out.println("发送了请求:" + requestVersion);
        if (requestVersion == null) throw new BaseBusinessException("参数错误");
        return requestVersion.toString();
    }
}