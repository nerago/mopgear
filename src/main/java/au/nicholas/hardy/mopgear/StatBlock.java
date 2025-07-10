package au.nicholas.hardy.mopgear;

public class StatBlock {
    int str;
    int mastery;
    int crit;
    int hit;
    int haste;
    int expertise;

    StatBlock copy() {
        StatBlock copy = new StatBlock();
        copy.str = str;
        copy.mastery = mastery;
        copy.crit = crit;
        copy.hit = hit;
        copy.haste = haste;
        copy.expertise = expertise;
        return copy;
    }

    void increment(StatBlock other) {
        str += other.str;
        mastery += other.mastery;
        crit += other.crit;
        hit += other.hit;
        haste += other.haste;
        expertise += other.expertise;
    }

    public StatBlock plus(StatBlock other) {
        StatBlock sum = new StatBlock();
        sum.str = str + other.str;
        sum.mastery = mastery + other.mastery;
        sum.crit = crit + other.crit;
        sum.hit = hit + other.hit;
        sum.haste = haste + other.haste;
        sum.expertise = expertise + other.expertise;
        return sum;
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
            default -> throw new IllegalArgumentException();
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

    public void append(StringBuilder sb) {
        if (str != 0)
            sb.append("str=").append(str).append(' ');
        if (mastery != 0)
            sb.append("mastery=").append(mastery).append(' ');
        if (crit != 0)
            sb.append("crit=").append(crit).append(' ');
        if (hit != 0)
            sb.append("hit=").append(hit).append(' ');
        if (haste != 0)
            sb.append("haste=").append(haste).append(' ');
        if (expertise != 0)
            sb.append("expertise=").append(expertise).append(' ');
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        append(builder);
        builder.append('}');
        return builder.toString();
    }
}
