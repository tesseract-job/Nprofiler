package org.nickle.nprofiler.registry;

public interface IRegistryClient {
    boolean regist() throws Exception;

    boolean heartBeat() throws Exception;
}
