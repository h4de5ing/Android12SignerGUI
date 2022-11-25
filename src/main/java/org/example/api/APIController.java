package org.example.api;

import org.example.api.bean.RequestVersion;
import org.example.api.bean.Response;
import org.example.api.bean.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;

@RestController
@RequestMapping("/index")
public class APIController {
    @RequestMapping("/")
    public ResponseEntity<Response> index() {
        Response response = new Response();
        response.setCode("200");
        response.setMessage("APP ota 接口正常运行中...");
        response.setData("");
        return new ResponseEntity(response, HttpStatus.OK);
    }

    //全局保存所有的签名 hash ,dirPath
    public static HashMap<String, String> map = new HashMap<>();

    @RequestMapping("/getSigns")
    public String getSigns() {
        return map.keySet().toString();
    }

    @PostMapping("/upload")
    public ResponseEntity<SuccessResponse> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) throw new BaseBusinessException("文件为空");
        String fileName = file.getOriginalFilename();
        String filePath = "./baseApk";
        File fileDir = new File(filePath);
        if (!fileDir.exists()) fileDir.mkdir();
        File dest = new File(fileDir.getAbsoluteFile() + File.separator + fileName);
        try {
            file.transferTo(dest);
            return new ResponseEntity<>(new SuccessResponse("文件上传成功"), HttpStatus.OK);
        } catch (Exception e) {
            throw new BaseBusinessException("文件上传失败:" + e.getMessage());
        }
    }

    @RequestMapping("/getVersion")
    public String getVersion(@Validated RequestVersion requestVersion) {
        System.out.println("发送了请求:" + requestVersion);
        if (requestVersion == null) throw new BaseBusinessException("参数错误");
        return requestVersion.toString();
    }
}