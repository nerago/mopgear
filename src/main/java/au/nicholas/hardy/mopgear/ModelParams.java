package au.nicholas.hardy.mopgear;

public class ModelParams {
    static final Secondary[] required = new Secondary[]{Secondary.Hit, Secondary.Expertise};
    static final Secondary[] priority = new Secondary[]{Secondary.Haste, Secondary.Mastery, Secondary.Crit};
    static final Secondary[] reforgeTargets = new Secondary[]{Secondary.Hit, Secondary.Expertise, Secondary.Haste};
    static final int TARGET_HIT = 961;
    static final int TARGET_EXPERTISE = 481;
    static final int PERMITTED_EXCEED = 50;
}
