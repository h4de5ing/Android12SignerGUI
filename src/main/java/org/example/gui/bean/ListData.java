package org.example.gui.bean;

public class ListData {
    private String tag;
    private String hash;
    private String apkPath;

    public ListData(String tag, String hash, String apkPath) {
        this.tag = tag;
        this.hash = hash;
        this.apkPath = apkPath;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    @Override
    public String toString() {
        return "ListData{" +
                "tag='" + tag + '\'' +
                ", hash='" + hash + '\'' +
                ", apkPath='" + apkPath + '\'' +
                '}';
    }
}
