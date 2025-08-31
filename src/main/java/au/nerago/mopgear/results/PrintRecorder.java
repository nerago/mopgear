package au.nerago.mopgear.results;

import java.util.ArrayList;
import java.util.List;

public class PrintRecorder {
    private final List<String> prints = new ArrayList<>();
    public boolean outputImmediate;

    public void println(String str) {
        prints.add(str);
        if (outputImmediate)
            outputLine(str);
    }

    public void printf(String format, Object... args) {
        String str = String.format(format, args);
        prints.add(str);
        if (outputImmediate)
            outputLine(str);
    }

    public void printfAndEcho(String format, Object[] args) {
        String str = String.format(format, args);
        prints.add(str);
        outputLine(str);
    }

    public void outputNow() {
        for (String str : prints) {
            outputLine(str);
        }
    }

    private static void outputLine(String str) {
        if (str.endsWith("\n"))
            OutputText.print(str);
        else
            OutputText.println(str);
    }

    public void append(PrintRecorder other) {
        this.prints.addAll(other.prints);
        if (outputImmediate) {
            other.outputNow();
        }
    }
}
