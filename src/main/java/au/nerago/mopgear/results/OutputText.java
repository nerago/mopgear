package au.nerago.mopgear.results;

import au.nerago.mopgear.io.DataLocation;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@SuppressWarnings("resource")
public class OutputText {
    private static PrintWriter writer;

    private static synchronized Writer currentWriter() throws IOException {
        if (writer == null) {
            writer = openNewFile();
        }
        return writer;
    }

    private static PrintWriter openNewFile() throws IOException {
        String filename = LocalDateTime.now().toString().replace(':', '-') + ".txt";
        Path path = DataLocation.resultsDir.resolve(filename);
        return new PrintWriter(Files.newBufferedWriter(path), true);
    }

    public static synchronized void switchToNewFile() {
        try {
            finish();
            writer = openNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void finish() {
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
