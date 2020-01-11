package org.nickle.nprofiler.registry;

import com.google.common.collect.Maps;
import feign.Feign;
import feign.Request;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.AgentInfo;
import org.nickle.nprofiler.exception.NprofilerException;
import org.nickle.nprofiler.service.IAgentServerService;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MemoryAgentRegistry implements IAgentRegistry {
    private final Map<String, AgentInfo> AGENT_MAP = Maps.newConcurrentMap();
    private final Map<String, IAgentServerService> AGENT_SERVICE_MAP = Maps.newConcurrentMap();

    @Override
    public void regist(AgentInfo agentInfo) throws Exception {
        String id = UUID.randomUUID().toString();
        agentInfo.setId(id);
        log.info("id:{}", id);
        AgentInfo oldAgentInfo = AGENT_MAP.put(id, agentInfo);
        if (oldAgentInfo != null) {
            log.info("重复注册:{}", oldAgentInfo);
        }
    }

    @Override
    public void delete(String socketInfo) throws Exception {
        AGENT_MAP.remove(socketInfo);
    }

    @Override
    public Collection<AgentInfo> getAllAgentInfo() throws Exception {
        return AGENT_MAP.values();
    }

    @Override
    public IAgentServerService getAgentService(String id) throws Exception {
        AgentInfo agentInfo = AGENT_MAP.get(id);
        IAgentServerService agentServerService;
        if (agentInfo == null) {
            throw new NprofilerException("机器没有注册");
        }
        try {
            agentServerService = AGENT_SERVICE_MAP.get(id);
            if (agentServerService == null) {
                Request.Options options = new Request.Options(3, TimeUnit.SECONDS, 3, TimeUnit.SECONDS, true);
                agentServerService = Feign.builder()
                        .encoder(new GsonEncoder())
                        .decoder(new GsonDecoder())
                        .options(options)
                        .target(IAgentServerService.class, agentInfo.getSocketInfo());
                AGENT_SERVICE_MAP.put(id, agentServerService);
            }
        } catch (Exception e) {
            log.error(e.toString());
            AGENT_MAP.remove(id);
            throw new NprofilerException("获取AgentService失败");
        }
        return agentServerService;
    }
}
