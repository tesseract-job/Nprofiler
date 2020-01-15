package org.nickle.nprofiler.perf.io;

import com.sun.tools.hat.internal.parser.ReadBuffer;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author wesley
 * @create 2020-01-14
 */
class FileReadBuffer implements ReadBuffer {

    private RandomAccessFile file;

    FileReadBuffer(RandomAccessFile file) {
        this.file = file;
    }

    private void seek(long pos) throws IOException {
        file.getChannel().position(pos);
    }

    @Override
    public synchronized void get(long pos, byte[] buf) throws IOException {
        seek(pos);
        file.read(buf);
    }

    @Override
    public synchronized char getChar(long pos) throws IOException {
        seek(pos);
        return file.readChar();
    }

    @Override
    public synchronized byte getByte(long pos) throws IOException {
        seek(pos);
        return (byte) file.read();
    }

    @Override
    public synchronized short getShort(long pos) throws IOException {
        seek(pos);
        return file.readShort();
    }

    @Override
    public synchronized int getInt(long pos) throws IOException {
        seek(pos);
        return file.readInt();
    }

    @Override
    public synchronized long getLong(long pos) throws IOException {
        seek(pos);
        return file.readLong();
    }
}
