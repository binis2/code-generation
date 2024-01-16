package net.binis.codegen.objects;

import net.binis.codegen.annotation.Include;

public interface Identifiable {

    @Include(forToString = true)
    long getId();

}
