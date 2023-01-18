package net.bmjo.hamstermod.entity.variant;

import java.util.Arrays;
import java.util.Comparator;

public enum HamsterVariant {
    DEFAULT(0),
    GREY(1),
    WHITE(2);

    private static final HamsterVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.
            comparingInt(HamsterVariant::getId)).toArray(HamsterVariant[]::new);
    private final int id;

    HamsterVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static HamsterVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
