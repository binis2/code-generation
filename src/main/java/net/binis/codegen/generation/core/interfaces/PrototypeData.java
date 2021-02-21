package net.binis.codegen.generation.core.interfaces;

import net.binis.codegen.enrich.PrototypeEnricher;

import java.util.List;

public interface PrototypeData {
    String getPrototypeName();
    String getName();
    String getClassName();
    String getClassPackage();
    boolean isClassGetters();
    boolean isClassSetters();
    String getInterfaceName();
    String getInterfacePackage();
    boolean isInterfaceSetters();
    String getModifierName();
    String getLongModifierName();
    String getModifierPackage();

    String getBaseClassName();

    boolean isGenerateConstructor();
    boolean isGenerateInterface();
    boolean isGenerateModifier();
    boolean isBase();

    String getBaseModifierClass();
    String getMixInClass();
    String getBasePath();

    List<PrototypeEnricher> getEnrichers();
    List<PrototypeEnricher> getInheritedEnrichers();

    void setClassGetters(boolean value);
    void setClassSetters(boolean value);
    void setInterfaceSetters(boolean value);
    void setGenerateConstructor(boolean value);
    void setGenerateInterface(boolean value);
    void setGenerateModifier(boolean value);
}
