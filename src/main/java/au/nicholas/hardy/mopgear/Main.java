package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.TopCollectorReporting;
import au.nicholas.hardy.mopgear.util.ArrayUtil;
import au.nicholas.hardy.mopgear.util.Tuple;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings({"CallToPrintStackTrace", "ThrowablePrintedToSystemOut", "SameParameterValue", "unused", "unchecked", "OptionalUsedAsFieldOrParameterType"})
public class Main {

    private static final Path directory = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\paladin_gearing");
    static final Path cacheFile = directory.resolve("cache.json");
    private static final Path gearRetFile = directory.resolve("gear-ret.json");
    private static final Path gearProtFile = directory.resolve("gear-prot.json");
    private static final Path gearBoomFile = directory.resolve("gear-druid-boom.json");
    private static final Path weightRetFile = directory.resolve("weight-ret-sim.txt");
    private static final Path weightProtDpsFile = directory.resolve("weight-prot-dps.txt");
    private static final Path weightProtMitigationFile = directory.resolve("weight-prot-mitigation.txt");
    private static final Path weightBoomFile = directory.resolve("weight-druid-boom.txt");
    public static final long BILLION = 1000 * 1000 * 1000;

    ItemCache itemCache;

    public static void main(String[] arg) throws IOException, ExecutionException, InterruptedException {
        new Main().run();
    }

    private void run() throws IOException, ExecutionException, InterruptedException {
        itemCache = new ItemCache(cacheFile);

        Instant startTime = Instant.now();

        try (ForkJoinPool myPool = new ForkJoinPool(12, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false, 20, 256, 10, null, 60, TimeUnit.SECONDS)) {
            myPool.submit(() -> exceptionalCheck(startTime)).get();
        }

        printElapsed(startTime);

        itemCache.cacheSave();
    }

    private void exceptionalCheck(Instant startTime) {
        try {
//            WowHead.fetchItem(89061);

//            multiSpecSpecifiedRating();
//            multiSpecSequential(startTime);

//            reforgeRet(startTime);
            reforgeProt(startTime);
//            reforgeBoom(startTime);
//        rankSomething();
//        multiSpecReforge(startTime);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    // protmit	21523312
    // protdps	38197350
    // ret	    15526158
    // SEE SPREADSHEET
    // mults: miti*33 prot_dps*8 ret*32

    private static ModelCombined standardRetModel() throws IOException {
        int gem = 76615;
        StatRatings statRatings = new StatRatingsWeights(weightRetFile, false, gem);
        statRatings = new StatRatingsWeightsMix(statRatings, 32, null, 0, gem);
        StatRequirements statRequirements = StatRequirements.ret();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.ret(), enchants);
    }

    private ModelCombined extendedRetModel() throws IOException {
        int gem = 76615;
        StatRatings statRatings = new StatRatingsWeights(weightRetFile, false, gem);
        statRatings = new StatRatingsWeightsMix(statRatings, 32, null, 0, gem);
        StatRequirements statRequirements = StatRequirements.ret();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinRet);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.retExtended(), enchants);
    }

    private static ModelCombined standardProtModel() throws IOException {
        int gem = 76615;
        StatRatings statMitigation = new StatRatingsWeights(weightProtMitigationFile, false, gem);
        StatRatings statDps = new StatRatingsWeights(weightProtDpsFile, false, gem);
        StatRatings statMix = new StatRatingsWeightsMix(statMitigation, 33, statDps, 8, gem);
        StatRequirements statRequirements = StatRequirements.prot();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.PaladinProt);
        return new ModelCombined(statMix, statRequirements, ReforgeRules.prot(), enchants);
    }

    private ModelCombined standardBoomModel() throws IOException {
        StatRatings statRatings = new StatRatingsWeights(weightBoomFile, false, null);
        StatRequirements statRequirements = StatRequirements.boom();
        DefaultEnchants enchants = new DefaultEnchants(SpecType.DruidBoom);
        return new ModelCombined(statRatings, statRequirements, ReforgeRules.boom(), enchants);
    }

    private ModelCombined nullMixedModel() {
        return new ModelCombined(null, StatRequirements.zero(), ReforgeRules.common(), null);
    }

    private void rankSomething() throws IOException {
        ModelCombined model = standardRetModel();
//        ModelCombined model = standardProtModel();

        // assumes socket bonus+non matching gems
        Map<Integer, StatBlock> enchants = Map.of(
//                86145, new StatBlock(120+285, 0, 0, 165, 0, 640,0,0,0),
                86145, new StatBlock(285, 0, 0, 165, 0, 640,0,0,0, 0),
                84870, new StatBlock(0,430,0,0,0,640,0,165,0, 0));

        rankAlternativesAsSingleItems(model, new int [] {82856,86794}, enchants, false);
//        rankAlternatives(new int [] {81129,81234,82850,81571}); // cloak
//        rankAlternatives(new int [] {84036,81190,81687,81130,81086}); // belt
//        rankAlternativesAsSingleItems(model, new int[]{84027, 81284, 81073, 81113, 82852}); // feet
//        rankAlternativesAsSingleItems(model, new int[]{86145, 82812, 84870}, enchants, false); // legs
    }

    private void reforgeRet(Instant startTime) throws IOException {
        ModelCombined model = standardRetModel();
//        ModelCombined model = extendedRetModel();
        EnumMap<SlotEquip, ItemData[]> items = readAndLoad(true, gearRetFile, model.reforgeRules());

//        reforgeProcess(items, model, startTime, true);
//        reforgeProcessPlus(model, startTime, 89069, SlotEquip.Ring1, true);


//        reforgeProcessPlus(items, model, startTime, true, 89817, false, true, null);
//        reforgeProcessPlus(items, model, startTime, true,86145, false, true, new StatBlock(285+80+120,0,0,165,160,160+160,0,0,0));
//        reforgeProcessPlus(items, model, startTime, 82824, false);
//        reforgeProcessPlusPlus(model, startTime, 81251, 81694);
        reforgeProcessRetFixed(model, startTime, true);
//        reforgeProcessRetChallenge(model, startTime);

        //        Tuple.Tuple2<Integer, Integer>[] extraItemArray = valorItemsArray();
//        Tuple.Tuple2<Integer, Integer>[] extraItemArray = msvItemsArray();
//        Tuple.Tuple2<Integer, Integer>[] extraItemArray = pvpCrapArray();
//        findUpgradeSetup(items, strengthPlateMsvArray(), model);
//        combinationDumb(items, model, startTime);

    }

    private void reforgeProt(Instant startTime) throws IOException {
        ModelCombined model = standardProtModel();
        EnumMap<SlotEquip, ItemData[]> items = readAndLoad(true, gearProtFile, model.reforgeRules());

//        reforgeProcessProtFixed(model, startTime, true);
//        reforgeProcessPlus(items, model, startTime, true,86145, false, true, null);
//        reforgeProcessPlus(items, model, startTime, true,90594, false, true, null);
//        reforgeProcessPlusPlus(items, model, startTime, 89817, 86075);
//        reforgeProcess(items, model, startTime, true);
        findUpgradeSetup(items, strengthPlateCelestialArray(), model);

        // so we could get a conclusive result from the ret, then set the common slots to fixed
    }

    private void reforgeBoom(Instant startTime) throws IOException {
        ModelCombined model = standardBoomModel();
        EnumMap<SlotEquip, ItemData[]> items = readAndLoad(true, gearBoomFile, model.reforgeRules());

        reforgeProcess(items, model, startTime, true);
//        findUpgradeSetup(items, intellectLeatherValorArray(), model);
    }

    private void combinationDumb(EnumMap<SlotEquip, ItemData[]> items, ModelCombined model, Instant startTime) {
        for (int extraId : new int[]{89503, 81129, 89649, 87060, 89665, 82812, 90910, 81284, 82814, 84807, 84870, 84790, 82822}) {
            ItemData extraItem = addExtra(items, model, extraId, Function.identity(), false, true);
            System.out.println("EXTRA " + extraItem);
        }
        //        ItemUtil.disenchant(items);
        ItemUtil.defaultEnchants(items, model, true);
        ItemUtil.bestForgesOnly(items, model);
        ItemLevel.scaleForChallengeMode(items);

        ModelCombined dumbModel = model.withNoRequirements();

        Optional<ItemSet> bestSet = chooseEngineAndRun(dumbModel, items, startTime, null, null);
        outputResult(bestSet, model, true);
    }

    private void findUpgradeSetup(EnumMap<SlotEquip, ItemData[]> reforgedItems, Tuple.Tuple2<Integer, Integer>[] extraItemArray, ModelCombined model) throws IOException {
//        SlotEquip slot = SlotEquip.Ring2;

//        long runSize = 10000000; // quick runs
        long runSize = 60000000; // 2 min total runs
//        long runSize = 100000000; // 4 min total runs
//        long runSize = 300000000; // 12 min total runs
//        long runSize = 1000000000; // 40 min runs

//        ItemUtil.defaultEnchants(reforgedItems, model, true);

        ItemSet baseSet = reforgeProcessLight(reforgedItems, model, runSize, true).get();
        double baseRating = model.calcRating(baseSet);
        System.out.printf("BASE RATING    = %.0f\n", baseRating);

        for (Tuple.Tuple2<Integer, Integer> extraItemInfo : extraItemArray) {
            int extraItemId = extraItemInfo.a();
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            extraItem = ItemUtil.defaultEnchants(extraItem, model, true);
            SlotEquip slot = extraItem.slot.toSlotEquip();
            if (extraItemInfo.b() != null) {
                System.out.println(extraItem.toStringExtended() + " $" + extraItemInfo.b());
            } else {
                System.out.println(extraItem.toStringExtended());
            }

            Function<ItemData, ItemData> enchanting = x -> ItemUtil.defaultEnchants(x, model, false);
            Optional<ItemSet> extraSet = reforgeProcessPlusCore(ItemUtil.deepClone(reforgedItems), model, null, false, extraItemId, slot, enchanting, true, runSize);
            if (extraSet.isPresent()) {
                double extraRating = model.calcRating(extraSet.get());
                double factor = extraRating / baseRating;
                System.out.printf("UPGRADE RATING = %.0f FACTOR = %1.3f\n", extraRating, factor);
            } else {
                System.out.print("UPGRADE SET NOT FOUND\n");
            }
            System.out.println();
        }
    }

    private Tuple.Tuple2<Integer, Integer>[] strengthPlatePvpArray() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                Tuple.create(84807, 1250),
                Tuple.create(84794, 2250),
                Tuple.create(84806, 1250),
                Tuple.create(84810, 1750),
                Tuple.create(84822, 1750),
                Tuple.create(84834, 1750),
                Tuple.create(84851, 2250),
                Tuple.create(84870, 2250),
                Tuple.create(84915, 1750),
                Tuple.create(84949, 1750),
                Tuple.create(84950, 1750),
                Tuple.create(84986, 1250),
                Tuple.create(84891, 1250),
                Tuple.create(84985, 1250),
                Tuple.create(84892, 1250),
                Tuple.create(84828, 1250),
                Tuple.create(84829, 1250),
        };
    }

    private static Tuple.Tuple2<Integer, Integer>[] strengthPlateMsvArray() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                // stone
                Tuple.create(85922, 0),
                Tuple.create(85925, 0),
//                Tuple.create(86134, 0), got heroic
                // feng
//                Tuple.create(85983, 0), got norm
                Tuple.create(85984, 0),
                Tuple.create(85985, 0),
                // garaj
//                Tuple.create(85991, 0), got norm
                Tuple.create(85992, 0),
                Tuple.create(89817, 0),
                // kings
                Tuple.create(86075, 0),
                Tuple.create(86076, 0),
                Tuple.create(86080, 0),
                // elegon
                Tuple.create(86130, 0), // prot weapon
//                Tuple.create(86140, 0), // ret weapon
                Tuple.create(86135, 0), // starcrusher questionable
                // will
                Tuple.create(86144, 0),
                Tuple.create(86145, 0),
//                Tuple.create(89823, 0) // got norm
        };
    }

    private static Tuple.Tuple2<Integer, Integer>[] strengthPlateValorArray() {
        Tuple.Tuple2<Integer, Integer> neckParagonPale = Tuple.create(89066, 1250);
        Tuple.Tuple2<Integer, Integer> neckBloodseekers = Tuple.create(89064, 1250);
        Tuple.Tuple2<Integer, Integer> beltKlaxxiConsumer = Tuple.create(89056, 1750);
        Tuple.Tuple2<Integer, Integer> legKovokRiven = Tuple.create(89093, 2500);
        Tuple.Tuple2<Integer, Integer> backYiCloakCourage = Tuple.create(89075, 1250);
        Tuple.Tuple2<Integer, Integer> headYiLeastFavorite = Tuple.create(89216, 2500);
        Tuple.Tuple2<Integer, Integer> headVoiceAmpGreathelm = Tuple.create(89280, 2500);
        Tuple.Tuple2<Integer, Integer> chestDawnblade = Tuple.create(89420, 2500);
        Tuple.Tuple2<Integer, Integer> chestCuirassTwin = Tuple.create(89421, 2500);
        Tuple.Tuple2<Integer, Integer> gloveOverwhelmSwarm = Tuple.create(88746, 1750);
        Tuple.Tuple2<Integer, Integer> wristBattleShadow = Tuple.create(88880, 1250);
        Tuple.Tuple2<Integer, Integer> wristBraidedBlackWhite = Tuple.create(88879, 1250);
        Tuple.Tuple2<Integer, Integer> bootYulonGuardian = Tuple.create(88864, 1750);
        Tuple.Tuple2<Integer, Integer> bootTankissWarstomp = Tuple.create(88862, 1750);

        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{neckParagonPale, neckBloodseekers, beltKlaxxiConsumer, legKovokRiven, backYiCloakCourage, headYiLeastFavorite, headVoiceAmpGreathelm, chestDawnblade,
                chestCuirassTwin, gloveOverwhelmSwarm, wristBattleShadow, wristBraidedBlackWhite, bootYulonGuardian, bootTankissWarstomp};
    }

    private Tuple.Tuple2<Integer, Integer>[] intellectLeatherValorArray() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                Tuple.create(89308, 2500),
                Tuple.create(89342, 1750),
                Tuple.create(89432, 2500),
                Tuple.create(88885, 1250),
                Tuple.create(88743, 1750),
                Tuple.create(89061, 1750),
                Tuple.create(89089, 2500),
                Tuple.create(88876, 1750),
                Tuple.create(89078, 1250),
                Tuple.create(89067, 1250),
                Tuple.create(89073, 1250),
                Tuple.create(89072, 1250),
                Tuple.create(89081, 1750),
                Tuple.create(89080, 1750),
        };
    }

    private Tuple.Tuple2<Integer, Integer>[] strengthPlateCelestialArrayTankPicks() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                Tuple.create(86794, 25),
                Tuple.create(86742, 40),
                Tuple.create(86760, 40),
                Tuple.create(86789, 25),
                Tuple.create(86759, 25),
                Tuple.create(86753, 25),
        };
    }

    private Tuple.Tuple2<Integer, Integer>[] strengthPlateCelestialArrayRetPicks() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
//                Tuple.create(86780, 25),
                Tuple.create(89969, 25),
//                Tuple.create(86794, 40),
                Tuple.create(86742, 40),
                // also trinket lei shen's final orders
        };
    }

    private Tuple.Tuple2<Integer, Integer>[] strengthPlateCelestialArray() {
        // skipping neck cloak offhand weaps
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                // plate
                Tuple.create(86803, 40),
                Tuple.create(86742, 40),
                Tuple.create(89976, 40),
                Tuple.create(89969, 25),
                Tuple.create(86752, 40),
                Tuple.create(89954, 45),
                Tuple.create(86751, 25),
                Tuple.create(86823, 30),
                Tuple.create(86860, 30),
                Tuple.create(86793, 40),
                Tuple.create(86868, 30),
                Tuple.create(89981, 30),
                Tuple.create(86852, 45),
                Tuple.create(86832, 45),
                Tuple.create(86794, 25),
                Tuple.create(86780, 25),
                Tuple.create(86779, 40),
                Tuple.create(89958, 45),
                Tuple.create(89956, 30),
                Tuple.create(86822, 45),
                Tuple.create(86904, 50),
                Tuple.create(86854, 45),
                Tuple.create(86849, 30),
                Tuple.create(86760, 40),
                Tuple.create(86848, 30),
                Tuple.create(89963, 45),
                Tuple.create(86903, 25),
                Tuple.create(86870, 50),
                Tuple.create(86891, 50),

                // ring
                Tuple.create(86820, 30),
                Tuple.create(86830, 30),
                Tuple.create(89972, 25),
                Tuple.create(86813, 30),
                Tuple.create(86880, 30),

                // weap
                Tuple.create(86799, 40),
                Tuple.create(86906, 30),
                Tuple.create(86789, 25),

                // neck
                Tuple.create(86759, 25),
                Tuple.create(86872, 25),
                Tuple.create(86739, 25),
                Tuple.create(86871, 30),
                Tuple.create(86835, 30),

                // back
                Tuple.create(86753, 25),
                Tuple.create(86812, 30),
                Tuple.create(86883, 25),
                Tuple.create(86853, 25)
        };
    }

    private void rankAlternativesAsSingleItems(ModelCombined model, int[] itemIds, Map<Integer, StatBlock> enchants, boolean scaleChallenge) {
        Stream<ItemData> stream = Arrays.stream(itemIds)
                .mapToObj(x -> new EquippedItem(x, new int[0], null))
                .map(x -> ItemUtil.loadItem(itemCache, x, true))
                .flatMap(x -> Arrays.stream(Reforger.reforgeItem(model.reforgeRules(), x)));
        if (enchants != null) {
            stream = stream.map(x ->
                            enchants.containsKey(x.id) ?
                                    x.changeFixed(enchants.get(x.id)) :
                                    ItemUtil.defaultEnchants(x,model,true)
                    );
        }
        if (scaleChallenge) {
            stream = stream.map(ItemLevel::scaleForChallengeMode);
        }
        List<ItemData> reforgedItems = stream
                .sorted(Comparator.comparingLong(x -> model.calcRating(x.totalStatCopy())))
                .toList();
        for (ItemData item : reforgedItems) {
            System.out.println(item + " " + model.calcRating(item.totalStatCopy()));
        }
    }

    private void multiSpecSequential(Instant startTime) throws IOException {
        ModelCombined modelNull = nullMixedModel();
        ModelCombined modelRet = standardRetModel();
        ModelCombined modelProt = standardProtModel();

        System.out.println("RET GEAR CURRENT");
        EnumMap<SlotEquip, ItemData[]> retMap = readAndLoad(true, gearRetFile, modelRet.reforgeRules());
        System.out.println("PROT GEAR CURRENT");
        EnumMap<SlotEquip, ItemData[]> protMap = readAndLoad(true, gearProtFile, modelProt.reforgeRules());
        ItemUtil.validateDualSets(retMap, protMap);
        EnumMap<SlotEquip, ItemData[]> commonMap = ItemUtil.commonInDualSet(retMap, protMap);

        Stream<ItemSet> commonStream = EngineStream.runSolverPartial(modelNull, commonMap, startTime, null, 0);

        // TODO solve for challenge dps too

        Long runSize = BILLION/5;
        Stream<ItemSet> protStream = commonStream.map(r -> subSolveBoth(r, retMap, modelRet, protMap, modelProt, runSize))
                .filter(Objects::nonNull);

        Collection<ItemSet> best = protStream.collect(
                new TopCollectorReporting<>(s -> dualRating(s, modelRet, modelProt),
                        s -> reportBetter(s, modelRet, modelProt, retMap, protMap)));
        outputResult(best, modelProt, true);
    }

    private void reportBetter(ItemSet itemSet, ModelCombined modelRet, ModelCombined modelProt, EnumMap<SlotEquip, ItemData[]> itemsRet, EnumMap<SlotEquip, ItemData[]> itemsProt) {
        long rating = modelProt.calcRating(itemSet) + modelRet.calcRating(itemSet.otherSet);
        synchronized (System.out) {
            System.out.println(LocalDateTime.now());
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            itemSet.otherSet.outputSet(modelRet);
            ItemSet tweakRet = Tweaker.tweak(itemSet.otherSet, modelRet, itemsRet);
            if (tweakRet != itemSet.otherSet) {
                System.out.println("TWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAK");
                tweakRet.outputSet(modelRet);
            }
            System.out.println("--------------------------------------- " + rating);
            itemSet.outputSet(modelProt);
            ItemSet tweakProt = Tweaker.tweak(itemSet, modelProt, itemsProt);
            if (tweakProt != itemSet) {
                System.out.println("TWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAK");
                tweakProt.outputSet(modelProt);
            }
            System.out.println("#######################################");

        }
    }

    private void reportBetter(ItemSet itemSet, ModelCombined model) {
        long rating = model.calcRating(itemSet);
        System.out.println(LocalDateTime.now());
        System.out.println("#######################################");
        itemSet.outputSet(model);
    }

    private long dualRating(ItemSet set, ModelCombined modelRet, ModelCombined modelProt) {
        return modelRet.calcRating(set.otherSet) + modelProt.calcRating(set);
    }

    private ItemSet subSolveBoth(ItemSet chosenSet, EnumMap<SlotEquip, ItemData[]> retMap, ModelCombined modelRet, EnumMap<SlotEquip, ItemData[]> protMap, ModelCombined modelProt, Long runSize) {
        EnumMap<SlotEquip, ItemData> chosenMap = chosenSet.items;

//        System.out.println(chosenMap.values().stream().map(ItemData::toString).reduce("", String::concat));

        Optional<ItemSet> retSet = subSolvePart(retMap, modelRet, chosenMap, null, runSize);
        if (retSet.isPresent()) {
            Optional<ItemSet> protSet = subSolvePart(protMap, modelProt, chosenMap, retSet.get(), runSize);
            return protSet.orElse(null);
        }
        return null;
    }

    private static Optional<ItemSet> subSolvePart(EnumMap<SlotEquip, ItemData[]> fullItemMap, ModelCombined model, EnumMap<SlotEquip, ItemData> chosenMap, ItemSet otherSet, Long runSize) {
        EnumMap<SlotEquip, ItemData[]> submitMap = fullItemMap.clone();
        ItemUtil.buildJobWithSpecifiedItemsFixed(chosenMap, submitMap);
        return chooseEngineAndRun(model, submitMap, null, runSize, otherSet);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessProtFixed(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(gearProtFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        Map<SlotEquip, ReforgeRecipe> presetReforge = new EnumMap<>(SlotEquip.class);
//        presetReforge.put(SlotEquip.Head, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Neck, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
        //presetReforge.put(SlotEquip.Shoulder, new ReforgeRecipie(StatType.Crit, StatType.Haste));
//        presetReforge.put(SlotEquip.Back, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
//        presetReforge.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
        presetReforge.put(SlotEquip.Wrist, new ReforgeRecipe(StatType.Expertise, StatType.Hit));
        presetReforge.put(SlotEquip.Ring1, new ReforgeRecipe(null, null));
        presetReforge.put(SlotEquip.Ring2, new ReforgeRecipe(StatType.Crit, StatType.Haste));
//        presetReforge.put(SlotEquip.Trinket2, new ReforgeRecipe(StatType.Expertise, StatType.Mastery));
//        presetReforge.put(SlotEquip.Weapon, new ReforgeRecipie(null, null));
//        presetReforge.put(SlotEquip.Offhand, new ReforgeRecipie(StatType.Parry, StatType.Hit));

        EnumMap<SlotEquip, ItemData[]> map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);
//        EnumMap<SlotEquip, ItemData[]> map = ItemUtil.standardItemsReforgedToMap(model.getReforgeRules(), items);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, startTime, null, null);
        outputResult(bestSet, model, detailedOutput);
        outputTweaked(bestSet, map, model);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessRetFixed(ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(gearRetFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);

        Map<SlotEquip, ReforgeRecipe> presetReforge = new EnumMap<>(SlotEquip.class);
//        presetReforge.put(SlotEquip.Head, new ReforgeRecipe(null, null));
//        presetReforge.put(SlotEquip.Neck, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
        //presetReforge.put(SlotEquip.Shoulder, new ReforgeRecipie(StatType.Crit, StatType.Haste));
//        presetReforge.put(SlotEquip.Back, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
//        presetReforge.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
//        presetReforge.put(SlotEquip.Wrist, new ReforgeRecipie(StatType.Dodge, StatType.Mastery));
        presetReforge.put(SlotEquip.Wrist, new ReforgeRecipe(StatType.Expertise, StatType.Hit));
        presetReforge.put(SlotEquip.Ring1, new ReforgeRecipe(null, null));
        presetReforge.put(SlotEquip.Ring2, new ReforgeRecipe(StatType.Crit, StatType.Haste));
//        presetReforge.put(SlotEquip.Trinket2, new ReforgeRecipe(StatType.Expertise, StatType.Mastery));
//        presetReforge.put(SlotEquip.Weapon, new ReforgeRecipie(null, null));
//        presetReforge.put(SlotEquip.Offhand, new ReforgeRecipie(StatType.Parry, StatType.Hit));

        EnumMap<SlotEquip, ItemData[]> map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), items, presetReforge);

        Optional<ItemSet> bestSet = chooseEngineAndRun(model, map, null, null, null);

        outputResult(bestSet, model, detailedOutput);
        outputTweaked(bestSet, map, model);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessRetChallenge(ModelCombined model, Instant startTime) throws IOException {
        // CHALLENGE MODE SET

        List<EquippedItem> itemIds = InputParser.readInput(gearRetFile);
        List<ItemData> inputSetItems = ItemUtil.loadItems(itemCache, itemIds, true);

        Map<SlotEquip, ReforgeRecipe> presetReforge = new EnumMap<>(SlotEquip.class);
        presetReforge.put(SlotEquip.Head, new ReforgeRecipe(null, null));
        presetReforge.put(SlotEquip.Neck, new ReforgeRecipe(StatType.Hit, StatType.Expertise));
        presetReforge.put(SlotEquip.Shoulder, new ReforgeRecipe(StatType.Crit, StatType.Haste));
        presetReforge.put(SlotEquip.Back, new ReforgeRecipe(StatType.Crit, StatType.Hit));
        presetReforge.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, StatType.Haste));
        presetReforge.put(SlotEquip.Wrist, new ReforgeRecipe(StatType.Hit, StatType.Haste));
        presetReforge.put(SlotEquip.Hand, new ReforgeRecipe(null, null));
        presetReforge.put(SlotEquip.Belt, new ReforgeRecipe(StatType.Crit, StatType.Haste));
        presetReforge.put(SlotEquip.Leg, new ReforgeRecipe(StatType.Hit, StatType.Expertise));
        presetReforge.put(SlotEquip.Foot, new ReforgeRecipe(null, null));
        presetReforge.put(SlotEquip.Ring1, new ReforgeRecipe(null, null));
        presetReforge.put(SlotEquip.Ring2, new ReforgeRecipe(StatType.Crit, StatType.Haste));
        presetReforge.put(SlotEquip.Trinket1, new ReforgeRecipe(StatType.Expertise, StatType.Haste));
        presetReforge.put(SlotEquip.Trinket2, new ReforgeRecipe(null, null));
        presetReforge.put(SlotEquip.Weapon, new ReforgeRecipe(StatType.Crit, StatType.Haste));

        EnumMap<SlotEquip, ItemData[]> map = ItemUtil.limitedItemsReforgedToMap(model.reforgeRules(), inputSetItems, presetReforge);

        // compared to raid dps
        int[] extraItems = new int[]{89503, 81129, 89649, 87060, 89665, 82812, 82814, 90910, 81284, 82822};

        Function<ItemData, ItemData> customiseItem = extraItem -> {
            if (extraItem.id == 82812) {
                return extraItem.changeFixed(new StatBlock(285, 0, 0, 165, 160, 160 + 60, 0, 0, 0, 0));
            } else if (extraItem.id == 89649) {
                return extraItem.changeFixed(new StatBlock(0, 0, 0, 0, 0, 320, 0, 0, 0, 0));
            } else if (extraItem.id == 81284) {
                return extraItem.changeFixed(new StatBlock(60 + 60, 0, 140, 0, 0, 120, 0, 0, 0, 0));
            } else if (extraItem.id == 87060) {
                return extraItem.changeFixed(new StatBlock(0, 0, 0, 0, 160, 160, 320 + 160 + 160, 120, 0, 0));
            } else if (extraItem.id == 89503) {
                return extraItem.changeStats(new StatBlock(501,751,0,334,334,0,0,0,0, 0))
                        .changeFixed(new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));
            } else if (extraItem.slot == SlotItem.Back) {
                return extraItem.changeFixed(new StatBlock(0, 0, 0, 180, 0, 0, 0, 0, 0, 0));
            } else {
                System.out.println("DEFAULT ENCHANT " + extraItem);
                return ItemUtil.defaultEnchants(extraItem, model, false);
            }
        };

        for (int extraId : extraItems) {
            ItemData extraItem = addExtra(map, model, extraId, customiseItem, false, true);
            System.out.println("EXTRA " + extraItem);
        }
        EnumMap<SlotEquip, ItemData[]> scaledMap = ItemLevel.scaleForChallengeMode(map);

        ItemSet bestScaledSet = chooseEngineAndRun(model, scaledMap, startTime, null, null).orElseThrow();

        System.out.println("SCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALEDSCALED");
        outputResult(Optional.of(bestScaledSet), model, true);

        for (SlotEquip slot : SlotEquip.values()) {
            ItemData scaledChoice = bestScaledSet.items.get(slot);
            if (scaledChoice != null) {
                ItemData[] options = map.get(slot);
                boolean inRaidDPSSet = inputSetItems.stream().anyMatch(x -> x.id == scaledChoice.id);

                if (inRaidDPSSet) {
                    // need exact item + forge but prescale
                    // note were using id match only, scaled stuff could confused normal "exact" match
                    // avoid engineering heads mixup
                    ItemData match = ArrayUtil.findOne(options, x -> x.id == scaledChoice.id && Objects.equals(x.reforge, scaledChoice.reforge));
                    options = new ItemData[]{match};
                } else {
                    options = ArrayUtil.allMatch(options, x -> x.id == scaledChoice.id);
                }

                map.put(slot, options);
            }
        }

        ModelCombined finalModel = new ModelCombined(model.statRatings(), StatRequirements.retWideCapRange(), model.reforgeRules(), model.enchants());
        Optional<ItemSet> bestSetFinal = chooseEngineAndRun(finalModel, map, startTime, null, null);

        System.out.println("FINALFINALFINALFINALFINALFINALFINALFINALFINALFINALFINAL");
        outputResult(bestSetFinal, model, true);
    }

    private void multiSpecSpecifiedRating() throws IOException {
        ModelCombined modelRet = standardRetModel();
        ModelCombined modelProt = standardProtModel();

        System.out.println("RET GEAR CURRENT");
        List<ItemData> retItems = ItemUtil.loadItems(itemCache, InputParser.readInput(gearRetFile), true);
        System.out.println("PROT GEAR CURRENT");
        List<ItemData> protItems = ItemUtil.loadItems(itemCache, InputParser.readInput(gearProtFile), true);

        Map<SlotEquip, ReforgeRecipe> reforgeRet = new EnumMap<>(SlotEquip.class);
        reforgeRet.put(SlotEquip.Head, new ReforgeRecipe(null, null));
        reforgeRet.put(SlotEquip.Neck, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
        reforgeRet.put(SlotEquip.Shoulder, new ReforgeRecipe(StatType.Expertise, StatType.Haste));
        reforgeRet.put(SlotEquip.Back, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
        reforgeRet.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
        reforgeRet.put(SlotEquip.Wrist, new ReforgeRecipe(StatType.Hit, StatType.Haste));
        reforgeRet.put(SlotEquip.Hand, new ReforgeRecipe(StatType.Crit, StatType.Hit));
        reforgeRet.put(SlotEquip.Belt, new ReforgeRecipe(StatType.Mastery, StatType.Expertise));
        reforgeRet.put(SlotEquip.Leg, new ReforgeRecipe(StatType.Crit, StatType.Mastery));
        reforgeRet.put(SlotEquip.Foot, new ReforgeRecipe(StatType.Mastery, StatType.Expertise));
        reforgeRet.put(SlotEquip.Ring1, new ReforgeRecipe(StatType.Crit, StatType.Haste));
        reforgeRet.put(SlotEquip.Ring2, new ReforgeRecipe(StatType.Crit, StatType.Mastery));
        reforgeRet.put(SlotEquip.Trinket1, new ReforgeRecipe(null, null));
        reforgeRet.put(SlotEquip.Trinket2, new ReforgeRecipe(StatType.Expertise, StatType.Mastery));
        reforgeRet.put(SlotEquip.Weapon, new ReforgeRecipe(StatType.Hit, StatType.Haste));

        Map<SlotEquip, ReforgeRecipe> reforgeProt = new EnumMap<>(SlotEquip.class);
        reforgeProt.put(SlotEquip.Head, new ReforgeRecipe(null, null));
        reforgeProt.put(SlotEquip.Neck, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
        reforgeProt.put(SlotEquip.Shoulder, new ReforgeRecipe(StatType.Dodge, StatType.Hit));
        reforgeProt.put(SlotEquip.Back, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
        reforgeProt.put(SlotEquip.Chest, new ReforgeRecipe(StatType.Crit, StatType.Expertise));
        reforgeProt.put(SlotEquip.Wrist, new ReforgeRecipe(StatType.Dodge, StatType.Mastery));
        reforgeProt.put(SlotEquip.Hand, new ReforgeRecipe(StatType.Parry, StatType.Hit));
        reforgeProt.put(SlotEquip.Belt, new ReforgeRecipe(StatType.Dodge, StatType.Hit));
        reforgeProt.put(SlotEquip.Leg, new ReforgeRecipe(StatType.Dodge, StatType.Mastery));
        reforgeProt.put(SlotEquip.Foot, new ReforgeRecipe(StatType.Parry, StatType.Expertise));
        reforgeProt.put(SlotEquip.Ring1, new ReforgeRecipe(StatType.Parry, StatType.Expertise));
        reforgeProt.put(SlotEquip.Ring2, new ReforgeRecipe(StatType.Crit, StatType.Mastery));
        reforgeProt.put(SlotEquip.Trinket1, new ReforgeRecipe(null, null));
        reforgeProt.put(SlotEquip.Trinket2, new ReforgeRecipe(StatType.Expertise, StatType.Mastery));
        reforgeProt.put(SlotEquip.Weapon, new ReforgeRecipe(null, null));
        reforgeProt.put(SlotEquip.Offhand, new ReforgeRecipe(StatType.Parry, StatType.Hit));

        EnumMap<SlotEquip, ItemData> retForgedItems = ItemUtil.chosenItemsReforgedToMap(retItems, reforgeRet);
        ItemSet retSet = ItemSet.manyItems(retForgedItems, null);

        EnumMap<SlotEquip, ItemData> protForgedItems = ItemUtil.chosenItemsReforgedToMap(protItems, reforgeProt);
        ItemSet protSet = ItemSet.manyItems(protForgedItems, null);

        retSet.outputSet(modelRet);
        System.out.println("---------------------" + (modelRet.calcRating(retSet) + modelProt.calcRating(protSet)));
        protSet.outputSet(modelProt);
    }

    private EnumMap<SlotEquip, ItemData[]> readAndLoad(boolean detailedOutput, Path file, ReforgeRules rules) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(file);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);
        EnumMap<SlotEquip, ItemData[]> result = ItemUtil.standardItemsReforgedToMap(rules, items);
        itemCache.cacheSave();
        return result;
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcess(EnumMap<SlotEquip, ItemData[]> reforgedItems, ModelCombined model, Instant startTime, boolean detailedOutput) throws IOException {
        Optional<ItemSet> bestSet = chooseEngineAndRun(model, reforgedItems, startTime, BILLION, null);
        outputResult(bestSet, model, detailedOutput);
        outputTweaked(bestSet, reforgedItems, model);
    }

    private Optional<ItemSet> reforgeProcessLight(EnumMap<SlotEquip, ItemData[]> reforgedItems, ModelCombined model, long runSize, boolean outputExistingGear) throws IOException {
        Optional<ItemSet> bestSet = chooseEngineAndRun(model, reforgedItems, null, runSize, null);
        return bestSet.map(itemSet -> Tweaker.tweak(itemSet, model, reforgedItems));
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlus(EnumMap<SlotEquip, ItemData[]> reforgedItems, ModelCombined model, Instant startTime, boolean detailedOutput, int extraItemId, boolean replace, boolean defaultEnchants, StatBlock extraItemEnchants) throws IOException {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        Function<ItemData, ItemData> enchanting =
                extraItemEnchants != null ? x -> x.changeFixed(extraItemEnchants) :
                defaultEnchants ? x -> ItemUtil.defaultEnchants(x, model, true) :
                        Function.identity();
        Optional<ItemSet> bestSet = reforgeProcessPlusCore(reforgedItems, model, startTime, detailedOutput, extraItemId, extraItem.slot.toSlotEquip(), enchanting, replace, null);
        outputResult(bestSet, model, detailedOutput);
    }

    private Optional<ItemSet> reforgeProcessPlusCore(EnumMap<SlotEquip, ItemData[]> reforgedItems, ModelCombined model, Instant startTime, boolean detailedOutput, int extraItemId, SlotEquip slot, Function<ItemData, ItemData> enchanting, boolean replace, Long runSize) throws IOException {
        EnumMap<SlotEquip, ItemData[]> runItems = reforgedItems.clone();
        ItemData extraItem = addExtra(runItems, model, extraItemId, slot, enchanting, replace, true);
        ArrayUtil.mapInPlace(runItems.get(slot), enchanting);

        if (detailedOutput) {
            System.out.println("EXTRA " + extraItem);
        }

        return chooseEngineAndRun(model, runItems, startTime, runSize, null);
    }

    private ItemData addExtra(EnumMap<SlotEquip, ItemData[]> reforgedItems, ModelCombined model, int extraItemId, Function<ItemData, ItemData> customiseItem, boolean replace, boolean customiseOthersInSlot) {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        return addExtra(reforgedItems, model, extraItemId, extraItem.slot.toSlotEquip(), customiseItem, replace, customiseOthersInSlot);
    }

    private ItemData addExtra(EnumMap<SlotEquip, ItemData[]> reforgedItems, ModelCombined model, int extraItemId, SlotEquip slot, Function<ItemData, ItemData> customiseItem, boolean replace, boolean customiseOthersInSlot) {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        extraItem = customiseItem.apply(extraItem);
        ItemData[] extraForged = Reforger.reforgeItem(model.reforgeRules(), extraItem);
        if (replace) {
            System.out.println("REPLACING " + (reforgedItems.get(slot) != null ? reforgedItems.get(slot)[0] : "NOTHING"));
            reforgedItems.put(slot, extraForged);
        } else {
            ItemData[] existing = reforgedItems.get(slot);
            if (ArrayUtil.anyMatch(existing, item -> item.id == extraItemId))
                throw new IllegalArgumentException("item already included " + extraItemId + " " + extraItem);
            reforgedItems.put(slot, ArrayUtil.concat(existing, extraForged));
        }
        ItemData[] slotArray = reforgedItems.get(slot);
        if (customiseOthersInSlot) {
            ArrayUtil.mapInPlace(slotArray, customiseItem);
        }
        ArrayUtil.forEach(slotArray, x -> { if (x.reforge == null) System.out.println("NEW " + slot + " " + x);});
        return extraItem;
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeAlternatives(Path file, ModelCombined model, Instant startTime, int[] alternateItems) throws IOException {
        EnumMap<SlotEquip, ItemData[]> reforgedItems = readAndLoad(false, file, model.reforgeRules());

        for (int extraItemId : alternateItems) {
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            Map<SlotEquip, ItemData[]> itemMap = new EnumMap<>(reforgedItems);
            itemMap.put(extraItem.slot.toSlotEquip(), new ItemData[]{extraItem});
            Optional<ItemSet> bestSets = EngineStream.runSolver(model, itemMap, null, null, 0);
            outputResult(bestSets, model, false);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlusPlus(EnumMap<SlotEquip, ItemData[]> reforgedItems, ModelCombined model, Instant startTime, int extraItemId1, int extraItemId2) throws IOException {
        Function<ItemData, ItemData> enchant = x -> ItemUtil.defaultEnchants(x, model, true);

        ItemData extraItem1 = addExtra(reforgedItems, model, extraItemId1, enchant, false, true);
        System.out.println("EXTRA " + extraItem1);

        ItemData extraItem2 = addExtra(reforgedItems, model, extraItemId2, enchant, false, true);
        System.out.println("EXTRA " + extraItem2);

        Optional<ItemSet> best = chooseEngineAndRun(model, reforgedItems, startTime, BILLION * 3, null);
        outputResult(best, model, true);
    }

    private static Optional<ItemSet> chooseEngineAndRun(ModelCombined model, EnumMap<SlotEquip, ItemData[]> reforgedItems, Instant startTime, Long runSize, ItemSet otherSet) {
        long estimate = ItemUtil.estimateSets(reforgedItems);
        if (runSize != null && estimate > runSize) {
            Optional<ItemSet> proposed = EngineRandom.runSolver(model, reforgedItems, startTime, otherSet, runSize);
            return proposed.map(itemSet -> Tweaker.tweak(itemSet, model, reforgedItems));
        } else {
            return EngineStream.runSolver(model, reforgedItems, startTime, otherSet, estimate);
        }
    }

    private void outputResult(Collection<ItemSet> bestSets, ModelCombined model, boolean detailedOutput) {
        if (detailedOutput) {
            System.out.println("@@@@@@@@@ Set count " + bestSets.size() + " @@@@@@@@@");
            bestSets.forEach(s -> System.out.println(s.getTotals()));
            bestSets.forEach(s -> {
                System.out.println("#######################################");
                s.outputSet(model);
            });
        } else {
            Optional<ItemSet> last = bestSets.stream().reduce((a, b) -> b);
            last.orElseThrow().outputSet(model);
        }
    }

    private void outputResult(Optional<ItemSet> bestSet, ModelCombined model, boolean detailedOutput) {
        if (bestSet.isPresent()) {
            bestSet.get().outputSet(model);
        } else {
            System.out.println("@@@@@@@@@ NO VALID SET RESULTS @@@@@@@@@");
        }
    }

    private void outputTweaked(Optional<ItemSet> bestSet, EnumMap<SlotEquip, ItemData[]> reforgedItems, ModelCombined model) {
        if (bestSet.isPresent()) {
            ItemSet tweakSet = Tweaker.tweak(bestSet.get(), model, reforgedItems);
            if (bestSet.get() != tweakSet) {
                System.out.println("TWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAKTWEAK");

                System.out.println(tweakSet.getTotals().toStringExtended() + " " + model.calcRating(tweakSet.getTotals()));
                for (Map.Entry<SlotEquip, ItemData> entry : tweakSet.getItems().entrySet()) {
                    ItemData orig = bestSet.get().items.get(entry.getKey());
                    ItemData change = entry.getValue();
                    if (!ItemData.isIdenticalItem(orig, change)) {
                        System.out.println(change + " " + model.calcRating(change.totalStatCopy()));
                    }
                }
            }
        }
    }

    private void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        System.out.println("elapsed = " + duration.toString());
    }
}
