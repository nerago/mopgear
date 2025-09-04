package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.ItemCache;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.results.JobInfo;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.BigStreamUtil;
import au.nerago.mopgear.util.TopCollectorReporting;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FindMultiSpec {
    public static final int RANDOM_COMBOS = 1000000;
    private final ItemCache itemCache;
    //        Long runSize = 200000L;
    private final long runSize = 2000L;
    private final boolean hackAllow = true;

    private final Map<Integer, ReforgeRecipe> fixedForge = new HashMap<>();
    private final List<SpecDetails> specs = new ArrayList<>();


    public FindMultiSpec(ItemCache itemCache) {
        this.itemCache = itemCache;
    }

    public void addFixedForge(int id, ReforgeRecipe reforge) {
        fixedForge.put(id, reforge);
    }

    public void addSpec(SpecDetails spec) {
        specs.add(spec);
    }

    public void solve(Instant startTime) {
        ModelCombined modelNull = ModelCombined.nullMixedModel();

        for (SpecDetails spec : specs) {
            spec.prepare(itemCache);
        }
        ItemUtil.validateDualSets(specs.stream().map(s -> s.itemOptions).toList());

        Map<Integer, List<ItemData>> commonMap = commonInMultiSet(specs);

        long commonCombos = ItemUtil.estimateSets(commonMap);
        OutputText.println("COMMON COMBOS " + commonCombos);

//        Stream<Map<Integer, ItemData>> commonStream = SolverPossibleStreams.runSolverPartial(commonMap);
        Stream<Map<Integer, ItemData>> commonStream = PossibleRandom.runSolverPartial(commonMap, RANDOM_COMBOS);

        commonStream = BigStreamUtil.countProgressSmall(commonCombos, startTime, commonStream);

        Stream<ProposedResults> resultStream = commonStream
                .map(r -> subSolveEach(r, specs))
                .filter(Objects::nonNull);

        Optional<ProposedResults> best = resultStream.collect(
                new TopCollectorReporting<>(s -> multiRating(s.resultJobs, specs),
                        s -> reportBetter(s.resultJobs, specs)));
        outputResultTwins(best, specs);
    }

    private Map<Integer, List<ItemData>> commonInMultiSet(List<SpecDetails> mapArray) {
        Map<Integer, List<ItemData>> commonOptions = new HashMap<>();
        Map<Integer, Set<String>> seenIn = new HashMap<>();
        for (SlotEquip slot : SlotEquip.values()) {
            for (SpecDetails spec : mapArray) {
                ItemData[] slotOptions = spec.itemOptions.get(slot);
                if (slotOptions != null) {
                    Arrays.stream(slotOptions).collect(Collectors.groupingBy(item -> item.id))
                            .forEach((id, forges) -> {
                                        commonOptions.compute(id, (x, prior) -> commonForges(prior, forges));
                                        seenIn.computeIfAbsent(id, x -> new HashSet<>()).add(spec.label);
                                    }
                            );
                }
            }
        }

        for (Map.Entry<Integer, ReforgeRecipe> entry : fixedForge.entrySet()) {
            int id = entry.getKey();
            List<ItemData> forgeList = commonOptions.get(id);
            if (forgeList == null)
                throw new IllegalArgumentException("fixed forge for " + id + " but not in sets");
            forgeList = ItemUtil.onlyMatchingForge(forgeList, entry.getValue());
            commonOptions.put(id, forgeList);
            OutputText.println("FIXED " + forgeList.getFirst().name);
        }

        for (Map.Entry<Integer, Set<String>> seenEntry : seenIn.entrySet()) {
            Integer id = seenEntry.getKey();
            if (seenEntry.getValue().size() <= 1 && !fixedForge.containsKey(id)) {
                commonOptions.remove(id);
            }
        }

        for (List<ItemData> lst : commonOptions.values()) {
            ItemData item = lst.getFirst();
            Set<String> specs = seenIn.get(item.id);
            OutputText.println("COMMON " + item.name + " " + String.join(" ", specs));
        }

        return commonOptions;
    }

    private static List<ItemData> commonForges(List<ItemData> prior, List<ItemData> forges) {
        if (prior == null) {
            return forges;
        }

        ArrayList<ItemData> commonForges = new ArrayList<>();
        for (ItemData a : prior) {
            for (ItemData b : forges) {
                if (ItemData.isIdenticalItem(a, b)) {
                    commonForges.add(a);
                }
            }
        }
        return commonForges;
    }

    private static EquipMap commonInDualSet(EquipMap retMap, EquipMap protMap) {
        EquipMap common = EquipMap.empty();
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData a = retMap.get(slot);
            ItemData b = protMap.get(slot);
            if (a == null || b == null)
                continue;

            if (ItemData.isIdenticalItem(a, b)) {
                common.put(slot, a);
            }
        }
        return common;
    }

    private ProposedResults subSolveEach(Map<Integer, ItemData> commonChoices, List<SpecDetails> specList) {
        List<JobInfo> results = new ArrayList<>();
        for (SpecDetails spec : specList) {
            JobInfo job = subSolvePart(spec.itemOptions, spec.model, commonChoices);
//                if (job.resultSet.isEmpty() || job.hackCount > 0) {
            if (job.resultSet.isEmpty()) {
                return null;
            }
            results.add(job);
        }
        return new ProposedResults(results, commonChoices);
    }

    private JobInfo subSolvePart(EquipOptionsMap fullItemMap, ModelCombined model, Map<Integer, ItemData> chosenMap) {
        EquipOptionsMap submitMap = fullItemMap.shallowClone();
        buildJobWithSpecifiedItemsFixed(chosenMap, submitMap);

        JobInfo job = new JobInfo();
        job.model = model;
        job.itemOptions = submitMap;
        job.runSize = runSize;
        job.hackAllow = hackAllow;
        return Solver.runJob(job);
    }

    static void buildJobWithSpecifiedItemsFixed(Map<Integer, ItemData> chosenMap, EquipOptionsMap submitMap) {
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] options = submitMap.get(slot);
            if (options == null) {
                continue;
            }

            boolean needUpdate = false;
            for (ItemData item : options) {
                if (chosenMap.containsKey(item.id)) {
                    needUpdate = true;
                    break;
                }
            }

            if (needUpdate) {
                ArrayList<ItemData> list = new ArrayList<>();
                for (ItemData item : options) {
                    if (chosenMap.containsKey(item.id)) {
                        boolean alreadyAdded = false;
                        for (ItemData x : list) {
                            if (x.id == item.id) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if (!alreadyAdded)
                            list.add(item);
                    } else {
                        list.add(item);
                    }
                }
                submitMap.put(slot, list.toArray(ItemData[]::new));
            }
        }
    }

    private long multiRating(List<JobInfo> resultJobs, List<SpecDetails> specList) {
        long total = 0;
        for (int i = 0; i < resultJobs.size(); ++i) {
            ItemSet set = resultJobs.get(i).resultSet.orElseThrow();
            SpecDetails spec = specList.get(i);
            total += spec.model.calcRating(set);
        }
        return total;
    }

    private void reportBetter(List<JobInfo> resultJobs, List<SpecDetails> specList) {
        long rating = multiRating(resultJobs, specList);
        synchronized (OutputText.class) {
            OutputText.printf("^^^^^^^^^ %s ^^^^^^^ %d ^^^^^^^^^\n", LocalDateTime.now(), rating);
            for (int i = 0; i < resultJobs.size(); ++i) {
                JobInfo job = resultJobs.get(i);
                ItemSet set = job.resultSet.orElseThrow();
                SpecDetails spec = specList.get(i);
                OutputText.printf("-------------- %s -------------- %s\n", spec.label, "<HACK>".repeat(job.hackCount));
                job.printRecorder.outputNow();
                set.outputSet(spec.model);
            }
            OutputText.println("#######################################");
        }
    }

    private void outputResultTwins(Optional<ProposedResults> bestSets, List<SpecDetails> specList) {
        if (bestSets.isPresent()) {
            List<JobInfo> resultJobs = bestSets.get().resultJobs;
            Map<Integer, ItemData> common = bestSets.get().chosenMap;

            OutputText.println("@@@@@@@@@ BEST SET(s) @@@@@@@@@");

            for (int i = 0; i < resultJobs.size(); ++i) {
                JobInfo job = resultJobs.get(i);
                ItemSet set = job.resultSet.orElseThrow();
                SpecDetails spec = specList.get(i);
                OutputText.printf("-------------- %s --------------\n", spec.label);
                set.outputSet(spec.model);
                job.printRecorder.outputNow();
            }

            OutputText.println("%%%%%%%%%%%%%%%%%%% COMMON-FORGE %%%%%%%%%%%%%%%%%%%");
            common.values().forEach(item -> OutputText.println(item.toString()));

            OutputText.println("%%%%%%%%%%%%%% Main.commonFixedItems %%%%%%%%%%%%%%%");
            common.values().forEach(item -> {
                if (item.reforge == null || item.reforge.isNull())
                    OutputText.printf("presetReforge.put(SlotEquip.%s, new ReforgeRecipe(null, null));\n", item.slot);
                else
                    OutputText.printf("presetReforge.put(SlotEquip.%s, new ReforgeRecipe(%s, %s));\n", item.slot, item.reforge.source(), item.reforge.dest());
            });
        } else {
            OutputText.println("@@@@@@@@@ NO BEST SET FOUND @@@@@@@@@");
        }
    }

    public static class SpecDetails {
        final String label;
        final Path gearFile;
        final ModelCombined model;
        final int[] extraItems; // TODO use this
        final boolean challengeScale;
        EquipOptionsMap itemOptions;

        public SpecDetails(String label, Path gearFile, ModelCombined model, int[] extraItems, boolean challengeScale) {
            this.label = label;
            this.gearFile = gearFile;
            this.model = model;
            this.extraItems = extraItems;
            this.challengeScale = challengeScale;
        }

        public void prepare(ItemCache itemCache) {
            itemOptions = ItemUtil.readAndLoad(itemCache, false, gearFile, model.reforgeRules(), null);
            for (int itemId : extraItems) {
                addExtra(itemId);
            }
        }

        private void addExtra(int itemId) {
            ItemData extraItem = ItemUtil.loadItemBasic(Jobs.itemCache, itemId);
            extraItem = ItemUtil.defaultEnchants(extraItem, model, true);
            ItemData[] extraForged = Reforger.reforgeItem(model.reforgeRules(), extraItem);

            SlotEquip slot = extraItem.slot.toSlotEquip(); // no smarts for rings/trinkets here
            ItemData[] existing = itemOptions.get(slot);
            if (ArrayUtil.anyMatch(existing, item -> item.id == itemId))
                throw new IllegalArgumentException("item already included " + itemId + " " + extraItem);

            itemOptions.put(slot, ArrayUtil.concat(existing, extraForged));

            ItemData[] slotArray = itemOptions.get(slot);
            HashSet<Integer> seen = new HashSet<>();
            ArrayUtil.forEach(slotArray, it -> {
                if (seen.add(it.id)) {
                    OutputText.println("NEW " + slot + " " + it);
                }
            });
        }
    }

    private record ProposedResults(List<JobInfo> resultJobs, Map<Integer, ItemData> chosenMap) {
    }
}
