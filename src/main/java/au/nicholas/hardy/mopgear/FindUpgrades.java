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
//    private static final long runSize = 50000000; // 2 min total runs
//    private static final long runSize = 100000000; // 4 min total runs
    private static final long runSize = 300000000; // 12 min total runs
//    private static final long runSize = 1000000000; // 40 min runs

    public FindUpgrades(ItemCache itemCache) {
        this.itemCache = itemCache;
    }

    public void findUpgradeSetup(ModelCombined model, EquipOptionsMap baseItems, Tuple.Tuple2<Integer, Integer>[] extraItemArray) {
        ItemSet baseSet = EngineUtil.chooseEngineAndRun(model, baseItems, null, runSize, null, null).orElseThrow();
        double baseRating = model.calcRating(baseSet);
        System.out.printf("\nBASE RATING    = %.0f\n\n", baseRating);

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

            checkForUpgrade(model, baseItems.deepClone(), extraItem, enchanting, slot, baseRating, bestCollection);

            if (slot == SlotEquip.Trinket1) {
                checkForUpgrade(model, baseItems.deepClone(), extraItem, enchanting, SlotEquip.Trinket2, baseRating, bestCollection);
            }
            if (slot == SlotEquip.Ring1) {
                checkForUpgrade(model, baseItems.deepClone(), extraItem, enchanting, SlotEquip.Ring2, baseRating, bestCollection);
            }
        }

        System.out.println("RANKING RANKING");
        bestCollection.forEach((item, factor) ->
                System.out.printf("%10s \t%35s \t$%d \t%1.3f\n", item.slot, item.name,
                        ArrayUtil.findAny(extraItemArray, x -> x.a() == item.id).b(),
                        factor));
    }

    private static void checkForUpgrade(ModelCombined model, EquipOptionsMap baseItems, ItemData extraItem, Function<ItemData, ItemData> enchanting, SlotEquip slot, double baseRating, BestCollection<ItemData> bestCollection) {
        extraItem = enchanting.apply(extraItem);
        System.out.println("OFFER " + extraItem);
        System.out.println("REPLACING " + (baseItems.get(slot) != null ? baseItems.get(slot)[0] : "NOTHING"));

        ItemData[] extraOptions = Reforger.reforgeItem(model.reforgeRules(), extraItem);
        baseItems.put(slot, extraOptions);
        ArrayUtil.mapInPlace(baseItems.get(slot), enchanting);

        StatBlock adjustment = FindStatRange.checkSetAdjust(model, baseItems);

        Optional<ItemSet> resultSet = EngineUtil.chooseEngineAndRun(model, baseItems, null, runSize, null, adjustment);
        if (resultSet.isPresent()) {
            System.out.println("SET STATS " + resultSet.get().totals);
            double extraRating = model.calcRating(resultSet.get());
            double factor = extraRating / baseRating;
            System.out.printf("UPGRADE RATING = %.0f FACTOR = %1.3f\n", extraRating, factor);
            bestCollection.add(extraItem, factor);
        } else {
            System.out.print("UPGRADE SET NOT FOUND\n");
        }
        System.out.println();
    }

    private boolean canSkipUpgradeCheck(ItemData extraItem, SlotEquip slot, EquipOptionsMap reforgedItems) {
        if (SourcesOfItems.ignoredItems.contains(extraItem.id))
            return true;

        if (reforgedItems.get(slot) == null) {
            System.out.println("SLOT NOT USED IN CURRENT SET " + extraItem.toStringExtended());
            return true;
        }
        if (reforgedItems.get(slot)[0].id == extraItem.id) {
            System.out.println("SAME ITEM " + extraItem.toStringExtended() + "\n");
            return true;
        }

        return false;
    }

}
