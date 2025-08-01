package au.nicholas.hardy.mopgear.util;

public class LongHolder {
    public long value;

    public long incrementAndGet() {
        return ++value;
    }
}
