package net.binis.codegen;

import net.binis.codegen.annotation.EnumPrototype;

@EnumPrototype(mixIn = TestPrototype.class, ordinalOffset = 10)
public enum MixIn2Prototype {
    MIXIN2_UNKNOWN,
    MIXIN2_KNOWN,
    MIXIN2_NEXT;
}