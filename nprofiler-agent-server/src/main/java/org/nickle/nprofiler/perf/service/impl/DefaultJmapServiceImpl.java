package org.nickle.nprofiler.perf.service.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.bean.JmapHeapInfo;
import org.nickle.nprofiler.exception.NprofilerException;
import org.nickle.nprofiler.perf.service.IJmapService;
import org.nickle.nprofiler.utils.NprofilerUtils;
import sun.jvm.hotspot.gc_implementation.g1.G1CollectedHeap;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSOldGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSYoungGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.ParallelScavengeHeap;
import sun.jvm.hotspot.gc_implementation.shared.MutableSpace;
import sun.jvm.hotspot.gc_interface.CollectedHeap;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.tools.Tool;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DefaultJmapServiceImpl  implements IJmapService {
    private static final double FACTOR = 1024 * 1024;

    @Override
    public JmapHeapInfo getProcessHeapSummary(int processId) throws Exception {
        HeapSummary heapSummary = new HeapSummary();
        heapSummary.exec(new String[]{String.valueOf(processId)});
        return heapSummary.getJmapHeapInfo();
    }

    @Data
    public static class HeapSummary extends Tool {
        private JmapHeapInfo jmapHeapInfo = new JmapHeapInfo();


        public void exec(String[] args) throws Exception {
            Method start = Tool.class.getDeclaredMethod("start", String[].class);
            start.setAccessible(true);
            start.invoke(this, new String[][]{args});
        }

        @Override
        public void run() {
            CollectedHeap heap = VM.getVM().getUniverse().heap();
            VM.Flag[] flags = VM.getVM().getCommandLineFlags();
            Map flagMap = new HashMap();
            if (flags == null) {
                log.error("WARNING: command line flags are not available");
            } else {
                for (int f = 0; f < flags.length; f++) {
                    flagMap.put(flags[f].getName(), flags[f]);
                }
            }
            populateGCAlgorithm(flagMap);
            populateHeapInfo(flagMap);
            populateHeapUsageInfo(heap);
        }

        // Helper methods
        private void populateHeapUsageInfo(CollectedHeap heap) {
            if (heap instanceof SharedHeap) {
                resolveSharedHeap(heap);
            } else if (heap instanceof ParallelScavengeHeap) {
                resolveParallelScavengeHeap(heap);
            } else {
                throw new NprofilerException("unknown CollectedHeap type : " + heap.getClass());
            }
            // printInternStringStatistics();
        }

        private void resolveParallelScavengeHeap(CollectedHeap heap) {
            ParallelScavengeHeap psh = (ParallelScavengeHeap) heap;
            JmapHeapInfo.GenInfo<JmapHeapInfo.GenInfo> psYoungGenInfo = new JmapHeapInfo.GenInfo();
            psYoungGenInfo.setName("PS Young Generation");
            PSYoungGen youngGen = psh.youngGen();
            populatePSYoungGen(psYoungGenInfo, youngGen);
            this.jmapHeapInfo.getHeapUsageInfo().getGenInfoList().add(psYoungGenInfo);

            JmapHeapInfo.GenInfo<JmapHeapInfo.GenInfoValue> psOldGenInfo = new JmapHeapInfo.GenInfo();
            psOldGenInfo.setName("PS Old Generation");
            populatePSOld(psh.oldGen(), psOldGenInfo);
            this.jmapHeapInfo.getHeapUsageInfo().getGenInfoList().add(psOldGenInfo);
        }

        private void resolveSharedHeap(CollectedHeap heap) {
            SharedHeap sharedHeap = (SharedHeap) heap;
            if (sharedHeap instanceof GenCollectedHeap) {
                GenCollectedHeap genHeap = (GenCollectedHeap) sharedHeap;
                resolveGenCollectedHeap(genHeap);
            } else if (sharedHeap instanceof G1CollectedHeap) {
                //G1CollectedHeap g1h = (G1CollectedHeap) sharedHeap;
                //resolveG1CollectedHeap(g1h);
                throw new NprofilerException("暂时不支持G1");
            } else {
                throw new NprofilerException("unknown SharedHeap type : " + heap.getClass());
            }
        }

        private void resolveGenCollectedHeap(GenCollectedHeap genHeap) {
            for (int n = 0; n < genHeap.nGens(); n++) {
                Generation gen = genHeap.getGen(n);
                if (gen instanceof sun.jvm.hotspot.memory.DefNewGeneration) {
                    JmapHeapInfo.GenInfo newGenerationGenInfo = new JmapHeapInfo.GenInfo();
                    newGenerationGenInfo.setName("New Generation (Eden + 1 Survivor Space)");
                    populateGen(gen, newGenerationGenInfo);
                    this.jmapHeapInfo.getHeapUsageInfo().getGenInfoList().add(newGenerationGenInfo);

                    JmapHeapInfo.GenInfo edenSpaceGenInfo = new JmapHeapInfo.GenInfo();
                    edenSpaceGenInfo.setName("Eden Space");
                    populateGen(((DefNewGeneration) gen).eden(), edenSpaceGenInfo);
                    this.jmapHeapInfo.getHeapUsageInfo().getGenInfoList().add(edenSpaceGenInfo);

                    JmapHeapInfo.GenInfo fromGenInfo = new JmapHeapInfo.GenInfo();
                    edenSpaceGenInfo.setName("From Space");
                    populateGen(((DefNewGeneration) gen).from(), fromGenInfo);
                    this.jmapHeapInfo.getHeapUsageInfo().getGenInfoList().add(fromGenInfo);

                    JmapHeapInfo.GenInfo toGenInfo = new JmapHeapInfo.GenInfo();
                    edenSpaceGenInfo.setName("From Space");
                    populateGen(((DefNewGeneration) gen).to(), toGenInfo);
                    this.jmapHeapInfo.getHeapUsageInfo().getGenInfoList().add(toGenInfo);

                } else {
                    JmapHeapInfo.GenInfo newGenerationGenInfo = new JmapHeapInfo.GenInfo();
                    newGenerationGenInfo.setName(gen.name());
                    populateGen(gen, newGenerationGenInfo);
                    this.jmapHeapInfo.getHeapUsageInfo().getGenInfoList().add(newGenerationGenInfo);
                }
            }
        }

//        private void resolveG1CollectedHeap(G1CollectedHeap g1h) {
//            G1MonitoringSupport g1mm = g1h.g1mm();
//            long edenRegionNum = g1mm.edenRegionNum();
//            long survivorRegionNum = g1mm.survivorRegionNum();
//            HeapRegionSetBase oldSet = g1h.oldSet();
//            HeapRegionSetBase humongousSet = g1h.humongousSet();
//            long oldRegionNum = oldSet.regionNum() + humongousSet.regionNum();
//            printG1Space("G1 Heap:", g1h.n_regions(),
//                    g1h.used(), g1h.capacity());
//            System.out.println("G1 Young Generation:");
//            printG1Space("Eden Space:", edenRegionNum,
//                    g1mm.edenUsed(), g1mm.edenCommitted());
//            printG1Space("Survivor Space:", survivorRegionNum,
//                    g1mm.survivorUsed(), g1mm.survivorCommitted());
//            printG1Space("G1 Old Generation:", oldRegionNum,
//                    g1mm.oldUsed(), g1mm.oldCommitted());
//        }

        private void populateHeapInfo(Map flagMap) {
            NprofilerUtils.mapToObj(flagMap, jmapHeapInfo, (value) -> {
                VM.Flag f = (VM.Flag) value;
                if (f != null) {
                    if (f.isBool()) {
                        return f.getBool() ? 1L : 0L;
                    } else {
                        return Long.parseLong(f.getValue());
                    }
                } else {
                    return -1;
                }
            });
        }

        private void populateGCAlgorithm(Map flagMap) {
            // print about new generation
            long l = getFlagValue("UseParNewGC", flagMap);
            if (l == 1L) {
                JmapHeapInfo.GcInfo gcInfo = new JmapHeapInfo.GcInfo();
                gcInfo.setDescription("using parallel threads in the new generation.");
                this.jmapHeapInfo.getGcInfoList().add(gcInfo);
            }

            l = getFlagValue("UseTLAB", flagMap);
            if (l == 1L) {
                JmapHeapInfo.GcInfo gcInfo = new JmapHeapInfo.GcInfo();
                gcInfo.setDescription("using thread-local object allocation.");
                this.jmapHeapInfo.getGcInfoList().add(gcInfo);
            }

            l = getFlagValue("UseConcMarkSweepGC", flagMap);
            if (l == 1L) {
                JmapHeapInfo.GcInfo gcInfo = new JmapHeapInfo.GcInfo();
                gcInfo.setDescription("Concurrent Mark-Sweep GC");
                this.jmapHeapInfo.getGcInfoList().add(gcInfo);
                return;
            }

            l = getFlagValue("UseParallelGC", flagMap);
            if (l == 1L) {
                l = getFlagValue("ParallelGCThreads", flagMap);
                JmapHeapInfo.GcInfo gcInfo = new JmapHeapInfo.GcInfo();
                gcInfo.setDescription(String.format("Parallel GC with %d  thread(s)", l));
                this.jmapHeapInfo.getGcInfoList().add(gcInfo);
                return;
            }

            l = getFlagValue("UseG1GC", flagMap);
            if (l == 1L) {
                l = getFlagValue("ParallelGCThreads", flagMap);
                JmapHeapInfo.GcInfo gcInfo = new JmapHeapInfo.GcInfo();
                gcInfo.setDescription(String.format("Garbage-First (G1) GC with %d thread(s)", l));
                this.jmapHeapInfo.getGcInfoList().add(gcInfo);
                return;
            }
            JmapHeapInfo.GcInfo gcInfo = new JmapHeapInfo.GcInfo();
            gcInfo.setDescription("Mark Sweep Compact GC");
            this.jmapHeapInfo.getGcInfoList().add(gcInfo);
        }

        private void populatePSYoungGen(JmapHeapInfo.GenInfo<JmapHeapInfo.GenInfo> psYoungGenInfo, PSYoungGen youngGen) {
            JmapHeapInfo.GenInfo<JmapHeapInfo.GenInfoValue> psYoungEdenInfo = new JmapHeapInfo.GenInfo();
            psYoungEdenInfo.setName("Eden Space");
            populateMutableSpace(youngGen.edenSpace(), psYoungEdenInfo);
            psYoungGenInfo.getGenInfoValueList().add(psYoungEdenInfo);


            JmapHeapInfo.GenInfo<JmapHeapInfo.GenInfoValue> psYoungEdenFromInfo = new JmapHeapInfo.GenInfo();
            psYoungEdenFromInfo.setName("From Space");
            populateMutableSpace(youngGen.fromSpace(), psYoungEdenFromInfo);
            psYoungGenInfo.getGenInfoValueList().add(psYoungEdenFromInfo);


            JmapHeapInfo.GenInfo<JmapHeapInfo.GenInfoValue> psYoungEdenToInfo = new JmapHeapInfo.GenInfo();
            psYoungEdenToInfo.setName("To Space");
            populateMutableSpace(youngGen.toSpace(), psYoungEdenToInfo);
            psYoungGenInfo.getGenInfoValueList().add(psYoungEdenToInfo);
        }

        private void populateMutableSpace(MutableSpace space, JmapHeapInfo.GenInfo<JmapHeapInfo.GenInfoValue> genInfo) {

            JmapHeapInfo.GenInfoValue capacity = new JmapHeapInfo.GenInfoValue();
            capacity.setName("capacity");
            populateGenInfoValue(capacity, space.capacity());
            genInfo.getGenInfoValueList().add(capacity);

            JmapHeapInfo.GenInfoValue used = new JmapHeapInfo.GenInfoValue();
            used.setName("used");
            populateGenInfoValue(used, space.used());
            genInfo.getGenInfoValueList().add(used);

            JmapHeapInfo.GenInfoValue free = new JmapHeapInfo.GenInfoValue();
            free.setName("free");
            populateGenInfoValue(free, space.capacity() - space.used());
            genInfo.getGenInfoValueList().add(free);

        }

        private void populatePSOld(PSOldGen oldGen, JmapHeapInfo.GenInfo genInfo) {
            JmapHeapInfo.GenInfoValue capacity = new JmapHeapInfo.GenInfoValue();
            capacity.setName("capacity");
            populateGenInfoValue(capacity, oldGen.capacity());
            genInfo.getGenInfoValueList().add(capacity);

            JmapHeapInfo.GenInfoValue used = new JmapHeapInfo.GenInfoValue();
            used.setName("used");
            populateGenInfoValue(used, oldGen.used());
            genInfo.getGenInfoValueList().add(used);

            JmapHeapInfo.GenInfoValue free = new JmapHeapInfo.GenInfoValue();
            free.setName("free");
            populateGenInfoValue(free, oldGen.capacity() - oldGen.used());
            genInfo.getGenInfoValueList().add(free);
        }


        private void populateGen(ContiguousSpace space, JmapHeapInfo.GenInfo genInfo) {
            JmapHeapInfo.GenInfoValue capacity = new JmapHeapInfo.GenInfoValue();
            capacity.setName("capacity");
            populateGenInfoValue(capacity, space.capacity());
            genInfo.getGenInfoValueList().add(capacity);

            JmapHeapInfo.GenInfoValue used = new JmapHeapInfo.GenInfoValue();
            used.setName("used");
            populateGenInfoValue(used, space.used());
            genInfo.getGenInfoValueList().add(used);

            JmapHeapInfo.GenInfoValue free = new JmapHeapInfo.GenInfoValue();
            free.setName("free");
            populateGenInfoValue(free, space.free());
            genInfo.getGenInfoValueList().add(free);
        }

        private void populateGen(Generation gen, JmapHeapInfo.GenInfo genInfo) {
            JmapHeapInfo.GenInfoValue capacity = new JmapHeapInfo.GenInfoValue();
            capacity.setName("capacity");
            populateGenInfoValue(capacity, gen.capacity());
            genInfo.getGenInfoValueList().add(capacity);

            JmapHeapInfo.GenInfoValue used = new JmapHeapInfo.GenInfoValue();
            used.setName("used");
            populateGenInfoValue(used, gen.used());
            genInfo.getGenInfoValueList().add(used);

            JmapHeapInfo.GenInfoValue free = new JmapHeapInfo.GenInfoValue();
            free.setName("free");
            populateGenInfoValue(free, gen.free());
            genInfo.getGenInfoValueList().add(free);
        }

//        private void printSpace(ContiguousSpace space) {
//            printValMB("capacity = ", space.capacity());
//            printValMB("used     = ", space.used());
//            printValMB("free     = ", space.free());
//            System.out.println(alignment + (double) space.used() * 100.0 / space.capacity() + "% used");
//        }
//
//        private void printG1Space(String spaceName, long regionNum,
//                                  long used, long capacity) {
//            long free = capacity - used;
//            System.out.println(spaceName);
//            printValue("regions  = ", regionNum);
//            printValMB("capacity = ", capacity);
//            printValMB("used     = ", used);
//            printValMB("free     = ", free);
//            double occPerc = (capacity > 0) ? (double) used * 100.0 / capacity : 0.0;
//            System.out.println(alignment + occPerc + "% used");
//        }


        private void populateGenInfoValue(JmapHeapInfo.GenInfoValue genInfoValue, long value) {
            if (value < 0) {
                genInfoValue.setValue((double) (value >>> 20));
            } else {
                double mb = value / FACTOR;
                genInfoValue.setValue(mb);
            }
        }

//        private void printValue(String title, long value) {
//            System.out.println(alignment + title + value);
//        }

        private long getFlagValue(String name, Map flagMap) {
            VM.Flag f = (VM.Flag) flagMap.get(name);
            if (f != null) {
                if (f.isBool()) {
                    return f.getBool() ? 1L : 0L;
                } else {
                    return Long.parseLong(f.getValue());
                }
            } else {
                return -1;
            }
        }

//        private void printInternStringStatistics() {
//            class StringStat implements StringTable.StringVisitor {
//                private int count;
//                private long size;
//                private OopField stringValueField;
//
//                StringStat() {
//                    VM vm = VM.getVM();
//                    SystemDictionary sysDict = vm.getSystemDictionary();
//                    InstanceKlass strKlass = sysDict.getStringKlass();
//                    // String has a field named 'value' of type 'char[]'.
//                    stringValueField = (OopField) strKlass.findField("value", "[C");
//                }
//
//                private long stringSize(Instance instance) {
//                    // We include String content in size calculation.
//                    return instance.getObjectSize() +
//                            stringValueField.getValue(instance).getObjectSize();
//                }
//
//                public void visit(Instance str) {
//                    count++;
//                    size += stringSize(str);
//                }
//
//                public void print() {
//                    System.out.println(count +
//                            " interned Strings occupying " + size + " bytes.");
//                }
//            }
//
//            StringStat stat = new StringStat();
//            StringTable strTable = VM.getVM().getStringTable();
//            strTable.stringsDo(stat);
//            stat.print();
//        }
    }

}
