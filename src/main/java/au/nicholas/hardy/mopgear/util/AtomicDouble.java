package au.nicholas.hardy.mopgear.util;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicDouble {
    private final AtomicReference<Optional<Double>> ref;

    public AtomicDouble() {
        ref = new AtomicReference<>(Optional.empty());
    }

    public Optional<Double> getAcquire() {
        return ref.getAcquire();
    }

    public void updateRelease(Double value) {
        ref.setRelease(Optional.ofNullable(value));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void clearIfValue(Optional<Double> value) {
        ref.compareAndExchangeRelease(value, Optional.empty());
    }
}
