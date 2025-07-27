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
    private static final Path weightFile = directory.resolve("weight.json");

    ItemCache itemCache;
    Model model;

    public static void main(String[] arg) throws IOException {
        new Main().run();
    }

    private void run() throws IOException {
        itemCache = new ItemCache(cacheFile);
        ModelCommon.validate();
        model = new ModelWeights(weightFile);
//        model = new ModelPriority();

        Instant startTime = Instant.now();

//        reforgeProcess(startTime);
        reforgeProcessPlus(startTime, 81694, false);
//        WowHead.fetchItem(81687);
//        rankAlternatives(new int [] {89649,81694});

        printElapsed(startTime);

        itemCache.cacheSave();
    }

    private void rankAlternatives(int[] itemIds) {
        List<ItemData> reforgedItems = Arrays.stream(itemIds)
                .mapToObj(x -> new EquippedItem(x, null, new int [0]))
                .map(x -> ItemUtil.loadItem(itemCache, x))
                .flatMap(x -> Reforge.reforgeItem(x).stream())
                .sorted(Comparator.comparingLong(x -> model.calcRating(x.totalStatCopy())))
                .toList();
        for (ItemData item : reforgedItems) {
            System.out.println(item + " " + model.calcRating(item.totalStatCopy()));
        }
    }

    private void reforgeProcess(Instant startTime) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(inputFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds);
        Map<SlotEquip, List<ItemData>> reforgedItems = ItemUtil.standardItemsToMap(items);
//        Collection<ItemSet> bestSets = new EngineStack(reforgedItems).runSolver();
        Collection<ItemSet> bestSets = EngineStream.runSolver(model, reforgedItems, startTime);
        outputResult(bestSets);
    }

    @SuppressWarnings("SameParameterValue")
    private void reforgeProcessPlus(Instant startTime, int extraItemId, boolean replace) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(inputFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds);
        Map<SlotEquip, List<ItemData>> reforgedItems = ItemUtil.standardItemsToMap(items);

        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        if (replace)
            reforgedItems.get(extraItem.slot.toSlotEquip()).clear();
        reforgedItems.get(extraItem.slot.toSlotEquip()).addAll(Reforge.reforgeItem(extraItem));
        System.out.println("EXTRA " + extraItem);

        Collection<ItemSet> bestSets = EngineStream.runSolver(model, reforgedItems, startTime);
        outputResult(bestSets);
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

    private void outputResult(Collection<ItemSet> bestSets) {
        System.out.println("@@@@@@@@@ Set count " + bestSets.size() + " @@@@@@@@@");
        bestSets.forEach(s -> System.out.println(s.getTotals()));
        bestSets.forEach(s -> {
            System.out.println("#######################################");
            System.out.println(s.getTotals() + " " + model.calcRating(s.getTotals()));
            for (ItemData it : s.getItems().toArrayReverse(ItemData[]::new)) {
                System.out.println(it + " " + model.calcRating(it.totalStatCopy()));
            }
        });
    }

    private void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        System.out.println("elapsed = " + duration.toString());
    }
}
