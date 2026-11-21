package com.nexoroprime.module;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class OptUtil {
    /** Sets an enum option by index (0 = first constant) without naming its class. */
    public static <E extends Enum<E>> void pick(Supplier<E> getter, Consumer<E> setter, int idx) {
        E[] all = getter.get().getDeclaringClass().getEnumConstants();
        setter.accept(all[Math.max(0, Math.min(idx, all.length - 1))]);
    }

    private OptUtil() {}
}
