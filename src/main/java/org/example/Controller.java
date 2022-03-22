package org.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    ComboBox<String> cbb;
    @FXML
    Button clear;
    @FXML
    TextField sign_file;
    @FXML
    Button open_sign_file;
    @FXML
    TextField apk_path;
    @FXML
    Button open_apk_path;
    @FXML
    Button start;
    @FXML
    TextArea log;
    String pk8 = "platform.pk8";
    String pem = "platform.x509.pem";
    //对齐命令的路径
    String zipalign = "";
    String apksigner = "";
    String java = "";
    File dirSign;//sign 文件夹
    File filePk8;
    File filePem;
    File fileAPK;//待签名的apk文件
    String outFileName = "out";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initEnv();
        cbb.setEditable(true);
        cbb.valueProperty().addListener((observableValue, oldValue, newValue) -> sign_file.setText(allFileList.get(newValue)));
        initSignDir();
        sign_file.textProperty().addListener((observableValue, s, newValue) -> updateStartStatus());
        apk_path.textProperty().addListener((observableValue, s, newValue) -> updateStartStatus());
        clear.setOnAction(event -> sign_file.setText(""));
        open_sign_file.setOnAction(event -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("选择一个文件夹");
            File file = dc.showDialog(new Stage());
            if (file != null) {
                dirSign = file;
                sign_file.setText(file.getAbsolutePath());
                filePk8 = new File(file.getAbsoluteFile() + File.separator + pk8);
                if (!filePk8.exists()) {
                    updateLog(filePk8 + " 文件不存在");
                }
                filePem = new File(file.getAbsoluteFile() + File.separator + pem);
                if (!filePem.exists()) {
                    updateLog(filePem + " 文件不存在");
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
                apk_path.setText(file.getAbsolutePath());
                try {
                    outFileName = file.getName().split("\\.")[0];
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
        start.setOnAction(event -> sign());
    }

    //初始化环境变量
    private void initEnv() {
        String javaPath = findJavaPath();
        String javaPath2 = PP.getInstance().getKey("java");
        if (checkPath(javaPath)) {
            java = javaPath;
        } else {
            if (checkPath(javaPath2)) java = javaPath2;
        }
        String zipaligzPath = fileExeFilePath("zipalign.exe");
        String zipaligzPath2 = PP.getInstance().getKey("zipalign");
        if (checkPath(zipaligzPath)) {
            zipalign = zipaligzPath;
        } else {
            if (checkPath(zipaligzPath2)) zipalign = zipaligzPath2;
        }
        String apksignerPath = fileExeFilePath("apksigner.jar");
        String apksignerPath2 = PP.getInstance().getKey("apksigner");
        if (checkPath(apksignerPath)) {
            apksigner = apksignerPath;
        } else {
            if (checkPath(apksignerPath2)) apksigner = apksignerPath2;
        }
        System.out.println(java);
        System.out.println(zipalign);
        System.out.println(apksigner);
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
        boolean isWindow = System.getProperty("os.name").startsWith("Windows");
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

    private boolean checkPath(String path) {
        boolean isExists = false;
        try {
            File file = new File(path);
            isExists = file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isExists;
    }

    private void updateStartStatus() {
        start.setDisable(false);
        if (dirSign == null || !dirSign.exists()) {
            start.setDisable(false);
        }
        if (filePk8 == null || !filePk8.exists()) {
            start.setDisable(false);
        }
        if (filePem == null || !filePem.exists()) {
            start.setDisable(false);
        }
        if (fileAPK == null || !fileAPK.exists()) {
            start.setDisable(false);
        }
    }

    private final HashMap<String, String> allFileList = new HashMap<>();

    private void initSignDir() {
        File file = new File("SignFiles");
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) allFileList.put(f.getName(), f.getAbsolutePath());
            cbb.getItems().addAll(allFileList.keySet());
            try {
                cbb.valueProperty().set(files[0].getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateStartStatus();
        }
    }

    private void sign() {
        String fileDir = sign_file.getText();
        String apkFile = apk_path.getText();
        if (new File(fileDir).exists()) {
            filePk8 = new File(new File(fileDir).getAbsoluteFile() + File.separator + pk8);
            filePem = new File(new File(fileDir).getAbsoluteFile() + File.separator + pem);
            if (filePk8.exists()) {
                if (filePem.exists()) {
                    fileAPK = new File(apkFile);
                    if (fileAPK.exists()) {
                        File outDir = new File("out");
                        if (!outDir.exists()) {
                            boolean result = outDir.mkdir();
                            System.out.println(outDir.getAbsolutePath() + (result ? " 创建成功" : "创建失败"));
                        }
                        String alignAPK = outDir.getAbsolutePath() + File.separator + "unalign.apk";
                        String outPath = outDir.getAbsolutePath() + File.separator + outFileName + "_signed.apk";
                        //开始对齐
                        runCommand(zipalign + " -p -v 4 " + fileAPK.getAbsolutePath() + " " + alignAPK);
//                        System.out.println("对齐文件：" + alignAPK);
                        //签名
                        runCommand(java + " -jar " + apksigner + " sign --key " + filePk8.getAbsolutePath() + " --cert " + filePem.getAbsolutePath() + " --out " + outDir.getAbsolutePath() + File.separator + outFileName + "_signed.apk " + alignAPK);
//                        System.out.println("签名文件：" + outPath);
                        updateLog("签名成功\n" + outPath);
                    } else {
                        updateLog(fileAPK + " 请选择一个APK");
                    }
                } else {
                    updateLog(filePem + " 文件不存在");
                }
            } else {
                updateLog(filePk8 + " 文件不存在");
            }
        } else {
            updateLog(fileDir + " \n路径不存在");
        }
    }

    private void runCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
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
}
