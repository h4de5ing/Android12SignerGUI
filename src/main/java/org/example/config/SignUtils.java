package org.example.config;

import com.android.apksig.ApkSigner;
import com.android.apksigner.ApkSignerTool;
import com.android.apksigner.PasswordRetriever;
import com.android.apksigner.SignerParams;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SignUtils {
    private static SignUtils signUtils;

    public static SignUtils getInstance() {
        if (null == signUtils) {
            signUtils = new SignUtils();
            initEnv();
        }
        return signUtils;
    }

    public static boolean isWindow = true;
    public static String java = "";
    public static String openssl = "";
    public static String keytool = "";

    private static void initEnv() {
        isWindow = System.getProperty("os.name").startsWith("Windows");
        String javaPath = findJavaPath();
        String javaPath1 = findCurrentPath("java");
        openssl = findCurrentPath("openssl");
        keytool = findCurrentPath("keytool");
        if (checkPath(javaPath1)) java = javaPath1;
        else if (checkPath(javaPath)) java = javaPath;
        else System.err.println("java路径 没有找到...【" + javaPath1 + "】不存在");
        System.out.println("如果签名工具在使用中有什么问题请提供问题截图或者日志联系开发者:moxi1992@gmail.com");
        System.out.println("java:" + java);
        System.out.println("openssl:" + openssl);
        System.out.println("keytools:" + keytool);
    }

    //查找执行文件
    private static String fileExeFilePath(String exe) {
        String result = "";
        try {
            //在class path路径里面找
            String[] split = System.getProperty("java.class.path").split(";");
            for (String s : split) {
//                System.out.println(s);
                if (new File(s + File.separator + exe).exists()) {
                    result = s + File.separator + exe;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String findJavaPath() {
        String result = "";
        //在系统环境变量里面找
//        Iterator it = System.getenv().entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry entry = (Map.Entry) it.next();
////            System.out.println(entry.getKey() + "=" + entry.getValue());
//            for (String s : entry.getValue().toString().split(";")) {
//                File javaPath = new File(s + File.separator + (isWindow ? "java.exe" : "java"));
//                if (javaPath.exists() && javaPath.getAbsolutePath().contains("bin")) {
//                    result = javaPath.getAbsolutePath();
//                }
//            }
//        }
        //在Path里面找
        String path = System.getenv("PATH");
        for (String s : path.split(";")) {
//            System.out.println(s);
            File javaPath = new File(s + File.separator + (isWindow ? "java.exe" : "java"));
            if (javaPath.exists() && javaPath.getAbsolutePath().contains("bin")) {
                result = javaPath.getAbsolutePath();
            }
        }
        return result;
    }

    private static String findCurrentPath(String name) {
        try {
            findFile(new File("."), (isWindow ? name + ".exe" : name));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getAbsolutePath;
    }

    private static String getAbsolutePath = "";

    private static void findFile(File file, String filename) throws IOException {
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            if (listFile.isDirectory()) findFile(listFile, filename);
            else if (listFile.getName().equals(filename)) getAbsolutePath = listFile.getCanonicalPath();
        }
    }

    private static boolean checkPath(String path) {
        boolean isExists = false;
        try {
            File file = new File(path);
            isExists = file.exists();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return isExists;
    }

    public static void write2File(String message, String path) {
        try {
            FileWriter writer = new FileWriter(path, false);
            writer.write(message);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runCommand(String command, StringChange stringChange) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream is = process.getInputStream();
            //TODO 在Mac系统上测试编码是否会有乱码问题
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
            String outInfo;
            while ((outInfo = reader.readLine()) != null) stringChange.updateLog(outInfo);
            process.waitFor();
            process.exitValue();
            is.close();
            reader.close();
        } catch (Exception e) {
            stringChange.updateLog(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sign(File inputApk, String tmpOutputApkPath, String pk8File, String pemFile) {
        List<SignerParams> signers = new ArrayList<>(1);
        SignerParams signerParams = new SignerParams();
        signerParams.setKeyFile(pk8File);
        signerParams.setCertFile(pemFile);
        signers.add(signerParams);
        List<ApkSigner.SignerConfig> signerConfigs = new ArrayList<>(signers.size());
        try (PasswordRetriever passwordRetriever = new PasswordRetriever()) {
            for (SignerParams signer : signers) {
                ApkSigner.SignerConfig signerConfig = ApkSignerTool.getSignerConfig(signer, passwordRetriever, true);
                signerConfigs.add(signerConfig);
            }
        }
        try {
            File tmpOutputApk = new File(tmpOutputApkPath);
            ApkSigner.Builder apkSignerBuilder =
                    new ApkSigner.Builder(signerConfigs)
                            .setInputApk(inputApk)
                            .setOutputApk(tmpOutputApk)
                            .setOtherSignersSignaturesPreserved(false)
                            .setV1SigningEnabled(true)
                            .setV2SigningEnabled(true)
                            .setV3SigningEnabled(true)
                            .setV4SigningEnabled(true)
                            .setForceSourceStampOverwrite(false)
                            .setAlignFileSize(false)
                            .setVerityEnabled(false)
                            .setV4ErrorReportingEnabled(false)
                            .setDebuggableApkPermitted(true)
                            .setSigningCertificateLineage(null)
                            .setMinSdkVersionForRotation(33)
                            .setRotationTargetsDevRelease(false);
            ApkSigner apkSigner = apkSignerBuilder.build();
            apkSigner.sign();
        } catch (Exception e) {
            System.err.println("签名失败" + e.getMessage());
            e.printStackTrace();
        }
    }
}
