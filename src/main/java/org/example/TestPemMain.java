package org.example;

import net.dongliu.apk.parser.ApkParser;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.DexClass;
import net.dongliu.apk.parser.bean.UseFeature;

import java.io.File;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TestPemMain {

    public static void main(String[] args) {
        try {
            String path = "D:\\test\\hardscan\\v5.01.25\\hardscan_v5.01.25_20230217163510_normal-release-unsigned.apk";
//            File apkFile = new File(path);
//            ZipFile file = new ZipFile(apkFile, ZipFile.OPEN_READ);
//            Enumeration list = file.entries();
//            while (list.hasMoreElements()) {
//                ZipEntry entry = (ZipEntry) list.nextElement();
//                if (entry.getName().contains(".dex")) {//只加载dex文件
//                    System.out.println(entry.getName());
//                    loadClassLoader(entry);
//                }
//            }
            ApkParser apkParser = new ApkParser(new File(path));
            ApkMeta apkMeta = apkParser.getApkMeta();
            System.out.println(apkMeta.getLabel());
            System.out.println(apkMeta.getPackageName());
            System.out.println(apkMeta.getVersionCode());
            System.out.println(apkParser.verifyApk());
            for (DexClass dexClass : apkParser.getDexClasses()) {
                System.out.println(dexClass.isPublic());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadClassLoader(ZipEntry entry) {

    }
}
