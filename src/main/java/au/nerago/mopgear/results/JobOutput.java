package au.nerago.mopgear.results;

import au.nerago.mopgear.ItemMapUtil;
import au.nerago.mopgear.domain.*;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class JobOutput {

    public final JobInput input;

    public Optional<SolvableItemSet> resultSet;
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

    public Optional<ItemSet> getFinalResultSet() {
        return resultSet.map(set -> ItemSet.ofSolvable(set, ItemMapUtil.mapperToFullItems(input.fullItemOptions)));
    }
}
