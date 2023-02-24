package org.example.config;

public class TagBean {
    public TagBean(String tag, String apkPath) {
        this.tag = tag;
        this.apkPath = apkPath;
    }

    public TagBean(String tag, String apkPath, String hash) {
        this.tag = tag;
        this.apkPath = apkPath;
        this.hash = hash;
    }

    public String tag;
    public String apkPath;
    public String hash;

    @Override
    public String toString() {
        return "{\"tag\":\"" + tag + "\",\"apkPath\":\"" + apkPath + "\",\"hash\":\"" + hash + "\"}";
    }
}
