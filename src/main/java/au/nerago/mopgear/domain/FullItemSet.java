package au.nerago.mopgear.domain;

import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.SetBonus;
import au.nerago.mopgear.results.OutputText;

import java.util.function.Function;

public record FullItemSet(StatBlock totalForRating, StatBlock totalForCaps, EquipMap items) {
    public static FullItemSet ofSolvable(SolvableItemSet other, Function<SolvableItem, FullItemData> itemConverter) {
        return new FullItemSet(other.totalForRating(), other.totalForCaps(), new EquipMap(other.items(), itemConverter));
    }

    public static FullItemSet manyItems(EquipMap items, StatBlock adjustment) {
        // trust caller is creating unique maps
        StatBlock rating = StatBlock.sumForRating(items);
        StatBlock caps = StatBlock.sumForCaps(items);
        if (adjustment != null) {
            rating = rating.plus(adjustment);
            caps = caps.plus(adjustment);
        }
        return new FullItemSet(rating, caps, items);
    }

    public void outputSet(ModelCombined model) {
        OutputText.println(totalForRating.toStringExtended() + " " + model.calcRating(this));
        items.forEachValue(it -> OutputText.println(it + " " + model.calcRating(it)));
        OutputText.printf("set bonus %1.2f\n", (double) model.setBonus().calc(this) / (double) SetBonus.DENOMIATOR);
    }

    public void outputSetDetailed(ModelCombined model) {
        OutputText.println("SET RATED " + totalForRating.toStringExtended() + " " + model.calcRating(this));
        OutputText.println("SET CONSTANT " + totalForCaps.toStringExtended());
        items.forEachValue(it -> OutputText.println(it.toStringExtended() + " " + model.calcRating(it)));
    }

    public void outputSetLight() {
        items.forEachValue(it -> OutputText.printf("%s [%d]\n", it.fullName(), it.ref().itemLevel()));
    }

    public boolean validate() {
        FullItemData weapon = items.getWeapon();
        if (weapon == null)
            throw new IllegalStateException("no weapon in set");
        if (weapon.slot() == SlotItem.Weapon2H && items.getOffhand() != null)
            throw new IllegalStateException("weapon 2H with unexpected offhand");
        if (weapon.slot() == SlotItem.Weapon1H && items.getOffhand() == null)
            throw new IllegalStateException("weapon 1H with missing offhand");

        FullItemData t1 = items.getTrinket1(), t2 = items.getTrinket2();
        FullItemData r1 = items.getRing1(), r2 = items.getRing2();
        return (t1 == null || t2 == null || t1.itemId() != t2.itemId()) &&
                (r1 == null || r2 == null || r1.itemId() != r2.itemId());
    }

    @Override
    public String toString() {
        return totalForRating.toString();
    }
}
