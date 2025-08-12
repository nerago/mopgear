package au.nicholas.hardy.mopgear;

public final class ItemData {
    public final int id;
    public final SlotItem slot;
    public final String name;
    public final ReforgeRecipe reforge;
    public final StatBlock stat;
    public final StatBlock statFixed;
    public final int socketCount;
    public final int socketBonus;
    public final int itemLevel;

    private ItemData(int id, SlotItem slot, String name, ReforgeRecipe reforge, StatBlock stat, StatBlock statFixed, int socketCount, int socketBonus, int itemLevel) {
        this.id = id;
        this.slot = slot;
        this.name = name;
        this.reforge = reforge;
        this.stat = stat;
        this.statFixed = statFixed;
        this.socketCount = socketCount;
        this.socketBonus = socketBonus;
        this.itemLevel = itemLevel;
    }

    public static ItemData build(int id, SlotItem slot, String name, StatBlock stat, int socketCount, int socketBonus, int itemLevel) {
        return new ItemData(id, slot, name, null, stat, StatBlock.empty, socketCount, socketBonus, itemLevel);
    }

    public ItemData changeNameAndStats(String changedName, StatBlock changedStats, ReforgeRecipe recipe) {
        return new ItemData(id, slot, changedName, recipe, changedStats, statFixed, socketCount, socketBonus, itemLevel);
    }

    public ItemData changeStats(StatBlock changedStats) {
        return new ItemData(id, slot, name, reforge, changedStats, statFixed, socketCount, socketBonus, itemLevel);
    }

    public ItemData changeFixed(StatBlock changedFixed) {
        return new ItemData(id, slot, name, reforge, stat, changedFixed, socketCount, socketBonus, itemLevel);
    }

    public ItemData withoutFixed() {
        return new ItemData(id, slot, name, reforge, stat, StatBlock.empty, socketCount, socketBonus, itemLevel);
    }

    public StatBlock totalStatCopy() {
        if (statFixed.isEmpty())
            return stat;
        else
            return stat.plus(statFixed);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{ ");
        if (slot != null)
            sb.append(slot).append(' ');
        if (name != null)
            sb.append('"').append(name).append("\" ");
        else
            sb.append("TOTAL ");
        stat.append(sb, false);
        if (!statFixed.isEmpty()) {
            sb.append("GEMS ");
            statFixed.append(sb, false);
        }
        sb.append('}');
        return sb.toString();
    }

    static boolean isSameEquippedItem(ItemData a, ItemData b) {
        return a.id == b.id && a.statFixed.equalsStats(b.statFixed);
    }

    static boolean isIdenticalItem(ItemData a, ItemData b) {
        return a.id == b.id && a.stat.equalsStats(b.stat) && a.statFixed.equalsStats(b.statFixed);
    }
}
