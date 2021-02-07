package net.binis.codegen.codegen.interfaces;

public interface PrototypeData {
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
    String getCreatorClass();
    boolean isCreatorModifier();
    String getMixInClass();
    String getBasePath();

}
