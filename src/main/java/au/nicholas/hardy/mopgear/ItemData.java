package au.nicholas.hardy.mopgear;

public class ItemData {
    String name;
    int str;
    int mastery;
    int crit;
    int hit;
    int haste;
    int expertise;

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
        str += other.str;
        mastery += other.mastery;
        crit += other.crit;
        hit += other.hit;
        haste += other.haste;
        expertise += other.expertise;
    }

    int get(Main.Secondary stat) {
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

    void set(Main.Secondary stat, int value) {
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
