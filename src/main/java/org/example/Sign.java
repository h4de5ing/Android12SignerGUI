package org.example;

import net.dongliu.apk.parser.ApkParser;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.example.api.utils.PemUtils;
import org.example.config.ConfigBean;
import org.example.config.GetConfig;
import org.example.config.SignUtils;
import org.example.config.TagBean;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Sign {
    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                SignUtils.getInstance();
                String pk8 = "platform.pk8";
                String pem = "platform.x509.pem";
                String apkPath = args[0];
                File fileApk = new File(apkPath);
                File signFiles = new File("SignFiles");
                GetConfig config = GetConfig.getInstance(args[1]);
                String tag = config.getKey("tag");
                String signs = config.getKey("signs");
//                System.out.println(tag);
                ApkParser apkParser = new ApkParser(fileApk);
                ApkMeta apkMeta = apkParser.getApkMeta();
                String packageName = apkMeta.getPackageName();
                String versionName = apkMeta.getVersionName();
                long versionCode = apkMeta.getVersionCode();
                List<TagBean> list = new ArrayList<>();
                for (String s : signs.split(",")) {
                    String dir = signFiles.getAbsolutePath() + File.separator + s + File.separator;
                    File signDir = new File(dir);
                    File filePk8 = new File(signDir.getAbsolutePath() + File.separator + pk8);
                    File filePem = new File(signDir.getAbsolutePath() + File.separator + pem);
                    if (filePem.exists()) {
                        if (filePk8.exists()) {
                            String outPath = "out" + File.separator + packageName + File.separator + "v" + versionName;
                            String outFileName = fileApk.getName().replace("-unsigned", "").replace(".apk", "_") + s + "_signed.apk";
                            String aboutPath = outPath + File.separator + outFileName;
                            File outDir = new File(outPath);
                            if (!outDir.exists()) outDir.mkdirs();
//                        System.out.println(outFileName);
                            SignUtils.runCommand(SignUtils.java + " -jar " + SignUtils.apksigner + " sign --key " + filePk8.getAbsolutePath() + " --cert " + filePem.getAbsolutePath() + " --out " + outPath + File.separator + outFileName + " " + apkPath, null);
                            updateLog(outFileName + " 签名成功");
                            new File(aboutPath + ".idsig").delete();
                            try {
                                list.add(new TagBean(tag, "v" + versionName + "/" + outFileName, PemUtils.getThumbprintMD5(PemUtils.getCertObject(filePem.getAbsolutePath()))));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else updateLog(filePk8 + " 文件不存在");
                    } else updateLog(filePem + " 文件不存在");
                }
                if (list.size() > 0) {
                    ConfigBean bean = new ConfigBean(packageName, versionCode, list);
                    System.out.println(bean);
                    SignUtils.write2File(bean.toString(), "out" + File.separator + packageName + File.separator + packageName + ".json");
                } else updateLog("没有生成签名apk");
            } else {
                updateLog("APK路径和配置文件未找到");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateLog(String message) {
        System.err.println(message);
    }

    public static void main33(String[] args) {
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