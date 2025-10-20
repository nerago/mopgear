package au.nerago.mopgear.results;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;

import java.time.Instant;
import java.util.function.Predicate;

public class JobInput {
    public final PrintRecorder printRecorder = new PrintRecorder();

    public ModelCombined model;
    public SolvableEquipOptionsMap itemOptions;
    public Instant startTime;
    public StatBlock adjustment;

    public long runSizeMultiply = 1;
    public boolean hackAllow;
    public boolean forceRandom;
    public boolean forceSkipIndex;
    public long forcedRunSized;
    public boolean singleThread;

    public ItemData extraItem;
    public int cost;
    public Predicate<SolvableItemSet> specialFilter;

    public void config(ModelCombined model, EquipOptionsMap itemOptions, Instant startTime, StatBlock adjustment) {
        this.model = model;
        setItemOptions(itemOptions);
        this.startTime = startTime;
        this.adjustment = adjustment;
    }

    public void setItemOptions(EquipOptionsMap itemOptions) {
        this.itemOptions = new SolvableEquipOptionsMap(itemOptions);
    }

    public void println(String str) {
        printRecorder.println(str);
    }

    public void printf(String format, Object... args) {
        printRecorder.printf(format, args);
    }
}
