package com.eric.logsupport.logutil;

import java.util.Locale;

public class SLog {
    private static ILog sLogger = null;

    public SLog() {
    }

    public static void setLogger(ILog logger) {
        sLogger = logger;
    }

    public static void setLogger(SmartLogImpl.Builder logger) {
        sLogger = new SmartLogImpl(logger);
    }

    public static void v(String tag, String message) {
        if (sLogger != null) {
            sLogger.v(tag, buildMessage(message));
        }

    }

    public static void v(String message) {
        if (sLogger != null) {
            sLogger.v((String)null, buildMessage(message));
        }

    }

    public static void d(String tag, String message) {
        if (sLogger != null) {
            sLogger.d(tag, buildMessage(message));
        }

    }

    public static void d(String message) {
        if (sLogger != null) {
            sLogger.d((String)null, buildMessage(message));
        }

    }

    public static void i(String tag, String message) {
        if (sLogger != null) {
            sLogger.i(tag, buildMessage(message));
        }

    }

    public static void i(String message) {
        if (sLogger != null) {
            sLogger.i((String)null, buildMessage(message));
        }

    }

    public static void w(String tag, String message) {
        if (sLogger != null) {
            sLogger.w(tag, buildMessage(message));
        }

    }

    public static void w(String message) {
        if (sLogger != null) {
            sLogger.w((String)null, buildMessage(message));
        }

    }

    public static void e(String tag, String message) {
        if (sLogger != null) {
            sLogger.e(tag, buildMessage(message));
        }

    }

    public static void e(String message) {
        if (sLogger != null) {
            sLogger.e((String)null, buildMessage(message));
        }

    }

    public static void e(String tag, String message, Throwable e) {
        if (sLogger != null) {
            sLogger.e(tag, buildMessage(message), e);
        }

    }

    public static void e(String message, Throwable e) {
        if (sLogger != null) {
            sLogger.e((String)null, buildMessage(message), e);
        }

    }

    public static void e(Throwable e) {
        if (sLogger != null) {
            sLogger.e((String)null, e);
        }

    }

    public static void printCallStack() {
        Throwable ex = new Throwable();
        StackTraceElement[] stackTraceElements = ex.getStackTrace();
        if (stackTraceElements != null) {
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] var3 = stackTraceElements;
            int var4 = stackTraceElements.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                StackTraceElement element = var3[var5];
                sb.append(element);
                sb.append("\n");
            }

            d("STACK", sb.toString());
        }

    }

    private static String buildMessage(String msg) {
        StackTraceElement[] trace = (new Throwable()).fillInStackTrace().getStackTrace();
        String caller = "(Unknown)";
        if (trace != null && trace.length >= 2) {
            caller = buildFileLineNum(trace[2]);
        }

        return String.format(Locale.CHINA, "%s: %s", caller, msg);
    }

    private static String buildFileLineNum(StackTraceElement ste) {
        if (ste == null) {
            return "(Unknown)";
        } else {
            StringBuilder buf = new StringBuilder();
            if (ste.isNativeMethod()) {
                buf.append("(Native Method)");
            } else {
                String fName = ste.getFileName();
                if (fName == null) {
                    buf.append("(Unknown Source)");
                } else {
                    int lineNum = ste.getLineNumber();
                    buf.append("(");
                    buf.append(fName);
                    if (lineNum >= 0) {
                        buf.append(":");
                        buf.append(lineNum);
                    }

                    buf.append(")");
                }
            }

            return buf.toString();
        }
    }
}
