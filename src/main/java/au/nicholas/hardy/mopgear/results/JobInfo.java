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
    public final List<String> prints = new ArrayList<>();
    public Optional<ItemSet> resultSet;
    public int hackCount;
    public ModelCombined model;
    public EquipOptionsMap itemOptions;
    public Instant startTime;
    public Long runSize;
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
        prints.add(str);
    }

    public void printf(String format, Object... args) {
        prints.add(String.format(format, args));
    }

    public void outputNow() {
        for (String str : prints) {
            if (str.endsWith("\n"))
                System.out.print(str);
            else
                System.out.println(str);
        }
    }
}
