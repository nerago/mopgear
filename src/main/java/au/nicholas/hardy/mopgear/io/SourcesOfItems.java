package au.nicholas.hardy.mopgear.io;

import au.nicholas.hardy.mopgear.ItemUtil;
import au.nicholas.hardy.mopgear.domain.EquipOptionsMap;
import au.nicholas.hardy.mopgear.domain.SlotItem;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.model.ReforgeRules;
import au.nicholas.hardy.mopgear.util.ArrayUtil;
import au.nicholas.hardy.mopgear.util.Tuple;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class SourcesOfItems {
    public static final List<Integer> ignoredItems = List.of(
            63207, // org port cloak
            84661 // fishing pole
    );

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlateMsvArray() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                // stone
                Tuple.create(85922, 1),
                Tuple.create(85925, 1),
//                Tuple.create(86134, 1), got heroic
                // feng
//                Tuple.create(85983, 2), got norm
                Tuple.create(85984, 2),
                Tuple.create(85985, 2),
                // garaj
//                Tuple.create(85991, 3), got norm
                Tuple.create(85992, 3),
                Tuple.create(89817, 3),
                // kings
                Tuple.create(86075, 4),
                Tuple.create(86076, 4),
                Tuple.create(86080, 4),
                // elegon
                Tuple.create(86130, 5), // prot weapon
                Tuple.create(86140, 5), // ret weapon
                Tuple.create(86135, 5), // got celestial
                // will
                Tuple.create(86144, 6),
                Tuple.create(86145, 6),
//                Tuple.create(89823, 6) // got norm
        };
    }

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlateMsvHeroicArray() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                // stone
                Tuple.create(87016, 1),
                Tuple.create(87015, 1),
//                Tuple.create(87060, 1), got heroic
                // feng
                Tuple.create(87026, 2),
                Tuple.create(87024, 2),
                Tuple.create(87025, 2),
                // garaj
                Tuple.create(89934, 3),
                Tuple.create(87035, 3),
                Tuple.create(87036, 3),
                // kings
                Tuple.create(87049, 4),
                Tuple.create(87048, 4),
                Tuple.create(87050, 4),
                // elegon
                Tuple.create(87062, 5), // prot weapon
//                Tuple.create(87061, 5), // ret weapon
                Tuple.create(87059, 5), // starcrusher
                Tuple.create(89937, 5),
                // will
                Tuple.create(89941, 6),
                Tuple.create(87071, 6),
                Tuple.create(87072, 6)
        };
    }

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlateValorArray() {
        Tuple.Tuple2<Integer, Integer> neckParagonPale = Tuple.create(89066, 1250);
        Tuple.Tuple2<Integer, Integer> neckBloodseekers = Tuple.create(89064, 1250);
        Tuple.Tuple2<Integer, Integer> beltKlaxxiConsumer = Tuple.create(89056, 1750);
        Tuple.Tuple2<Integer, Integer> legKovokRiven = Tuple.create(89093, 2500);
        Tuple.Tuple2<Integer, Integer> backYiCloakCourage = Tuple.create(89075, 1250);
        Tuple.Tuple2<Integer, Integer> headYiLeastFavorite = Tuple.create(89216, 2500);
        Tuple.Tuple2<Integer, Integer> headVoiceAmpGreathelm = Tuple.create(89280, 2500);
        Tuple.Tuple2<Integer, Integer> chestDawnblade = Tuple.create(89420, 2500);
        Tuple.Tuple2<Integer, Integer> chestCuirassTwin = Tuple.create(89421, 2500);
        Tuple.Tuple2<Integer, Integer> gloveOverwhelmSwarm = Tuple.create(88746, 1750);
        Tuple.Tuple2<Integer, Integer> wristBattleShadow = Tuple.create(88880, 1250);
        Tuple.Tuple2<Integer, Integer> wristBraidedBlackWhite = Tuple.create(88879, 1250);
        Tuple.Tuple2<Integer, Integer> bootYulonGuardian = Tuple.create(88864, 1750);
        Tuple.Tuple2<Integer, Integer> bootTankissWarstomp = Tuple.create(88862, 1750);

        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{neckParagonPale, neckBloodseekers, beltKlaxxiConsumer, legKovokRiven, backYiCloakCourage, headYiLeastFavorite, headVoiceAmpGreathelm, chestDawnblade,
                chestCuirassTwin, gloveOverwhelmSwarm, wristBattleShadow, wristBraidedBlackWhite, bootYulonGuardian, bootTankissWarstomp};
    }

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlatePvpArray() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                Tuple.create(84794, 2250),
                Tuple.create(84806, 1250),
                Tuple.create(84807, 1250),
                Tuple.create(84810, 1750),
                Tuple.create(84822, 1750),
                Tuple.create(84828, 1250),
                Tuple.create(84829, 1250),
                Tuple.create(84834, 1750),
                Tuple.create(84851, 2250),
                Tuple.create(84870, 2250),
                Tuple.create(84891, 1250),
                Tuple.create(84892, 1250),
                Tuple.create(84915, 1750),
                Tuple.create(84949, 1750),
                Tuple.create(84950, 1750),
                Tuple.create(84985, 1250),
                Tuple.create(84986, 1250),
        };
    }

    public static Tuple.Tuple2<Integer, Integer>[] intellectLeatherValorArray() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                Tuple.create(89308, 2500),
                Tuple.create(89342, 1750),
                Tuple.create(89432, 2500),
                Tuple.create(88885, 1250),
                Tuple.create(88743, 1750),
                Tuple.create(89061, 1750),
                Tuple.create(89089, 2500),
                Tuple.create(88876, 1750),
                Tuple.create(89078, 1250),
                Tuple.create(89067, 1250),
                Tuple.create(89073, 1250),
                Tuple.create(89072, 1250),
                Tuple.create(89081, 1750),
                Tuple.create(89080, 1750),
        };
    }

    public static Tuple.Tuple2<Integer, Integer>[] intellectLeatherCelestialArray() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                Tuple.create(89957, 45),
                Tuple.create(86856, 30),
                Tuple.create(86644, 50),
                Tuple.create(86840, 30),
                Tuple.create(86645, 55),
                Tuple.create(86786, 25),
                Tuple.create(86768, 25),
                Tuple.create(86648, 50),
                Tuple.create(86746, 40),
                Tuple.create(86748, 25),
                Tuple.create(89971, 25),
                Tuple.create(86646, 55),
                Tuple.create(86878, 50),
                Tuple.create(86814, 30),
                Tuple.create(86873, 30),
                Tuple.create(86792, 40),
                Tuple.create(86907, 50),
                Tuple.create(86893, 50),
                Tuple.create(86808, 40),
                Tuple.create(86797, 40),
                Tuple.create(86806, 25),
                Tuple.create(89426, 15),
                Tuple.create(86754, 25),
                Tuple.create(86783, 25),
                Tuple.create(86810, 25),
                Tuple.create(86767, 25),
                Tuple.create(89968, 25),
        };
    }

    public static Tuple.Tuple2<Integer, Integer>[] intellectClothValorCelestialP1Array() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                Tuple.create(89337,2500 ),
                Tuple.create(89340,1750 ),
                Tuple.create(89433,2500 ),
                Tuple.create(88893,1250 ),
                Tuple.create(88742,1750 ),
                Tuple.create(89062,1750 ),
                Tuple.create(89088,2500 ),
                Tuple.create(88878,1750 ),
                Tuple.create(89077,1250 ),
                Tuple.create(89068,1250 ),
                Tuple.create(89072,1250 ),
                Tuple.create(89081,1750 ),
                Tuple.create(86809,40 ),
                Tuple.create(86770,25 ),
                Tuple.create(86758,40 ),
                Tuple.create(86787,25 ),
                Tuple.create(89966,25 ),
                Tuple.create(86798,40 ),
                Tuple.create(89965,40 ),
                Tuple.create(89971,25 ),
                Tuple.create(86796,25 ),
                Tuple.create(86829,15 ),
                Tuple.create(86754,25 ),
                Tuple.create(86810,25 ),
                Tuple.create(86767,25 ),
                Tuple.create(86773,40 ),
                Tuple.create(86792,40 )
        };
    }

    public static Tuple.Tuple2<Integer, Integer>[] filterItemLevel(ItemCache itemCache, Tuple.Tuple2<Integer, Integer>[] existing, int maxItemLevel) {
        return Arrays.stream(existing)
                .map(tup -> Tuple.create(tup, ItemUtil.loadItemBasic(itemCache, tup.a())))
                .filter(tup2 -> tup2.b().itemLevel <= maxItemLevel)
                .map(Tuple.Tuple2::a)
                .toArray(Tuple.Tuple2[]::new);
    }

    public static Tuple.Tuple2<Integer, Integer>[] filterExclude(Tuple.Tuple2<Integer, Integer>[] existing, List<Integer> exclude) {
        return Arrays.stream(existing)
                .filter(tup -> !exclude.contains(tup.a()))
                .toArray(Tuple.Tuple2[]::new);
    }

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlateCelestialArrayTankPicks() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                Tuple.create(86794, 25),
                Tuple.create(86742, 40),
                Tuple.create(86760, 40),
                Tuple.create(86789, 25),
                Tuple.create(86759, 25),
                Tuple.create(86753, 25),
        };
    }

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlateCelestialArrayRetPicks() {
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                Tuple.create(89969, 25),
                Tuple.create(86794, 25),
                Tuple.create(86742, 40),
                // also trinket lei shen's final orders
        };
    }

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlateCelestialArray() {
        // skipping neck cloak offhand weaps
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{
                // plate
                Tuple.create(86803, 40),
                Tuple.create(86742, 40),
                Tuple.create(89976, 40),
                Tuple.create(89969, 25),
                Tuple.create(86752, 40),
                Tuple.create(89954, 45),
                Tuple.create(86751, 25),
                Tuple.create(86823, 30),
                Tuple.create(86860, 30),
                Tuple.create(86793, 40),
                Tuple.create(86868, 30),
                Tuple.create(89981, 30),
                Tuple.create(86852, 45),
                Tuple.create(86832, 45),
                Tuple.create(86794, 25),
                Tuple.create(86780, 25),
                Tuple.create(86779, 40),
                Tuple.create(89958, 45),
                Tuple.create(89956, 30),
                Tuple.create(86822, 45),
                Tuple.create(86904, 50),
                Tuple.create(86854, 45),
                Tuple.create(86849, 30),
                Tuple.create(86760, 40),
                Tuple.create(86848, 30),
                Tuple.create(89963, 45),
                Tuple.create(86903, 25),
                Tuple.create(86870, 50),
                Tuple.create(86891, 50),

                // ring
                Tuple.create(86820, 30),
                Tuple.create(86830, 30),
                Tuple.create(89972, 25),
                Tuple.create(86813, 30),
                Tuple.create(86880, 30),

                // weap
                Tuple.create(86799, 40),
                Tuple.create(86906, 30),
                Tuple.create(86789, 25),

                // neck
                Tuple.create(86759, 25),
                Tuple.create(86872, 25),
                Tuple.create(86739, 25),
                Tuple.create(86871, 30),
                Tuple.create(86835, 30),

                // back
                Tuple.create(86753, 25),
                Tuple.create(86812, 30),
                Tuple.create(86883, 25),
                Tuple.create(86853, 25)
        };
    }

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlateHeartOfFear() {
        // skipping neck cloak offhand weaps
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{

                Tuple.create(86154,1 ),
                Tuple.create(89826,1 ),
                Tuple.create(86203,1 ),
                Tuple.create(86155,1 ),
                Tuple.create(89828,2 ),
                Tuple.create(86165,2 ),
                Tuple.create(86164,2 ),
                Tuple.create(86162,2 ),
                Tuple.create(86174,3 ),
                Tuple.create(89832,3 ),
                Tuple.create(86177,3 ),
                Tuple.create(86172,3 ),
                Tuple.create(86202,4 ),
                Tuple.create(86201,4 ),
                Tuple.create(85322,4 ),
                Tuple.create(86213,5 ),
                Tuple.create(86219,5 ),
                Tuple.create(85320,5 ),
                Tuple.create(89837,6 ),
                Tuple.create(85323,6 ),
                Tuple.create(86191,0 ),
                Tuple.create(86190,0 )
        };
    }

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlateHeartOfFearHeroic() {
        // skipping neck cloak offhand weaps
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{

                Tuple.create(86966,3 ),
        };
    }

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlateCrafted() {
        // skipping neck cloak offhand weaps
        return (Tuple.Tuple2<Integer, Integer>[]) new Tuple.Tuple2[]{

                Tuple.create(87402,0 ),
                Tuple.create(87406,0 ),
                Tuple.create(87405,0 ),
                Tuple.create(87407,0 ),
        };
    }

    public static Tuple.Tuple2<Integer, Integer>[] bagItemsArray(ModelCombined model, List<Integer> skip) throws IOException {
        Tuple.Tuple2<Integer, Integer>[] bagArray = InputBagsParser.readInput(DataLocation.bagsFile);
        if (skip == null)
            return bagArray;
        else
            return filterExclude(bagArray, skip);
    }

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlateCurrentItemsRet(ItemCache itemCache, ModelCombined model) throws IOException {
        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearRetFile, ReforgeRules.ret(), null);
        Stream<Tuple.Tuple2<Integer, Integer>> itemStream = items.entrySet().stream()
                .filter(it -> it.b()[0].slot != SlotItem.Weapon && it.b()[0].slot != SlotItem.Trinket && it.b()[0].slot != SlotItem.Ring)
                .map(tup -> Tuple.create(tup.b()[0].id, 0));
        return itemStream.toArray(Tuple.Tuple2[]::new);
    }

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlateCurrentItemsProt(ItemCache itemCache, ModelCombined model) throws IOException {
        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearProtFile, ReforgeRules.ret(), null);
        Stream<Tuple.Tuple2<Integer, Integer>> itemStream = items.entrySet().stream()
                .filter(it -> it.b()[0].slot != SlotItem.Weapon && it.b()[0].slot != SlotItem.Trinket && it.b()[0].slot != SlotItem.Ring)
                .map(tup -> Tuple.create(tup.b()[0].id, 0));
        return itemStream.toArray(Tuple.Tuple2[]::new);
    }

    public static Tuple.Tuple2<Integer, Integer>[] strengthPlateValorCelestialP1(ItemCache itemCache) {
        Tuple.Tuple2<Integer, Integer>[] filteredCelestialArray = SourcesOfItems.filterItemLevel(itemCache, strengthPlateCelestialArray(), 476);
        return ArrayUtil.concat(filteredCelestialArray, strengthPlateValorArray());
    }
}
