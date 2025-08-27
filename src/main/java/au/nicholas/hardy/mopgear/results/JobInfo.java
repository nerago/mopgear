package au.nicholas.hardy.mopgear.results;

import au.nicholas.hardy.mopgear.domain.EquipOptionsMap;
import au.nicholas.hardy.mopgear.domain.ItemData;
import au.nicholas.hardy.mopgear.domain.ItemSet;
import au.nicholas.hardy.mopgear.domain.StatBlock;
import au.nicholas.hardy.mopgear.model.ModelCombined;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
    public boolean singleThread;
    public StatBlock adjustment;
    public ItemData extraItem;
    public double factor;

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
