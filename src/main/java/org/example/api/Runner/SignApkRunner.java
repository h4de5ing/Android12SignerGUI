package org.example.api.Runner;

import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Log
public class SignApkRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        log.info(">>>>>>>>>>>>>>>计划执行签名任务<<<<<<<<<<<<<");
    }
}
