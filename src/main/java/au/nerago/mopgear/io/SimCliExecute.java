package au.nerago.mopgear.io;

import java.io.IOException;
import java.nio.file.Path;

public class SimCliExecute {
    private static final String EXEC = "D:\\prog\\wowsim\\wowsimcli-windows.exe";

    public static void run(Path inFile, Path outFile) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(EXEC, "sim", "--infile", inFile.toString(), "--outfile", outFile.toString());
        Process process = processBuilder.start();
        process.waitFor();
    }
}
