package au.nerago.mopgear.results;

import au.nerago.mopgear.domain.EquipOptionsMap;
import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.ItemSet;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.model.ModelCombined;

import java.time.Instant;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class JobInfo {
    public final PrintRecorder printRecorder = new PrintRecorder();
    public Optional<ItemSet> resultSet;
    public boolean hackAllow;
    public int hackCount;
    public ModelCombined model;
    public EquipOptionsMap itemOptions;
    public Instant startTime;
    public Long runSize;
    public boolean forceRandom;
    public boolean singleThread;
    public StatBlock adjustment;
    public ItemData extraItem;
    public double factor;
    public int cost;

    public void config(ModelCombined model, EquipOptionsMap itemOptions, Instant startTime, Long runSize, StatBlock adjustment) {
        this.model = model;
        this.itemOptions = itemOptions;
        this.startTime = startTime;
        this.runSize = runSize;
        this.adjustment = adjustment;
    }

    public void println(String str) {
        printRecorder.println(str);
    }

    public void printf(String format, Object... args) {
        printRecorder.printf(format, args);
    }

    public void printfAndEcho(String format, Object... args) {
        printRecorder.printfAndEcho(format, args);
    }
}
