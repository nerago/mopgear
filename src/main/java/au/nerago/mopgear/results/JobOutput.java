package au.nerago.mopgear.results;

import au.nerago.mopgear.ItemMapUtil;
import au.nerago.mopgear.domain.ItemSet;
import au.nerago.mopgear.domain.SolvableItemSet;

import java.util.Optional;

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
