package org.nickle.nprofiler.perf.validator;

import org.nickle.nprofiler.bean.JpsProcessInfo;
import org.springframework.util.StringUtils;

public class DefaultJpsProcessInfoValidator implements IJVMInfoValiator<JpsProcessInfo> {
    @Override
    public boolean validate(JpsProcessInfo processInfo) {
        String mainClass = processInfo.getMainClass();
        if (StringUtils.isEmpty(mainClass) || "".equals((mainClass = mainClass.trim()))) {
            return false;
        }
        if ("org.nickle.nprofiler.spring.AgentServerApplication".equals(mainClass) ||
                "org.nickle.nprofiler.ManageServerApplication".equals(mainClass)) {
            return false;
        }
        return true;
    }
}
