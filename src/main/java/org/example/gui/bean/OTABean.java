package org.example.gui.bean;

import java.util.ArrayList;
import java.util.List;

public class OTABean {
    private String packageName;
    private long versionCode;
    private List<ListData> list;

    public OTABean(String packageName, long versionCode) {
        this.packageName = packageName;
        this.versionCode = versionCode;
    }

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

    public void add(ListData data) {
        if (list == null) list = new ArrayList<>();
        list.add(data);
    }

    public List<ListData> getList() {
        return list;
    }

    public void setList(List<ListData> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "OTABean{" +
                "packageName='" + packageName + '\'' +
                ", versionCode=" + versionCode +
                ", list=" + list +
                '}';
    }
}
