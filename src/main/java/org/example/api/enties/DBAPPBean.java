package org.example.api.enties;

import lombok.Data;

@Data

public class DBAPPBean {
    public DBAPPBean(String packageName, long versionCode, String hash, String apkPath) {
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.hash = hash;
        this.apkPath = apkPath;
    }

    public String packageName;
    public long versionCode;
    public String hash;
    public String apkPath;
}
