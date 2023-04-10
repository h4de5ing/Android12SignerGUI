package org.example;

import org.example.config.PemUtils;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.*;

public class Md5 {
    public static void main(String[] args) {
        String fileDir = "D:\\Android12SignerGUI\\SignFiles";
        getAllPem(new File(fileDir));
        for (File listFile : pemList) {
            try {
                X509Certificate certObject = PemUtils.getCertObject(listFile.getAbsolutePath());
                String print = PemUtils.getThumbprintMD5(certObject);
                md5.add(print);
                System.out.println(listFile.getAbsolutePath() + " [" + print + "]");
//                System.out.println(print);
            } catch (Exception e) {
                //System.err.println("发生异常:" + listFile.getAbsolutePath());
                //e.printStackTrace();
            }
        }
//        for (String s : md5) {
//            System.out.println(s);
//        }

    }

    public static List<String> md5 = new ArrayList<>();

//    public static Set<String> md5 = new HashSet<>();

    public static Set<File> pemList = new HashSet<>();

    public static void getAllPem(File file) {
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            if (listFile.isDirectory()) getAllPem(listFile);
            else if (listFile.getName().contains("platform.x509.pem")) pemList.add(listFile.getAbsoluteFile());
        }
    }
}
