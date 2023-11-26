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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import net.binis.codegen.generation.core.Structures;
import net.binis.codegen.generation.core.types.ModifierType;

import java.util.List;
import java.util.Map;

public interface PrototypeField {
    Structures.Parsed<ClassOrInterfaceDeclaration> getParsed();
    String getName();
    MethodDeclaration getDescription();
    FieldDeclaration getDeclaration();
    String getFullType();
    Type getType();
    boolean isCollection();
    boolean isExternal();
    boolean isGenericMethod();
    boolean isGenericField();
    boolean isCustom();
    Structures.Ignores getIgnores();
    PrototypeDescription<ClassOrInterfaceDeclaration> getPrototype();
    Map<String, Type> getGenerics();
    Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> getTypePrototypes();
    MethodDeclaration getInterfaceGetter();
    MethodDeclaration getInterfaceSetter();
    MethodDeclaration getImplementationGetter();
    MethodDeclaration getImplementationSetter();

    List<ModifierDescription> getModifiers();

    PrototypeField getParent();

    void addModifier(ModifierType type, MethodDeclaration modifier, PrototypeDescription<ClassOrInterfaceDeclaration> origin);
    MethodDeclaration generateGetter();
    MethodDeclaration generateSetter();
    MethodDeclaration generateInterfaceGetter();
    MethodDeclaration generateInterfaceSetter();

    interface ModifierDescription {
        ModifierType getType();
        MethodDeclaration getModifier();
        PrototypeDescription<ClassOrInterfaceDeclaration> getOrigin();
    }

}
