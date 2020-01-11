package org.nickle.nprofiler.perf.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.common.AgentServerException;
import org.nickle.nprofiler.common.NprofilerUtils;
import org.nickle.nprofiler.perf.bean.JmapHeapInfo;
import org.nickle.nprofiler.perf.service.IJmapService;
import sun.jvm.hotspot.debugger.JVMDebugger;
import sun.jvm.hotspot.gc_implementation.g1.G1CollectedHeap;
import sun.jvm.hotspot.gc_implementation.g1.G1MonitoringSupport;
import sun.jvm.hotspot.gc_implementation.g1.HeapRegion;
import sun.jvm.hotspot.gc_implementation.g1.HeapRegionSetBase;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSOldGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSYoungGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.ParallelScavengeHeap;
import sun.jvm.hotspot.gc_implementation.shared.MutableSpace;
import sun.jvm.hotspot.gc_interface.CollectedHeap;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.oops.InstanceKlass;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.tools.Tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DefaultJmapServiceImpl implements IJmapService {
    @Override
    public JmapHeapInfo getProcessHeapSummary(int processId) throws Exception {
        return null;
    }

    public class HeapSummary extends Tool {
        private JmapHeapInfo jmapHeapInfo = new JmapHeapInfo();

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


        }

        // Helper methods
        private void populateHeapUsageInfo(CollectedHeap heap) {
            if (heap instanceof SharedHeap) {
                resolveSharedHeap(heap);
            } else if (heap instanceof ParallelScavengeHeap) {
                resolveParallelScavengeHeap(heap);
            } else {
                throw new AgentServerException("unknown CollectedHeap type : " + heap.getClass());
            }
            printInternStringStatistics();
        }

        private void resolveParallelScavengeHeap(CollectedHeap heap) {
            ParallelScavengeHeap psh = (ParallelScavengeHeap) heap;
            PSYoungGen youngGen = psh.youngGen();
            printPSYoungGen(youngGen);
            PSOldGen oldGen = psh.oldGen();
            long oldFree = oldGen.capacity() - oldGen.used();
            System.out.println("PS Old Generation");
            printValMB("capacity = ", oldGen.capacity());
            printValMB("used     = ", oldGen.used());
            printValMB("free     = ", oldFree);
            System.out.println(alignment + (double) oldGen.used() * 100.0 / oldGen.capacity() + "% used");
        }

        private void resolveSharedHeap(CollectedHeap heap) {
            SharedHeap sharedHeap = (SharedHeap) heap;
            if (sharedHeap instanceof GenCollectedHeap) {
                GenCollectedHeap genHeap = (GenCollectedHeap) sharedHeap;
                for (int n = 0; n < genHeap.nGens(); n++) {
                    Generation gen = genHeap.getGen(n);
                    if (gen instanceof sun.jvm.hotspot.memory.DefNewGeneration) {
                        System.out.println("New Generation (Eden + 1 Survivor Space):");
                        printGen(gen);

                        ContiguousSpace eden = ((DefNewGeneration) gen).eden();
                        System.out.println("Eden Space:");
                        printSpace(eden);

                        ContiguousSpace from = ((DefNewGeneration) gen).from();
                        System.out.println("From Space:");
                        printSpace(from);

                        ContiguousSpace to = ((DefNewGeneration) gen).to();
                        System.out.println("To Space:");
                        printSpace(to);
                    } else {
                        System.out.println(gen.name() + ":");
                        printGen(gen);
                    }
                }
            } else if (sharedHeap instanceof G1CollectedHeap) {
                G1CollectedHeap g1h = (G1CollectedHeap) sharedHeap;
                G1MonitoringSupport g1mm = g1h.g1mm();
                long edenRegionNum = g1mm.edenRegionNum();
                long survivorRegionNum = g1mm.survivorRegionNum();
                HeapRegionSetBase oldSet = g1h.oldSet();
                HeapRegionSetBase humongousSet = g1h.humongousSet();
                long oldRegionNum = oldSet.regionNum() + humongousSet.regionNum();
                printG1Space("G1 Heap:", g1h.n_regions(),
                        g1h.used(), g1h.capacity());
                System.out.println("G1 Young Generation:");
                printG1Space("Eden Space:", edenRegionNum,
                        g1mm.edenUsed(), g1mm.edenCommitted());
                printG1Space("Survivor Space:", survivorRegionNum,
                        g1mm.survivorUsed(), g1mm.survivorCommitted());
                printG1Space("G1 Old Generation:", oldRegionNum,
                        g1mm.oldUsed(), g1mm.oldCommitted());
            } else {
                throw new RuntimeException("unknown SharedHeap type : " + heap.getClass());
            }
        }

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

        private void printPSYoungGen(PSYoungGen youngGen) {
            System.out.println("PS Young Generation");
            MutableSpace eden = youngGen.edenSpace();
            System.out.println("Eden Space:");
            printMutableSpace(eden);
            MutableSpace from = youngGen.fromSpace();
            System.out.println("From Space:");
            printMutableSpace(from);
            MutableSpace to = youngGen.toSpace();
            System.out.println("To Space:");
            printMutableSpace(to);
        }

        private void printMutableSpace(MutableSpace space) {
            printValMB("capacity = ", space.capacity());
            printValMB("used     = ", space.used());
            long free = space.capacity() - space.used();
            printValMB("free     = ", free);
            System.out.println(alignment + (double) space.used() * 100.0 / space.capacity() + "% used");
        }

        private static String alignment = "   ";

        private void printGen(Generation gen) {
            printValMB("capacity = ", gen.capacity());
            printValMB("used     = ", gen.used());
            printValMB("free     = ", gen.free());
            System.out.println(alignment + (double) gen.used() * 100.0 / gen.capacity() + "% used");
        }

        private void printSpace(ContiguousSpace space) {
            printValMB("capacity = ", space.capacity());
            printValMB("used     = ", space.used());
            printValMB("free     = ", space.free());
            System.out.println(alignment + (double) space.used() * 100.0 / space.capacity() + "% used");
        }

        private void printG1Space(String spaceName, long regionNum,
                                  long used, long capacity) {
            long free = capacity - used;
            System.out.println(spaceName);
            printValue("regions  = ", regionNum);
            printValMB("capacity = ", capacity);
            printValMB("used     = ", used);
            printValMB("free     = ", free);
            double occPerc = (capacity > 0) ? (double) used * 100.0 / capacity : 0.0;
            System.out.println(alignment + occPerc + "% used");
        }

        private static final double FACTOR = 1024 * 1024;

        private void printValMB(String title, long value) {
            if (value < 0) {
                System.out.println(alignment + title + (value >>> 20) + " MB");
            } else {
                double mb = value / FACTOR;
                System.out.println(alignment + title + value + " (" + mb + "MB)");
            }
        }

        private void printValue(String title, long value) {
            System.out.println(alignment + title + value);
        }

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

        private void printInternStringStatistics() {
            class StringStat implements StringTable.StringVisitor {
                private int count;
                private long size;
                private OopField stringValueField;

                StringStat() {
                    VM vm = VM.getVM();
                    SystemDictionary sysDict = vm.getSystemDictionary();
                    InstanceKlass strKlass = sysDict.getStringKlass();
                    // String has a field named 'value' of type 'char[]'.
                    stringValueField = (OopField) strKlass.findField("value", "[C");
                }

                private long stringSize(Instance instance) {
                    // We include String content in size calculation.
                    return instance.getObjectSize() +
                            stringValueField.getValue(instance).getObjectSize();
                }

                public void visit(Instance str) {
                    count++;
                    size += stringSize(str);
                }

                public void print() {
                    System.out.println(count +
                            " interned Strings occupying " + size + " bytes.");
                }
            }

            StringStat stat = new StringStat();
            StringTable strTable = VM.getVM().getStringTable();
            strTable.stringsDo(stat);
            stat.print();
        }
    }

}
