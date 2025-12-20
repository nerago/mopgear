package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.results.PrintRecorder;
import au.nerago.mopgear.util.*;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class SolverCapPhasedIndexed extends SolverCapPhased {

    public SolverCapPhasedIndexed(ModelCombined model, StatBlock adjustment, PrintRecorder printRecorder, Long topCombosMultiply) {
        super(model, adjustment, printRecorder, topCombosMultiply);
    }

    @Override
    protected Stream<SkinnyItemSet> generateSkinnyComboStream(List<SkinnyItem[]> optionsList, boolean parallel) {
        BigInteger targetCombos = BigInteger.valueOf(100000 * topCombosMultiply);
        BigInteger skip = BigInteger.ONE;
        if (estimate.compareTo(targetCombos) > 0) {
            skip = Primes.roundToPrime(estimate.divide(targetCombos));
        }
        printRecorder.println("generateSkinnyComboStream skip=" + skip + " trying " + estimate.divide(skip));
        return generateDumbStream(estimate, skip).parallel()
                .map(index -> makeSet(optionsList, index));
    }

    private static Stream<BigInteger> generateDumbStream(BigInteger max, BigInteger skip) {
        long start = ThreadLocalRandom.current().nextLong(skip.longValueExact());
        return Stream.iterate(BigInteger.valueOf(start), x -> x.compareTo(max) < 0, x -> x.add(skip));
    }

    private SkinnyItemSet makeSet(List<SkinnyItem[]> optionsList, BigInteger mainIndex) {
        SkinnyItemSet itemSet = null;

        for (SkinnyItem[] list : optionsList) {
            int size = list.length;

            BigInteger[] divRem = mainIndex.divideAndRemainder(BigInteger.valueOf(size));

            int thisIndex = divRem[1].intValueExact();
            mainIndex = divRem[0];

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
