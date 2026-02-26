package au.nerago.mopgear.process;

import au.nerago.mopgear.domain.EquipMap;
import au.nerago.mopgear.domain.SpecType;
import au.nerago.mopgear.io.SimCliExecute;
import au.nerago.mopgear.io.SimInputModify;
import au.nerago.mopgear.io.SimOutputReader;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.results.AsWowSimJson;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.util.BigStreamUtil;
import au.nerago.mopgear.util.StreamNeedClose;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindMultiSpecSim {
    private final FindMultiSpec multi;

    public FindMultiSpecSim(FindMultiSpec multi) {
        this.multi = multi;
    }

    public void process(Collection<FindMultiSpec.ProposedResults> proposedOptions) {
        List<SimulateTask> tasks = proposedOptions.stream()
                .flatMap(prop ->
                        prop.resultJobs().stream().map(job ->
                            new SimulateTask(job.getFinalResultSet().orElseThrow().items(), job.input.model.spec())
                        ))
                .distinct()
                .toList();

        OutputText.printf("SimulateTasks %d\n", tasks.size());

        try (StreamNeedClose<SimulateTask> simStream = BigStreamUtil.countProgress(tasks.size(), Instant.now(), tasks.stream().peek(SimulateTask::runSimulate))) {
            simStream.forEach(_ -> {});
        }

        List<SimulatedResults> results = proposedOptions.stream().map(prop ->
                new SimulatedResults(
                        prop,
                        prop.resultJobs().stream()
                                .map(job -> {
                                    EquipMap targetItems = job.getFinalResultSet().orElseThrow().items();
                                    Optional<SimulateTask> matchedTask = tasks.stream().filter(task -> task.equalsParts(targetItems, job.input.model.spec())).findFirst();
                                    return matchedTask.orElseThrow();
                                })
                                .toList()
                )
        ).toList();

        OutputText.println("@@@@@@@@@@@@@@@@ RESULTS @@@@@@@@@@@@@@@@");
        results.forEach(res -> {
            OutputText.println("&&&&&&&&&&&&& " + res.parent.resultId());
            Stream.of(0, 1, 2).forEach(specIndex -> {
                ModelCombined model = multi.specs.get(specIndex).model;
                OutputText.println("------------- " + multi.specs.get(specIndex).label);
                res.parent.resultJobs().get(specIndex).getFinalResultSet().orElseThrow().outputSetDetailed(model);
                AsWowSimJson.writeFullToOut(res.sims.get(specIndex).equip, model);
                res.sims.get(specIndex).resultStats.print();
            });
        });

        OutputText.println("@@@@@@@@@@@@@@@@ SPREADSHEET COPY @@@@@@@@@@@@@@@@");
        OutputText.println(results.stream().map(r -> r.parent.resultId().toString()).collect(Collectors.joining(",")));
        for (int specIndex = 0; specIndex < multi.specs.size(); ++specIndex) {
            int finalSpecIndex = specIndex;
            for (ToDoubleFunction<SimOutputReader.SimResultStats> stat : SimOutputReader.SimResultStats.eachStat()) {
                OutputText.println(results.stream().map(r -> String.valueOf(stat.applyAsDouble(r.sims.get(finalSpecIndex).resultStats))).collect(Collectors.joining(",")));
            }
        }
    }

    private record SimulatedResults(FindMultiSpec.ProposedResults parent, List<SimulateTask> sims) {
    }

    private static class SimulateTask {
        private final EquipMap equip;
        private final SpecType spec;
        private SimOutputReader.SimResultStats resultStats;

        public SimulateTask(EquipMap equip, SpecType spec) {
            this.equip = equip;
            this.spec = spec;
        }

        public void runSimulate() {
            UUID taskId = UUID.randomUUID();
            Path inputFile = SimInputModify.makeWithGear(spec, equip, taskId.toString(), SimInputModify.SimSpeed.SlowAccurate);
            Path outputFile = inputFile.resolveSibling(inputFile.getFileName() + ".out");
            SimCliExecute.run(inputFile, outputFile);
            resultStats = SimOutputReader.readInput(outputFile, true);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SimulateTask that)) return false;
            return equip.equalsTypedSwappable(that.equip) && spec == that.spec;
        }

        @Override
        public int hashCode() {
            return Objects.hash(equip, spec);
        }

        public boolean equalsParts(EquipMap targetItems, SpecType spec) {
            return equip.equalsTypedSwappable(targetItems) && spec == this.spec;
        }
    }
}
