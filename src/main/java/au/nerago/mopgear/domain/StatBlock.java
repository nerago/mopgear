package au.nerago.mopgear.domain;

public record StatBlock(int primary, int stam, int mastery, int crit, int hit, int haste,
                        int expertise, int dodge, int parry, int spirit) {

    public static StatBlock add(StatBlock a, StatBlock b) {
        if (a != null && b != null)
            return a.plus(b);
        else if (a != null)
            return a;
        else
            return b;
    }

    public StatBlock plus(StatBlock other) {
        return new StatBlock(
                primary + other.primary,
                stam + other.stam,
                mastery + other.mastery,
                crit + other.crit,
                hit + other.hit,
                haste + other.haste,
                expertise + other.expertise,
                dodge + other.dodge,
                parry + other.parry,
                spirit + other.spirit);
    }

    public StatBlock plus(StatBlock first, StatBlock second) {
        return new StatBlock(
                primary + first.primary + second.primary,
                stam + first.stam + second.stam,
                mastery + first.mastery + second.mastery,
                crit + first.crit + second.crit,
                hit + first.hit + second.hit,
                haste + first.haste + second.haste,
                expertise + first.expertise + second.expertise,
                dodge + first.dodge + second.dodge,
                parry + first.parry + second.parry,
                spirit + first.spirit + second.spirit);
    }

    public StatBlock multiply(int multiply) {
        return new StatBlock(
                primary * multiply,
                stam * multiply,
                mastery * multiply,
                crit * multiply,
                hit * multiply,
                haste * multiply,
                expertise * multiply,
                dodge * multiply,
                parry * multiply,
                spirit * multiply);
    }

    public static StatBlock sum(EquipMap items) {
        int primary = 0;
        int stam = 0;
        int mastery = 0;
        int crit = 0;
        int hit = 0;
        int haste = 0;
        int expertise = 0;
        int dodge = 0;
        int parry = 0;
        int spirit = 0;
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData item = items.get(slot);
            if (item != null) {
                StatBlock stat = item.stat;
                primary += stat.primary;
                stam += stat.stam;
                mastery += stat.mastery;
                crit += stat.crit;
                hit += stat.hit;
                haste += stat.haste;
                expertise += stat.expertise;
                dodge += stat.dodge;
                parry += stat.parry;
                spirit += stat.spirit;
                StatBlock fixed = item.statFixed;
                primary += fixed.primary;
                stam += fixed.stam;
                mastery += fixed.mastery;
                crit += fixed.crit;
                hit += fixed.hit;
                haste += fixed.haste;
                expertise += fixed.expertise;
                dodge += fixed.dodge;
                parry += fixed.parry;
                spirit += fixed.spirit;
            }
        }
        return new StatBlock(primary, stam, mastery, crit, hit, haste, expertise, dodge, parry, spirit);
    }

    public int get(StatType stat) {
        switch (stat) {
            case Primary -> {
                return primary;
            }
            case Stam -> {
                return stam;
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
            case Spirit -> {
                return spirit;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public static StatBlock of(StatType stat, int value) {
        int primary = 0;
        int stam = 0;
        int mastery = 0;
        int crit = 0;
        int hit = 0;
        int haste = 0;
        int expertise = 0;
        int dodge = 0;
        int parry = 0;
        int spirit = 0;
        switch (stat) {
            case Primary -> primary = value;
            case Stam -> stam = value;
            case Mastery -> mastery = value;
            case Crit -> crit = value;
            case Hit -> hit = value;
            case Haste -> haste = value;
            case Expertise -> expertise = value;
            case Dodge -> dodge = value;
            case Parry -> parry = value;
            case Spirit -> spirit = value;
            default -> throw new IllegalArgumentException();
        }
        return new StatBlock(primary, stam, mastery, crit, hit, haste, expertise, dodge, parry, spirit);
    }

    public static StatBlock of(StatType a_stat, int a_value, StatType b_stat, int b_value) {
        int primary = 0;
        int stam = 0;
        int mastery = 0;
        int crit = 0;
        int hit = 0;
        int haste = 0;
        int expertise = 0;
        int dodge = 0;
        int parry = 0;
        int spirit = 0;
        if (a_stat == b_stat)
            throw new IllegalArgumentException();
        switch (a_stat) {
            case Primary -> primary = a_value;
            case Stam -> stam = a_value;
            case Mastery -> mastery = a_value;
            case Crit -> crit = a_value;
            case Hit -> hit = a_value;
            case Haste -> haste = a_value;
            case Expertise -> expertise = a_value;
            case Dodge -> dodge = a_value;
            case Parry -> parry = a_value;
            case Spirit -> spirit = a_value;
            default -> throw new IllegalArgumentException();
        }
        switch (b_stat) {
            case Primary -> primary = b_value;
            case Stam -> stam = b_value;
            case Mastery -> mastery = b_value;
            case Crit -> crit = b_value;
            case Hit -> hit = b_value;
            case Haste -> haste = b_value;
            case Expertise -> expertise = b_value;
            case Dodge -> dodge = b_value;
            case Parry -> parry = b_value;
            case Spirit -> spirit = b_value;
            default -> throw new IllegalArgumentException();
        }
        return new StatBlock(primary, stam, mastery, crit, hit, haste, expertise, dodge, parry, spirit);
    }

    public StatBlock withChange(StatType stat, int value) {
        int primary = this.primary;
        int stam = this.stam;
        int mastery = this.mastery;
        int crit = this.crit;
        int hit = this.hit;
        int haste = this.haste;
        int expertise = this.expertise;
        int dodge = this.dodge;
        int parry = this.parry;
        int spirit = this.spirit;
        switch (stat) {
            case Primary -> primary = value;
            case Stam -> stam = value;
            case Mastery -> mastery = value;
            case Crit -> crit = value;
            case Hit -> hit = value;
            case Haste -> haste = value;
            case Expertise -> expertise = value;
            case Dodge -> dodge = value;
            case Parry -> parry = value;
            case Spirit -> spirit = value;
            default -> throw new IllegalArgumentException();
        }
        return new StatBlock(primary, stam, mastery, crit, hit, haste, expertise, dodge, parry, spirit);
    }

    public StatBlock withChange(StatType a_stat, int a_value, StatType b_stat, int b_value) {
        int primary = this.primary;
        int stam = this.stam;
        int mastery = this.mastery;
        int crit = this.crit;
        int hit = this.hit;
        int haste = this.haste;
        int expertise = this.expertise;
        int dodge = this.dodge;
        int parry = this.parry;
        int spirit = this.spirit;
        if (a_stat == b_stat)
            throw new IllegalArgumentException();
        switch (a_stat) {
            case Primary -> primary = a_value;
            case Stam -> stam = a_value;
            case Mastery -> mastery = a_value;
            case Crit -> crit = a_value;
            case Hit -> hit = a_value;
            case Haste -> haste = a_value;
            case Expertise -> expertise = a_value;
            case Dodge -> dodge = a_value;
            case Parry -> parry = a_value;
            case Spirit -> spirit = a_value;
            default -> throw new IllegalArgumentException();
        }
        switch (b_stat) {
            case Primary -> primary = b_value;
            case Stam -> stam = b_value;
            case Mastery -> mastery = b_value;
            case Crit -> crit = b_value;
            case Hit -> hit = b_value;
            case Haste -> haste = b_value;
            case Expertise -> expertise = b_value;
            case Dodge -> dodge = b_value;
            case Parry -> parry = b_value;
            case Spirit -> spirit = b_value;
            default -> throw new IllegalArgumentException();
        }
        return new StatBlock(primary, stam, mastery, crit, hit, haste, expertise, dodge, parry, spirit);
    }

    public void append(StringBuilder sb, boolean extended) {
        if (primary != 0)
            sb.append("primary=").append(primary).append(' ');
        if (stam != 0)
            sb.append("stam=").append(stam).append(' ');
        if (mastery != 0)
            sb.append("mastery=").append(mastery).append(' ');
        if (crit != 0)
            sb.append("crit=").append(crit).append(' ');
        if (hit != 0)
            sb.append("one=").append(hit).append(' ');
        if (haste != 0)
            sb.append("haste=").append(haste).append(' ');
        if (expertise != 0)
            sb.append("two=").append(expertise).append(' ');
        if (dodge != 0)
            sb.append("dodge=").append(dodge).append(' ');
        if (parry != 0)
            sb.append("parry=").append(parry).append(' ');
        if (spirit != 0)
            sb.append("spirit=").append(spirit).append(' ');
        if (extended && (hit != 0 || expertise != 0 || spirit != 0))
            sb.append("superhit=").append(hit + expertise + spirit).append(' ');
    }

    public final static StatBlock empty = new StatBlock(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

    public boolean isEmpty() {
        return primary == 0 && stam == 0 && mastery == 0 && crit == 0 && hit == 0 && haste == 0 &&
                expertise == 0 && dodge == 0 && parry == 0 && spirit == 0;
    }

    public boolean equalsStats(StatBlock stats) {
        return primary == stats.primary && stam == stats.stam && mastery == stats.mastery && crit == stats.crit && hit == stats.hit &&
                haste == stats.haste && expertise == stats.expertise && dodge == stats.dodge && parry == stats.parry && spirit == stats.spirit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return equalsStats((StatBlock) o);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        append(builder, false);
        builder.append('}');
        return builder.toString();
    }

    public String toStringExtended() {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        append(builder, true);
        builder.append('}');
        return builder.toString();
    }
}
