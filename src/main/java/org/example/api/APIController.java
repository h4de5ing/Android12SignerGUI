package org.example.api;

import org.example.api.bean.*;
import org.example.api.enties.DBAPPBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/index")
public class APIController {
    @RequestMapping("/")
    public ResponseEntity<Response> index() {
        Response response = new Response();
        response.setCode("200");
        response.setMessage("APP ota 接口正常运行中...");
        response.setData("");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //全局保存所有的签名 hash ,dirPath
    public static Map<String, String> map = new HashMap<>();
    public static List<DBAPPBean> appList = new ArrayList();

    @RequestMapping("/getSigns")
    public String getSigns() {
        return map.keySet().toString();
    }

    @RequestMapping("/apks")
    public String apks() {
        return appList.toString();
    }


    @PostMapping("/upload")
    public ResponseEntity<SuccessResponse> upload(@RequestParam("file") MultipartFile file, String packageName, long version, String sign) {
        if (file.isEmpty()) throw new BaseBusinessException(DefaultError.PARAMETER_EMPTY);
        String fileName = packageName + "_" + sign + "_" + version + "_" + file.getOriginalFilename();
        String filePath = "baseApk";
        File fileDir = new File(filePath);
        if (!fileDir.exists()) fileDir.mkdir();
        File dest = new File(fileDir.getAbsoluteFile() + File.separator + fileName);
        try {
            file.transferTo(dest);
            //更新路径
//            String apkPath = filePath + File.separator + fileName;
            return new ResponseEntity<>(new SuccessResponse("文件上传成功"), HttpStatus.OK);
        } catch (Exception e) {
            throw new BaseBusinessException("文件上传失败:" + e.getMessage());
        }
    }

    /**
     * 根据 app参数请求版本
     *
     * @param requestVersion 请求信息
     */
    @RequestMapping("/getVersion")
    public ResponseEntity<ResponseVersion> getVersion(@Validated RequestVersion requestVersion) {
        System.out.println("收到请求:" + requestVersion);
        return new ResponseEntity<>(new ResponseVersion(updateApp(requestVersion)), HttpStatus.OK);
    }

    /**
     * 根据tag请求app的版本号
     *
     * @return
     */
    @RequestMapping("/getTagVersion")
    public String getTagVersion() {
        return "";
    }

    private DBAPPBean updateApp(RequestVersion requestVersion) {
        DBAPPBean searchApp = new DBAPPBean(requestVersion.getPackageName(), requestVersion.getVersionCode(), requestVersion.getHash(), "");
        int idx = appList.indexOf(searchApp);
        if (idx > -1 && idx < appList.size()) {
            searchApp = appList.get(idx);
            if (requestVersion.getVersionCode() > searchApp.getVersionCode()) appList.set(idx, searchApp);
        } else {
            appList.add(searchApp);
            searchApp.versionCode = 0;
        }
        return searchApp;
    }
}