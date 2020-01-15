package org.nickle.nprofiler.perf.io;

import com.sun.tools.hat.internal.model.Snapshot;
import com.sun.tools.hat.internal.parser.PositionDataInputStream;
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
public class Reader {

    private PositionDataInputStream in;

    public Reader(PositionDataInputStream in) {
        this.in = in;
    }


    /**
     * Read a snapshot from a file.
     *
     * @param heapFile The name of a file containing a heap dump
     * @param callStack If true, read the call stack of allocaation sites
     */
    public static Snapshot readFile(String heapFile)throws IOException {
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
                log.error(msg);
                throw new NprofilerException(msg);
            }
            heapFile = heapFile.substring(0, pos);
        }
        PositionDataInputStream in = new PositionDataInputStream(
                new BufferedInputStream(new FileInputStream(heapFile)));
        try {
            int i = in.readInt();
            if (i == HprofReader.MAGIC_NUMBER) {
                HprofReader r = new HprofReader(heapFile, in, dumpNumber);
                return r.read();
            } else {
                throw new NprofilerException("Unrecognized magic number: " + i);
            }
        } finally {
            in.close();
        }
    }

}
