package au.nerago.mopgear.results;

import au.nerago.mopgear.io.DataLocation;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class OutputText {
    private static PrintWriter writer;

    private static Writer currentWriter() throws IOException {
        if (writer == null) {
            String filename = LocalDateTime.now().toString().replace(':', '-') + ".txt";
            Path path = DataLocation.resultsDir.resolve(filename);
            writer = new PrintWriter(Files.newBufferedWriter(path), true);
        }
        return writer;
    }

    public static void finish() {
        writer.flush();
        writer.close();
    }

    public static void println() {
        try {
            System.out.println();
            currentWriter().write("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void println(String str) {
        try {
            System.out.println(str);
            currentWriter().write(str + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void print(String str) {
        try {
            System.out.print(str);
            currentWriter().write(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void printf(String format, Object... args) {
        try {
            String str = String.format(format, args);
            System.out.print(str);
            currentWriter().write(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ThrowablePrintedToSystemOut")
    public static void printException(Throwable ex) throws IOException {
        currentWriter().write(ex.toString());
        ex.printStackTrace(writer);

        System.out.println(ex);
        ex.printStackTrace(System.out);
    }
}
