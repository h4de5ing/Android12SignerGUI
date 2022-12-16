package org.example.api.utils;


import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class APKUtils {
    private static final Namespace NS = Namespace.getNamespace("http://schemas.android.com/apk/res/android");

    public static void main(String[] args) {
        String path = "./baseAPk/base.apk";
        SAXBuilder builder = new SAXBuilder();
        Document document;
        InputStream stream = null;
        try {
            stream = new ByteArrayInputStream(AXMLPrinter.getManifestXMLFromAPK(path).getBytes(StandardCharsets.UTF_8));
            document = builder.build(stream);
            Element root = document.getRootElement();
            System.out.println(root.getAttributeValue("versionCode", NS));
            System.out.println(root.getAttributeValue("versionName", NS));
            String s = root.getAttributes().toString();
            String c[] = s.split(",");
            for (String a : c) {
                if (a.contains("package")) {
                    System.out.println(a.substring(a.indexOf("package=\"") + 9, a.lastIndexOf("\"")));
                }
            }

            List booklist = root.getChildren("uses-sdk");
            Element book = (Element) booklist.get(0);
            System.out.println("minSdkVersion:" + book.getAttributeValue("minSdkVersion", NS));
            System.out.println("targetSdkVersion" + book.getAttributeValue("targetSdkVersion", NS));

            booklist = root.getChildren("uses-permission");
            for (Iterator iter = booklist.iterator(); iter.hasNext(); ) {
                Element tempBook = (Element) iter.next();
                System.out.println("permissions:" + tempBook.getAttributeValue("name", NS));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void getAllAPKInfo() {

    }

    /**
     * 从 apk 中获取 MD5 签名信息
     *
     * @param apkPath apk文件路径
     * @return 返回md5值
     * @throws Exception
     */
    public static String getApkSignatureMD5(String apkPath) throws Exception {
        byte[] sign = getSignaturesFromApk(apkPath);
        if (sign != null) return hexDigest(sign, "MD5");
        else return "";
    }

    public static String getApkSignatureSHA1(String apkPath) throws Exception {
        byte[] sign = getSignaturesFromApk(apkPath);
        if (sign != null) return hexDigest(sign, "SHA1");
        else return "";
    }

    public static String getApkSignatureSHA256(String apkPath) throws Exception {
        byte[] sign = getSignaturesFromApk(apkPath);
        if (sign != null) return hexDigest(sign, "SHA256");
        else return "";
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
