package org.example;

import org.example.config.PemUtils;
import org.example.config.ConfigBean;
import org.example.config.TagBean;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Config {
    public static void main(String[] args) {
        try {
            boolean isDelete = false;
            try {
                if (args.length > 0) isDelete = Boolean.valueOf(args[0]);
            } catch (Exception e) {
                System.err.println("无参数输入 isDelete=" + isDelete);
            }
            File file = new File(new File(".").getCanonicalFile() + File.separator);
            System.out.println("工作目录:" + file.getAbsoluteFile());
            System.out.println("父级目录:" + file.getParentFile().getAbsolutePath());
            File[] files = file.listFiles();
            if (files != null) {
                String packageName = "";
                String versionName = "";
                long versionCode = 0;
                List<TagBean> list = new ArrayList<>();
                for (File listFile : file.listFiles()) {
                    if (listFile.getName().endsWith(".apk")) {
                        try {
                            if (listFile.exists()) {
//                                System.out.println("APK路径:" + listFile.getAbsolutePath());
                                PemUtils.getAPkInfo(listFile.getAbsolutePath());
                                packageName = PemUtils.getPackageName();
                                versionName = PemUtils.getVersionName();
                                versionCode = PemUtils.getVersionCode();
                                String tag = "";
                                try {
                                    tag = readFile(listFile.getAbsolutePath().replace(".apk", ".json"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                TagBean tagBean = new TagBean(tag.replace("\n", ""), "v" + versionName + "/" + listFile.getName());
                                tagBean.hash = PemUtils.getApkSignerMD5(listFile.getAbsolutePath());
                                list.add(tagBean);
                            } else {
                                System.err.println("对应APK文件不存在 " + listFile.getAbsoluteFile());
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
                        FileWriter writer = new FileWriter(file.getParentFile().getAbsolutePath() + File.separator + packageName + ".json");
                        writer.write(bean.toString());
                        writer.flush();
                        if (isDelete) for (File file1 : files) if (file1.getName().endsWith(".json")) file1.delete();
                        System.out.println("配置文件生成成功");
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
