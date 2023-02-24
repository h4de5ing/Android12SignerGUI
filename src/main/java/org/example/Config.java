package org.example;

import net.dongliu.apk.parser.ApkParser;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.example.config.ConfigBean;
import org.example.config.TagBean;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Config {
    public static void main22(String[] args) {
        try {
            File file = new File(new File(".").getCanonicalFile() + File.separator);
//            System.out.println("工作目录:" + file.getAbsoluteFile());
            File[] files = file.listFiles();
            if (files != null) {
                for (File listFile : file.listFiles()) {
                    if (listFile.getName().endsWith(".apk")) {
                        System.out.println("APK路径:" + listFile.getAbsolutePath());
                        ApkParser apkParser = new ApkParser(listFile.getAbsolutePath());
//                        ApkMeta apkMeta = apkParser.getApkMeta();
//                        String packageName = apkMeta.getPackageName();
//                        long versionCode = apkMeta.getVersionCode();
                        String md5 = apkParser.getCertificateMetaList().get(0).getCertBase64Md5();
                        System.out.println(md5);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            File file = new File(new File(".").getCanonicalFile() + File.separator);
//            System.out.println("工作目录:" + file.getAbsoluteFile());
            File[] files = file.listFiles();
            if (files != null) {
                String packageName = "";
                long versionCode = 0;
                List<TagBean> list = new ArrayList<>();
                for (File listFile : file.listFiles()) {
                    if (listFile.getName().endsWith(".json")) {
                        try {
                            String newName = listFile.getAbsolutePath().replace(".json", ".apk");
                            File apkFile = new File(newName);
                            if (apkFile.exists()) {
                                System.out.println("APK路径:" + apkFile.getAbsolutePath());
                                ApkParser apkParser = new ApkParser(apkFile.getAbsolutePath());
                                ApkMeta apkMeta = apkParser.getApkMeta();
                                packageName = apkMeta.getPackageName();
                                versionCode = apkMeta.getVersionCode();
                                String jsonStr = readFile(listFile.getAbsolutePath());
                                JSONObject object = new org.json.JSONObject(jsonStr);
                                String tag = object.getString("tag");
                                String apkPath = object.getString("apkPath");
                                TagBean tagBean = new TagBean(tag, apkPath);
                                System.out.println(apkParser.getCertificateMetaList());
                                tagBean.hash = apkParser.getCertificateMetaList().get(0).getCertBase64Md5();
                                list.add(tagBean);
                            } else {
                                System.err.println("对应APK文件不存在 " + apkFile.getAbsoluteFile());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    if (list.size() > 0) {
                        ConfigBean bean = new ConfigBean(packageName, versionCode, list);
                        System.out.println(bean);
                        FileWriter writer = new FileWriter(new File("..") + File.separator + packageName + ".json");
                        writer.write(bean.toString());
                        writer.flush();
                        for (File file1 : files) if (file1.getName().endsWith(".json")) file1.delete();
                    } else {
                        System.err.println("2.没有找到APK以及配置文件");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("1.没有找到APK以及配置文件");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String filename) {
        File file = new File(filename);
        BufferedReader bufferedReader = null;
        StringBuilder sb = new StringBuilder();
        try {
            if (file.exists()) {
                bufferedReader = new BufferedReader(new FileReader(filename));
                String line;
                while ((line = bufferedReader.readLine()) != null) sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(bufferedReader);
        }
        return sb.toString();
    }

    public static void closeIO(Closeable... closeables) {
        if (null == closeables) return;
        for (Closeable cb : closeables) {
            try {
                if (null == cb) {
                    continue;
                }
                cb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}