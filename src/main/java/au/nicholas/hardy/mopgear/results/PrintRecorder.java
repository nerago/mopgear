package au.nicholas.hardy.mopgear.results;

import java.util.ArrayList;
import java.util.List;

public class PrintRecorder {
    private final List<String> prints = new ArrayList<>();
    public boolean outputImmediate;

    public void println(String str) {
        prints.add(str);
        if (outputImmediate)
            outputNow(str);
    }

    public void printf(String format, Object... args) {
        String str = String.format(format, args);
        prints.add(str);
        if (outputImmediate)
            outputNow(str);
    }

    public void outputNow() {
        for (String str : prints) {
            outputNow(str);
        }
    }

    private static void outputNow(String str) {
        if (str.endsWith("\n"))
            System.out.print(str);
        else
            System.out.println(str);
    }

    public void append(PrintRecorder other) {
        this.prints.addAll(other.prints);
    }
}
