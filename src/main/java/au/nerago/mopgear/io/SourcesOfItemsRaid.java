package au.nerago.mopgear.io;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.Tuple;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"unused"})
public class SourcesOfItemsRaid {
    @SuppressWarnings("unchecked")
    private static final Tuple.Tuple4<Integer, String, String, String>[] spreadSheetHeroicIds = new Tuple.Tuple4[] {
            Tuple.create(87014,"Stonemaw Armguards", "Wrist","Dogs MSV"),
            Tuple.create(87018,"Cape of Three Lanterns", "Back","Dogs MSV"),
            Tuple.create(87015,"Jasper Clawfeet", "Feet","Dogs MSV"),
            Tuple.create(87060,"Star-Stealer Waistguard", "Waist","Dogs MSV"),
            Tuple.create(87019,"Stonebound Cinch", "Waist","Dogs MSV"),
            Tuple.create(87013,"Stoneflesh Leggings", "Legs","Dogs MSV"),
            Tuple.create(87016,"Beads of the Mogu'shi", "Neck","Dogs MSV"),
            Tuple.create(87020,"Sixteen-Fanged Crown", "Head","Dogs MSV"),
            Tuple.create(89931,"Claws of Amethyst", "Hands","Dogs MSV"),
            Tuple.create(87017,"Jade Dust Leggings", "Legs","Dogs MSV"),
            Tuple.create(87021,"Heavenly Jade Greatboots", "Feet","Dogs MSV"),
            Tuple.create(89929,"Stonefang Chestguard", "Chest","Dogs MSV"),
            Tuple.create(89930,"Ruby-Linked Girdle", "Waist","Dogs MSV"),
            Tuple.create(87012,"Dagger of the Seven Stars", "Weapon","Dogs MSV"),
            Tuple.create(87057,"Bottle of Infinite Stars", "Trinket","Elegon MSV"),
            Tuple.create(87065,"Light of the Cosmos", "Trinket","Elegon MSV"),
            Tuple.create(87064,"Orbital Belt", "Waist","Elegon MSV"),
            Tuple.create(89937,"Band of Bursting Novas", "Finger","Elegon MSV"),
            Tuple.create(87067,"Phasewalker Striders", "Feet","Elegon MSV"),
            Tuple.create(87059,"Starcrusher Gauntlets", "Hands","Elegon MSV"),
            Tuple.create(87058,"Chestguard of Total Annihilation", "Chest","Elegon MSV"),
            Tuple.create(89938,"Galaxyfire Girdle", "Waist","Elegon MSV"),
            Tuple.create(87068,"Shoulders of Empyreal Focus", "Shoulder","Elegon MSV"),
            Tuple.create(87063,"Vial of Dragon's Blood", "Trinket","Elegon MSV"),
            Tuple.create(89939,"Crown of Keening Stars", "Head","Elegon MSV"),
            Tuple.create(87062,"Elegion the Fanged Crescent", "Weapon","Elegon MSV"),
            Tuple.create(87061,"Starshatter", "Weapon","Elegon MSV"),
            Tuple.create(87066,"Torch of the Celestial Spark", "Weapon","Elegon MSV"),
            Tuple.create(89425,"Fan of Fiery Winds", "Held In Off-hand","Feng MSV"),
            Tuple.create(89933,"Feng's Ring of Dreams", "Finger","Feng MSV"),
            Tuple.create(87024,"Nullification Greathelm", "Head","Feng MSV"),
            Tuple.create(89932,"Feng's Seal of Binding", "Finger","Feng MSV"),
            Tuple.create(87028,"Amulet of Seven Curses", "Neck","Feng MSV"),
            Tuple.create(87022,"Tomb Raider's Girdle", "Waist","Feng MSV"),
            Tuple.create(87026,"Cloak of Peacock Feathers", "Back","Feng MSV"),
            Tuple.create(87030,"Chain of Shadow", "Waist","Feng MSV"),
            Tuple.create(87025,"Bracers of Six Oxen", "Wrist","Feng MSV"),
            Tuple.create(87029,"Hood of Cursed Dreams", "Head","Feng MSV"),
            Tuple.create(87023,"Wildfire Worldwalkers", "Feet","Feng MSV"),
            Tuple.create(87027,"Imperial Ghostbinder's Robes", "Chest","Feng MSV"),
            Tuple.create(87031,"Legplates of Sagacious Shadows", "Legs","Feng MSV"),
            Tuple.create(87044,"Arrow Breaking Windcloak", "Back","Feng MSV"),
            Tuple.create(87040,"Circuit of the Frail Soul", "Finger","Gara'jal MSV"),
            Tuple.create(87039,"Eye of the Ancient Spirit", "Shield","Gara'jal MSV"),
            Tuple.create(87038,"Shadowsummoner Spaulders", "Shoulder","Gara'jal MSV"),
            Tuple.create(89934,"Bonded Soul Bracers", "Wrist","Gara'jal MSV"),
            Tuple.create(87036,"Soulgrasp Choker", "Neck","Gara'jal MSV"),
            Tuple.create(87033,"Netherrealm Shoulderpads", "Shoulder","Gara'jal MSV"),
            Tuple.create(87034,"Fetters of Death", "Waist","Gara'jal MSV"),
            Tuple.create(87035,"Sollerets of Spirit Splitting", "Feet","Gara'jal MSV"),
            Tuple.create(87041,"Spaulders of the Divided Mind", "Shoulder","Gara'jal MSV"),
            Tuple.create(87037,"Sandals of the Severed Soul", "Feet","Gara'jal MSV"),
            Tuple.create(87042,"Leggings of Imprisoned Will", "Legs","Gara'jal MSV"),
            Tuple.create(87043,"Bindings of Ancient Spirits", "Wrist","Gara'jal MSV"),
            Tuple.create(87032,"Gara’kal, Fist of the Spiritbinder", "Weapon","Gara'jal MSV"),
            Tuple.create(87054,"Bracers of Dark Thoughts", "Wrist","Kings MSV"),
            Tuple.create(87053,"Zian's Choker of Coalesced Shadow", "Neck","Kings MSV"),
            Tuple.create(87045,"Amulet of the Hidden Kings", "Neck","Kings MSV"),
            Tuple.create(89935,"Bracers of Violent Meditation", "Wrist","Kings MSV"),
            Tuple.create(89936,"Mindshard Drape", "Back","Kings MSV"),
            Tuple.create(87050,"Steelskin, Qiang's Impervious Shield", "Shield","Kings MSV"),
            Tuple.create(87046,"Screaming Tiger, Qiang’s Unbreakable Polearm", "Weapon","Kings MSV"),
            Tuple.create(87051,"Hood of Blind Eyes", "Head","Kings MSV"),
            Tuple.create(87048,"Breastplate of the Kings' Guard", "Chest","Kings MSV"),
            Tuple.create(87055,"Meng's Treads of Insanity", "Feet","Kings MSV"),
            Tuple.create(87049,"Shoulderguards of the Unflanked", "Shoulder","Kings MSV"),
            Tuple.create(87052,"Undying Shadow Grips", "Hands","Kings MSV"),
            Tuple.create(87047,"Subetai's Pillaging Leggings", "Legs","Kings MSV"),
            Tuple.create(87056,"Girdle of Delirious Visions", "Waist","Kings MSV"),
            Tuple.create(87072,"Lei Shen's Final Orders", "Trinket","Will MSV"),
            Tuple.create(87076,"Worldwaker Cabochon", "Neck","Will MSV"),
            Tuple.create(87075,"Qin-xi's Polarizing Seal", "Trinket","Will MSV"),
            Tuple.create(87070,"Crown of Opportunistic Strikes", "Head","Will MSV"),
            Tuple.create(87073,"Hood of Focused Energy", "Head","Will MSV"),
            Tuple.create(87071,"Jang-xi's Devastating Legplates", "Legs","Will MSV"),
            Tuple.create(87074,"Tihan, Scepter of the Sleeping Emperor", "Weapon","Will MSV"),
            Tuple.create(87069,"Fang Kung, Spark of Titans", "Weapon","Will MSV"),
            Tuple.create(89940,"Dreadeye Gaze", "Head","Will MSV"),
            Tuple.create(89941,"Chestguard of Eternal Vigilance", "Chest","Will MSV"),
            Tuple.create(87077,"Magnetized Leggings", "Legs","Will MSV"),
            Tuple.create(87078,"Spaulders of the Emperor's Rage", "Shoulder","Will MSV"),
            Tuple.create(87825,"Grips of Terra Cotta", "Hands","Will MSV"),
            Tuple.create(89942,"Enameled Grips of Solemnity", "Hands","Will MSV"),
            Tuple.create(86986,"Shoulderpads of Misshapen Life", "Shoulder","Amber-Shaper HOF"),
            Tuple.create(86982,"Seal of the Profane", "Finger","Amber-Shaper HOF"),
            Tuple.create(86981,"Belt of Malleable Amber", "Waist","Amber-Shaper HOF"),
            Tuple.create(86984,"Treads of Deadly Secretions", "Feet","Amber-Shaper HOF"),
            Tuple.create(86985,"Monstrous Stompers", "Feet","Amber-Shaper HOF"),
            Tuple.create(86960,"Tornado-Summoning Censer", "Held In Off-hand","Blade Lord HOF"),
            Tuple.create(86955,"Waistplate of Overwhelming Assault", "Waist","Blade Lord HOF"),
            Tuple.create(86961,"Drape of Gathering Clouds", "Back","Blade Lord HOF"),
            Tuple.create(86957,"Ring of the Bladed Tempest", "Finger","Blade Lord HOF"),
            Tuple.create(86954,"Bracers of Unseen Strikes", "Wrist","Blade Lord HOF"),
            Tuple.create(86953,"Choker of the Unleashed Storm", "Neck","Blade Lord HOF"),
            Tuple.create(89922,"Hood of Stilled Winds", "Head","Blade Lord HOF"),
            Tuple.create(89921,"Pauldrons of the Broken Blade", "Shoulder","Blade Lord HOF"),
            Tuple.create(86958,"Twisting Wind Bracers", "Wrist","Blade Lord HOF"),
            Tuple.create(86959,"Boots of the Blowing Wind", "Feet","Blade Lord HOF"),
            Tuple.create(86962,"Bracers of Tempestuous Fury", "Wrist","Blade Lord HOF"),
            Tuple.create(86956,"Windblade Talons", "Hands","Blade Lord HOF"),
            Tuple.create(90740,"Kaz'tik's Stormseizer Gauntlets", "Hands","Blade Lord HOF"),
            Tuple.create(89920,"Sword Dancer's Leggings", "Legs","Blade Lord HOF"),
            Tuple.create(89926,"Shadow Heart Spaulders", "Shoulder","Empress HOF"),
            Tuple.create(86989,"Leggings of Shadow Infestation", "Legs","Empress HOF"),
            Tuple.create(89928,"Legplates of Regal Reinforcement", "Legs","Empress HOF"),
            Tuple.create(89927,"Hood of Dark Dreams", "Head","Empress HOF"),
            Tuple.create(86991,"Crown of the Doomed Empress", "Head","Empress HOF"),
            Tuple.create(86865,"Kri'tak, Imperial Scepter of the Swarm", "Weapon","Empress HOF"),
            Tuple.create(86949,"Fragment of Fear Made Flesh", "Finger","Vizier HOF"),
            Tuple.create(86948,"Attenuating Bracers", "Wrist","Vizier HOF"),
            Tuple.create(89919,"Warbelt of Sealed Pods", "Waist","Vizier HOF"),
            Tuple.create(86945,"Hisek's Chrysanthemum Cape", "Back","Vizier HOF"),
            Tuple.create(89918,"Scent-Soaked Sandals", "Feet","Vizier HOF"),
            Tuple.create(89917,"Pheromone-Coated Choker", "Neck","Vizier HOF"),
            Tuple.create(86943,"Boots of the Still Breath", "Feet","Vizier HOF"),
            Tuple.create(86947,"Gloves of Grasping Claws", "Hands","Vizier HOF"),
            Tuple.create(86951,"Mail of Screaming Secrets", "Chest","Vizier HOF"),
            Tuple.create(86946,"Vizier's Ruby Signet", "Finger","Vizier HOF"),
            Tuple.create(86950,"Gauntlets of Undesired Gifts", "Hands","Vizier HOF"),
            Tuple.create(86952,"Chestplate of the Forbidden Tower", "Chest","Vizier HOF"),
            Tuple.create(86944,"Articulated Legplates", "Legs","Vizier HOF"),
            Tuple.create(87822,"Zor'lok's Fizzing Chestguard", "Chest","Vizier HOF"),
            Tuple.create(86974,"Painful Thorned Ring", "Finger","Wind Lord HOF"),
            Tuple.create(86976,"Korven's Amber-Sealed Beetle", "Neck","Wind Lord HOF"),
            Tuple.create(86979,"Impaling Treads", "Feet","Wind Lord HOF"),
            Tuple.create(86975,"Robes of Torn Nightmares", "Chest","Wind Lord HOF"),
            Tuple.create(86980,"Cloak of Raining Blades", "Back","Wind Lord HOF"),
            Tuple.create(86978,"Wingslasher Pauldrons", "Shoulder","Wind Lord HOF"),
            Tuple.create(86977,"Clutches of Dying Hope", "Hands","Wind Lord HOF"),
            Tuple.create(86963,"Legbreaker Greatcloak", "Back","Garalon HOF"),
            Tuple.create(86967,"Necklace of Congealed Weaknesses", "Neck","Garalon HOF"),
            Tuple.create(86971,"Stormwake Mistcloak", "Back","Garalon HOF"),
            Tuple.create(86969,"Sandals of the Unbidden", "Feet","Garalon HOF"),
            Tuple.create(86970,"Xaril's Hood of Intoxicating Vapors", "Head","Garalon HOF"),
            Tuple.create(86966,"Garalon's Hollow Skull", "Head","Garalon HOF"),
            Tuple.create(86968,"Ring of the Shattered Shell", "Finger","Garalon HOF"),
            Tuple.create(89924,"Shoulders of Foaming Fluids", "Shoulder","Garalon HOF"),
            Tuple.create(86972,"Robes of Eighty Lights", "Chest","Garalon HOF"),
            Tuple.create(89923,"Garalon's Graven Carapace", "Chest","Garalon HOF"),
            Tuple.create(86965,"Grips of the Leviathan", "Hands","Garalon HOF"),
            Tuple.create(86964,"Bonebreaker Gauntlets", "Hands","Garalon HOF"),
            Tuple.create(89925,"Vestments of Steaming Ichor", "Chest","Garalon HOF"),
            Tuple.create(86973,"Grasps of Panic", "Hands","Garalon HOF"),
            Tuple.create(87169,"Robes of the Unknown Fear", "Chest","Lei Shi ToES"),
            Tuple.create(87171,"Cuirass of the Animated Protector", "Chest","Lei Shi ToES"),
            Tuple.create(87167,"Terror in the Mists", "Trinket","Lei Shi ToES"),
            Tuple.create(87172,"Darkmist Vortex", "Trinket","Lei Shi ToES"),
            Tuple.create(87150,"Cloak of Overwhelming Corruption", "Back","Protectors ToES"),
            Tuple.create(87153,"Asani's Uncleansed Sandals", "Feet","Protectors ToES"),
            Tuple.create(87154,"Lightning Prisoner's Boots", "Feet","Protectors ToES"),
            Tuple.create(87146,"Deepwater Greatboots", "Feet","Protectors ToES"),
            Tuple.create(87151,"Watersoul Signet", "Finger","Protectors ToES"),
            Tuple.create(87144,"Regail's Band of the Endless", "Finger","Protectors ToES"),
            Tuple.create(87155,"Casque of Expelled Corruption", "Head","Protectors ToES"),
            Tuple.create(89943,"Legguards of Failing Purification", "Legs","Protectors ToES"),
            Tuple.create(87147,"Shackle of Eversparks", "Neck","Protectors ToES"),
            Tuple.create(87148,"Kaolan's Withering Necklace", "Neck","Protectors ToES"),
            Tuple.create(89944,"Waterborne Shoulderguards", "Shoulder","Protectors ToES"),
            Tuple.create(87152,"Regail’s Crackling Dagger", "Weapon","Protectors ToES"),
            Tuple.create(87145,"Bracers of Defiled Earth", "Wrist","Protectors ToES"),
            Tuple.create(87149,"Cuffs of the Corrupted Waters", "Wrist","Protectors ToES"),
            Tuple.create(89950,"Wrap of Instant Petrification", "Chest","Sha ToES"),
            Tuple.create(89949,"Robes of Pinioned Eyes", "Chest","Sha ToES"),
            Tuple.create(87174,"Dreadwoven Leggings of Failure", "Legs","Sha ToES"),
            Tuple.create(87175,"Essence of Terror", "Trinket","Sha ToES"),
            Tuple.create(89951,"Shadowgrip Girdle", "Waist","Sha ToES"),
            Tuple.create(87173,"Kilrak, Jaws of Terror", "Weapon","Sha ToES"),
            Tuple.create(87176,"Shin'ka, Execution of Domination", "Weapon","Sha ToES"),
            Tuple.create(87159,"Daybreak Drape", "Back","Tsulong ToES"),
            Tuple.create(87157,"Sunwrought Mail Hauberk", "Chest","Tsulong ToES"),
            Tuple.create(87162,"Sandals of the Blackest Night", "Feet","Tsulong ToES"),
            Tuple.create(87165,"Sollerets of Instability", "Feet","Tsulong ToES"),
            Tuple.create(87158,"Dread Shadow Ring", "Finger","Tsulong ToES"),
            Tuple.create(89945,"Gauntlets of the Shadow's Caress", "Hands","Tsulong ToES"),
            Tuple.create(89946,"Grasps of Serpentine Might", "Hands","Tsulong ToES"),
            Tuple.create(89948,"Fear-Blackened Leggings", "Legs","Tsulong ToES"),
            Tuple.create(89947,"Shoulderpads of Twisted Fate", "Shoulder","Tsulong ToES"),
            Tuple.create(87163,"Spirits of the Sun", "Trinket","Tsulong ToES"),
            Tuple.create(87160,"Stuff of Nightmares", "Trinket","Tsulong ToES"),
            Tuple.create(87161,"Belt of Embodied Terror", "Waist","Tsulong ToES"),
            Tuple.create(87183,"Binder's Chain of Unending Summer", "Waist","Tsulong ToES"),
            Tuple.create(87181,"Weaver's Cord of Eternal Autumn", "Waist","Tsulong ToES"),
            Tuple.create(87184,"Mender's Girdle of Endless Spring", "Waist","Tsulong ToES"),
            Tuple.create(87177,"Invoker's Belt of Final Winter", "Waist","Tsulong ToES"),
            Tuple.create(87179,"Sorcerer's Belt of Final Winter", "Waist","Tsulong ToES"),
            Tuple.create(87180,"Stalker's Cord of Eternal Autumn", "Waist","Tsulong ToES"),
            Tuple.create(87186,"Patroller's Girdle of Endless Spring", "Waist","Tsulong ToES"),
            Tuple.create(87182,"Ranger's Chain of Unending Summer", "Waist","Tsulong ToES"),
            Tuple.create(87185,"Protector's Girdle of Endless Spring", "Waist","Tsulong ToES"),
            Tuple.create(87178,"Healer's Belt of Final Winter", "Waist","Tsulong ToES"),
            Tuple.create(87183,"Binder's Chain of Unending Summer", "Waist","Lei Shi ToES"),
            Tuple.create(87181,"Weaver's Cord of Eternal Autumn", "Waist","Lei Shi ToES"),
            Tuple.create(87184,"Mender's Girdle of Endless Spring", "Waist","Lei Shi ToES"),
            Tuple.create(87177,"Invoker's Belt of Final Winter", "Waist","Lei Shi ToES"),
            Tuple.create(87179,"Sorcerer's Belt of Final Winter", "Waist","Lei Shi ToES"),
            Tuple.create(87180,"Stalker's Cord of Eternal Autumn", "Waist","Lei Shi ToES"),
            Tuple.create(87186,"Patroller's Girdle of Endless Spring", "Waist","Lei Shi ToES"),
            Tuple.create(87182,"Ranger's Chain of Unending Summer", "Waist","Lei Shi ToES"),
            Tuple.create(87185,"Protector's Girdle of Endless Spring", "Waist","Lei Shi ToES"),
            Tuple.create(87178,"Healer's Belt of Final Winter", "Waist","Lei Shi ToES"),
            Tuple.create(87164,"Loshan, Terror Incarnate", "Weapon","Tsulong ToES"),
            Tuple.create(87156,"Gao-Rei, Staff of the Legendary Protector", "Weapon","Tsulong ToES"),
            Tuple.create(87002,"Yaungol Slayer's Tunic", "Chest","Empress HOF"),
            Tuple.create(87109,"White Tiger Chestguard", "Chest","Empress HOF"),
            Tuple.create(87104,"White Tiger Breastplate", "Chest","Empress HOF"),
            Tuple.create(87099,"White Tiger Battleplate", "Chest","Empress HOF"),
            Tuple.create(87124,"Tunic of the Thousandfold Blades", "Chest","Empress HOF"),
            Tuple.create(87190,"Sha-Skin Robes", "Chest","Empress HOF"),
            Tuple.create(87010,"Robes of the Burning Scroll", "Chest","Empress HOF"),
            Tuple.create(87092,"Red Crane Vest", "Chest","Empress HOF"),
            Tuple.create(87084,"Red Crane Tunic", "Chest","Empress HOF"),
            Tuple.create(87094,"Red Crane Chestguard", "Chest","Empress HOF"),
            Tuple.create(87117,"Guardian Serpent Robes", "Chest","Empress HOF"),
            Tuple.create(87122,"Guardian Serpent Raiment", "Chest","Empress HOF"),
            Tuple.create(87129,"Firebird's Tunic", "Chest","Empress HOF"),
            Tuple.create(87139,"Firebird's Hauberk", "Chest","Empress HOF"),
            Tuple.create(87134,"Firebird's Cuirass", "Chest","Empress HOF"),
            Tuple.create(86936,"Eternal Blossom Vestment", "Chest","Empress HOF"),
            Tuple.create(86938,"Eternal Blossom Tunic", "Chest","Empress HOF"),
            Tuple.create(86931,"Eternal Blossom Robes", "Chest","Empress HOF"),
            Tuple.create(86923,"Eternal Blossom Raiment", "Chest","Empress HOF"),
            Tuple.create(86918,"Chestguard of the Lost Catacomb", "Chest","Empress HOF"),
            Tuple.create(87197,"Chestguard of Resounding Rings", "Chest","Empress HOF"),
            Tuple.create(86913,"Breastplate of the Lost Catacomb", "Chest","Empress HOF"),
            Tuple.create(87193,"Battleplate of Resounding Rings", "Chest","Empress HOF"),
            Tuple.create(87003,"Yaungol Slayer's Gloves", "Hands","Wind Lord HOF"),
            Tuple.create(87110,"White Tiger Handguards", "Hands","Wind Lord HOF"),
            Tuple.create(87105,"White Tiger Gloves", "Hands","Wind Lord HOF"),
            Tuple.create(87100,"White Tiger Gauntlets", "Hands","Wind Lord HOF"),
            Tuple.create(87187,"Sha-Skin Gloves", "Hands","Wind Lord HOF"),
            Tuple.create(87089,"Red Crane Handwraps", "Hands","Wind Lord HOF"),
            Tuple.create(87085,"Red Crane Grips", "Hands","Wind Lord HOF"),
            Tuple.create(87095,"Red Crane Gauntlets", "Hands","Wind Lord HOF"),
            Tuple.create(86919,"Handguards of the Lost Catacomb", "Hands","Wind Lord HOF"),
            Tuple.create(87198,"Handguards of Resounding Rings", "Hands","Wind Lord HOF"),
            Tuple.create(87114,"Guardian Serpent Handwraps", "Hands","Wind Lord HOF"),
            Tuple.create(87119,"Guardian Serpent Gloves", "Hands","Wind Lord HOF"),
            Tuple.create(87125,"Gloves of the Thousandfold Blades", "Hands","Wind Lord HOF"),
            Tuple.create(87007,"Gloves of the Burning Scroll", "Hands","Wind Lord HOF"),
            Tuple.create(86914,"Gauntlets of the Lost Catacomb", "Hands","Wind Lord HOF"),
            Tuple.create(87194,"Gauntlets of Resounding Rings", "Hands","Wind Lord HOF"),
            Tuple.create(87130,"Firebird's Handwraps", "Hands","Wind Lord HOF"),
            Tuple.create(87135,"Firebird's Grips", "Hands","Wind Lord HOF"),
            Tuple.create(87140,"Firebird's Gloves", "Hands","Wind Lord HOF"),
            Tuple.create(86928,"Eternal Blossom Handwraps", "Hands","Wind Lord HOF"),
            Tuple.create(86939,"Eternal Blossom Handguards", "Hands","Wind Lord HOF"),
            Tuple.create(86924,"Eternal Blossom Grips", "Hands","Wind Lord HOF"),
            Tuple.create(86933,"Eternal Blossom Gloves", "Hands","Wind Lord HOF"),
            Tuple.create(87004,"Yaungol Slayer's Headguard", "Head","Sha ToES"),
            Tuple.create(87101,"White Tiger Helmet", "Head","Sha ToES"),
            Tuple.create(87106,"White Tiger Headguard", "Head","Sha ToES"),
            Tuple.create(87111,"White Tiger Faceguard", "Head","Sha ToES"),
            Tuple.create(87188,"Sha-Skin Hood", "Head","Sha ToES"),
            Tuple.create(87090,"Red Crane Helm", "Head","Sha ToES"),
            Tuple.create(87086,"Red Crane Headpiece", "Head","Sha ToES"),
            Tuple.create(87096,"Red Crane Crown", "Head","Sha ToES"),
            Tuple.create(87008,"Hood of the Burning Scroll", "Head","Sha ToES"),
            Tuple.create(87126,"Helmet of the Thousandfold Blades", "Head","Sha ToES"),
            Tuple.create(86915,"Helmet of the Lost Catacomb", "Head","Sha ToES"),
            Tuple.create(87192,"Helmet of Resounding Rings", "Head","Sha ToES"),
            Tuple.create(87120,"Guardian Serpent Hood", "Head","Sha ToES"),
            Tuple.create(87115,"Guardian Serpent Cowl", "Head","Sha ToES"),
            Tuple.create(87136,"Firebird's Helmet", "Head","Sha ToES"),
            Tuple.create(87141,"Firebird's Headpiece", "Head","Sha ToES"),
            Tuple.create(87131,"Firebird's Faceguard", "Head","Sha ToES"),
            Tuple.create(86920,"Faceguard of the Lost Catacomb", "Head","Sha ToES"),
            Tuple.create(87199,"Faceguard of Resounding Rings", "Head","Sha ToES"),
            Tuple.create(86929,"Eternal Blossom Helm", "Head","Sha ToES"),
            Tuple.create(86925,"Eternal Blossom Headpiece", "Head","Sha ToES"),
            Tuple.create(86940,"Eternal Blossom Headguard", "Head","Sha ToES"),
            Tuple.create(86934,"Eternal Blossom Cover", "Head","Sha ToES"),
            Tuple.create(87005,"Yaungol Slayer's Legguards", "Legs","Amber-Shaper HOF"),
            Tuple.create(87102,"White Tiger Legplates", "Legs","Amber-Shaper HOF"),
            Tuple.create(87112,"White Tiger Legguards", "Legs","Amber-Shaper HOF"),
            Tuple.create(87107,"White Tiger Greaves", "Legs","Amber-Shaper HOF"),
            Tuple.create(87189,"Sha-Skin Leggings", "Legs","Amber-Shaper HOF"),
            Tuple.create(87091,"Red Crane Legwraps", "Legs","Amber-Shaper HOF"),
            Tuple.create(87097,"Red Crane Legguards", "Legs","Amber-Shaper HOF"),
            Tuple.create(87087,"Red Crane Leggings", "Legs","Amber-Shaper HOF"),
            Tuple.create(87195,"Legplates of Resounding Rings", "Legs","Amber-Shaper HOF"),
            Tuple.create(87127,"Legguards of the Thousandfold Blades", "Legs","Amber-Shaper HOF"),
            Tuple.create(86921,"Legguards of the Lost Catacomb", "Legs","Amber-Shaper HOF"),
            Tuple.create(87200,"Legguards of Resounding Rings", "Legs","Amber-Shaper HOF"),
            Tuple.create(87009,"Leggings of the Burning Scroll", "Legs","Amber-Shaper HOF"),
            Tuple.create(87116,"Guardian Serpent Legwraps", "Legs","Amber-Shaper HOF"),
            Tuple.create(87121,"Guardian Serpent Leggings", "Legs","Amber-Shaper HOF"),
            Tuple.create(86916,"Greaves of the Lost Catacomb", "Legs","Amber-Shaper HOF"),
            Tuple.create(87132,"Firebird's Legwraps", "Legs","Amber-Shaper HOF"),
            Tuple.create(87137,"Firebird's Legguards", "Legs","Amber-Shaper HOF"),
            Tuple.create(87142,"Firebird's Kilt", "Legs","Amber-Shaper HOF"),
            Tuple.create(86930,"Eternal Blossom Legwraps", "Legs","Amber-Shaper HOF"),
            Tuple.create(86926,"Eternal Blossom Legguards", "Legs","Amber-Shaper HOF"),
            Tuple.create(86935,"Eternal Blossom Leggings", "Legs","Amber-Shaper HOF"),
            Tuple.create(86941,"Eternal Blossom Breeches", "Legs","Amber-Shaper HOF"),
            Tuple.create(87006,"Yaungol Slayer's Spaulders", "Shoulder","Lei Shi ToES"),
            Tuple.create(87113,"White Tiger Shoulderguards", "Shoulder","Lei Shi ToES"),
            Tuple.create(87103,"White Tiger Pauldrons", "Shoulder","Lei Shi ToES"),
            Tuple.create(87108,"White Tiger Mantle", "Shoulder","Lei Shi ToES"),
            Tuple.create(87128,"Spaulders of the Thousandfold Blades", "Shoulder","Lei Shi ToES"),
            Tuple.create(86922,"Shoulderguards of the Lost Catacomb", "Shoulder","Lei Shi ToES"),
            Tuple.create(87201,"Shoulderguards of Resounding Rings", "Shoulder","Lei Shi ToES"),
            Tuple.create(87191,"Sha-Skin Mantle", "Shoulder","Lei Shi ToES"),
            Tuple.create(87088,"Red Crane Spaulders", "Shoulder","Lei Shi ToES"),
            Tuple.create(87098,"Red Crane Shoulderguards", "Shoulder","Lei Shi ToES"),
            Tuple.create(87093,"Red Crane Mantle", "Shoulder","Lei Shi ToES"),
            Tuple.create(86917,"Pauldrons of the Lost Catacomb", "Shoulder","Lei Shi ToES"),
            Tuple.create(87196,"Pauldrons of Resounding Rings", "Shoulder","Lei Shi ToES"),
            Tuple.create(87011,"Mantle of the Burning Scroll", "Shoulder","Lei Shi ToES"),
            Tuple.create(87123,"Guardian Serpent Shoulderguards", "Shoulder","Lei Shi ToES"),
            Tuple.create(87118,"Guardian Serpent Mantle", "Shoulder","Lei Shi ToES"),
            Tuple.create(87138,"Firebird's Spaulders", "Shoulder","Lei Shi ToES"),
            Tuple.create(87143,"Firebird's Shoulderwraps", "Shoulder","Lei Shi ToES"),
            Tuple.create(87133,"Firebird's Mantle", "Shoulder","Lei Shi ToES"),
            Tuple.create(86927,"Eternal Blossom Spaulders", "Shoulder","Lei Shi ToES"),
            Tuple.create(86937,"Eternal Blossom Shoulderwraps", "Shoulder","Lei Shi ToES"),
            Tuple.create(86942,"Eternal Blossom Shoulderguards", "Shoulder","Lei Shi ToES"),
            Tuple.create(86932,"Eternal Blossom Mantle", "Shoulder","Lei Shi ToES"),
            Tuple.create(90858,"Seal of the Prime", "Finger","Faction"),
            Tuple.create(90859,"Seal of the Lucid", "Finger","Faction"),
            Tuple.create(90860,"Seal of the Unscathed", "Finger","Faction"),
            Tuple.create(90861,"Seal of the Windreaver", "Finger","Faction"),
            Tuple.create(90862,"Seal of the Bloodseeker", "Finger","Faction"),
            Tuple.create(79331,"Relic of Yu'lon", "Trinket","Darkmoon"),
            Tuple.create(79328,"Relic of Xuen", "Trinket","Darkmoon"),
            Tuple.create(79327,"Relic of Xuen", "Trinket","Darkmoon"),
            Tuple.create(79330,"Relic of Chi-Ji", "Trinket","Darkmoon"),
            Tuple.create(79329,"Relic of Niuzao", "Trinket","Darkmoon"),
            Tuple.create(87166,"Spiritsever", "Weapon","Lei Shi ToES"),
            Tuple.create(87168,"Taoren, the Soul Burner", "Weapon","Lei Shi ToES"),
            Tuple.create(87170,"Jin'ya, Orb of the Waterspeaker", "Weapon","Lei Shi ToES"),
            Tuple.create(86988,"Claws of Shek'zeer", "Weapon","Empress HOF"),
            Tuple.create(95165,"Achillobator Ring","Ring","Oondasta"),
            Tuple.create(95181,"Amulet of the Titanorex","Neck","Oondasta"),
            Tuple.create(95186,"Belt of Crushed Dreams","Belt","Oondasta"),
            Tuple.create(95188,"Belt of the Arch Avimimus","Belt","Oondasta"),
            Tuple.create(95189,"Belt of the Dying Diemetradon","Belt","Oondasta"),
            Tuple.create(95192,"Belt of the Tyrannotitan","Belt","Oondasta"),
            Tuple.create(95152,"Breastplate of the Iguanocolossus","Chest","Oondasta"),
            Tuple.create(95199,"Carnotaur Battlegloves","Hand","Oondasta"),
            Tuple.create(95177,"Choker of Stygimolochy","Neck","Oondasta"),
            Tuple.create(95164,"Eye of Oondasta","Ring","Oondasta"),
            Tuple.create(95147,"Fancifully Frilled Tunic","Chest","Oondasta"),
            Tuple.create(95166,"Forzarra's Last Meal","Ring","Oondasta"),
            Tuple.create(95184,"Girdle of Dimorphodontics","Belt","Oondasta"),
            Tuple.create(95187,"Girdle of the Derrodoccus","Belt","Oondasta"),
            Tuple.create(95196,"Gloves of Gastric Rumbling","Hand","Oondasta"),
            Tuple.create(95194,"Gloves of Tyranomancy","Hand","Oondasta"),
            Tuple.create(95195,"Gloves of Unliving Fossil","Hand","Oondasta"),
            Tuple.create(95193,"Gloves of Varsoon the Greater","Hand","Oondasta"),
            Tuple.create(95149,"Gorgoraptor Scale Chest","Chest","Oondasta"),
            Tuple.create(95178,"Lootraptor's Amulet","Neck","Oondasta"),
            Tuple.create(95150,"Mail of the Mosschopper","Chest","Oondasta"),
            Tuple.create(95179,"Necklace of the Hazillosaurus","Neck","Oondasta"),
            Tuple.create(95200,"Orndo Mando's Gloves","Hand","Oondasta"),
            Tuple.create(95180,"Overcompensating Chain of the Alpha Male","Neck","Oondasta"),
            Tuple.create(95167,"Ring of King Kangrom","Ring","Oondasta"),
            Tuple.create(95163,"Ring of Shamuru","Ring","Oondasta"),
            Tuple.create(95182,"Robes of Zalmoxes","Chest","Oondasta"),
            Tuple.create(95151,"Scorched Spiritfire Drape","Chest","Oondasta"),
            Tuple.create(95201,"Skullsmashing Gauntlets","Hand","Oondasta"),
            Tuple.create(95185,"Terrorful Weave","Belt","Oondasta"),
            Tuple.create(95197,"Therapsid Scale Gloves","Hand","Oondasta"),
            Tuple.create(95153,"Tyrant King Battleplate","Chest","Oondasta"),
            Tuple.create(95148,"Vest of the Bordomorono","Chest","Oondasta"),
            Tuple.create(95191,"Voolar's Bloodied Belt","Belt","Oondasta"),
            Tuple.create(95198,"Vulcanodon Gauntlets","Hand","Oondasta"),
            Tuple.create(95183,"Waistband of Elder Falcarius","Chest","Oondasta"),
            Tuple.create(95190,"Waistband of Furious Stomping","Belt","Oondasta"),
    };

    public static void findNormalVariants() {
        ArrayUtil.mapAsNew(spreadSheetHeroicIds, tuple -> {
            Integer id = tuple.a();
            String name = tuple.b();
            String slot = tuple.c();
            String boss = tuple.d();
            return findNormalVariants(id, name, slot, boss);
        }, DifficultyVersions[]::new);
    }

    private static DifficultyVersions findNormalVariants(int itemId, String name, String slot, String boss) {
        FullItemData item = ItemCache.instance.get(itemId, 0);
        if (!nameCompare(name, item))
            throw new RuntimeException(String.format("name mismatch %d '%s' vs '%s'",  itemId, name, item.shared.name()));
        if (!slotCompare(slot, item))
            throw new RuntimeException(String.format("slot mismatch %d '%s' vs '%s'",  itemId, slot, item.slot().name()));

        List<FullItemData> versions = WowSimDB.instance.itemStream()
                .filter(it -> it.shared.name().equals(item.shared.name()))
                .filter(it -> it.itemLevel() > 463) // ignore challenge mode downscales
                .filter(it -> it.shared.ref().upgradeLevel() == 2) // fully upgraded only
                .sorted(Comparator.comparingInt(FullItemData::itemLevel))
                .toList();

        if (versions.isEmpty())
            throw new RuntimeException("didn't find version matching expected criteria");
        if (versions.size() == 3 && !versions.get(2).equalsTyped(item))
            throw new RuntimeException("best version should have been as id searched for if parameter list was correct");

//        System.out.println(name);
//        System.out.println(versions.stream().map(it -> String.format("%d{%d}", it.itemLevel(), it.shared.ref().upgradeLevel())).collect(Collectors.joining(", ")));

        if (versions.size() == 3) {
            return new DifficultyVersions(versions.get(0), versions.get(1), versions.get(2));
        } else if (versions.size() == 1) {
            return new DifficultyVersions(versions.getFirst(), null, null);
        } else {
            throw new RuntimeException("unexpected set of item versions");
        }
    }

    private static boolean nameCompare(String name, FullItemData item) {
        if (name.replaceAll(",", "").equals(item.shared.name().replaceAll(",", "")))
            return true;
        if (name.replaceAll("’", "'").equals(item.shared.name().replaceAll("’", "'")))
            return true;
        // TODO check sheet for correct name
        if (name.equals("Shin'ka, Execution of Domination") && item.shared.name().equals("Shin'ka, Execution of Dominion"))
            return true;
        return name.equals(item.shared.name());
    }

    private static boolean slotCompare(String slot, FullItemData item) {
        if (item.slot() == SlotItem.Foot && slot.equals("Feet"))
            return true;
        if (item.slot() == SlotItem.Belt && slot.equals("Waist"))
            return true;
        if (item.slot() == SlotItem.Leg && slot.equals("Legs"))
            return true;
        if (item.slot() == SlotItem.Hand && slot.equals("Hands"))
            return true;
        if (item.slot() == SlotItem.Weapon1H && slot.equals("Weapon"))
            return true;
        if (item.slot() == SlotItem.Weapon2H && slot.equals("Weapon"))
            return true;
        if (item.slot() == SlotItem.Ring && slot.equals("Finger"))
            return true;
        if (item.slot() == SlotItem.Offhand && slot.equals("Held In Off-hand"))
            return true;
        if (item.slot() == SlotItem.Offhand && slot.equals("Shield"))
            return true;
        return slot.equals(item.slot().name());
    }

    public record DifficultyVersions(FullItemData celestial, FullItemData normal, FullItemData heroic) {

    }
}
