package au.nerago.mopgear.io;

import java.nio.file.Path;

public class DataLocation {
    private static final Path directory = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\paladin_gearing");
    public static final Path weightBoomFile = directory.resolve("weight-druid-boom.txt");
    public static final Path weightBearFile = directory.resolve("weight-druid-bear.txt");
    public static final Path weightWarlockFile = directory.resolve("weight-warlock-destro.txt");
    public static final Path weightProtMitigationFile = directory.resolve("weight-prot-mitigation.txt");
    public static final Path weightProtDpsFile = directory.resolve("weight-prot-dps.txt");
    public static final Path weightRetFile = directory.resolve("weight-ret-sim.txt");
    public static final Path bagsFile = directory.resolve("bags-gear-bags.json");
    public static final Path gearBoomFile = directory.resolve("gear-druid-boom.json");
    public static final Path gearTreeFile = directory.resolve("gear-druid-resto.json");
    public static final Path gearBearFile = directory.resolve("gear-druid-bear.json");
    public static final Path gearWarlockFile = directory.resolve("gear-warlock-destro.json");
    public static final Path gearProtDpsFile = directory.resolve("gear-prot-dps.json");
    public static final Path gearProtDefenceFile = directory.resolve("gear-prot-defence.json");
    public static final Path gearRetFile = directory.resolve("gear-ret.json");
    public static final Path cacheFile = directory.resolve("cache.json");
    public static final Path resultsDir = directory.resolve("results");
}
