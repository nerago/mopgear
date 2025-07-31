package au.nicholas.hardy.mopgear;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Main {

    private static final Path directory = Path.of("C:\\Users\\nicholas\\Dropbox\\prog\\paladin_gearing");
    static final Path cacheFile = directory.resolve("cache.json");
    private static final Path inputFile = directory.resolve("input.json");
    private static final Path weightFileMine = directory.resolve("weight-mysim.json");
    private static final Path weightFileStandard = directory.resolve("weight-standard.json");

    ItemCache itemCache;
    Model model;

    public static void main(String[] arg) throws IOException {
        new Main().run();
    }

    private void run() throws IOException {
        itemCache = new ItemCache(cacheFile);
        ModelCommon.validate();

        Instant startTime = Instant.now();

        reforgeSomething(startTime);
//        rankSomething();

        printElapsed(startTime);

        itemCache.cacheSave();
    }

    private void rankSomething() throws IOException {
//        model = new ModelWeights(weightFileMine, true);
        model = new ModelWeights(weightFileStandard, true);

//        rankAlternatives(new int [] {89530,81239,81567,81180,81568}); // necks
//        rankAlternatives(new int [] {81129,81234,82850,81571}); // cloak
//        rankAlternatives(new int [] {84036,81190,81687,81130,81086}); // belt
        rankAlternatives(new int [] {84027,81284,81073,81113,82852}); // feet
    }

    private void reforgeSomething(Instant startTime) throws IOException {
//        model = new ModelPriority();
        model = new ModelWeights(weightFileMine, false);
        reforgeProcess(startTime, false);
//        reforgeProcessPlus(startTime, 89069, SlotEquip.Ring1, true);
//        reforgeProcessPlus(startTime, 89345, true); // shoulder
//        reforgeProcessPlus(startTime, 89064, true);
//        reforgeProcessPlusPlus(startTime, 81251, 81694);
//        reforgeAlternatives(startTime, new int [] {89345, 88879, 88747, 89055, 89064, 89066, 89075, 89074});
    }

    private void rankAlternatives(int[] itemIds) {
        List<ItemData> reforgedItems = Arrays.stream(itemIds)
                .mapToObj(x -> new EquippedItem(x, null, new int [0]))
                .map(x -> ItemUtil.loadItem(itemCache, x, true))
                .flatMap(x -> Reforge.reforgeItem(x).stream())
                .sorted(Comparator.comparingLong(x -> model.calcRating(x.totalStatCopy())))
                .toList();
        for (ItemData item : reforgedItems) {
            System.out.println(item + " " + model.calcRating(item.totalStatCopy()));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcess(Instant startTime, boolean detailedOutput) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(inputFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, detailedOutput);
        Map<SlotEquip, List<ItemData>> reforgedItems = ItemUtil.standardItemsToMap(items);
//        Collection<ItemSet> bestSets = new EngineStack(reforgedItems).runSolver();
        Collection<ItemSet> bestSets = EngineStream.runSolver(model, reforgedItems, startTime);
        outputResult(bestSets, detailedOutput);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlus(Instant startTime, int extraItemId, boolean replace) throws IOException {
        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        reforgeProcessPlus(startTime, extraItemId, extraItem.slot.toSlotEquip(), replace);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlus(Instant startTime, int extraItemId, SlotEquip slot, boolean replace) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(inputFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, true);
        Map<SlotEquip, List<ItemData>> reforgedItems = ItemUtil.standardItemsToMap(items);

        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        if (replace)
            reforgedItems.get(slot).clear();
        reforgedItems.get(slot).addAll(Reforge.reforgeItem(extraItem));
        System.out.println("EXTRA " + extraItem);

        Collection<ItemSet> bestSets = EngineStream.runSolver(model, reforgedItems, startTime);
        outputResult(bestSets, true);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeAlternatives(Instant startTime, int[] alternateItems) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(inputFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, true);
        Map<SlotEquip, List<ItemData>> reforgedItems = ItemUtil.standardItemsToMap(items);

        for (int extraItemId: alternateItems) {
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            Map<SlotEquip, List<ItemData>> itemMap = new EnumMap<>(reforgedItems);
            itemMap.put(extraItem.slot.toSlotEquip(), Collections.singletonList(extraItem));
            Collection<ItemSet> bestSets = EngineStream.runSolver(model, itemMap, null);
            outputResult(bestSets, false);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlusPlus(Instant startTime, int extraItemId1, int extraItemId2) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(inputFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds, true);
        Map<SlotEquip, List<ItemData>> reforgedItems = ItemUtil.standardItemsToMap(items);

        ItemData extraItem1 = ItemUtil.loadItemBasic(itemCache, extraItemId1);
        reforgedItems.get(extraItem1.slot.toSlotEquip()).addAll(Reforge.reforgeItem(extraItem1));
        System.out.println("EXTRA " + extraItem1);

        ItemData extraItem2 = ItemUtil.loadItemBasic(itemCache, extraItemId2);
        reforgedItems.get(extraItem2.slot.toSlotEquip()).addAll(Reforge.reforgeItem(extraItem2));
        System.out.println("EXTRA " + extraItem2);

        Collection<ItemSet> bestSets = EngineStream.runSolver(model, reforgedItems, startTime);
        outputResult(bestSets, true);
    }

//    private void findUpgrade(Instant startTime) throws IOException {
//        List<EquippedItem> baseItemIds = Main.readInput();
//        List<ItemData> baseItems = loadItems(baseItemIds);
//        Collection<ItemSet> bestSets = Engine.runSolver(items, startTime);
//        bestSets.forEach(s -> System.out.println(s.totals));
//        bestSets.forEach(s -> {
//            System.out.println("#######################################");
//            System.out.println(s.totals);
//            for (ItemData it : s.items.toArrayReverse(ItemData[]::new)) {
//                System.out.println(it);
//            }
//        });
//    }

    private void outputResult(Collection<ItemSet> bestSets, boolean detailedOutput) {
        if (detailedOutput) {
            System.out.println("@@@@@@@@@ Set count " + bestSets.size() + " @@@@@@@@@");
            bestSets.forEach(s -> System.out.println(s.getTotals()));
            bestSets.forEach(s -> {
                System.out.println("#######################################");
                outputSet(s);
            });
        } else {
            Optional<ItemSet> last = bestSets.stream().reduce((a, b) -> b);;
            outputSet(last.orElseThrow());
        }
    }

    private void outputSet(ItemSet s) {
        System.out.println(s.getTotals() + " " + model.calcRating(s.getTotals()));
        for (ItemData it : s.getItems().toArrayReverse(ItemData[]::new)) {
            System.out.println(it + " " + model.calcRating(it.totalStatCopy()));
        }
    }

    private void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        System.out.println("elapsed = " + duration.toString());
    }
}
