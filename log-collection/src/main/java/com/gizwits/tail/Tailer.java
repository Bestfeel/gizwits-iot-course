
package com.gizwits.tail;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.io.IOUtils.EOF;


public class Tailer implements Runnable {

    private static final int DEFAULT_DELAY_MILLIS = 1000;

    private static final String RAF_MODE = "r";

    private static final int DEFAULT_BUFSIZE = 4096;

    // The default charset used for reading files
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

    /**
     * Buffer on top of RandomAccessFile.
     */
    private final byte inbuf[];

    /**
     * The file which will be tailed.
     */
    private final File file;

    /**
     * The character set that will be used to read the file.
     */
    private final Charset cset;

    /**
     * The amount of time to wait for the file to be updated.
     */
    private final long delayMillis;

    /**
     * Whether to tail from the end or start of file,d
     */
    private final boolean end;

    /**
     * The listener to notify of events when tailing.
     */
    private final TailerListener listener;

    /**
     * Whether to close and reopen the file whilst waiting for more input.
     */
    private final boolean reOpen;

    /**
     * The Tailer will run as long as this value is true.
     */
    private volatile boolean run = true;

    /**
     * 如果 从尾部开始读取文件,默认输出最后200行
     */
    private final static int numLine = 200;

    /**
     * Creates a Tailer for the given file, starting from the beginning, with the default delay of 1.0s.
     *
     * @param file     The file to follow.
     * @param listener the TailerListener to use.
     */
    public Tailer(final File file, final TailerListener listener) {
        this(file, listener, DEFAULT_DELAY_MILLIS);
    }

    /**
     * Creates a Tailer for the given file, starting from the beginning.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     */
    public Tailer(final File file, final TailerListener listener, final long delayMillis) {
        this(file, listener, delayMillis, false);
    }

    /**
     * Creates a Tailer for the given file, with a delay other than the default 1.0s.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     */
    public Tailer(final File file, final TailerListener listener, final long delayMillis, final boolean end) {
        this(file, listener, delayMillis, end, DEFAULT_BUFSIZE);
    }

    /**
     * Creates a Tailer for the given file, with a delay other than the default 1.0s.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen      if true, close and reopen the file between reading chunks
     */
    public Tailer(final File file, final TailerListener listener, final long delayMillis, final boolean end,
                  final boolean reOpen) {
        this(file, listener, delayMillis, end, reOpen, DEFAULT_BUFSIZE);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param bufSize     Buffer size
     */
    public Tailer(final File file, final TailerListener listener, final long delayMillis, final boolean end,
                  final int bufSize) {
        this(file, listener, delayMillis, end, false, bufSize);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen      if true, close and reopen the file between reading chunks
     * @param bufSize     Buffer size
     */
    public Tailer(final File file, final TailerListener listener, final long delayMillis, final boolean end,
                  final boolean reOpen, final int bufSize) {
        this(file, DEFAULT_CHARSET, listener, delayMillis, end, reOpen, bufSize);
    }

    /**
     * Creates a Tailer for the given file, with a specified buffer size.
     *
     * @param file        the file to follow.
     * @param cset        the Charset to be used for reading the file
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen      if true, close and reopen the file between reading chunks
     * @param bufSize     Buffer size
     */
    public Tailer(final File file, final Charset cset, final TailerListener listener, final long delayMillis,
                  final boolean end, final boolean reOpen
            , final int bufSize) {
        this.file = file;
        this.delayMillis = delayMillis;
        this.end = end;
        this.inbuf = new byte[bufSize];

        // Save and prepare the listener
        this.listener = listener;
        listener.init(this);
        this.reOpen = reOpen;
        this.cset = cset;
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param bufSize     buffer size.
     * @return The new Tailer
     */
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis,
                                final boolean end, final int bufSize) {
        return create(file, listener, delayMillis, end, false, bufSize);
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen      whether to close/reopen the file between chunks
     * @param bufSize     buffer size.
     * @return The new Tailer
     */
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis,
                                final boolean end, final boolean reOpen,
                                final int bufSize) {
        return create(file, DEFAULT_CHARSET, listener, delayMillis, end, reOpen, bufSize);
    }

    /**
     * Creates and starts a Tailer for the given file.
     *
     * @param file        the file to follow.
     * @param charset     the character set to use for reading the file
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen      whether to close/reopen the file between chunks
     * @param bufSize     buffer size.
     * @return The new Tailer
     */
    public static Tailer create(final File file, final Charset charset, final TailerListener listener,
                                final long delayMillis, final boolean end, final boolean reOpen
            , final int bufSize) {
        final Tailer Tailer = new Tailer(file, charset, listener, delayMillis, end, reOpen, bufSize);
        final Thread thread = new Thread(Tailer);
        thread.setDaemon(true);
        thread.start();
        return Tailer;
    }

    /**
     * Creates and starts a Tailer for the given file with default buffer size.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @return The new Tailer
     */
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis,
                                final boolean end) {
        return create(file, listener, delayMillis, end, DEFAULT_BUFSIZE);
    }

    /**
     * Creates and starts a Tailer for the given file with default buffer size.
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @param end         Set to true to tail from the end of the file, false to tail from the beginning of the file.
     * @param reOpen      whether to close/reopen the file between chunks
     * @return The new Tailer
     */
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis,
                                final boolean end, final boolean reOpen) {
        return create(file, listener, delayMillis, end, reOpen, DEFAULT_BUFSIZE);
    }

    /**
     * Creates and starts a Tailer for the given file, starting at the beginning of the file
     *
     * @param file        the file to follow.
     * @param listener    the TailerListener to use.
     * @param delayMillis the delay between checks of the file for new content in milliseconds.
     * @return The new Tailer
     */
    public static Tailer create(final File file, final TailerListener listener, final long delayMillis) {
        return create(file, listener, delayMillis, false);
    }

    /**
     * Creates and starts a Tailer for the given file, starting at the beginning of the file
     * with the default delay of 1.0s
     *
     * @param file     the file to follow.
     * @param listener the TailerListener to use.
     * @return The new Tailer
     */
    public static Tailer create(final File file, final TailerListener listener) {
        return create(file, listener, DEFAULT_DELAY_MILLIS, false);
    }

    /**
     * Return the file.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets whether to keep on running.
     *
     * @return whether to keep on running.
     * @since 2.5
     */
    protected boolean getRun() {
        return run;
    }

    /**
     * Return the delay in milliseconds.
     *
     * @return the delay in milliseconds.
     */
    public long getDelay() {
        return delayMillis;
    }

    /**
     * Follows changes in the file, calling the TailerListener's handle method for each new line.
     */
    public void run() {
        RandomAccessFile reader = null;
        try {

            long last = 0; // The last time the file was checked for changes
            long position = 0; // position within the file
            // 从最后开始监听输出
            if (end) {
                readLastNLine(numLine).forEach(line -> listener.handle(line));
            }

            while (getRun() && reader == null) {
                try {
                    reader = new RandomAccessFile(file, RAF_MODE);
                } catch (final FileNotFoundException e) {
                    listener.fileNotFound();
                }
                if (reader == null) {
                    Thread.sleep(delayMillis);
                } else {
                    // The current position in the file,判断是否需要重头开始读取
                    position = end ? file.length() : 0;
                    last = file.lastModified();
                    reader.seek(position);
                }
            }
            while (getRun()) {
                final boolean newer = FileUtils.isFileNewer(file, last); // IO-279, must be done first
                // Check the file length to see if it was rotated
                final long length = file.length();
                if (length < position) {
                    // File was rotated
                    listener.fileRotated();
                    // Reopen the reader after rotation
                    try {
                        // Ensure that the old file is closed iff we re-open it successfully
                        final RandomAccessFile save = reader;
                        reader = new RandomAccessFile(file, RAF_MODE);
                        // At this point, we're sure that the old file is rotated
                        // Finish scanning the old file and then we'll start with the new one
                        try {
                            readLines(save);
                        } catch (IOException ioe) {
                            listener.handle(ioe);
                        }
                        position = 0;
                        // close old file explicitly rather than relying on GC picking up previous RAF
                        IOUtils.closeQuietly(save);
                    } catch (final FileNotFoundException e) {
                        // in this case we continue to use the previous reader and position values
                        listener.fileNotFound();
                    }
                    continue;
                } else {
                    // File was not rotated
                    // See if the file needs to be read again
                    if (length > position) {
                        // The file has more content than it did last time
                        position = readLines(reader);
                        last = file.lastModified();
                    } else if (newer) {
                        /*
                         * This can happen if the file is truncated or overwritten with the exact same length of
                         * information. In cases like this, the file position needs to be reset
                         */
                        position = 0;
                        reader.seek(position); // cannot be null here

                        // Now we can read new lines
                        position = readLines(reader);
                        last = file.lastModified();
                    }
                }
                if (reOpen) {
                    IOUtils.closeQuietly(reader);
                }
                Thread.sleep(delayMillis);
                if (getRun() && reOpen) {
                    reader = new RandomAccessFile(file, RAF_MODE);
                    reader.seek(position);
                }
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            stop(e);
        } catch (final Exception e) {
            stop(e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Stops the Tailer with an exception
     *
     * @param e The exception to send to listener
     */
    private void stop(final Exception e) {
        listener.handle(e);
        stop();
    }

    /**
     * Allows the Tailer to complete its current loop and return.
     */
    public void stop() {
        this.run = false;
    }


    /**
     * Read new lines.
     *
     * @param reader The file to read
     * @return The new position after the lines have been read
     * @throws IOException if an I/O error occurs.
     */
    private long readLines(final RandomAccessFile reader) throws IOException {
        ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(64);
        long pos = reader.getFilePointer();
        long rePos = pos; // position to re-read
        int num;
        boolean seenCR = false;
        while (getRun() && ((num = reader.read(inbuf)) != EOF)) {
            for (int i = 0; i < num; i++) {
                final byte ch = inbuf[i];
                switch (ch) {
                    case '\n':
                        seenCR = false; // swallow CR before LF
                        listener.handle(new String(lineBuf.toByteArray(), cset));
                        lineBuf.reset();
                        rePos = pos + i + 1;
                        break;
                    case '\r':
                        if (seenCR) {
                            lineBuf.write('\r');
                        }
                        seenCR = true;
                        break;
                    default:
                        if (seenCR) {
                            seenCR = false; // swallow final CR
                            listener.handle(new String(lineBuf.toByteArray(), cset));
                            lineBuf.reset();
                            rePos = pos + i + 1;
                        }
                        lineBuf.write(ch);
                }
            }
            pos = reader.getFilePointer();
        }
        IOUtils.closeQuietly(lineBuf); // not strictly necessary
        reader.seek(rePos); // Ensure we can re-read if necessary

        if (listener instanceof TailerListenerAdapter) {
            ((TailerListenerAdapter) listener).endOfFileReached();
        }

        return rePos;
    }

    /**
     * 读取文件最后N行
     *
     * @param numRead
     * @return
     */
    public List<String> readLastNLine(long numRead) {
        // 定义结果集
        List<String> result = new ArrayList<String>();
        //行数统计
        long count = 0;
        // 使用随机读取
        RandomAccessFile reader = null;
        try {
            //使用读模式
            reader = new RandomAccessFile(file, RAF_MODE);
            //读取文件长度
            long length = reader.length();
            //如果是0，代表是空文件，直接返回空结果
            if (length == 0L) {
                return result;
            } else {
                //初始化游标
                long pos = length - 1;
                while (pos > 0) {
                    pos--;
                    //开始读取
                    reader.seek(pos);
                    //如果读取到\n代表是读取到一行
                    if (reader.readByte() == '\n') {
                        //使用readLine获取当前行,ISO-8859-1，解决中文乱码的问题
                        String line = new String(reader.readLine().getBytes("ISO-8859-1"), cset);
                        //保存结果
                        result.add(line);
                        //行数统计，如果到达了numRead指定的行数，就跳出循环
                        count++;
                        if (count == numRead) {
                            break;
                        }
                    }
                }
                if (pos == 0) {
                    reader.seek(0);
                    result.add(reader.readLine());
                }
            }
        } catch (IOException e) {
            IOUtils.closeQuietly(reader);
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(reader);
        }
        // 倒序
        Collections.reverse(result);
        return result;
    }


}
