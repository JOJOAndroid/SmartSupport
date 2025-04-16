package com.eric.logsupport.logutil;


public interface ILog {
    void v(String var1, String var2);

    void d(String var1, String var2);

    void i(String var1, String var2);

    void w(String var1, String var2);

    void e(String var1, String var2);

    void e(String var1, Throwable var2);

    void e(String var1, String var2, Throwable var3);
}

