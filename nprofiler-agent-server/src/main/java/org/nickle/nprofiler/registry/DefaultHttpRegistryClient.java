package org.nickle.nprofiler.registry;

import feign.*;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.AgentInfo;
import org.nickle.nprofiler.bean.CommonResponse;
import org.nickle.nprofiler.exception.NprofilerException;

import java.util.concurrent.TimeUnit;

import static org.nickle.nprofiler.constant.CommonConstant.AGENT_HEART_BEAT_MAPPING;
import static org.nickle.nprofiler.constant.CommonConstant.AGENT_REGIST_MAPPING;

@Slf4j
public class DefaultHttpRegistryClient implements IRegistryClient {
    private RemoteService remoteService;

    @Override
    public boolean regist(String socketInfo) throws Exception {
        validate();
        if (socketInfo == null) {
            socketInfo = getDefaultSocketInfo();
        }
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setDescription("test");
        agentInfo.setName("测试");
        agentInfo.setSocketInfo(socketInfo);
        CommonResponse commonResponse = remoteService.regist(agentInfo);
        if (commonResponse.getCode().equals(CommonResponse.SUCCESS_CODE)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean heartBeat(String socketInfo) throws Exception {
        validate();
        if (socketInfo == null) {
            socketInfo = getDefaultSocketInfo();
        }
        CommonResponse commonResponse = remoteService.heartBeat(socketInfo);
        if (commonResponse.getCode().equals(CommonResponse.SUCCESS_CODE)) {
            return true;
        }
        return false;
    }

    private String getDefaultSocketInfo() {
        return "http://localhost:9001";
    }

    public void init(String remoteServerUrl) {
        Request.Options options = new Request.Options(3, TimeUnit.SECONDS, 3, TimeUnit.SECONDS, true);
        remoteService = Feign.builder()
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .options(options)
                .target(RemoteService.class, remoteServerUrl);
    }

    private void validate() {
        if (remoteService == null) {
            throw new NprofilerException("DefaultHttpRegistryClient 没有初始化");
        }
    }

    private interface RemoteService {
        @RequestLine("POST " + AGENT_REGIST_MAPPING)
        @Headers("Content-Type: application/json")
        CommonResponse regist(AgentInfo agentInfo);

        @RequestLine("PUT " + AGENT_HEART_BEAT_MAPPING)
        CommonResponse heartBeat(@Param("socketInfo") String socketInfo);

    }

}
