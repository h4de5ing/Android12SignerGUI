package org.example;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.api.utils.PemUtils;

import java.io.*;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class Controller implements Initializable {
    @FXML
    ComboBox<String> cbb;
    @FXML
    Button clear;
    @FXML
    Button jks;
    @FXML
    Button hash;
    @FXML
    TextField sign_file;
    @FXML
    Button open_sign_file;
    @FXML
    TextField apk_path;
    @FXML
    Button open_apk_path;
    @FXML
    Button check_apk;
    @FXML
    Button start;
    @FXML
    TextArea log;
    @FXML
    CheckBox multi;
    @FXML
    CheckBox cbIdsig;
    @FXML
    FlowPane platforms;
    String pk8 = "platform.pk8";
    String pem = "platform.x509.pem";
    //对齐命令的路径
    String apksigner = "";
    String java = "";
    String openssl = "";
    String keytool = "";
    File dirSign;//sign 文件夹
    File fileAPK;//待签名的apk文件
    String outFileName = "out";
    boolean idsig = true;//是否删除
    boolean isWindow = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initEnv();
        cbIdsig.setSelected(true);
        cbb.setEditable(true);
        cbb.valueProperty().addListener((observableValue, oldValue, newValue) -> sign_file.setText(allFileList.get(newValue)));
        initSignDir();
        multi.selectedProperty().addListener((observableValue, oldValue, newValue) -> platforms.setVisible(newValue));
        cbIdsig.selectedProperty().addListener((observableValue, oldValue, newValue) -> idsig = newValue);
        sign_file.textProperty().addListener((observableValue, s, newValue) -> updateStartStatus());
        apk_path.textProperty().addListener((observableValue, s, newValue) -> updateStartStatus());
        clear.setOnAction(event -> sign_file.setText(""));
        jks.setOnAction(event -> jks());
        hash.setOnAction(event -> hash());
        check_apk.setOnAction(event -> printSign());
        open_sign_file.setOnAction(event -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("选择一个文件夹");
            dc.setInitialDirectory(new File("./"));
            File file = dc.showDialog(new Stage());
            if (file != null) {
                dirSign = file;
                sign_file.setText(dirSign.getAbsolutePath());
                try {
                    cbb.valueProperty().set(dirSign.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            updateStartStatus();
        });
        open_apk_path.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("APK file (*.apk)", "*.apk");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(new Stage());
            if (file != null) {
                fileAPK = file;
                check_apk.setDisable(false);
                apk_path.setText(file.getAbsolutePath());
                try {
                    outFileName = file.getName().split("\\.apk")[0];
                } catch (Exception e) {
                    e.printStackTrace();
                }
                updateStartStatus();
            }
        });
        apk_path.textProperty().addListener((observableValue, s, apk) -> {
            apk_path.setText(apk);
            updateStartStatus();
        });
        start.setOnAction(event -> {
            if (multi.isSelected()) multiSign();
            else sign();
        });
        updateStartStatus();
    }

    //初始化环境变量
    private void initEnv() {
        isWindow = System.getProperty("os.name").startsWith("Windows");
        String javaPath = findJavaPath();
        String javaPath1 = findCurrentPath("java");
        openssl = findCurrentPath("openssl");
        keytool = findCurrentPath("keytool");
        if (checkPath(javaPath1)) java = javaPath1;
        else if (checkPath(javaPath)) java = javaPath;
        else System.err.println("java路径 没有找到...【" + javaPath1 + "】不存在");
        String apksignerPath = new File("apksigner.jar").getAbsolutePath();
        String apksignerPath2 = fileExeFilePath("apksigner.jar");
        if (checkPath(apksignerPath)) apksigner = apksignerPath;
        else if (checkPath(apksignerPath2)) apksigner = apksignerPath2;
        else System.err.println("签名工具apksigner.jar 没有找到...【" + apksignerPath + "】不存在");
        System.out.println("如果签名工具在使用中有什么问题请提供问题截图或者日志联系开发者:moxi1992@gmail.com");
        System.out.println(java);
        System.out.println(apksigner);
        System.out.println(openssl);
        System.out.println(keytool);
    }

    //查找执行文件
    private String fileExeFilePath(String exe) {
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

    private String findJavaPath() {
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

    private String findCurrentPath(String name) {
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

    private boolean checkPath(String path) {
        boolean isExists = false;
        try {
            File file = new File(path);
            isExists = file.exists();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return isExists;
    }

    private void updateStartStatus() {
        if (fileAPK != null) start.setDisable(!fileAPK.exists());
        else start.setDisable(true);
        platforms.setVisible(multi.isSelected());
        if (System.currentTimeMillis() >= getExpire("20240101")) {
            start.setDisable(true);
            updateLog("授权过期，请联系开发者重新获取授权：moxi1992@gmail.com");
        }
    }

    private long getExpire(String date) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new SimpleDateFormat("yyyyMMdd").parse(date));
            return cal.getTimeInMillis();
        } catch (Exception ignored) {
        }
        return getExpire("20240601");
    }

    private final TreeMap<String, String> allFileList = new TreeMap<>();

    private void initSignDir() {
        File file = new File("SignFiles");
        File[] files = file.listFiles();
        //更新多平台选项
        platforms.setPadding(new Insets(5, 5, 5, 0));
        if (files != null) {
            for (File f : files) {
                allFileList.put(f.getName(), f.getAbsolutePath());
                CheckBox checkBox = new CheckBox(f.getName());
                checkBox.setSelected(true);
                platforms.getChildren().add(checkBox);
            }
            cbb.getItems().addAll(allFileList.keySet());
            try {
                cbb.valueProperty().set(files[0].getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //单独签一个
    private void sign() {
        String fileDir = sign_file.getText();
        String apkFile = apk_path.getText();
        File outDir = new File("out");
        if (!outDir.exists()) {
            boolean result = outDir.mkdir();
            System.out.println(outDir.getAbsolutePath() + (result ? " 创建成功" : "创建失败"));
        }
        if (new File(fileDir).exists()) {
            File filePk8 = new File(new File(fileDir).getAbsoluteFile() + File.separator + pk8);
            File filePem = new File(new File(fileDir).getAbsoluteFile() + File.separator + pem);
            if (filePk8.exists()) {
                if (filePem.exists()) {
                    fileAPK = new File(apkFile);
                    if (fileAPK.exists()) {
                        //签名
                        String outPath = outDir.getAbsolutePath() + File.separator + outFileName + "_" + new File(fileDir).getName() + "_signed.apk";
                        runCommand(java + " -jar " + apksigner + " sign --key " + filePk8.getAbsolutePath() + " --cert " + filePem.getAbsolutePath() + " --out " + outPath + " " + apkFile);
//                        System.out.println("签名文件：" + outPath);
                        updateLog(new File(fileDir).getName() + " 签名成功\n" + outPath);
                        deleteFile(outDir);
                    } else updateLog(fileAPK + " 请选择一个APK");
                } else updateLog(filePem + " 文件不存在");
            } else updateLog(filePk8 + " 文件不存在");
        } else updateLog(fileDir + " \n签名文件路径不存在");
    }

    private void multiSign() {
        String apkFile = apk_path.getText();
        fileAPK = new File(apkFile);
        File outDir = new File("out");
        if (!outDir.exists()) {
            boolean result = outDir.mkdir();
            System.out.println(outDir.getAbsolutePath() + (result ? " 创建成功" : "创建失败"));
        }
        if (fileAPK.exists()) {
            ObservableList<Node> children = platforms.getChildren();
            for (int i = 0; i < children.size(); i++) {
                Node child = children.get(i);
                CheckBox checkBox = (CheckBox) child;
                if (checkBox.isSelected()) {//如果选中就操作
                    String fileDir = allFileList.get(checkBox.getText());
                    File filePk8 = new File(new File(fileDir).getAbsoluteFile() + File.separator + pk8);
                    File filePem = new File(new File(fileDir).getAbsoluteFile() + File.separator + pem);
                    if (filePk8.exists()) {
                        if (filePem.exists()) {
                            String outPath = outDir.getAbsolutePath() + File.separator + outFileName + "_" + checkBox.getText() + "_signed.apk";
                            //签名
                            runCommand(java + " -jar " + apksigner + " sign --key " + filePk8.getAbsolutePath() + " --cert " + filePem.getAbsolutePath() + " --out " + outPath + " " + fileAPK);
                            updateLog(checkBox.getText() + " 签名成功\n" + outPath);
                            if (i == (children.size() - 1)) deleteFile(outDir);
                        } else updateLog(filePem + " 文件不存在");
                    } else updateLog(filePk8 + " 文件不存在");
                }
            }
        } else updateLog(fileAPK + " 请选择一个APK");
    }

    private void deleteFile(File outDir) {
        if (idsig) {
            for (File file : Objects.requireNonNull(outDir.listFiles())) {
                if (file.getAbsolutePath().endsWith(".idsig")) {
                    boolean deleteResult = file.delete();
                    System.out.println(file.getName() + (deleteResult ? " 删除成功" : " 删除失败"));
                }
            }
        }
    }

    private void runCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream is = process.getInputStream();
            //TODO 在Mac系统上测试编码是否会有乱码问题
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
            String outInfo;
            while ((outInfo = reader.readLine()) != null) updateLog(outInfo);
            process.waitFor();
            process.exitValue();
            is.close();
            reader.close();
        } catch (Exception e) {
            updateLog(e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateLog(String message) {
        Platform.runLater(() -> log.appendText(message + "\n"));
    }

    private void jks() {
        try {
            String fileDir = sign_file.getText();
            File platformJks = new File(new File(fileDir).getAbsoluteFile() + File.separator + "platform.jks");
            if (platformJks.exists()) {
                updateLog(platformJks.getAbsolutePath() + " 文件已经存在，无需转换");
            } else {
                File filePk8 = new File(new File(fileDir).getAbsoluteFile() + File.separator + pk8);
                File filePem = new File(new File(fileDir).getAbsoluteFile() + File.separator + pem);

                File platformPem = new File(new File(fileDir).getAbsoluteFile() + File.separator + "platform.pem");
                runCommand(openssl + " pkcs8 -inform DER -nocrypt -in " + filePk8.getAbsolutePath() + " -out " + platformPem.getAbsolutePath());
                File platformP12 = new File(new File(fileDir).getAbsoluteFile() + File.separator + "platform.p12");
                runCommand(openssl + " pkcs12 -export -in  " + filePem.getAbsolutePath() + " -out " + platformP12.getAbsolutePath() + " -inkey  " + platformPem.getAbsolutePath() + " -password pass:android -name android");
                runCommand(keytool + " -importkeystore -deststorepass android -destkeystore " + platformJks.getAbsolutePath() + " -srckeystore " + platformP12.getAbsolutePath() + " -srcstoretype PKCS12 -srcstorepass android");
                if (platformJks.exists()) {
                    updateLog("转换成功");
                    platformPem.delete();
                    platformP12.delete();
                }
            }
        } catch (Exception e) {
            updateLog("发生异常:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //查看签名文件hash
    private void hash() {
        String fileDir = sign_file.getText();
        if (new File(fileDir).exists()) {
            File filePem = new File(new File(fileDir).getAbsoluteFile() + File.separator + pem);
            if (filePem.exists()) {
                try {
                    X509Certificate certObject = PemUtils.getCertObject(filePem.getAbsolutePath());
                    updateLog(filePem.getAbsolutePath() + "\nmd5:" + PemUtils.getThumbprintMD5(certObject) + "\nsha1:" + PemUtils.getThumbprintSHA1(certObject) + "\nsha256:" + PemUtils.getThumbprintSHA256(certObject) + "\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void printSign() {
        String apkFile = apk_path.getText();
        File fileAPK = new File(apkFile);
        if (fileAPK.exists()) {
            runCommand(keytool + " -printcert -jarfile " + apkFile);
        }
    }
}