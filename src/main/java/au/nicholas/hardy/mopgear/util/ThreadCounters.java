package au.nicholas.hardy.mopgear.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadCounters {
    private final Map<Long, LongHolder> holderMap;
    private final ThreadLocal<LongHolder> threadLocal;

    public ThreadCounters() {
        holderMap = new ConcurrentHashMap<>();
        threadLocal = ThreadLocal.withInitial(this::makeHolder);
    }

    private LongHolder makeHolder() {
        LongHolder holder = new LongHolder();
        long threadId = Thread.currentThread().threadId();
        holderMap.put(threadId, holder);
        return holder;
    }

    public long incrementAndGet() {
        return threadLocal.get().incrementAndGet();
    }

    public long getTotal() {
        long total = 0;
        for (LongHolder holder : holderMap.values()) {
            total += holder.value;
        }
        return total;
    }
}
