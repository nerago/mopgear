package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.Slot;

public class ItemData {
    Slot slot;
    String name;
    int str;
    int mastery;
    int crit;
    int hit;
    int haste;
    int expertise;

    ItemData copy() {
        ItemData copy = new ItemData();
        copy.slot = slot;
        copy.name = name;
        copy.str = str;
        copy.mastery = mastery;
        copy.crit = crit;
        copy.hit = hit;
        copy.haste = haste;
        copy.expertise = expertise;
        return copy;
    }

    void increment(ItemData other) {
        str += other.str;
        mastery += other.mastery;
        crit += other.crit;
        hit += other.hit;
        haste += other.haste;
        expertise += other.expertise;
    }

    int get(Secondary stat) {
        switch (stat) {
            case Mastery -> {
                return mastery;
            }
            case Crit -> {
                return crit;
            }
            case Hit -> {
                return hit;
            }
            case Haste -> {
                return haste;
            }
            case Expertise -> {
                return expertise;
            }
            default -> {
                throw new IllegalArgumentException();
            }
        }
    }

    void set(Secondary stat, int value) {
        switch (stat) {
            case Mastery -> mastery = value;
            case Crit -> crit = value;
            case Hit -> hit = value;
            case Haste -> haste = value;
            case Expertise -> expertise = value;
            default -> throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        if (slot != null)
            sb.append(slot).append(' ');
        if (name != null)
            sb.append('"').append(name).append('"');
        else
            sb.append("TOTAL");
        if (str != 0)
            sb.append(", str=").append(str);
        if (mastery != 0)
            sb.append(", mastery=").append(mastery);
        if (crit != 0)
            sb.append(", crit=").append(crit);
        if (hit != 0)
            sb.append(", hit=").append(hit);
        if (haste != 0)
            sb.append(", haste=").append(haste);
        if (expertise != 0)
            sb.append(", expertise=").append(expertise);
        sb.append('}');
        return sb.toString();
    }
}
