package org.example.api.enties;

import lombok.Data;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DBAPPBean dbappBean = (DBAPPBean) o;

        if (versionCode != dbappBean.versionCode) return false;
        if (!Objects.equals(packageName, dbappBean.packageName))
            return false;
        return Objects.equals(hash, dbappBean.hash);
    }

    @Override
    public int hashCode() {
        int result = packageName != null ? packageName.hashCode() : 0;
        result = 31 * result + (int) (versionCode ^ (versionCode >>> 32));
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        return result;
    }
}
