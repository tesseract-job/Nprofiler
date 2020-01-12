package org.nickle.nprofiler.registry;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import feign.Feign;
import feign.Request;
import feign.Retryer;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.AgentInfo;
import org.nickle.nprofiler.exception.NprofilerException;
import org.nickle.nprofiler.service.IAgentServerService;

import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MemoryAgentRegistry implements IAgentRegistry {
    private final Map<String, AgentInfo> AGENT_MAP = Maps.newConcurrentMap();
    private final Map<String, IAgentServerService> AGENT_SERVICE_MAP = Maps.newConcurrentMap();
    private final Set<String> REGIST_SOCKET_INFO = Sets.newConcurrentHashSet();
    private final Object LOCK = new Object();

    @Override
    public void regist(AgentInfo agentInfo) throws Exception {
        @NotBlank final String socketInfo = agentInfo.getSocketInfo();
        synchronized (LOCK) {
            if (REGIST_SOCKET_INFO.contains(socketInfo)) {
                log.info("重复注册:{}", agentInfo);
                return;
            }
            REGIST_SOCKET_INFO.add(socketInfo);
        }
        String id = UUID.randomUUID().toString();
        agentInfo.setId(id);
        log.info("id:{}", id);
        AGENT_MAP.put(id, agentInfo);
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
                        .retryer(Retryer.NEVER_RETRY)
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

    @Override
    public void checkAgentInfo(AgentInfo agentInfo) throws Exception {
        if (REGIST_SOCKET_INFO.contains(agentInfo.getSocketInfo())) {
            return;
        }
        this.regist(agentInfo);
    }
}
