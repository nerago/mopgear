package au.nerago.mopgear.results;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;

import java.time.Instant;
import java.util.function.Predicate;

public class JobInput {
    public JobInput(RunSizeCategory runSizeCategory, long runSizeAdditionalMultiply, boolean phasedAcceptable) {
        this.runSizeCategory = runSizeCategory;
        this.runSizeAdditionalMultiply = runSizeAdditionalMultiply;
        this.phasedAcceptable = phasedAcceptable;
    }

    public ModelCombined model;
    public EquipOptionsMap fullItemOptions;
    public SolvableEquipOptionsMap itemOptions;
    public Instant startTime;
    public StatBlock adjustment;

    public final long runSizeAdditionalMultiply;
    public final RunSizeCategory runSizeCategory;
    public boolean hackAllow;
    public SolveMethod forceMethod;
    public final boolean phasedAcceptable;
    public boolean singleThread;

    public FullItemData extraItem;
    public int cost;
    public Predicate<SolvableItemSet> specialFilter;

    public final PrintRecorder printRecorder = new PrintRecorder();

    public void setItemOptions(EquipOptionsMap itemOptions) {
        this.fullItemOptions = itemOptions;
        this.itemOptions = new SolvableEquipOptionsMap(itemOptions);
    }

    public void println(String str) {
        printRecorder.println(str);
    }

    public void printf(String format, Object... args) {
        printRecorder.printf(format, args);
    }

    public enum SolveMethod {
        PhasedTop, PhasedFull, Full, SkipIndex, PhasedIndexedTop, Random
    }

    public enum RunSizeCategory {
        Final, Medium, SubSolveItem
    }
}
