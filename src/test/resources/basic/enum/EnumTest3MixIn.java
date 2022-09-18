package net.binis.codegen;

import net.binis.codegen.annotation.EnumPrototype;

@EnumPrototype(mixIn = TestPrototype.class)
public enum MixInPrototype {
    MIXIN_UNKNOWN,
    MIXIN_KNOWN,
    MIXIN_NEXT;
}