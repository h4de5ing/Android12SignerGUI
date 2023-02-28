package org.example;

import org.example.config.PemUtils;
import org.example.config.ConfigBean;
import org.example.config.GetConfig;
import org.example.config.SignUtils;
import org.example.config.TagBean;

import java.io.File;
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
                System.out.println("待签名APK " + apkPath);
                File fileApk = new File(apkPath);
                File signFiles = new File("SignFiles");
                GetConfig config = GetConfig.getInstance(args[1]);
                String tempTag = config.getKey("tag");
                String tag = tempTag == null ? "" : tempTag;
                System.out.println("tag:" + tag);
                String tempSigns = config.getKey("signs");
                if (tempSigns == null) System.err.println("没有配置签名信息，请配置app.config");
                String signs = tempSigns == null ? "" : tempSigns;
                System.out.println("signs:" + signs);
                PemUtils.getAPkInfo(fileApk.getAbsolutePath());
                String packageName = PemUtils.getPackageName();
                String versionName = PemUtils.getVersionName();
                long versionCode = PemUtils.getVersionCode();
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
                            SignUtils.sign(fileApk, outPath + File.separator + outFileName, filePk8.getAbsolutePath(), filePem.getAbsolutePath());
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
                updateLog("APK路径和配置文件未找到，java -cp APKSignerGUI-1.3-SNAPSHOT.jar org.example.Sign app.apk app.config");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateLog(String message) {
        System.err.println(message);
    }
}