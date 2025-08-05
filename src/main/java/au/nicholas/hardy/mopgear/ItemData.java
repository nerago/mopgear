package au.nicholas.hardy.mopgear;

public final class ItemData {
    public final SlotItem slot;
    public final String name;
    public final StatBlock stat;
    public final StatBlock statFixed;
    public final int id;

    public ItemData(SlotItem slot, String name, StatBlock stat, StatBlock statFixed, int id) {
        this.slot = slot;
        this.name = name;
        this.stat = stat;
        this.statFixed = statFixed;
        this.id = id;
    }

    public StatBlock totalStatCopy() {
        if (statFixed.isEmpty())
            return stat;
        else
            return stat.plus(statFixed);
    }

    public ItemData disenchant() {
        return new ItemData(slot, name, stat, StatBlock.empty, id);
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
        stat.append(sb);
        if (!statFixed.isEmpty()) {
            sb.append("GEMS ");
            statFixed.append(sb);
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
