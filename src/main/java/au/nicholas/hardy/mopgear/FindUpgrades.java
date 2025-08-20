package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.io.ItemCache;
import au.nicholas.hardy.mopgear.io.SourcesOfItems;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.util.ArrayUtil;
import au.nicholas.hardy.mopgear.util.BestCollection;
import au.nicholas.hardy.mopgear.util.Tuple;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings({"SameParameterValue", "unused", "OptionalUsedAsFieldOrParameterType"})
public class FindUpgrades {
    private ItemCache itemCache;

//    private static final long runSize = 10000000; // quick runs
    private static final long runSize = 50000000; // 2 min total runs
//    private static final long runSize = 100000000; // 4 min total runs
//    private static final long runSize = 300000000; // 12 min total runs
//    private static final long runSize = 1000000000; // 40 min runs

    public FindUpgrades(ItemCache itemCache) {
        this.itemCache = itemCache;
    }

    public void findUpgradeSetup(ModelCombined model, EquipOptionsMap baseItems, Tuple.Tuple2<Integer, Integer>[] extraItemArray) {
        ItemSet baseSet = EngineUtil.chooseEngineAndRun(model, baseItems, null, runSize, null).orElseThrow();
        double baseRating = model.calcRating(baseSet);
        System.out.printf("BASE RATING    = %.0f\n", baseRating);

        Function<ItemData, ItemData> enchanting = x -> ItemUtil.defaultEnchants(x, model, true);

        BestCollection<ItemData> bestCollection = new BestCollection<>();
        for (Tuple.Tuple2<Integer, Integer> extraItemInfo : extraItemArray) {
            int extraItemId = extraItemInfo.a();
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            SlotEquip slot = extraItem.slot.toSlotEquip();

            if (canSkipUpgradeCheck(extraItem, slot, baseItems))
                continue;

            if (extraItemInfo.b() != null) {
                System.out.println(extraItem.toStringExtended() + " $" + extraItemInfo.b());
            } else {
                System.out.println(extraItem.toStringExtended());
            }

            Optional<ItemSet> resultDirect = reforgeProcessPlusCore(baseItems.deepClone(), model, extraItem, slot, enchanting);
            reportUpgrade(model, extraItem, baseRating, bestCollection, resultDirect);

            if (slot == SlotEquip.Trinket1) {
                Optional<ItemSet> resultTrinket = reforgeProcessPlusCore(baseItems.deepClone(), model, extraItem, SlotEquip.Trinket2, enchanting);
                reportUpgrade(model, extraItem, baseRating, bestCollection, resultTrinket);
            }
            if (slot == SlotEquip.Ring1) {
                Optional<ItemSet> resultRing = reforgeProcessPlusCore(baseItems.deepClone(), model, extraItem, SlotEquip.Ring2, enchanting);
                reportUpgrade(model, extraItem, baseRating, bestCollection, resultRing);
            }
        }

        System.out.println("RANKING RANKING");
        bestCollection.forEach((item, factor) ->
                System.out.printf("%10s \t%35s \t$%d \t%1.3f\n", item.slot, item.name,
                        ArrayUtil.findOne(extraItemArray, x -> x.a() == item.id).b(),
                        factor));
    }

    private boolean canSkipUpgradeCheck(ItemData extraItem, SlotEquip slot, EquipOptionsMap reforgedItems) {
        if (SourcesOfItems.ignoredItems.contains(extraItem.id))
            return true;

        if (reforgedItems.get(slot) == null) {
            System.out.println("SLOT NOT USED IN CURRENT SET " + extraItem.toStringExtended());
            return true;
        }
        if (reforgedItems.get(slot)[0].id == extraItem.id) {
            System.out.println("SAME ITEM " + extraItem.toStringExtended());
            return true;
        }

        return false;
    }

    private static void reportUpgrade(ModelCombined model, ItemData extraItem, double baseRating, BestCollection<ItemData> bestCollection, Optional<ItemSet> extraSet) {
        if (extraSet.isPresent()) {
            System.out.println("PROPOSED " + extraSet.get().totals);
            double extraRating = model.calcRating(extraSet.get());
            double factor = extraRating / baseRating;
            System.out.printf("UPGRADE RATING = %.0f FACTOR = %1.3f\n", extraRating, factor);
            bestCollection.add(extraItem, factor);
        } else {
            System.out.print("UPGRADE SET NOT FOUND\n");
        }
        System.out.println();
    }

    private Optional<ItemSet> reforgeProcessPlusCore(EquipOptionsMap submitItems, ModelCombined model, ItemData extraItem, SlotEquip slot, Function<ItemData, ItemData> enchanting) {
        EquipOptionsMap runItems = submitItems.deepClone();
        extraItem = enchanting.apply(extraItem);
        ItemData[] extraForged = Reforger.reforgeItem(model.reforgeRules(), extraItem);

        System.out.println("REPLACING " + (runItems.get(slot) != null ? runItems.get(slot)[0] : "NOTHING"));
        runItems.put(slot, extraForged);
        ArrayUtil.mapInPlace(runItems.get(slot), enchanting);
        return EngineUtil.chooseEngineAndRun(model, runItems, null, runSize, null);
    }

}
