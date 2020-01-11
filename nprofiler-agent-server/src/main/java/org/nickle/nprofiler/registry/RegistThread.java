package org.nickle.nprofiler.registry;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.exception.NprofilerException;

@Data
@Slf4j
public class RegistThread extends Thread {
    private String socketInfo;
    private IRegistryClient registryClient;
    private boolean isRegist;

    public RegistThread(IRegistryClient registryClient) {
        this.registryClient = registryClient;
    }

    @Override
    public void run() {
        if (registryClient == null) {
            throw new NprofilerException("registryClient为空");
        }
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            if (!isRegist) {
                try {
                    log.info("开始注册");
                    if (registryClient.regist(socketInfo)) {
                        isRegist = true;
                        log.info("注册成功");
                    }
                } catch (Exception e) {
                    log.error(e.toString());
                }
                continue;
            }

            try {
                log.info("开始心跳");
                if (registryClient.heartBeat(socketInfo)) {
                    log.info("心跳成功");
                } else {
                    isRegist = false;
                }
            } catch (Exception e) {
                log.error(e.toString());
                isRegist = false;
            }

        }

    }
}
