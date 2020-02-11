package org.nickle.nprofiler.perf.hat.io.read;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author wesley
 * @create 2020-01-14
 */
@Slf4j
class MappedReadBuffer implements ReadBuffer {
    private MappedByteBuffer buf;

    MappedReadBuffer(MappedByteBuffer buf) {
        this.buf = buf;
    }

    static ReadBuffer create(RandomAccessFile file) throws IOException {
        FileChannel ch = file.getChannel();
        long size = ch.size();
        if (canUseFileMap() && (size <= Integer.MAX_VALUE)) {
            MappedByteBuffer buf;
            try {
                buf = ch.map(FileChannel.MapMode.READ_ONLY, 0, size);
                ch.close();
                return new MappedReadBuffer(buf);
            } catch (IOException exp) {
               log.error("File mapping failed, will use direct read");
            }
        }
        return new FileReadBuffer(file);
    }

    private static boolean canUseFileMap() {
        // set jhat.disableFileMap to any value other than "false"
        // to disable file mapping
        String prop = System.getProperty("jhat.disableFileMap");
        return prop == null || prop.equals("false");
    }

    private void seek(long pos) throws IOException {
        assert pos <= Integer.MAX_VALUE :  "position overflow";
        buf.position((int)pos);
    }

    @Override
    public synchronized void get(long pos, byte[] res) throws IOException {
        seek(pos);
        buf.get(res);
    }

    @Override
    public synchronized char getChar(long pos) throws IOException {
        seek(pos);
        return buf.getChar();
    }

    @Override
    public synchronized byte getByte(long pos) throws IOException {
        seek(pos);
        return buf.get();
    }

    @Override
    public synchronized short getShort(long pos) throws IOException {
        seek(pos);
        return buf.getShort();
    }

    @Override
    public synchronized int getInt(long pos) throws IOException {
        seek(pos);
        return buf.getInt();
    }

    @Override
    public synchronized long getLong(long pos) throws IOException {
        seek(pos);
        return buf.getLong();
    }
}

