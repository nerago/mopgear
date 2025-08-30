package au.nerago.mopgear.util;

public class LongHolder {
    public long value;

    public long incrementAndGet() {
        return ++value;
    }
}
