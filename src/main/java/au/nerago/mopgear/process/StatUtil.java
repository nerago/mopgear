package au.nerago.mopgear.process;

import au.nerago.mopgear.domain.SolvableItem;
import au.nerago.mopgear.domain.StatBlock;
import au.nerago.mopgear.domain.StatType;
import au.nerago.mopgear.model.IItem;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.util.LowHighHolder;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public class StatUtil {
    public static LowHighHolder<SolvableItem> findMinMax(@Nullable StatRequirements.StatRequirementsWithHitExpertise requirements, SolvableItem[] itemArray, StatType statType) {
        if (statType == StatType.Hit && requirements != null)
            return findMinMaxHit(itemArray, requirements::effectiveHit);
        else
            return findMinMaxGeneric(itemArray, statType);
    }

    private static LowHighHolder<SolvableItem> findMinMaxGeneric(SolvableItem[] itemArray, StatType statType) {
        LowHighHolder<SolvableItem> holder = new LowHighHolder<>();
        for (SolvableItem item : itemArray) {
            int value = item.totalCap().get(statType);
            holder.add(item, value);
        }
        return holder;
    }

    private static LowHighHolder<SolvableItem> findMinMaxHit(SolvableItem[] itemArray, ToIntFunction<StatBlock> effectiveHit) {
        LowHighHolder<SolvableItem> holder = new LowHighHolder<>();
        for (SolvableItem item : itemArray) {
            int value = effectiveHit.applyAsInt(item.totalCap());
            holder.add(item, value);
        }
        return holder;
    }
}
