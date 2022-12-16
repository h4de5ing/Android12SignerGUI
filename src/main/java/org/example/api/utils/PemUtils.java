package org.example.api.utils;

import org.example.api.APIController;
import org.example.api.enties.DBAPPBean;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PemUtils {
    public static void run() {
        File file = new File("SignFiles");
        pemList.clear();
        getAllPem(file);
        for (File listFile : pemList) {
            try {
                X509Certificate certObject = getCertObject(listFile.getAbsolutePath());
                String print = getThumbprintMD5(certObject);
                System.out.println(listFile.getAbsolutePath() + " [" + print + "]");
                APIController.map.put(print, listFile.getParent());
            } catch (Exception e) {
                //System.err.println("发生异常:" + listFile.getAbsolutePath());
                //e.printStackTrace();
            }
        }
        File apkFile = new File("baseApk");
        apkList.clear();
        getAllAPK(apkFile);
        for (File listFile : apkList) {
            try {
                DBAPPBean bean = APKUtils.getOneApkInfo(listFile.getAbsolutePath());
                if (bean != null) APIController.appList.add(bean);
            } catch (Exception e) {
                //System.err.println("发生异常:" + listFile.getAbsolutePath());
                //e.printStackTrace();
            }
        }
    }

    public static Set<File> pemList = new HashSet<>();
    public static Set<File> apkList = new HashSet<>();

    public static void getAllPem(File file) {
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            if (listFile.isDirectory()) getAllPem(listFile);
            else if (listFile.getName().contains("platform.x509.pem")) pemList.add(listFile.getAbsoluteFile());
        }
    }

    public static void getAllAPK(File file) {
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            if (listFile.isDirectory()) getAllAPK(listFile);
            else if (listFile.getName().contains(".apk")) apkList.add(listFile.getAbsoluteFile());
        }
    }

    public static X509Certificate getCertObject(String filePath) throws IOException, CertificateException {
        try (FileInputStream is = new FileInputStream(filePath)) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(is);
        }
    }

    public static String getThumbprintSHA1(X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(cert.getEncoded());
        return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
    }

    public static String getThumbprintMD5(X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(cert.getEncoded());
        return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
    }

    public static String getThumbprintSHA256(X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(cert.getEncoded());
        return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
    }

    /**
     * 从 apk 中获取 MD5 签名信息
     *
     * @param apkPath apk文件路径
     * @return 返回md5值
     * @throws Exception
     */
    public static String getApkSignatureMD5(String apkPath) throws Exception {
        return hexDigest(getSignaturesFromApk(apkPath), "MD5");
    }

    public static String getApkSignatureSHA1(String apkPath) throws Exception {
        return hexDigest(getSignaturesFromApk(apkPath), "SHA1");
    }

    public static String getApkSignatureSHA256(String apkPath) throws Exception {
        return hexDigest(getSignaturesFromApk(apkPath), "SHA256");
    }

    /**
     * 从APK中读取签名
     *
     * @param apkPath apk路径
     * @return
     * @throws IOException
     */
    public static byte[] getSignaturesFromApk(String apkPath) throws IOException {
        File file = new File(apkPath);
        JarFile jarFile = new JarFile(file);
        try {
            JarEntry je = jarFile.getJarEntry("AndroidManifest.xml");
            byte[] readBuffer = new byte[8192];
            Certificate[] certs = loadCertificates(jarFile, je, readBuffer);
            if (certs != null) for (Certificate c : certs) return c.getEncoded();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 加载签名
     *
     * @param jarFile    jar文件
     * @param je         jar实体
     * @param readBuffer 读取缓存
     * @return
     */
    public static Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer) {
        try {
            InputStream is = jarFile.getInputStream(je);
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
            }
            is.close();
            return je != null ? je.getCertificates() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String hexDigest(byte[] bytes, String algorithm) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance(algorithm);
            byte[] md5Bytes = md5.digest(bytes);
            StringBuilder hexValue = new StringBuilder();
            for (byte md5Byte : md5Bytes) {
                int val = ((int) md5Byte) & 0xff;
                if (val < 16) hexValue.append("0");
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}