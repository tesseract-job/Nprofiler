package org.nickle.nprofiler.perf.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.JstatGCInfo;
import org.nickle.nprofiler.exception.NprofilerException;
import org.nickle.nprofiler.perf.service.IJstatService;
import sun.jvmstat.monitor.*;
import sun.jvmstat.monitor.event.HostEvent;
import sun.jvmstat.monitor.event.HostListener;
import sun.jvmstat.monitor.event.VmStatusChangeEvent;
import sun.tools.jstat.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author wesley
 * @create 2020-01-12
 */
@Slf4j
public class DefaultJstatServiceImpl implements IJstatService {

    @Override
    public JstatGCInfo getGCSummary(int processId) throws Exception {
        JstatGCInfo gcInfo = run(new String[]{"-gc", String.valueOf(processId)});
        if (gcInfo == null){
            throw new NprofilerException("JstatGCInfo获取为空");
        }else {
            try {
                return gcInfo;
            } catch (ClassFormatError e) {
                log.error(e.toString());
                throw new NprofilerException("JstatGCInfo获取为空");
            }
        }
    }

    private JstatGCInfo run(String[] args) throws MonitorException {
        Arguments arguments = new Arguments(args);
        final VmIdentifier vmId = arguments.vmId();
        int interval = arguments.sampleInterval();
        final MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost(vmId);
        MonitoredVm monitoredVm = monitoredHost.getMonitoredVm(vmId, interval);
        final JstatTool jstatTool = new JstatTool(monitoredVm);
        OutputFormatter formatter = null;
        if (arguments.isSpecialOption()) {
            OptionFormat format = arguments.optionFormat();
            formatter = new OptionOutputFormatter(monitoredVm, format);
        } else {
            List<Monitor> logged = monitoredVm.findByPattern(arguments.counterNames());
            Collections.sort(logged, arguments.comparator());
            List<Monitor> constants = new ArrayList<Monitor>();
            for (Iterator i = logged.iterator(); i.hasNext();) {
                Monitor m = (Monitor)i.next();
                if (!(m.isSupported() || arguments.showUnsupported())) {
                    i.remove();
                    continue;
                }
                if (m.getVariability() == Variability.CONSTANT) {
                    i.remove();
                    if (arguments.printConstants()) constants.add(m);
                } else if ((m.getUnits() == Units.STRING)
                        && !arguments.printStrings()) {
                    i.remove();
                }
            }
            if (logged.isEmpty()) {
                monitoredHost.detach(monitoredVm);
                return null;
            }

            formatter = new RawOutputFormatter(logged,
                    arguments.printStrings());
        }

        // handle user termination requests by stopping sampling loops
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                jstatTool.stopLogging();
            }
        });
        // handle target termination events for targets other than ourself
        HostListener terminator = new HostListener() {
            @Override
            public void vmStatusChanged(VmStatusChangeEvent ev) {
                Integer lvmid = new Integer(vmId.getLocalVmId());
                if (ev.getTerminated().contains(lvmid)) {
                    jstatTool.stopLogging();
                } else if (!ev.getActive().contains(lvmid)) {
                    jstatTool.stopLogging();
                }
            }
            @Override
            public void disconnected(HostEvent ev) {
                if (monitoredHost == ev.getMonitoredHost()) {
                    jstatTool.stopLogging();
                }
            }
        };
        ArrayList<JstatGCInfo> resList = new ArrayList<>(arguments.sampleCount());
        jstatTool.logSamples(formatter,
                arguments.headerRate(),
                arguments.sampleInterval(),
                arguments.sampleCount(),
                vmId.getLocalVmId(),
                resList);
        if (vmId.getLocalVmId() != 0) {
            monitoredHost.addHostListener(terminator);
        }
        monitoredHost.detach(monitoredVm);
        return resList.get(0);
    }

    @Slf4j
    public static class JstatTool {

        private MonitoredVm monitoredVm;
        private volatile boolean active = true;

        public JstatTool(MonitoredVm monitoredVm) {
            this.monitoredVm = monitoredVm;
        }

        /**
         * method to for asynchronous termination of sampling loops
         */
        public void stopLogging() {
            active = false;
        }

        /**
         * print samples according to the given format.
         */
        public void logSamples(OutputFormatter formatter, int headerRate,
                               int sampleInterval, int sampleCount,int vmId,ArrayList<JstatGCInfo> resList)
                throws MonitorException {
            long iterationCount = 0;
            int printHeaderCount = 0;
            // if printHeader == 0, then only an initial column header is desired.
            int printHeader = headerRate;
            if (printHeader == 0) {
                // print the column header once, disable future printing
                printHeader = -1;
            }
            while (active) {
                // check if it's time to print another column header
                if (printHeader > 0 && --printHeaderCount <= 0) {
                    printHeaderCount = printHeader;
                }
                JstatGCInfo jStatResult = new JstatGCInfo(formatter.getRow());
                jStatResult.setVmId(vmId);
                resList.add(jStatResult);
                // check if we've hit the specified sample count
                if (sampleCount > 0 && ++iterationCount >= sampleCount) {
                    break;
                }

                try { Thread.sleep(sampleInterval); } catch (Exception e) { log.error(e.getMessage());}
            }
        }

    }



}
