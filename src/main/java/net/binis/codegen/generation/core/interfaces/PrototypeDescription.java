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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import net.binis.codegen.annotation.type.EmbeddedModifierType;
import net.binis.codegen.enrich.Enricher;
import net.binis.codegen.enrich.GeneratedFile;
import net.binis.codegen.generation.core.Parsables;
import net.binis.codegen.generation.core.Structures;
import net.binis.codegen.options.CodeOption;
import org.apache.commons.lang3.tuple.Triple;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface PrototypeDescription<T extends TypeDeclaration<T>> {

    boolean isProcessed();
    boolean isInvalid();

    JavaParser getParser();

    Class<?> getCompiled();

    String getPrototypeFileName();
    String getPrototypeClassName();

    PrototypeData getProperties();
    List<PrototypeData> getAdditionalProperties();

    String getParsedName();
    String getParsedFullName();

    String getInterfaceName();
    String getInterfaceFullName();

    String getImplementorFullName();

    TypeDeclaration<T> getDeclaration();
    CompilationUnit getDeclarationUnit();

    List<CompilationUnit> getFiles();

    PrototypeDescription<T> getBase();
    PrototypeDescription<T> getMixIn();

    List<PrototypeField> getFields();

    Structures.Ignores getIgnores();

    ClassOrInterfaceDeclaration getImplementation();
    CompilationUnit getImplementationUnit();
    ClassOrInterfaceDeclaration getInterface();
    CompilationUnit getInterfaceUnit();

    List<Triple<ClassOrInterfaceDeclaration, Node, PrototypeDescription<ClassOrInterfaceDeclaration>>> getInitializers();
    List<Consumer<BlockStmt>> getCustomInitializers();

    void registerClass(String key, ClassOrInterfaceDeclaration declaration);
    ClassOrInterfaceDeclaration getRegisteredClass(String key);

    void registerPostProcessAction(Runnable task);
    void processActions();

    boolean isValid();

    boolean isNested();

    boolean isExternal();

    boolean isCodeEnum();

    boolean isMixIn();

    String getParentClassName();

    Map<String, List<ElementDescription>> getElements();

    Element getElement();

    Element getPrototypeElement();

    List<Parsables.Entry.Bag> getRawElements();

    Element findElement(String name, ElementKind... kind);

    Element findElement(Element parent, String name, ElementKind kind);

    TypeDeclaration<?> getParent();

    String getParentPackage();

    EmbeddedModifierType getEmbeddedModifierType();

    Optional<PrototypeField> findField(String name);

    void addEmbeddedModifier(EmbeddedModifierType type);

    void setEmbeddedModifier(EmbeddedModifierType type);

    boolean hasOption(Class<? extends CodeOption> option);
    boolean hasEnricher(Class<? extends Enricher> enricher);

    Map<String, PrototypeConstant> getConstants();

    GeneratedFile addCustomFile(String id);

    GeneratedFile getCustomFile(String id);

    Map<String, Structures.GeneratedFileHandler> getCustomFiles();

    void addProperties(Structures.PrototypeDataHandler properties);
}

