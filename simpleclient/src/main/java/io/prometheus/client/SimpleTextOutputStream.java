package io.prometheus.client;

public interface SimpleTextOutputStream {
    SimpleTextOutputStream write(byte[] a);

    SimpleTextOutputStream write(byte[] a, int offset, int len);

    SimpleTextOutputStream write(char c);

    SimpleTextOutputStream write(String s);

    SimpleTextOutputStream write(Number n);

    SimpleTextOutputStream write(boolean b);

    SimpleTextOutputStream write(long n);

    SimpleTextOutputStream write(double d);
}
