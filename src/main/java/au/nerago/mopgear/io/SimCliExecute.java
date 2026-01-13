package au.nerago.mopgear.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SimCliExecute {
    private static final String EXEC = "D:\\prog\\wowsim\\wowsimcli-windows.exe";

    public static void run(Path inFile, Path outFile) {
        try {
            Files.deleteIfExists(outFile);

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(EXEC, "sim", "--infile", inFile.toString(), "--outfile", outFile.toString());
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
