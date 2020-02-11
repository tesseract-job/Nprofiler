package org.nickle.nprofiler.perf.hat.io.read;

import org.nickle.nprofiler.perf.hat.model.Snapshot;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.exception.NprofilerException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author wesley
 * @create 2020-01-14
 */
@Slf4j
public abstract class Reader {

    protected PositionDataInputStream in;

    protected Reader(PositionDataInputStream in) {
        this.in = in;
    }

    abstract public Snapshot read() throws IOException;

    public static Snapshot readFile(String heapFile, boolean callStack,
                                    int debugLevel,boolean calculateRefs) throws IOException {
        Snapshot snapshot = readFile(heapFile, callStack, debugLevel);
        snapshot.resolve(calculateRefs);
        return snapshot;
    }

    private static Snapshot readFile(String heapFile, boolean callStack,
                                    int debugLevel)
            throws IOException {
        int dumpNumber = 1;
        int pos = heapFile.lastIndexOf('#');
        if (pos > -1) {
            String num = heapFile.substring(pos+1, heapFile.length());
            try {
                dumpNumber = Integer.parseInt(num, 10);
            } catch (java.lang.NumberFormatException ex) {
                String msg = "In file name \"" + heapFile
                        + "\", a dump number was "
                        + "expected after the :, but \""
                        + num + "\" was found instead.";
                throw new NprofilerException(msg);
            }
            heapFile = heapFile.substring(0, pos);
        }
        PositionDataInputStream in = new PositionDataInputStream(
                new BufferedInputStream(new FileInputStream(heapFile)));
        try {
            int i = in.readInt();
            if (i == HprofReader.MAGIC_NUMBER) {
                Reader r
                        = new HprofReader(heapFile, in, dumpNumber,
                        callStack, debugLevel);
                return r.read();
            } else {
                throw new NprofilerException("Unrecognized magic number: " + i);
            }
        } finally {
            in.close();
        }
    }
}

