package au.nicholas.hardy.mopgear;

public interface StatRatings {
    long calcRating(StatBlock totals);

    StatBlock standardGem();
}
