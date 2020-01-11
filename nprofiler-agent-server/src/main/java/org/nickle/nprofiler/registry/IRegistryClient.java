package org.nickle.nprofiler.registry;

public interface IRegistryClient {
    void regist(String socketInfo) throws Exception;

    void heartBeat(String socketInfo) throws Exception;
}
