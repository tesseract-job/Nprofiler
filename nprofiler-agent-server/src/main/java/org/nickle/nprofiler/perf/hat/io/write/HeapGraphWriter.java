package org.nickle.nprofiler.perf.hat.io.write;

import java.io.IOException;

/**
 * Interface implemented by all heap graph writers.
 */
public interface HeapGraphWriter {

    /** writes the heap graph in the given file */
    public void write(String fileName) throws IOException;

}
