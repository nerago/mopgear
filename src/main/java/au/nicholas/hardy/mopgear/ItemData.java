package au.nicholas.hardy.mopgear;

public final class ItemData {
    public final SlotItem slot;
    public final String name;
    public final StatBlock stat;
    public final StatBlock statFixed;

    public ItemData(SlotItem slot, String name, StatBlock stat, StatBlock statFixed) {
        this.slot = slot;
        this.name = name;
        this.stat = stat;
        this.statFixed = statFixed;
    }

    public ItemData copy() {
        return new ItemData(slot, name, stat.copy(), statFixed);
    }

    public StatBlock totalStatCopy() {
        if (statFixed.isEmpty())
            return stat.copy();
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
        stat.append(sb);
        if (!statFixed.isEmpty()) {
            sb.append("GEMS ");
            statFixed.append(sb);
        }
        sb.append('}');
        return sb.toString();
    }
}
