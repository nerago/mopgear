package au.nicholas.hardy.mopgear;

public class ItemData {
    String name;
    Integer str;
    Integer mastery;
    Integer crit;
    Integer hit;
    Integer haste;
    Integer expertise;

    ItemData copy() {
        ItemData copy = new ItemData();
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
        if (str != null && other.str != null)
            str += other.str;
        else if (other.str != null)
            str = other.str;

        if (mastery != null && other.mastery != null)
            mastery += other.mastery;
        else if (other.mastery != null)
            mastery = other.mastery;

        if (crit != null && other.crit != null)
            crit += other.crit;
        else if (other.crit != null)
            crit = other.crit;

        if (hit != null && other.hit != null)
            hit += other.hit;
        else if (other.hit != null)
            hit = other.hit;

        if (haste != null && other.haste != null)
            haste += other.haste;
        else if (other.haste != null)
            haste = other.haste;

        if (expertise != null && other.expertise != null)
            expertise += other.expertise;
        else if (other.expertise != null)
            expertise = other.expertise;
    }

    Integer get(Main.Secondary stat) {
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
                return null;
            }
        }
    }

    void set(Main.Secondary stat, Integer value) {
        switch (stat) {
            case Mastery -> mastery = value;
            case Crit -> crit = value;
            case Hit -> hit = value;
            case Haste -> haste = value;
            case Expertise -> expertise = value;
            default -> {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public String toString() {
        return "ItemData{" +
                "name='" + name + '\'' +
                ", str=" + str +
                ", mastery=" + mastery +
                ", crit=" + crit +
                ", hit=" + hit +
                ", haste=" + haste +
                ", expertise=" + expertise +
                '}';
    }
}
