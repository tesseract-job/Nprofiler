package org.nickle.nprofiler.registry;

import feign.Feign;
import feign.Param;
import feign.RequestLine;
import feign.gson.GsonDecoder;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.AgentInfo;
import org.nickle.nprofiler.exception.NprofilerException;

import static org.nickle.nprofiler.constant.CommonConstant.AGENT_HEART_BEAT_MAPPING;
import static org.nickle.nprofiler.constant.CommonConstant.AGENT_REGIST_MAPPING;

@Slf4j
public class DefaultHttpRegistryClient implements IRegistryClient {
    private RemoteService remoteService;

    @Override
    public void regist(String socketInfo) throws Exception {
        validate();
    }

    @Override
    public void heartBeat(String socketInfo) throws Exception {
        validate();
    }


    public void init(String remoteServerUrl) {
        remoteService = Feign.builder()
                .decoder(new GsonDecoder())
                .target(RemoteService.class, remoteServerUrl);
    }

    private void validate() {
        if (remoteService == null) {
            throw new NprofilerException("DefaultHttpRegistryClient 没有初始化");
        }
    }

    private interface RemoteService {
        @RequestLine("POST " + AGENT_REGIST_MAPPING)
        void regist(AgentInfo agentInfo);

        @RequestLine("PUT " + AGENT_HEART_BEAT_MAPPING)
        void heartBeat(@Param("socketInfo") String socketInfo);

    }

}
