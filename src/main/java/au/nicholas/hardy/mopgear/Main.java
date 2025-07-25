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

    ItemCache itemCache;

    public static void main(String[] arg) throws IOException {
        new Main().run();
    }

    private void run() throws IOException {
        itemCache = new ItemCache(cacheFile);
        ModelParams.validate();

        Instant startTime = Instant.now();

//        reforgeProcess(startTime);
        reforgeProcessPlus(startTime, 84036);
//        WowHead.fetchItem(81687);

        printElapsed(startTime);

        itemCache.cacheSave();
    }



    private void reforgeProcess(Instant startTime) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(inputFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds);
        Map<SlotEquip, List<ItemData>> reforgedItems = ItemUtil.standardItemsToMap(items);
//        Collection<ItemSet> bestSets = new EngineStack(reforgedItems).runSolver();
        Collection<ItemSet> bestSets = EngineStream.runSolver(reforgedItems, startTime);
        outputResult(bestSets);
    }

    private void reforgeProcessPlus(Instant startTime, int extraItemId) throws IOException {
        List<EquippedItem> itemIds = InputParser.readInput(inputFile);
        List<ItemData> items = ItemUtil.loadItems(itemCache, itemIds);
        Map<SlotEquip, List<ItemData>> reforgedItems = ItemUtil.standardItemsToMap(items);

        ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
        reforgedItems.get(extraItem.slot.toSlotEquip()).clear(); // replace
        reforgedItems.get(extraItem.slot.toSlotEquip()).addAll(Reforge.reforgeItem(extraItem));
        System.out.println("EXTRA " + extraItem);

        Collection<ItemSet> bestSets = EngineStream.runSolver(reforgedItems, startTime);
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

    private static void outputResult(Collection<ItemSet> bestSets) {
        System.out.println("@@@@@@@@@ Set count " + bestSets.size() + " @@@@@@@@@");
        bestSets.forEach(s -> System.out.println(s.getTotals()));
        bestSets.forEach(s -> {
            System.out.println("#######################################");
            System.out.println(s.getTotals());
            for (ItemData it : s.getItems().toArrayReverse(ItemData[]::new)) {
                System.out.println(it);
            }
        });
    }

    private void printElapsed(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        System.out.println("elapsed = " + duration.toString());
    }
}
