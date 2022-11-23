package org.example.api.bean;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)//解决参数为null不返回前端
public class RequestVersion implements Serializable {
    @NotNull(message = "应用包名不能为空")
    private String packageName;
    @NotNull(message = "应用版本号不能为空")
    private long versionCode;
    @NotNull(message = "应用签名hash不能为空")
    private String hash;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "RequestVersion{" +
                "packageName='" + packageName + '\'' +
                ", versionCode=" + versionCode +
                ", hash='" + hash + '\'' +
                '}';
    }
}
