package org.nickle.nprofiler.perf.service.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.perf.bean.JpsProcessInfo;
import org.nickle.nprofiler.perf.service.IJavaProcessService;
import sun.jvmstat.monitor.*;
import sun.tools.jps.Arguments;

import java.util.List;
import java.util.Set;

@Slf4j
public class DefaultJavaProcessServiceImpl implements IJavaProcessService {
    private final String[] DEFAULT_CMD = {"-l"};

    @Override
    public List<JpsProcessInfo> getAllJavaProcess() throws Exception {
        List<JpsProcessInfo> processInfoList = Lists.newArrayList();
        Arguments arguments = new Arguments(DEFAULT_CMD);
        HostIdentifier hostId = arguments.hostId();
        MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost(hostId);
        Set<Integer> jvms = monitoredHost.activeVms();

        for (Integer jvm : jvms) {
            JpsProcessInfo jpsProcessInfo = new JpsProcessInfo();
            int lvmid = jvm;
            jpsProcessInfo.setProcessId(lvmid);
            if (arguments.isQuiet()) {
                processInfoList.add(jpsProcessInfo);
                continue;
            }

            MonitoredVm vm;
            String vmidString = "//" + lvmid + "?mode=r";

            String errorString = null;

            try {
                errorString = " -- process information unavailable";
                VmIdentifier id = new VmIdentifier(vmidString);
                vm = monitoredHost.getMonitoredVm(id, 0);

                errorString = " -- main class information unavailable";
                String mainClass = MonitoredVmUtil.mainClass(vm, arguments.showLongPaths());
                jpsProcessInfo.setMainClass(mainClass);

                errorString = " -- detach failed";
                monitoredHost.detach(vm);

                errorString = null;
            } catch (Exception e) {
                log.error(e.toString());
            } finally {
                if (errorString != null) {
                    jpsProcessInfo.setDescription(errorString);
                }
                processInfoList.add(jpsProcessInfo);
            }
        }
        return processInfoList;
    }
}
