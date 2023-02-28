package org.example.config;

import com.android.apksig.ApkVerifier;
import com.android.apksig.apk.ApkFormatException;
import com.android.apksig.apk.ApkUtils;
import com.android.apksig.internal.apk.AndroidBinXmlParser;
import com.android.apksig.util.DataSource;
import com.android.apksig.util.DataSources;
import com.android.apksigner.HexEncoding;

import java.io.*;
import java.nio.ByteBuffer;
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
            } catch (Exception e) {
                //System.err.println("发生异常:" + listFile.getAbsolutePath());
                //e.printStackTrace();
            }
        }
        File apkFile = new File("baseApk");
        apkList.clear();
        getAllAPK(apkFile);
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
        return HexEncoding.encode(md.digest()).toLowerCase();
    }

    public static String getThumbprintMD5(X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(cert.getEncoded());
        return HexEncoding.encode(md.digest()).toLowerCase();
    }

    public static String getThumbprintSHA256(X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(cert.getEncoded());
        return HexEncoding.encode(md.digest()).toLowerCase();
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
     * 加载签名,无法加载v2签名
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

    public static String getApkSignerMD5(String apkPath) {
        String md5 = "";
        try {
            ApkVerifier.Builder apkVerifierBuilder = new ApkVerifier.Builder(new File(apkPath));
            ApkVerifier apkVerifier = apkVerifierBuilder.build();
            ApkVerifier.Result result = apkVerifier.verify();
            if (result.isVerified()) {
                if (result.isVerifiedUsingV1Scheme()) System.out.println("v1 scheme");
                if (result.isVerifiedUsingV2Scheme()) System.out.println("v2 scheme");
                if (result.isVerifiedUsingV3Scheme()) System.out.println("v3 scheme");
                if (result.isVerifiedUsingV31Scheme()) System.out.println("v3.1 scheme");
                if (result.isVerifiedUsingV4Scheme()) System.out.println("v4 scheme");
                md5 = getThumbprintMD5(result.getSignerCertificates().get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5;
    }

    public static void getAPkInfo(String apkPath) {
        try {
            DataSource inputApk = DataSources.asDataSource(new RandomAccessFile(apkPath, "r"));
            ByteBuffer buffer = ApkUtils.getAndroidManifest(inputApk);
            getPackageNameFromBinaryAndroidManifest(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String versionCode = "";
    private static String versionName = "";
    private static String sharedUserId = "";
    private static String packageName = "";

    public static void getPackageNameFromBinaryAndroidManifest(ByteBuffer androidManifestContents) {
        try {
            try {
                AndroidBinXmlParser parser = new AndroidBinXmlParser(androidManifestContents);
                int eventType = parser.getEventType();
                while (eventType != AndroidBinXmlParser.EVENT_END_DOCUMENT) {
                    if ((eventType == AndroidBinXmlParser.EVENT_START_ELEMENT)
                            && (parser.getDepth() == 1)
                            && ("manifest".equals(parser.getName()))
                            && (parser.getNamespace().isEmpty())) {
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            String attributeName = parser.getAttributeName(i);
                            String attributeStringValue = parser.getAttributeStringValue(i);
                            if ("versionCode".equals(parser.getAttributeName(i)))
                                versionCode = parser.getAttributeStringValue(i);
                            if ("versionName".equals(parser.getAttributeName(i)))
                                versionName = parser.getAttributeStringValue(i);
                            if ("sharedUserId".equals(parser.getAttributeName(i)))
                                sharedUserId = parser.getAttributeStringValue(i);
                            if ("package".equals(parser.getAttributeName(i))) packageName = attributeStringValue;
//                            System.out.println(attributeName + " = " + attributeStringValue);
                        }
                    }
                    eventType = parser.next();
                }
            } catch (AndroidBinXmlParser.XmlParserException e) {
                throw new ApkFormatException("Unable to determine APK package name: malformed binary resource: AndroidManifest.xml", e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPackageName() {
        return packageName;
    }

    public static String getVersionName() {
        return versionName;
    }

    public static long getVersionCode() {
        return !Objects.equals(versionCode, "") ? Long.parseLong(versionCode) : 0L;
    }

    public static String getSharedUserId() {
        return sharedUserId;
    }
}