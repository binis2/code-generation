package net.binis.codegen.generation.core.interfaces;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 Binis Belev
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
    boolean isGenerateImplementation();
    boolean isGenerateInterface();
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
    void setGenerateImplementation(boolean value);
    void setGenerateInterface(boolean value);
}
