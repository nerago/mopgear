package au.nicholas.hardy.mopgear;

public final class StatBlock {
    public final int str;
    public final int mastery;
    public final int crit;
    public final int hit;
    public final int haste;
    public final int expertise;
    public final int dodge;
    public final int parry;

    public StatBlock(int str, int mastery, int crit, int hit, int haste, int expertise, int dodge, int parry) {
        this.str = str;
        this.mastery = mastery;
        this.crit = crit;
        this.hit = hit;
        this.haste = haste;
        this.expertise = expertise;
        this.dodge = dodge;
        this.parry = parry;
    }

    StatBlock copy() {
        return new StatBlock(str, mastery, crit, hit, haste, expertise, dodge, parry);
    }

    public StatBlock plus(StatBlock other) {
        return new StatBlock(
                str + other.str,
                mastery + other.mastery,
                crit + other.crit,
                hit + other.hit,
                haste + other.haste,
                expertise + other.expertise,
                dodge + other.dodge,
                parry + other.parry
        );
    }

    int get(StatType stat) {
        switch (stat) {
            case Strength -> {
                return str;
            }
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
            case Dodge -> {
                return dodge;
            }
            case Parry -> {
                return parry;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    StatBlock withChange(StatType a_stat, int a_value) {
        int str = this.str;
        int mastery = this.mastery;
        int crit = this.crit;
        int hit = this.hit;
        int haste = this.haste;
        int expertise = this.expertise;
        int dodge = this.dodge;
        int parry = this.parry;
        switch (a_stat) {
            case Strength -> str = a_value;
            case Mastery -> mastery = a_value;
            case Crit -> crit = a_value;
            case Hit -> hit = a_value;
            case Haste -> haste = a_value;
            case Expertise -> expertise = a_value;
            case Dodge -> dodge = a_value;
            case Parry -> parry = a_value;
            default -> throw new IllegalArgumentException();
        }
        return new StatBlock(str, mastery, crit, hit, haste, expertise, dodge, parry);
    }

    StatBlock withChange(StatType a_stat, int a_value, StatType b_stat, int b_value) {
        int mastery = this.mastery;
        int crit = this.crit;
        int hit = this.hit;
        int haste = this.haste;
        int expertise = this.expertise;
        int dodge = this.dodge;
        int parry = this.parry;
        switch (a_stat) {
            case Mastery -> mastery = a_value;
            case Crit -> crit = a_value;
            case Hit -> hit = a_value;
            case Haste -> haste = a_value;
            case Expertise -> expertise = a_value;
            case Dodge -> dodge = a_value;
            case Parry -> parry = a_value;
            default -> throw new IllegalArgumentException();
        }
        switch (b_stat) {
            case Mastery -> mastery = b_value;
            case Crit -> crit = b_value;
            case Hit -> hit = b_value;
            case Haste -> haste = b_value;
            case Expertise -> expertise = b_value;
            case Dodge -> dodge = b_value;
            case Parry -> parry = b_value;
            default -> throw new IllegalArgumentException();
        }
        return new StatBlock(str, mastery, crit, hit, haste, expertise, dodge, parry);
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
        if (dodge != 0)
            sb.append("dodge=").append(dodge).append(' ');
        if (parry != 0)
            sb.append("parry=").append(parry).append(' ');
    }

    public final static StatBlock empty = new StatBlock(0, 0, 0, 0, 0, 0, 0, 0);

    public boolean isEmpty() {
        return str == 0 && mastery == 0 && crit == 0 && hit == 0 && haste == 0 && expertise == 0 && dodge == 0 && parry == 0;
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
