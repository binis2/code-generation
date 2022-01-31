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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.function.Consumer;

public interface PrototypeDescription<T extends TypeDeclaration<T>> {

    boolean isProcessed();
    boolean isInvalid();

    JavaParser getParser();

    Class<?> getCompiled();

    String getPrototypeFileName();

    PrototypeData getProperties();

    String getParsedName();
    String getParsedFullName();

    String getInterfaceName();
    String getInterfaceFullName();

    String getImplementorFullName();

    TypeDeclaration<T> getDeclaration();
    List<CompilationUnit> getFiles();

    PrototypeDescription<T> getBase();
    PrototypeDescription<T> getMixIn();

    List<PrototypeField> getFields();

    ClassOrInterfaceDeclaration getSpec();
    ClassOrInterfaceDeclaration getIntf();

    List<Triple<ClassOrInterfaceDeclaration, Node, ClassOrInterfaceDeclaration>> getInitializers();
    List<Consumer<BlockStmt>> getCustomInitializers();

    void registerClass(String key, ClassOrInterfaceDeclaration declaration);
    ClassOrInterfaceDeclaration getRegisteredClass(String key);

    void registerPostProcessAction(Runnable task);
    void processActions();

    boolean isValid();

    boolean isNested();

}
