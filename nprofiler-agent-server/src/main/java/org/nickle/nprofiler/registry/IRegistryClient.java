package org.nickle.nprofiler.registry;

public interface IRegistryClient {
    boolean regist(String socketInfo) throws Exception;

    boolean heartBeat(String socketInfo) throws Exception;
}
