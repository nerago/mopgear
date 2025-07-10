package au.nicholas.hardy.mopgear;

public final class ItemData {
    SlotItem slot;
    String name;
    StatBlock stat;

    public ItemData(SlotItem slot, String name) {
        this.slot = slot;
        this.name = name;
        this.stat = new StatBlock();
    }

    private ItemData(SlotItem slot, String name, StatBlock stat) {
        this.slot = slot;
        this.name = name;
        this.stat = stat;
    }

    public ItemData copy() {
        return new ItemData(slot, name, stat.copy());
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
        sb.append('}');
        return sb.toString();
    }
}
