package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.model.StatRequirements;
import au.nerago.mopgear.results.PrintRecorder;
import au.nerago.mopgear.util.*;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class SolverCapPhasedIndexed extends SolverCapPhased {

    public SolverCapPhasedIndexed(ModelCombined model, StatBlock adjustment, PrintRecorder printRecorder, Long topCombosMultiply) {
        super(model, adjustment, printRecorder, topCombosMultiply);
    }

    @Override
    protected Stream<SkinnyItemSet> generateSkinnyComboStream(List<SkinnyItem[]> optionsList, boolean parallel) {
        long skip = topCombosMultiply != null ? Primes.roundToPrimeInt(64000 / topCombosMultiply) : Primes.roundToPrimeInt(64000);
        printRecorder.println("generateSkinnyComboStream skip=" + skip);
        return generateDumbStream(estimate, skip).parallel()
                .mapToObj(index -> makeSet(optionsList, index));
    }

    private static LongStream generateDumbStream(long count, long skip) {
        long start = ThreadLocalRandom.current().nextLong(skip);
        return LongStream.iterate(start, x -> x < count, x -> x + skip);
    }

    private SkinnyItemSet makeSet(List<SkinnyItem[]> optionsList, long mainIndex) {
        SkinnyItemSet itemSet = null;

        for (SkinnyItem[] list : optionsList) {
            int size = list.length;

            int thisIndex = (int) (mainIndex % size);
            mainIndex /= size;

            SkinnyItem choice = list[thisIndex];
            if (itemSet == null) {
                itemSet = SkinnyItemSet.single(choice);
            } else {
                itemSet = itemSet.withAddedItem(choice);
            }
        }

        return itemSet;
    }
}
