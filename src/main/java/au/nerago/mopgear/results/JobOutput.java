package au.nerago.mopgear.results;

import au.nerago.mopgear.domain.ItemSet;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class JobOutput {

    public JobInput input;

    public Optional<ItemSet> resultSet;
    public long resultRating;
    public int hackCount;

    public JobOutput(JobInput input) {
        this.input = input;
    }

    public void println(String str) {
        input.printRecorder.println(str);
    }

    public void printf(String format, Object... args) {
        input.printRecorder.printf(format, args);
    }
}
