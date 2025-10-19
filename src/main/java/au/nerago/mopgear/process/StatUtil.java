package au.nerago.mopgear.process;

import au.nerago.mopgear.domain.ItemData;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.util.LowHighHolder;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public class StatUtil {
    public static LowHighHolder<ItemData> findMinMax(@Nullable StatRequirements.StatRequirementsWithHitExpertise requirements, ItemData[] itemArray, StatType statType) {
        if (statType == StatType.Hit && requirements != null)
            return findMinMaxHit(itemArray, requirements::effectiveHit);
        else
            return findMinMaxGeneric(itemArray, statType);
    }

    private static LowHighHolder<ItemData> findMinMaxGeneric(ItemData[] itemArray, StatType statType) {
        LowHighHolder<ItemData> holder = new LowHighHolder<>();
        for (ItemData item : itemArray) {
            int value = item.totalCap.get(statType);
            holder.add(item, value);
        }
        return holder;
    }

    private static LowHighHolder<ItemData> findMinMaxHit(ItemData[] itemArray, ToIntFunction<StatBlock> effectiveHit) {
        LowHighHolder<ItemData> holder = new LowHighHolder<>();
        for (ItemData item : itemArray) {
            int value = effectiveHit.applyAsInt(item.totalCap);
            holder.add(item, value);
        }
        return holder;
    }
}
