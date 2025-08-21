package au.nicholas.hardy.mopgear.model;

import au.nicholas.hardy.mopgear.domain.SocketType;
import au.nicholas.hardy.mopgear.domain.StatBlock;

public interface StatRatings {
    long calcRating(StatBlock totals);

    StatBlock gemChoice(SocketType socket);
}
