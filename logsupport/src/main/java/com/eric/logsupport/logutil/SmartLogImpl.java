package com.eric.logsupport.logutil;

import android.os.Environment;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmartLogImpl implements ILog {
    private String TAG = "Smart_Log";
    private boolean sOpenLog = true;
    private boolean sOpenLogToFile = false;
    private int sPrintPriorityLimit = 2;
    private int sFileMaxSize;
    private String sLogFilePath;
    private String sLogFileName;
    private String sVersionName;
    private int sProcessId;
    private static final Object FILE_WRITER_MUTEX = new Object();
    private static final long CHECK_FILE_TIME = 10800000L;
    private long recordCheckFileTime = 0L;

    public SmartLogImpl(Builder builder) {
        if (!TextUtils.isEmpty(SmartLogImpl.Builder.tag)) {
            this.TAG = SmartLogImpl.Builder.tag;
        }

        if (TextUtils.isEmpty(SmartLogImpl.Builder.folderName)) {
            throw new IllegalArgumentException("folderName is NULL!");
        } else {
            this.sLogFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + SmartLogImpl.Builder.folderName + "/";
            this.sLogFileName = SmartLogImpl.Builder.folderName + "_log.txt";
            if (TextUtils.isEmpty(this.sLogFilePath)) {
                this.sLogFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/unknow/";
            }

            if (TextUtils.isEmpty(this.sLogFileName)) {
                this.sLogFileName = "unknow_log.txt";
            }

            if (SmartLogImpl.Builder.limit <= 0) {
                this.priorityLimit(SmartLogImpl.Builder.limit);
            }

            this.sVersionName = SmartLogImpl.Builder.versionName;
            this.sOpenLogToFile = SmartLogImpl.Builder.sOpenLogToFile;
            this.sProcessId = Process.myPid();
            this.sFileMaxSize = 6291456;
        }
    }

    public SmartLogImpl setLogFileMaxSize(int size_unit_mb) {
        if (size_unit_mb > 0 && size_unit_mb <= 50) {
            this.sFileMaxSize = size_unit_mb * 1024 * 1024;
        }

        return this;
    }

    public SmartLogImpl openLog(boolean isOpen) {
        this.sOpenLog = isOpen;
        return this;
    }

    public SmartLogImpl openLogToFile(boolean isOpen) {
        this.sOpenLogToFile = isOpen;
        return this;
    }

    public SmartLogImpl priorityLimit(int limit) {
        if (limit >= 2 && limit <= 6) {
            this.sPrintPriorityLimit = limit;
        }

        return this;
    }

    private void println(int priority, String tag, String msg) {
        this.println(priority, tag, msg, (String)null);
    }

    private void println(int priority, String tag, String msg, String throwableMsg) {
        if (this.sOpenLog) {
            if (priority >= this.sPrintPriorityLimit) {
                StringBuilder sbLog = new StringBuilder();
                if (!TextUtils.isEmpty(this.sVersionName)) {
                    sbLog.append("[").append(this.sVersionName).append("]");
                }

                sbLog.append("[").append(this.sProcessId).append("]");
                if (!TextUtils.isEmpty(tag)) {
                    sbLog.append("[").append(tag).append("]");
                }

                if (!TextUtils.isEmpty(msg)) {
                    sbLog.append(msg);
                }

                if (!TextUtils.isEmpty(throwableMsg)) {
                    sbLog.append("\n").append(throwableMsg);
                }

                Log.println(priority, this.TAG, sbLog.toString());
                this.writeToFile(sbLog.toString());
            }
        }
    }

    public void v(String tag, String message) {
        this.println(2, tag, message);
    }

    public void d(String tag, String message) {
        this.println(3, tag, message);
    }

    public void i(String tag, String message) {
        this.println(4, tag, message);
    }

    public void w(String tag, String message) {
        this.println(5, tag, message);
    }

    public void e(String tag, String message) {
        this.println(6, tag, message);
    }

    public void e(String tag, Throwable ex) {
        this.println(6, tag, (String)null, Log.getStackTraceString(ex));
    }

    public void e(String tag, String message, Throwable ex) {
        this.println(6, tag, message, Log.getStackTraceString(ex));
    }

    private void writeToFile(String strLog) {
        if (this.sOpenLogToFile) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String date = dateFormat.format(new Date());
            File file = new File(this.getPath() + this.sLogFileName);
            if (!file.exists()) {
                try {
                    boolean isSuccess = file.createNewFile();
                    Log.i(this.TAG, "create log file, isSuccess=" + isSuccess);
                } catch (IOException var9) {
                    IOException e = var9;
                    Log.e(this.TAG, Log.getStackTraceString(e));
                }
            }

            try {
                OutputStreamWriter streamWriter = new OutputStreamWriter(new FileOutputStream(this.getPath() + this.sLogFileName, true), Charset.forName("UTF-8"));

                try {
                    streamWriter.write(date);
                    streamWriter.write(":");
                    streamWriter.write(strLog);
                    streamWriter.write("\r\n");
                    streamWriter.flush();
                    if (SystemClock.elapsedRealtime() - this.recordCheckFileTime >= 10800000L) {
                        this.checkFileSizeOverflow();
                    }
                } catch (Throwable var10) {
                    try {
                        streamWriter.close();
                    } catch (Throwable var8) {
                        var10.addSuppressed(var8);
                    }

                    throw var10;
                }

                streamWriter.close();
            } catch (Throwable var11) {
                Throwable e = var11;
                Log.e(this.TAG, Log.getStackTraceString(e));
            }

        }
    }

    private void checkFileSizeOverflow() {
        synchronized(FILE_WRITER_MUTEX) {
            this.recordCheckFileTime = SystemClock.elapsedRealtime();
            String logFilePath = this.getPath() + this.sLogFileName;
            File file = new File(logFilePath);
            long fileLength = file.length();
            Log.d(this.TAG, "checkFileSizeOverflow, fileLength=" + fileLength);
            if (fileLength >= (long)this.sFileMaxSize) {
                File temp = new File(this.getPath() + "w_temp.txt");

                try {
                    FileInputStream in;
                    label121: {
                        in = new FileInputStream(file);

                        try {
                            FileOutputStream tempout = new FileOutputStream(temp);

                            label87: {
                                try {
                                    byte[] buf = new byte[4089];
                                    long skip = in.skip(fileLength / 2L);
                                    Log.v(this.TAG, "skip file: " + skip);

                                    while(true) {
                                        int len = in.read(buf);
                                        if (len < 0) {
                                            if (file.delete()) {
                                                Log.d(this.TAG, "file slimming finish, fileLength=" + temp.length());
                                                if (temp.renameTo(new File(logFilePath))) {
                                                    break label87;
                                                }

                                                Log.e(this.TAG, "writeToFile " + temp.getAbsolutePath() + " rename To " + logFilePath + " failed");
                                                if (!temp.delete()) {
                                                    Log.w(this.TAG, "delete temp file failed! ");
                                                }
                                                break label87;
                                            }

                                            Log.e(this.TAG, "writeToFile file:" + file.getAbsolutePath() + "delete failed");
                                            if (!temp.delete()) {
                                                Log.w(this.TAG, "delete temp file failed! ");
                                            }
                                            break;
                                        }

                                        tempout.write(buf, 0, len);
                                    }
                                } catch (Throwable var16) {
                                    try {
                                        tempout.close();
                                    } catch (Throwable var15) {
                                        var16.addSuppressed(var15);
                                    }

                                    throw var16;
                                }

                                tempout.close();
                                break label121;
                            }

                            tempout.close();
                        } catch (Throwable var17) {
                            try {
                                in.close();
                            } catch (Throwable var14) {
                                var17.addSuppressed(var14);
                            }

                            throw var17;
                        }

                        in.close();
                        return;
                    }

                    in.close();
                } catch (Throwable var18) {
                    Throwable e = var18;
                    Log.e(this.TAG, Log.getStackTraceString(e));
                }
            }
        }
    }

    private String getPath() {
        File file = new File(this.sLogFilePath);
        if (!file.exists() && !file.mkdirs()) {
            Log.e(this.TAG, "mkdirs " + this.sLogFilePath + " failed.");
        }

        return file.getAbsolutePath() + "/";
    }

    public static class Builder {
        private static String tag;
        private static String folderName;
        private static String versionName;
        private static boolean sOpenLogToFile;
        private static int limit;

        public Builder() {
        }

        public Builder tag(String tag) {
            SmartLogImpl.Builder.tag = tag;
            return this;
        }

        public Builder folderName(String folderName) {
            SmartLogImpl.Builder.folderName = folderName;
            return this;
        }

        public Builder versionName(String versionName) {
            SmartLogImpl.Builder.versionName = versionName;
            return this;
        }

        public Builder openLogToFile(boolean sOpenLogToFile) {
            SmartLogImpl.Builder.sOpenLogToFile = sOpenLogToFile;
            return this;
        }

        public Builder limit(int limit) {
            SmartLogImpl.Builder.limit = limit;
            return this;
        }

        public SmartLogImpl build() {
            return new SmartLogImpl(this);
        }
    }
}