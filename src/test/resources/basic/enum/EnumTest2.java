package net.binis.codegen;

import lombok.Getter;
import net.binis.codegen.annotation.EnumPrototype;

@EnumPrototype
public enum TestPrototype {
    UNKNOWN("unknown", true, 5),
    KNOWN("known", false, 10),
    NEXT("next", true, 15);

    private final String label;
    @Getter
    private final boolean check;
    @Getter
    private final int value;

    public static final String CONSTANT = "const";

    public String getLabel() {
        return label;
    }

    TestPrototype(String label, boolean check, int value) {
        this.label = label;
        this.check = check;
        this.value = value;
    }
}