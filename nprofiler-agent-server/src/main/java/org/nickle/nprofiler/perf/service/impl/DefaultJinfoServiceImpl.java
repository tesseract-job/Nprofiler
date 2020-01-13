package org.nickle.nprofiler.perf.service.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.JinfoConfiguration;
import org.nickle.nprofiler.perf.service.IJinfoService;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.tools.Tool;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class DefaultJinfoServiceImpl implements IJinfoService {

    @Override
    public JinfoConfiguration getInfoConfiguration(int processId) throws Exception {
        InfoSummary infoSummary = new InfoSummary();
        infoSummary.exec(new String[]{String.valueOf(processId)});
        return infoSummary.getJinfoConfiguration();
    }

    @Data
    public static class InfoSummary extends Tool{

        private JinfoConfiguration jinfoConfiguration = new JinfoConfiguration();

        public void exec(String[] args) throws Exception{
            Method start = Tool.class.getDeclaredMethod("start", String[].class);
            start.setAccessible(true);
            start.invoke(this, new String[][]{args});
        }

        @Override
        public void run() {
            Properties sysProps = VM.getVM().getSystemProperties();
            HashMap sysPropMap = new HashMap();
            if (sysProps == null){
                log.error("WARNING: command line flags are not available");
            }else {
                Enumeration keys = sysProps.keys();
                while (keys.hasMoreElements()) {
                    Object key = keys.nextElement();
                    Object value = sysProps.get(key);
                    sysPropMap.put(key, value);
                }
            }

            VM.Flag[] flags = VM.getVM().getCommandLineFlags();
            Map flagMap = new HashMap();
            if (flags == null) {
                log.error("WARNING: command line flags are not available");
            } else {
                for (int index = 0; index < flags.length; index++) {
                    VM.Flag flag = flags[index];
                    flagMap.put(flag.getName(), flag.getValue());
                }
            }
            jinfoConfiguration.setSysPropMap(sysPropMap);
            jinfoConfiguration.setFlagMap(flagMap);

        }




    }
}
