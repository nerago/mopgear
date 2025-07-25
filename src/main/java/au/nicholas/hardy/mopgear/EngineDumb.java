package au.nicholas.hardy.mopgear;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EngineDumb {
    static Collection<ItemSet> runSolver(Map<SlotEquip, List<ItemData>> items) {
        // empty slots shouldn't be seen here at all
        int[] scales = items.values().stream().mapToInt(List::size).toArray();
        return null;
    }

}
