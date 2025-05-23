package net.binis.codegen.generation.core.interfaces;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2024 Binis Belev
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

import com.github.javaparser.ast.expr.AnnotationExpr;
import net.binis.codegen.annotation.type.GenerationStrategy;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.options.CodeOption;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PrototypeData {
    Class<? extends Annotation> getPrototypeAnnotation();
    AnnotationExpr getPrototypeAnnotationExpression();
    String getPrototypeName();
    String getPrototypeFullName();
    String getName();
    String getClassName();

    default String getClassFullName() {
        return getClassPackage() + "." + getClassName();
    }

    String getClassPackage();
    boolean isClassPackageSet();
    boolean isClassGetters();
    boolean isClassSetters();
    String getInterfaceName();

    default String getInterfaceFullName() {
        return getInterfacePackage() + "." + getInterfaceName();
    }

    default String getImplementorFullName() {
        return getClassPackage() + "." + getClassName();
    }

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
    String getInterfacePath();
    String getImplementationPath();

    int getOrdinalOffset();

    GenerationStrategy getStrategy();

    Map<String, Object> getCustom();

    List<PrototypeEnricher> getEnrichers();
    List<PrototypeEnricher> getInheritedEnrichers();
    Set<Class<? extends CodeOption>> getOptions();

    void setClassGetters(boolean value);
    void setClassSetters(boolean value);
    void setInterfaceSetters(boolean value);
    void setGenerateConstructor(boolean value);
    void setGenerateImplementation(boolean value);
    void setGenerateInterface(boolean value);
}
