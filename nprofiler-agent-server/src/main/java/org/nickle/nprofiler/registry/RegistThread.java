package org.nickle.nprofiler.registry;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class RegistThread extends Thread {
    private String socketInfo;
    private IRegistryClient registryClient;
    private boolean isRegist;

    @Override
    public void run() {

        while (!Thread.interrupted()) {
            if (!isRegist) {
                try {
                    registryClient.regist(socketInfo);
                } catch (Exception e) {
                    log.error(e.toString());
                }
                continue;
            }

            try {
                registryClient.heartBeat(socketInfo);
            } catch (Exception e) {
                log.error(e.toString());
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }

    }
}
