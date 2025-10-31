package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.Tuple;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ReadLog {
    static final private Path path = Path.of("C:\\games\\World of Warcraft\\_classic_\\Logs\\warcraftlogsarchive\\WoWCombatLog-102325_212056.txt");

    private final Map<String, String> names = new HashMap<>();
    private final Map<Tuple.Tuple2<String, SpecType>, List<LogItemInfo>> gearMap = new HashMap<>();

    private final Set<Integer> ignoreItems = Set.of(89193, 69210, 53, 43157, 45583); // shirt/tabard

    public List<LogPlayerInfo> run() {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            parseReader(reader);
            return gearMap.entrySet().stream().map(e -> new LogPlayerInfo(e.getKey().a(), e.getKey().b(), e.getValue())).toList();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void parseReader(BufferedReader reader) throws IOException {
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            } else if (line.contains("COMBATANT_INFO")) {
                parseCombatant(line);
            } else if (line.contains("SPELL_AURA_APPLIED")) {
                parseAuraPlayerName(line);
            }
        }
    }

    private void parseCombatant(String line) throws IOException {
        String playerId = betweenFirstDeliminatorPair(line, ',', ',');
        String playerName = names.get(playerId);
        if (playerName == null || playerName.isBlank())
            throwParseError("Unknown player " + playerId);

//        if (playerName.contains("Viiolate") || playerName.contains("Ragnoroth") || playerName.contains("Komui") || playerName.contains("Neravi"))
//            System.out.println(line);

//        String[] lineParts = line.split(",");
//        int specId = Integer.parseInt(lineParts[24]);
//        System.out.printf("%s %d\n", playerName, specId);

        String gearInfo = betweenFirstDeliminatorPair(line, '[', ']');

        List<LogItemInfo> itemInfoList = new ArrayList<>();
        StringReader input = new StringReader(gearInfo);
        while (true) {
            int firstChar = input.read();
            if (firstChar == -1)
                break;
            else if (firstChar != '(')
                throwParseError("expected (");

            int itemId = readRequiredInt(input);
            readExpected(input, ',');

            int itemLevel = readRequiredInt(input);
            readExpected(input, ",(");

            OptionalInt enchantId = readOptionalInt(input);
            if (enchantId.isPresent()) {
                readExpected(input, ',');
                readRequiredInt(input); // temporary shaman enchant
                readExpected(input, ',');
                int engineerEnchant = readRequiredInt(input);
                if (engineerEnchant != 0 && engineerEnchant != 4223 && engineerEnchant != 4898 && engineerEnchant != 4897) // nitro boosts, synapse springs, goblin glider
                    throwParseError("unknown enchant " + engineerEnchant);
                readExpected(input, "),(),(");
            } else {
                readExpected(input, "),(),(");
            }

            ArrayList<Integer> gemIdList = new ArrayList<>();
            while (true) {
                OptionalInt gemId = readOptionalInt(input);
                if (gemId.isPresent()) {
                    gemIdList.add(gemId.getAsInt());
                    readExpected(input, ',');
                    readRequiredInt(input); // gem item level
                    char c = (char) input.read();
                    if (c == ')')
                        break;
                } else {
                    readExpected(input, ')');
                    break;
                }
            }

            if (itemId != 0 && !ignoreItems.contains(itemId)) {
                LogItemInfo info = new LogItemInfo(itemId, itemLevel, enchantId, gemIdList.stream().mapToInt(x -> x).toArray());
                itemInfoList.add(info);
            }

            readExpected(input, ')');
            char lastChar = (char) input.read();
            if (lastChar == ')')
                break;
        }

        printInfo(playerName, itemInfoList);
        SpecType spec = PlayerSpecs.findSpec(itemInfoList);

        gearMap.put(Tuple.create(playerName, spec), itemInfoList);
    }

    private void printInfo(String playerName, List<LogItemInfo> itemInfoList) {
        StringBuilder build = new StringBuilder();
        build.append(playerName).append(": ");
        for (LogItemInfo item : itemInfoList) {
            FullItemData itemData = ItemCache.instance.get(item.itemId(), 0);
            if (itemData == null)
                throw new RuntimeException("unknown item " + item.itemId());
//            WowSimDB.instance.lookupItem(ItemRef.buildBasic())
            build.append(itemData.shared.name()).append(" | ");
        }
        System.out.println(build);
    }

    private static int readRequiredInt(StringReader input) throws IOException {
        char c = (char) input.read();
        if (!Character.isDigit(c)) {
            throwParseError("expected digit");
        }

        StringBuilder str = new StringBuilder();
        str.append(c);

        while (true) {
            input.mark(1);
            c = (char) input.read();
            if (Character.isDigit(c)) {
                str.append(c);
            } else {
                input.reset();
                break;
            }
        }

        return Integer.parseInt(str.toString());
    }

    private static OptionalInt readOptionalInt(StringReader input) throws IOException {
        StringBuilder str = new StringBuilder();

        while (true) {
            input.mark(1);
            char c = (char) input.read();
            if (Character.isDigit(c)) {
                str.append(c);
            } else {
                input.reset();
                break;
            }
        }

        if (!str.isEmpty())
            return OptionalInt.of(Integer.parseInt(str.toString()));
        else
            return OptionalInt.empty();
    }

    private static void readExpected(StringReader input, char expect) throws IOException {
        char c = (char) input.read();
        if (c != expect)
            throwParseError("expected " + expect);
    }

    private static void readExpected(StringReader input, String expect) throws IOException {
        for (int i = 0; i < expect.length(); ++i) {
            readExpected(input, expect.charAt(i));
        }
    }

    private static String betweenFirstDeliminatorPair(String line, char charStart, char charEnd) {
        int first = line.indexOf(charStart);
        int second = line.indexOf(charEnd, first + 1);
        return line.substring(first + 1, second);
    }

    private static void throwParseError(String msg) {
        throw new IllegalStateException(msg);
    }

    private void parseAuraPlayerName(String line) {
        String[] parts = line.split(",");
        String playerId = parts[1];
        if (playerId.startsWith("Player")) {
            String fullName = parts[2];
            String name = fullName.substring(1).split("-")[0];
            names.put(playerId, name);
        }
    }

    // ENCOUNTER_START,1505,"Tsulong",5,10,996,19
    // COMBATANT_INFO,Player-4385-05E82065,0,268,196,19478,21368,8699,0,0,0,2011,2011,2011,4807,4807,4807,4289,4289,4289,4289,0,0,52163,0,
    // (,2,1,1,2,0,0,-1),(),[(85346,504,(),(),(76879,90,76686,90)),(86205,504,(),(),()),(85344,504,(4915,0,0),(),(76686,90)),(0,0,(),(),()),
    // (86158,504,(4419,0,0),(),(76628,88,76606,88)),(86086,497,(),(),(76660,90,76572,88,76572,88)),(85345,504,(4826,0,0),(),(76628,88)),
    // (90445,504,(4429,0,0),(),()),(87043,510,(4414,0,0),(),()),(85347,504,(4430,0,0),(),()),(90525,511,(),(),()),(89803,497,(),(),()),
    // (89080,497,(),(),()),(79330,484,(),(),()),(89078,497,(4423,0,0),(),()),(86227,504,(4442,0,0),(),(89882,513)),(87039,510,(4434,0,0),(),()),
    // (0,0,(),(),())],
    // [Player-4385-05E82065,105691,1,Player-4385-05E7CDDD,77747,1,Player-4385-05E7CDDD,116956,1,Player-4385-05E7CDD6,113742,1,Player-4385-05E89729,1459,1,Player-4385-05E852E3,20217,1],0,0,(988,1068,987,196,0,701)
    // 10/16/2025 22:55:55.72111  SPELL_AURA_REMOVED,Player-4385-05E82065,"Iniles-Galakras-US",0x514,0x80000000,Player-4385-05E82065,"Iniles-Galakras-US",0x514,0x80000000,114695,"Pursuit of Justice",0x1,BUFF

    // COMBATANT_INFO,Player-4385-05E852E3,0,19410,196,28453,206,201,1871,1695,0,1518,1518,1518,8295,8295,8295,2829,2829,2829,3751,0,0,56674,340793600,
    // (,0,0,2,2,0,2,-1),(),[(87024,510,(),(),(76886,90,76576,88)),(87036,510,(),(),()),(86659,491,(4805,0,0),(),(76642,90)),(0,0,(),(),()),(85323,504,(4419,0,0),(),(76699,90,76699,90)),(87060,510,(0,0,4223),(),(76633,88,76576,88,76699,90)),(85320,504,(4823,0,0),(),(76667,90)),(86979,517,(4429,0,0),(),(76699,90)),(89934,510,(4411,0,0),(),(76699,90)),(86662,491,(4433,0,4898),(),(76699,90)),(86957,517,(),(),()),(90862,497,(),(),()),(86802,484,(),(),()),(79329,484,(),(),()),(87026,510,(4424,0,4897),(),()),(86906,491,(4444,0,0),(),(89881,513,76699,90)),(86075,497,(4993,0,0),(),()),(0,0,(),(),())],[Player-4385-05E88516,34477,1,Player-4385-05E852E3,25780,1,Player-4385-05E7CDD6,113742,1,Player-4385-05E7CEBE,19506,1,Player-4385-05E852E3,105696,1,Pet-0-4391-996-11480-18128-0100C5C9A2,135678,1,Player-4385-05E852E3,104272,1,Player-4385-05E852E3,20217,1,Player-4385-05E89729,1459,1,Player-4385-05E88516,19506,1,Player-4385-05E81EA7,19506,1],0,0,(456,194,457,704,454,0)
    // 10/16/2025 22:55:55.76311  SPELL_AURA_APPLIED,Player-4385-05E852E3,"Neravi-Galakras-US",0x40511,0x80000000,Player-4385-05E852E3,"Neravi-Galakras-US",0x40511,0x80000000,132365,"Vengeance",0x1,BUFF

    // COMBATANT_INFO,playerGUID,Strength,Agility,Stamina,Intelligence,Dodge,Parry,Block,CritMelee,CritRanged,CritSpell,Speed,Lifesteal,HasteMelee,HasteRanged,HasteSpell,Avoidance,Mastery,VersatilityDamageDone,VersatilityHealingDone,VersatilityDamageTaken,Armor,
    // CurrentSpecID,(Class Talent 1, ...),(PvP Talent 1, ...),[Artifact Trait ID 1, Trait Effective Level 1, ...],
    // [(Equipped Item ID 1,Equipped Item iLvL 1,(Permanent Enchant ID, Temp Enchant ID, On Use Spell Enchant ID),(Bonus List ID 1, ...),(Gem ID 1, Gem iLvL 1, ...)) ...],
    // [Interesting Aura Caster GUID 1, Interesting Aura Spell ID 1, ...]

    // COMBATANT_INFO,Player-4385-05E82065,0,268,  196,19478,21368,8699,   0,0,   0,2011,2011,2011,4807,4807,4807,4289,4289,4289,4289,0,0,52163,0,
    // COMBATANT_INFO,Player-4385-05E852E3,0,19410,196,28453,206,   201,1871,1695,0,1518,1518,1518,8295,8295,8295,2829,2829,2829,3751,0,0,56674,340793600,

}
