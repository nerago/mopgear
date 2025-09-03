package au.nerago.mopgear.io;

import au.nerago.mopgear.ItemUtil;
import au.nerago.mopgear.domain.CostedItem;
import au.nerago.mopgear.domain.SlotItem;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.ReforgeRules;
import au.nerago.mopgear.util.Tuple;
import au.nerago.mopgear.domain.EquipOptionsMap;
import au.nerago.mopgear.util.ArrayUtil;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "unused"})
public class SourcesOfItems {
    public static final List<Integer> ignoredItems = List.of(
            63207, // org port cloak
            84661, // fishing pole
            90042 // straw hat
    );

    public static CostedItem[] strengthPlateMsvArray() {
        return new CostedItem[]{
                // stone
                new CostedItem(85922, 101),
                new CostedItem(85925, 101),
//                new CostedItem(86134, 101), got heroic
                // feng
//                new CostedItem(85983, 102), got norm
                new CostedItem(85984, 102),
                new CostedItem(85985, 102),
                // garaj
//                new CostedItem(85991, 103), got norm
                new CostedItem(85992, 103),
                new CostedItem(89817, 103),
                // kings
                new CostedItem(86075, 104),
                new CostedItem(86076, 104),
                new CostedItem(86080, 104),
                // elegon
                new CostedItem(86130, 105), // prot weapon
                new CostedItem(86140, 105), // ret weapon
                new CostedItem(86135, 105), // got celestial
                // will
                new CostedItem(86144, 106),
                new CostedItem(86145, 106),
//                new CostedItem(89823, 106) // got norm
        };
    }

    public static CostedItem[] strengthPlateMsvHeroicArray() {
        return new CostedItem[]{
                // stone
                new CostedItem(87016, 201),
                new CostedItem(87015, 201),
//                new CostedItem(87060, 1), got heroic
                // feng
                new CostedItem(87026, 202),
                new CostedItem(87024, 202),
                new CostedItem(87025, 202),
                // garaj
                new CostedItem(89934, 203),
                new CostedItem(87035, 203),
                new CostedItem(87036, 203),
                // kings
                new CostedItem(87049, 204),
                new CostedItem(87048, 204),
                new CostedItem(87050, 204),
                // elegon
                new CostedItem(87062, 205), // prot weapon
//                new CostedItem(87061, 205), // ret weapon
                new CostedItem(87059, 205), // starcrusher
                new CostedItem(89937, 205),
                // will
                new CostedItem(89941, 206),
                new CostedItem(87071, 206),
                new CostedItem(87072, 206)
        };
    }

    public static CostedItem[] strengthPlateValorArray() {
        CostedItem neckParagonPale = new CostedItem(89066, 1250);
        CostedItem neckBloodseekers = new CostedItem(89064, 1250);
        CostedItem beltKlaxxiConsumer = new CostedItem(89056, 1750);
        CostedItem legKovokRiven = new CostedItem(89093, 2500);
        CostedItem backYiCloakCourage = new CostedItem(89075, 1250);
        CostedItem headYiLeastFavorite = new CostedItem(89216, 2500);
        CostedItem headVoiceAmpGreathelm = new CostedItem(89280, 2500);
        CostedItem chestDawnblade = new CostedItem(89420, 2500);
        CostedItem chestCuirassTwin = new CostedItem(89421, 2500);
        CostedItem gloveOverwhelmSwarm = new CostedItem(88746, 1750);
        CostedItem wristBattleShadow = new CostedItem(88880, 1250);
        CostedItem wristBraidedBlackWhite = new CostedItem(88879, 1250);
        CostedItem bootYulonGuardian = new CostedItem(88864, 1750);
        CostedItem bootTankissWarstomp = new CostedItem(88862, 1750);

        return new CostedItem[]{neckParagonPale, neckBloodseekers, beltKlaxxiConsumer, legKovokRiven, backYiCloakCourage, headYiLeastFavorite, headVoiceAmpGreathelm, chestDawnblade,
                chestCuirassTwin, gloveOverwhelmSwarm, wristBattleShadow, wristBraidedBlackWhite, bootYulonGuardian, bootTankissWarstomp};
    }

    public static CostedItem[] strengthPlatePvpArray() {
        return new CostedItem[]{
                new CostedItem(84794, 2250),
                new CostedItem(84806, 1250),
                new CostedItem(84807, 1250),
                new CostedItem(84810, 1750),
                new CostedItem(84822, 1750),
                new CostedItem(84828, 1250),
                new CostedItem(84829, 1250),
                new CostedItem(84834, 1750),
                new CostedItem(84851, 2250),
                new CostedItem(84870, 2250),
                new CostedItem(84891, 1250),
                new CostedItem(84892, 1250),
                new CostedItem(84915, 1750),
                new CostedItem(84949, 1750),
                new CostedItem(84950, 1750),
                new CostedItem(84985, 1250),
                new CostedItem(84986, 1250),
        };
    }

    public static CostedItem[] intellectLeatherValorArray() {
        return new CostedItem[]{
                new CostedItem(89308, 2500),
                new CostedItem(89342, 1750),
                new CostedItem(89432, 2500),
                new CostedItem(88885, 1250),
                new CostedItem(88743, 1750),
                new CostedItem(89061, 1750),
                new CostedItem(89089, 2500),
                new CostedItem(88876, 1750),
                new CostedItem(89078, 1250),
                new CostedItem(89067, 1250),
                new CostedItem(89073, 1250),
                new CostedItem(89072, 1250),
                new CostedItem(89081, 1750),
                new CostedItem(89080, 1750),
        };
    }

    public static CostedItem[] agilityLeatherValorArray() {
        return new CostedItem[]{
                new CostedItem(89341, 1750),
                new CostedItem(89060, 1750),
                new CostedItem(88884, 1250),
                new CostedItem(89300, 2500),
                new CostedItem(88744, 1750),
                new CostedItem(88868, 1750),
                new CostedItem(89090, 2500),
                new CostedItem(89431, 2500),

                new CostedItem(89067, 1250),
                new CostedItem(89065, 1250),
                new CostedItem(89070, 1250),
                new CostedItem(89082, 1750),
        };
    }

    public static CostedItem[] agilityLeatherCelestialArray() {
        return new CostedItem[]{
                new CostedItem(86804, 40),
                new CostedItem(86743, 40),
                new CostedItem(86750, 40),
                new CostedItem(86795, 40),
                new CostedItem(86763, 25),
                new CostedItem(89970, 25),
                new CostedItem(86782, 25),
                new CostedItem(89967, 25),
                new CostedItem(86776, 25),
                new CostedItem(86772, 40),
        };
    }

    public static CostedItem[] intellectLeatherCelestialArray() {
        return new CostedItem[]{
                new CostedItem(89957, 45),
                new CostedItem(86856, 30),
                new CostedItem(86644, 50),
                new CostedItem(86840, 30),
                new CostedItem(86645, 55),
                new CostedItem(86786, 25),
                new CostedItem(86768, 25),
                new CostedItem(86648, 50),
                new CostedItem(86746, 40),
                new CostedItem(86748, 25),
                new CostedItem(89971, 25),
                new CostedItem(86646, 55),
                new CostedItem(86878, 50),
                new CostedItem(86814, 30),
                new CostedItem(86873, 30),
                new CostedItem(86792, 40),
                new CostedItem(86907, 50),
                new CostedItem(86893, 50),
                new CostedItem(86808, 40),
                new CostedItem(86797, 40),
                new CostedItem(86806, 25),
                new CostedItem(89426, 15),
                new CostedItem(86754, 25),
                new CostedItem(86783, 25),
                new CostedItem(86810, 25),
                new CostedItem(86767, 25),
                new CostedItem(89968, 25),
        };
    }

    public static CostedItem[] intellectClothValorCelestialP1Array() {
        return new CostedItem[]{
                new CostedItem(89337, 2500),
                new CostedItem(89340, 1750),
                new CostedItem(89433, 2500),
                new CostedItem(88893, 1250),
                new CostedItem(88742, 1750),
                new CostedItem(89062, 1750),
                new CostedItem(89088, 2500),
                new CostedItem(88878, 1750),
                new CostedItem(89077, 1250),
                new CostedItem(89068, 1250),
                new CostedItem(89072, 1250),
                new CostedItem(89081, 1750),
                new CostedItem(86809, 40),
                new CostedItem(86770, 25),
                new CostedItem(86758, 40),
                new CostedItem(86787, 25),
                new CostedItem(89966, 25),
                new CostedItem(86798, 40),
                new CostedItem(89965, 40),
                new CostedItem(89971, 25),
                new CostedItem(86796, 25),
//                new CostedItem(86829,15 ),
                new CostedItem(86754, 25),
                new CostedItem(86810, 25),
                new CostedItem(86767, 25),
                new CostedItem(86773, 40),
                new CostedItem(86792, 40)
        };
    }

    public static CostedItem[] filterItemLevel(ItemCache itemCache, CostedItem[] existing, int maxItemLevel) {
        return Arrays.stream(existing)
                .map(tup -> Tuple.create(tup, ItemUtil.loadItemBasic(itemCache, tup.itemId())))
                .filter(tup2 -> tup2.b().itemLevel <= maxItemLevel)
                .map(Tuple.Tuple2::a)
                .toArray(CostedItem[]::new);
    }

    public static CostedItem[] filterExclude(CostedItem[] existing, List<Integer> exclude) {
        return Arrays.stream(existing)
                .filter(tup -> !exclude.contains(tup.itemId()))
                .toArray(CostedItem[]::new);
    }

    public static CostedItem[] strengthPlateCelestialArray() {
        // skipping neck cloak offhand weaps
        return new CostedItem[]{
                // plate
                new CostedItem(86803, 40),
                new CostedItem(86742, 40),
                new CostedItem(89976, 40),
                new CostedItem(89969, 25),
                new CostedItem(86752, 40),
                new CostedItem(89954, 45),
                new CostedItem(86751, 25),
                new CostedItem(86823, 30),
                new CostedItem(86860, 30),
                new CostedItem(86793, 40),
                new CostedItem(86868, 30),
                new CostedItem(89981, 30),
                new CostedItem(86852, 45),
                new CostedItem(86832, 45),
                new CostedItem(86794, 25),
                new CostedItem(86780, 25),
                new CostedItem(86779, 40),
                new CostedItem(89958, 45),
                new CostedItem(89956, 30),
                new CostedItem(86822, 45),
                new CostedItem(86904, 50),
                new CostedItem(86854, 45),
                new CostedItem(86849, 30),
                new CostedItem(86760, 40),
                new CostedItem(86848, 30),
                new CostedItem(89963, 45),
                new CostedItem(86903, 25),
                new CostedItem(86870, 50),
                new CostedItem(86891, 50),

                // ring
                new CostedItem(86820, 30),
                new CostedItem(86830, 30),
                new CostedItem(89972, 25),
                new CostedItem(86813, 30),
                new CostedItem(86880, 30),

                // weap
                new CostedItem(86799, 40),
                new CostedItem(86906, 30),
                new CostedItem(86789, 25),

                // neck
                new CostedItem(86759, 25),
                new CostedItem(86872, 25),
                new CostedItem(86739, 25),
                new CostedItem(86871, 30),
                new CostedItem(86835, 30),

                // back
                new CostedItem(86753, 25),
                new CostedItem(86812, 30),
                new CostedItem(86883, 25),
                new CostedItem(86853, 25)
        };
    }

    public static CostedItem[] strengthPallyTankSetCelestial() {
        return new CostedItem[]{
//                new CostedItem(86661, 75), // head
//                new CostedItem(86659, 75), // shoulder
                new CostedItem(86663, 55),
                new CostedItem(86662, 45),
                new CostedItem(86660, 55),
        };
    }

    public static CostedItem[] strengthPallyRetSetCelestial() {
        return new CostedItem[]{
//                new CostedItem(86681, 75), // head
//                new CostedItem(86679, 75), // shoulder
                new CostedItem(86683, 55),
                new CostedItem(86682, 45),
                new CostedItem(86680, 55),
        };
    }

    public static CostedItem[] strengthPlateHeartOfFear() {
        // skipping neck cloak offhand weaps
        return new CostedItem[]{
                new CostedItem(86154, 301),
                new CostedItem(89826, 301),
                new CostedItem(86203, 301),
                new CostedItem(86155, 301),
                new CostedItem(89828, 302),
                new CostedItem(86165, 302),
                new CostedItem(86164, 302),
                new CostedItem(86162, 302),
                new CostedItem(86174, 303),
                new CostedItem(89832, 303),
                new CostedItem(86177, 303),
                new CostedItem(86172, 303),
                new CostedItem(86202, 304),
                new CostedItem(86201, 304),
                new CostedItem(85322, 304),
                new CostedItem(86213, 305),
                new CostedItem(86219, 305),
                new CostedItem(85320, 305),
                new CostedItem(89837, 306),
                new CostedItem(85323, 306),
                new CostedItem(86191, 300),
                new CostedItem(86190, 300)
        };
    }

    public static CostedItem[] strengthPlateHeartOfFearHeroic() {
        // skipping neck cloak offhand weaps
        return new CostedItem[]{
                new CostedItem(86945, 401),
                new CostedItem(89919, 401),
                new CostedItem(86944, 401),
                new CostedItem(86946, 401),
                new CostedItem(89921, 402),
                new CostedItem(86956, 402),
                new CostedItem(86955, 402),
                new CostedItem(86957, 402),
                new CostedItem(86966, 403),
                new CostedItem(89923, 403),
                new CostedItem(86967, 403),
                new CostedItem(86968, 403),
                new CostedItem(86980, 404),
                new CostedItem(86979, 404),
                new CostedItem(87110, 404),
                new CostedItem(86986, 405),
                new CostedItem(86987, 405),
                new CostedItem(87112, 405),
                new CostedItem(89928, 406),
                new CostedItem(87109, 406),
        };
    }

    public static CostedItem[] strengthPlateCrafted() {
        // skipping neck cloak offhand weaps
        return new CostedItem[]{
                new CostedItem(87402, 0),
                new CostedItem(87406, 0),
                new CostedItem(87405, 0),
                new CostedItem(87407, 0),

                new CostedItem(82979, 0),
                new CostedItem(82980, 0),
                new CostedItem(82976, 0),
                new CostedItem(82975, 0),
        };
    }

    public static CostedItem[] bagItemsArray(List<Integer> skip) {
        CostedItem[] bagArray = InputBagsParser.readInput(DataLocation.bagsFile);
        if (skip == null)
            return bagArray;
        else
            return filterExclude(bagArray, skip);
    }

    public static CostedItem[] bagItemsArray(Path file, List<Integer> skip) {
        CostedItem[] bagArray = InputBagsParser.readInput(file);
        if (skip == null)
            return bagArray;
        else
            return filterExclude(bagArray, skip);
    }

    public static CostedItem[] strengthPlateCurrentItemsRet(ItemCache itemCache, ModelCombined model) {
        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearRetFile, ReforgeRules.ret(), null);
        Stream<CostedItem> itemStream = items.entryStream()
                .filter(it -> it.b()[0].slot != SlotItem.Weapon && it.b()[0].slot != SlotItem.Trinket && it.b()[0].slot != SlotItem.Ring)
                .map(tup -> new CostedItem(tup.b()[0].id, 0));
        return itemStream.toArray(CostedItem[]::new);
    }

    public static CostedItem[] strengthPlateCurrentItemsProt(ItemCache itemCache, ModelCombined model) {
        EquipOptionsMap items = ItemUtil.readAndLoad(itemCache, true, DataLocation.gearProtFile, ReforgeRules.ret(), null);
        Stream<CostedItem> itemStream = items.entryStream()
                .filter(it -> it.b()[0].slot != SlotItem.Weapon && it.b()[0].slot != SlotItem.Trinket && it.b()[0].slot != SlotItem.Ring)
                .map(tup -> new CostedItem(tup.b()[0].id, 0));
        return itemStream.toArray(CostedItem[]::new);
    }

    public static CostedItem[] strengthPlateValorCelestialTank(ItemCache itemCache) {
        CostedItem[] filteredCelestialArray = SourcesOfItems.filterItemLevel(itemCache, strengthPlateCelestialArray(), 483);
        return ArrayUtil.concat(filteredCelestialArray, strengthPlateValorArray(), strengthPallyTankSetCelestial());
    }

    public static CostedItem[] strengthPlateValorCelestialRet(ItemCache itemCache) {
        CostedItem[] filteredCelestialArray = SourcesOfItems.filterItemLevel(itemCache, strengthPlateCelestialArray(), 483);
        return ArrayUtil.concat(filteredCelestialArray, strengthPlateValorArray(), strengthPallyRetSetCelestial());
    }

    public static CostedItem[] intellectLeatherValorCelestial(ItemCache itemCache) {
        CostedItem[] filteredCelestialArray = SourcesOfItems.filterItemLevel(itemCache, intellectLeatherCelestialArray(), 483);
        return ArrayUtil.concat(filteredCelestialArray, intellectLeatherValorArray());
    }

    public static CostedItem[] get(String param) {
        try {
            return (CostedItem[]) SourcesOfItems.class.getMethod(param).invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
