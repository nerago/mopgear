package au.nerago.mopgear.model;

import au.nerago.mopgear.domain.ItemSet;

import java.util.stream.Stream;

public class StatRequirementsNull implements StatRequirements {
    @Override
    public boolean filterOneSet(ItemSet set) {
        return true;
    }

    @Override
    public Stream<ItemSet> filterSets(Stream<ItemSet> stream) {
        return stream;
    }

    @Override
    public Stream<ItemSet> filterSetsMax(Stream<ItemSet> stream) {
        return stream;
    }
}
