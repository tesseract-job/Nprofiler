package org.nickle.nprofiler.perf.hat.io;

import com.sun.tools.hat.internal.model.*;
import com.sun.tools.hat.internal.parser.PositionDataInputStream;
import lombok.extern.slf4j.Slf4j;
import org.nickle.nprofiler.exception.NprofilerException;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

;

/**
 * 读取.hprof文件
 * @author wesley
 * @create 2020-01-14
 */
@Slf4j
public class HprofReader extends Reader implements ArrayTypeCodes {

    final static int MAGIC_NUMBER = 0x4a415641;
    private final static String[] VERSIONS = {
            " PROFILE 1.0\0",
            " PROFILE 1.0.1\0",
            " PROFILE 1.0.2\0",
    };
    private static final int VERSION_JDK12BETA3 = 0;
    private static final int VERSION_JDK12BETA4 = 1;
    private static final int VERSION_JDK6       = 2;
    static final int HPROF_UTF8          = 0x01;
    static final int HPROF_LOAD_CLASS    = 0x02;
    static final int HPROF_UNLOAD_CLASS  = 0x03;
    static final int HPROF_FRAME         = 0x04;
    static final int HPROF_TRACE         = 0x05;
    static final int HPROF_ALLOC_SITES   = 0x06;
    static final int HPROF_HEAP_SUMMARY  = 0x07;
    static final int HPROF_START_THREAD  = 0x0a;
    static final int HPROF_END_THREAD    = 0x0b;
    static final int HPROF_HEAP_DUMP     = 0x0c;
    static final int HPROF_CPU_SAMPLES   = 0x0d;
    static final int HPROF_CONTROL_SETTINGS = 0x0e;
    static final int HPROF_LOCKSTATS_WAIT_TIME = 0x10;
    static final int HPROF_LOCKSTATS_HOLD_TIME = 0x11;
    static final int HPROF_GC_ROOT_UNKNOWN       = 0xff;
    static final int HPROF_GC_ROOT_JNI_GLOBAL    = 0x01;
    static final int HPROF_GC_ROOT_JNI_LOCAL     = 0x02;
    static final int HPROF_GC_ROOT_JAVA_FRAME    = 0x03;
    static final int HPROF_GC_ROOT_NATIVE_STACK  = 0x04;
    static final int HPROF_GC_ROOT_STICKY_CLASS  = 0x05;
    static final int HPROF_GC_ROOT_THREAD_BLOCK  = 0x06;
    static final int HPROF_GC_ROOT_MONITOR_USED  = 0x07;
    static final int HPROF_GC_ROOT_THREAD_OBJ    = 0x08;
    static final int HPROF_GC_CLASS_DUMP         = 0x20;
    static final int HPROF_GC_INSTANCE_DUMP      = 0x21;
    static final int HPROF_GC_OBJ_ARRAY_DUMP         = 0x22;
    static final int HPROF_GC_PRIM_ARRAY_DUMP         = 0x23;
    static final int HPROF_HEAP_DUMP_SEGMENT     = 0x1c;
    static final int HPROF_HEAP_DUMP_END         = 0x2c;
    private static final int T_CLASS = 2;
    private int version;
    private int debugLevel;
    private long currPos;
    private int dumpsToSkip;
    private boolean callStack;
    private int identifierSize;
    private ConcurrentHashMap<Long, String> names;
    private ConcurrentHashMap<Integer, ThreadObject> threadObjects;
    private ConcurrentHashMap<Long, String> classNameFromObjectID;
    private ConcurrentHashMap<Integer, String> classNameFromSerialNo;
    private ConcurrentHashMap<Long, StackFrame> stackFrames;
    private ConcurrentHashMap<Integer, StackTrace> stackTraces;
    private Snapshot snapshot;
    public HprofReader(String fileName, PositionDataInputStream in,
                       int dumpNumber, boolean callStack, int debugLevel)
            throws IOException {
        super(in);
        RandomAccessFile file = new RandomAccessFile(fileName, "r");
        this.snapshot = new Snapshot(MappedReadBuffer.create(file));
        this.dumpsToSkip = dumpNumber - 1;
        this.callStack = callStack;
        this.debugLevel = debugLevel;
        names = new ConcurrentHashMap<>();
        threadObjects = new ConcurrentHashMap<>(43);
        classNameFromObjectID = new ConcurrentHashMap<>();
        if (callStack) {
            stackFrames = new ConcurrentHashMap<>(43);
            stackTraces = new ConcurrentHashMap<>(43);
            classNameFromSerialNo = new ConcurrentHashMap<>();
        }
    }

    public Snapshot read() throws IOException {
        currPos = 4;
        version = readVersionHeader();
        identifierSize = in.readInt();
        snapshot.setIdentifierSize(identifierSize);
        if (version >= VERSION_JDK12BETA4) {
            snapshot.setNewStyleArrayClass(true);
        } else {
            snapshot.setNewStyleArrayClass(false);
        }

        currPos += 4;
        if (identifierSize != 4 && identifierSize != 8) {
            throw new NprofilerException("I'm sorry, but I can't deal with an identifier size of " + identifierSize + ".  I can only deal with 4 or 8.");
        }
        log.info("Dump file created " + (new Date(in.readLong())));
        currPos += 8;
        for (;;) {
            int type;
            try {
                type = in.readUnsignedByte();
            } catch (EOFException ignored) {
                break;
            }
            in.readInt();     
            long length = in.readInt() & 0xffffffffL;
            if (debugLevel > 0) {
                log.info("Read record type " + type
                        + ", length " + length
                        + " at position " + toHex(currPos));
            }
            if (length < 0) {
                throw new IOException("Bad record length of " + length
                        + " at byte " + toHex(currPos+5)
                        + " of file.");
            }
            currPos += 9 + length;
            switch (type) {
                case HPROF_UTF8: {
                    long id = readID();
                    byte[] chars = new byte[(int)length - identifierSize];
                    in.readFully(chars);
                    names.put(new Long(id), new String(chars));
                    break;
                }
                case HPROF_LOAD_CLASS: {
                    int serialNo = in.readInt();     
                    long classID = readID();
                    int stackTraceSerialNo = in.readInt();
                    long classNameID = readID();
                    Long classIdI = new Long(classID);
                    String nm = getNameFromID(classNameID).replace('/', '.');
                    classNameFromObjectID.put(classIdI, nm);
                    if (classNameFromSerialNo != null) {
                        classNameFromSerialNo.put(new Integer(serialNo), nm);
                    }
                    break;
                }

                case HPROF_HEAP_DUMP: {
                    if (dumpsToSkip <= 0) {
                        try {
                            readHeapDump(length, currPos);
                        } catch (EOFException exp) {
                            handleEOF(exp, snapshot);
                        }
                        if (debugLevel > 0) {
                            log.info("    Finished processing instances in heap dump.");
                        }
                        return snapshot;
                    } else {
                        dumpsToSkip--;
                        skipBytes(length);
                    }
                    break;
                }

                case HPROF_HEAP_DUMP_END: {
                    if (version >= VERSION_JDK6) {
                        if (dumpsToSkip <= 0) {
                            skipBytes(length);  
                            return snapshot;
                        } else {
                            dumpsToSkip--;
                        }
                    } else {
                        log.warn("Ignoring unrecognized record type " + type);
                    }
                    skipBytes(length);
                    break;
                }

                case HPROF_HEAP_DUMP_SEGMENT: {
                    if (version >= VERSION_JDK6) {
                        if (dumpsToSkip <= 0) {
                            try {
                                readHeapDump(length, currPos);
                            } catch (EOFException exp) {
                                handleEOF(exp, snapshot);
                            }
                        } else {
                            skipBytes(length);
                        }
                    } else {
                        log.warn("Ignoring unrecognized record type " + type);
                        skipBytes(length);
                    }
                    break;
                }

                case HPROF_FRAME: {
                    if (stackFrames == null) {
                        skipBytes(length);
                    } else {
                        long id = readID();
                        String methodName = getNameFromID(readID());
                        String methodSig = getNameFromID(readID());
                        String sourceFile = getNameFromID(readID());
                        int classSer = in.readInt();
                        String className = classNameFromSerialNo.get(new Integer(classSer));
                        int lineNumber = in.readInt();
                        if (lineNumber < StackFrame.LINE_NUMBER_NATIVE) {
                            log.warn("Weird stack frame line number:  " + lineNumber);
                            lineNumber = StackFrame.LINE_NUMBER_UNKNOWN;
                        }
                        stackFrames.put(new Long(id),
                                new StackFrame(methodName, methodSig,
                                        className, sourceFile,
                                        lineNumber));
                    }
                    break;
                }
                case HPROF_TRACE: {
                    if (stackTraces == null) {
                        skipBytes(length);
                    } else {
                        int serialNo = in.readInt();
                        int threadSeq = in.readInt();
                        StackFrame[] frames = new StackFrame[in.readInt()];
                        for (int i = 0; i < frames.length; i++) {
                            long fid = readID();
                            frames[i] = stackFrames.get(new Long(fid));
                            if (frames[i] == null) {
                                throw new NprofilerException("Stack frame " + toHex(fid) + " not found");
                            }
                        }
                        stackTraces.put(new Integer(serialNo),
                                new StackTrace(frames));
                    }
                    break;
                }
                case HPROF_UNLOAD_CLASS:
                case HPROF_ALLOC_SITES:
                case HPROF_START_THREAD:
                case HPROF_END_THREAD:
                case HPROF_HEAP_SUMMARY:
                case HPROF_CPU_SAMPLES:
                case HPROF_CONTROL_SETTINGS:
                case HPROF_LOCKSTATS_WAIT_TIME:
                case HPROF_LOCKSTATS_HOLD_TIME:
                {
                    // Ignore these record types
                    skipBytes(length);
                    break;
                }
                default: {
                    skipBytes(length);
                    log.warn("Ignoring unrecognized record type " + type);
                }
            }
        }

        return snapshot;
    }

    private void skipBytes(long length) throws IOException {
        while (length > 0) {
            long skipped = in.skip(length);
            length -= skipped;
            if (skipped == 0) {
                // EOF or other problem, throw exception
                throw new NprofilerException("Couldn't skip enough bytes");
            }
        }
    }

    private int readVersionHeader() throws IOException {
        int candidatesLeft = VERSIONS.length;
        boolean[] matched = new boolean[VERSIONS.length];
        for (int i = 0; i < candidatesLeft; i++) {
            matched[i] = true;
        }

        int pos = 0;
        while (candidatesLeft > 0) {
            char c = (char) in.readByte();
            currPos++;
            for (int i = 0; i < VERSIONS.length; i++) {
                if (matched[i]) {
                    if (c != VERSIONS[i].charAt(pos)) {
                        matched[i] = false;
                        --candidatesLeft;
                    } else if (pos == VERSIONS[i].length() - 1) {
                        return i;
                    }
                }
            }
            ++pos;
        }
        throw new NprofilerException("Version string not recognized at byte " + (pos+3));
    }

    private void readHeapDump(long bytesLeft, long posAtEnd) throws IOException {
        while (bytesLeft > 0) {
            int type = in.readUnsignedByte();
            if (debugLevel > 0) {
                log.info("    Read heap sub-record type " + type
                        + " at position "
                        + toHex(posAtEnd - bytesLeft));
            }
            bytesLeft--;
            switch(type) {
                case HPROF_GC_ROOT_UNKNOWN: {
                    long id = readID();
                    bytesLeft -= identifierSize;
                    snapshot.addRoot(new Root(id, 0, Root.UNKNOWN, ""));
                    break;
                }
                case HPROF_GC_ROOT_THREAD_OBJ: {
                    long id = readID();
                    int threadSeq = in.readInt();
                    int stackSeq = in.readInt();
                    bytesLeft -= identifierSize + 8;
                    threadObjects.put(new Integer(threadSeq),
                            new ThreadObject(id, stackSeq));
                    break;
                }
                case HPROF_GC_ROOT_JNI_GLOBAL: {
                    long id = readID();
                    long globalRefId = readID();
                    bytesLeft -= 2*identifierSize;
                    snapshot.addRoot(new Root(id, 0, Root.NATIVE_STATIC, ""));
                    break;
                }
                case HPROF_GC_ROOT_JNI_LOCAL: {
                    long id = readID();
                    int threadSeq = in.readInt();
                    int depth = in.readInt();
                    bytesLeft -= identifierSize + 8;
                    ThreadObject to = getThreadObjectFromSequence(threadSeq);
                    StackTrace st = getStackTraceFromSerial(to.stackSeq);
                    if (st != null) {
                        st = st.traceForDepth(depth+1);
                    }
                    snapshot.addRoot(new Root(id, to.threadId,
                            Root.NATIVE_LOCAL, "", st));
                    break;
                }
                case HPROF_GC_ROOT_JAVA_FRAME: {
                    long id = readID();
                    int threadSeq = in.readInt();
                    int depth = in.readInt();
                    bytesLeft -= identifierSize + 8;
                    ThreadObject to = getThreadObjectFromSequence(threadSeq);
                    StackTrace st = getStackTraceFromSerial(to.stackSeq);
                    if (st != null) {
                        st = st.traceForDepth(depth+1);
                    }
                    snapshot.addRoot(new Root(id, to.threadId,
                            Root.JAVA_LOCAL, "", st));
                    break;
                }
                case HPROF_GC_ROOT_NATIVE_STACK: {
                    long id = readID();
                    int threadSeq = in.readInt();
                    bytesLeft -= identifierSize + 4;
                    ThreadObject to = getThreadObjectFromSequence(threadSeq);
                    StackTrace st = getStackTraceFromSerial(to.stackSeq);
                    snapshot.addRoot(new Root(id, to.threadId,
                            Root.NATIVE_STACK, "", st));
                    break;
                }
                case HPROF_GC_ROOT_STICKY_CLASS: {
                    long id = readID();
                    bytesLeft -= identifierSize;
                    snapshot.addRoot(new Root(id, 0, Root.SYSTEM_CLASS, ""));
                    break;
                }
                case HPROF_GC_ROOT_THREAD_BLOCK: {
                    long id = readID();
                    int threadSeq = in.readInt();
                    bytesLeft -= identifierSize + 4;
                    ThreadObject to = getThreadObjectFromSequence(threadSeq);
                    StackTrace st = getStackTraceFromSerial(to.stackSeq);
                    snapshot.addRoot(new Root(id, to.threadId,
                            Root.THREAD_BLOCK, "", st));
                    break;
                }
                case HPROF_GC_ROOT_MONITOR_USED: {
                    long id = readID();
                    bytesLeft -= identifierSize;
                    snapshot.addRoot(new Root(id, 0, Root.BUSY_MONITOR, ""));
                    break;
                }
                case HPROF_GC_CLASS_DUMP: {
                    int bytesRead = readClass();
                    bytesLeft -= bytesRead;
                    break;
                }
                case HPROF_GC_INSTANCE_DUMP: {
                    int bytesRead = readInstance();
                    bytesLeft -= bytesRead;
                    break;
                }
                case HPROF_GC_OBJ_ARRAY_DUMP: {
                    long bytesRead = readArray(false);
                    bytesLeft -= bytesRead;
                    break;
                }
                case HPROF_GC_PRIM_ARRAY_DUMP: {
                    long bytesRead = readArray(true);
                    bytesLeft -= bytesRead;
                    break;
                }
                default: {
                    throw new NprofilerException("Unrecognized heap dump sub-record type:  " + type);
                }
            }
        }
        if (bytesLeft != 0) {
            log.warn("Error reading heap dump or heap dump segment:  Byte count is " + bytesLeft + " instead of 0");
            skipBytes(bytesLeft);
        }
        if (debugLevel > 0) {
            log.info("    Finished heap sub-records.");
        }
    }

    private long readID() throws IOException {
        return (identifierSize == 4)?
                (Snapshot.SMALL_ID_MASK & (long)in.readInt()) : in.readLong();
    }

    //
    // Read a java value.  If result is non-null, it's expected to be an
    // array of one element.  We use it to fake multiple return values.
    // @returns the number of bytes read
    //
    private int readValue(JavaThing[] resultArr) throws IOException {
        byte type = in.readByte();
        return 1 + readValueForType(type, resultArr);
    }

    private int readValueForType(byte type, JavaThing[] resultArr)
            throws IOException {
        if (version >= VERSION_JDK12BETA4) {
            type = signatureFromTypeId(type);
        }
        return readValueForTypeSignature(type, resultArr);
    }

    private int readValueForTypeSignature(byte type, JavaThing[] resultArr)
            throws IOException {
        switch (type) {
            case '[':
            case 'L': {
                long id = readID();
                if (resultArr != null) {
                    resultArr[0] = new JavaObjectRef(id);
                }
                return identifierSize;
            }
            case 'Z': {
                int b = in.readByte();
                if (b != 0 && b != 1) {
                    log.warn("Illegal boolean value read");
                }
                if (resultArr != null) {
                    resultArr[0] = new JavaBoolean(b != 0);
                }
                return 1;
            }
            case 'B': {
                byte b = in.readByte();
                if (resultArr != null) {
                    resultArr[0] = new JavaByte(b);
                }
                return 1;
            }
            case 'S': {
                short s = in.readShort();
                if (resultArr != null) {
                    resultArr[0] = new JavaShort(s);
                }
                return 2;
            }
            case 'C': {
                char ch = in.readChar();
                if (resultArr != null) {
                    resultArr[0] = new JavaChar(ch);
                }
                return 2;
            }
            case 'I': {
                int val = in.readInt();
                if (resultArr != null) {
                    resultArr[0] = new JavaInt(val);
                }
                return 4;
            }
            case 'J': {
                long val = in.readLong();
                if (resultArr != null) {
                    resultArr[0] = new JavaLong(val);
                }
                return 8;
            }
            case 'F': {
                float val = in.readFloat();
                if (resultArr != null) {
                    resultArr[0] = new JavaFloat(val);
                }
                return 4;
            }
            case 'D': {
                double val = in.readDouble();
                if (resultArr != null) {
                    resultArr[0] = new JavaDouble(val);
                }
                return 8;
            }
            default: {
                throw new NprofilerException("Bad value signature:  " + type);
            }
        }
    }

    private ThreadObject getThreadObjectFromSequence(int threadSeq)
            throws IOException {
        ThreadObject to = threadObjects.get(new Integer(threadSeq));
        if (to == null) {
            throw new NprofilerException("Thread " + threadSeq +
                    " not found for JNI local ref");
        }
        return to;
    }

    private String getNameFromID(long id) throws IOException {
        return getNameFromID(new Long(id));
    }

    private String getNameFromID(Long id) throws IOException {
        if (id.longValue() == 0L) {
            return "";
        }
        String result = names.get(id);
        if (result == null) {
            log.warn("Name not found at " + toHex(id.longValue()));
            return "unresolved name " + toHex(id.longValue());
        }
        return result;
    }

    private StackTrace getStackTraceFromSerial(int ser) throws IOException {
        if (stackTraces == null) {
            return null;
        }
        StackTrace result = stackTraces.get(new Integer(ser));
        if (result == null) {
            log.warn("Stack trace not found for serial # " + ser);
        }
        return result;
    }

    //
    // Handle a HPROF_GC_CLASS_DUMP
    // Return number of bytes read
    //
    private int readClass() throws IOException {
        long id = readID();
        StackTrace stackTrace = getStackTraceFromSerial(in.readInt());
        long superId = readID();
        long classLoaderId = readID();
        long signersId = readID();
        long protDomainId = readID();
        long reserved1 = readID();
        long reserved2 = readID();
        int instanceSize = in.readInt();
        int bytesRead = 7 * identifierSize + 8;

        int numConstPoolEntries = in.readUnsignedShort();
        bytesRead += 2;
        for (int i = 0; i < numConstPoolEntries; i++) {
            int index = in.readUnsignedShort();
            bytesRead += 2;
            bytesRead += readValue(null);
        }

        int numStatics = in.readUnsignedShort();
        bytesRead += 2;
        JavaThing[] valueBin = new JavaThing[1];
        JavaStatic[] statics = new JavaStatic[numStatics];
        for (int i = 0; i < numStatics; i++) {
            long nameId = readID();
            bytesRead += identifierSize;
            byte type = in.readByte();
            bytesRead++;
            bytesRead += readValueForType(type, valueBin);
            String fieldName = getNameFromID(nameId);
            if (version >= VERSION_JDK12BETA4) {
                type = signatureFromTypeId(type);
            }
            String signature = "" + ((char) type);
            JavaField f = new JavaField(fieldName, signature);
            statics[i] = new JavaStatic(f, valueBin[0]);
        }

        int numFields = in.readUnsignedShort();
        bytesRead += 2;
        JavaField[] fields = new JavaField[numFields];
        for (int i = 0; i < numFields; i++) {
            long nameId = readID();
            bytesRead += identifierSize;
            byte type = in.readByte();
            bytesRead++;
            String fieldName = getNameFromID(nameId);
            if (version >= VERSION_JDK12BETA4) {
                type = signatureFromTypeId(type);
            }
            String signature = "" + ((char) type);
            fields[i] = new JavaField(fieldName, signature);
        }
        String name = classNameFromObjectID.get(new Long(id));
        if (name == null) {
            log.warn("Class name not found for " + toHex(id));
            name = "unknown-name@" + toHex(id);
        }
        JavaClass c = new JavaClass(id, name, superId, classLoaderId, signersId,
                protDomainId, fields, statics,
                instanceSize);
        snapshot.addClass(id, c);
        snapshot.setSiteTrace(c, stackTrace);

        return bytesRead;
    }

    private String toHex(long addr) {
        return com.sun.tools.hat.internal.util.Misc.toHex(addr);
    }

    //
    // Handle a HPROF_GC_INSTANCE_DUMP
    // Return number of bytes read
    //
    private int readInstance() throws IOException {
        long start = in.position();
        long id = readID();
        StackTrace stackTrace = getStackTraceFromSerial(in.readInt());
        long classID = readID();
        int bytesFollowing = in.readInt();
        int bytesRead = (2 * identifierSize) + 8 + bytesFollowing;
        JavaObject jobj = new JavaObject(classID, start);
        skipBytes(bytesFollowing);
        snapshot.addHeapObject(id, jobj);
        snapshot.setSiteTrace(jobj, stackTrace);
        return bytesRead;
    }

    //
    // Handle a HPROF_GC_OBJ_ARRAY_DUMP or HPROF_GC_PRIM_ARRAY_DUMP
    // Return number of bytes read
    //
    private long readArray(boolean isPrimitive) throws IOException {
        long start = in.position();
        long id = readID();
        StackTrace stackTrace = getStackTraceFromSerial(in.readInt());
        int num = in.readInt();
        long bytesRead = identifierSize + 8;
        long elementClassID;
        if (isPrimitive) {
            elementClassID = in.readByte();
            bytesRead++;
        } else {
            elementClassID = readID();
            bytesRead += identifierSize;
        }

        // Check for primitive arrays:
        byte primitiveSignature = 0x00;
        int elSize = 0;
        if (isPrimitive || version < VERSION_JDK12BETA4) {
            switch ((int)elementClassID) {
                case T_BOOLEAN: {
                    primitiveSignature = (byte) 'Z';
                    elSize = 1;
                    break;
                }
                case T_CHAR: {
                    primitiveSignature = (byte) 'C';
                    elSize = 2;
                    break;
                }
                case T_FLOAT: {
                    primitiveSignature = (byte) 'F';
                    elSize = 4;
                    break;
                }
                case T_DOUBLE: {
                    primitiveSignature = (byte) 'D';
                    elSize = 8;
                    break;
                }
                case T_BYTE: {
                    primitiveSignature = (byte) 'B';
                    elSize = 1;
                    break;
                }
                case T_SHORT: {
                    primitiveSignature = (byte) 'S';
                    elSize = 2;
                    break;
                }
                case T_INT: {
                    primitiveSignature = (byte) 'I';
                    elSize = 4;
                    break;
                }
                case T_LONG: {
                    primitiveSignature = (byte) 'J';
                    elSize = 8;
                    break;
                }
            }
            if (version >= VERSION_JDK12BETA4 && primitiveSignature == 0x00) {
                throw new NprofilerException("Unrecognized typecode:  "
                        + elementClassID);
            }
        }
        if (primitiveSignature != 0x00) {
            long size = elSize * (long)num;
            bytesRead += size;
            JavaValueArray va = new JavaValueArray(primitiveSignature, start);
            skipBytes(size);
            snapshot.addHeapObject(id, va);
            snapshot.setSiteTrace(va, stackTrace);
        } else {
            long sz = (long)num * identifierSize;
            bytesRead += sz;
            JavaObjectArray arr = new JavaObjectArray(elementClassID, start);
            skipBytes(sz);
            snapshot.addHeapObject(id, arr);
            snapshot.setSiteTrace(arr, stackTrace);
        }
        return bytesRead;
    }

    private byte signatureFromTypeId(byte typeId) throws IOException {
        switch (typeId) {
            case T_CLASS: {
                return (byte) 'L';
            }
            case T_BOOLEAN: {
                return (byte) 'Z';
            }
            case T_CHAR: {
                return (byte) 'C';
            }
            case T_FLOAT: {
                return (byte) 'F';
            }
            case T_DOUBLE: {
                return (byte) 'D';
            }
            case T_BYTE: {
                return (byte) 'B';
            }
            case T_SHORT: {
                return (byte) 'S';
            }
            case T_INT: {
                return (byte) 'I';
            }
            case T_LONG: {
                return (byte) 'J';
            }
            default: {
                throw new NprofilerException("Invalid type id of " + typeId);
            }
        }
    }

    private void handleEOF(EOFException exp, Snapshot snapshot) {
        if (debugLevel > 0) {
            exp.printStackTrace();
        }
        log.warn("Unexpected EOF. Will miss information...");
        // we have EOF, we have to tolerate missing references
        snapshot.setUnresolvedObjectsOK(true);
    }

    private void warn(String msg) {
        System.out.println("WARNING: " + msg);
    }

    //
    // A trivial data-holder class for HPROF_GC_ROOT_THREAD_OBJ.
    //
    private class ThreadObject {

        long threadId;
        int stackSeq;

        ThreadObject(long threadId, int stackSeq) {
            this.threadId = threadId;
            this.stackSeq = stackSeq;
        }
    }

}
