package org.example.api;


import lombok.extern.java.Log;
import org.example.api.utils.PemUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Log
public class InitRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        log.info("开机启动...");
        new Thread(PemUtils::run).start();
    }
}
