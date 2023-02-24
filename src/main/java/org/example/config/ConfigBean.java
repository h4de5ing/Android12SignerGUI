package org.example.config;

import java.util.List;

public class ConfigBean {
    public ConfigBean(String packageName, long versionCode, List<TagBean> list) {
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.list = list;
    }

    public String packageName;
    public long versionCode;
    public List<TagBean> list;

    @Override
    public String toString() {
        return "{\"packageName\":\"" + packageName + "\",\"versionCode\":" + versionCode + ",\"list\":" + list+"}";
    }
}