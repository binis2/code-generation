package net.binis.codegen.enrich;

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
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface PrototypeLookup {

    JavaParser getParser();

    void registerParsed(String prototype, PrototypeDescription<?> parsed);
    void registerGenerated(String prototype, PrototypeDescription<ClassOrInterfaceDeclaration> generated);
    void registerExternalLookup(UnaryOperator<String> lookup);

    PrototypeDescription<ClassOrInterfaceDeclaration> findParsed(String prototype);
    PrototypeDescription<ClassOrInterfaceDeclaration> findGenerated(String prototype);
    PrototypeDescription<ClassOrInterfaceDeclaration> findExternal(String prototype);
    PrototypeDescription<ClassOrInterfaceDeclaration> findByInterfaceName(String name);
    PrototypeDescription<ClassOrInterfaceDeclaration> findEnum(String generated);
    Optional<PrototypeField> findField(String prototype, String name);
    boolean isParsed(String prototype);
    boolean isGenerated(String prototype);
    boolean isExternal(String prototype);
    Collection<PrototypeDescription<ClassOrInterfaceDeclaration>> parsed();
    Collection<PrototypeDescription<ClassOrInterfaceDeclaration>> generated();

    List<PrototypeDescription<ClassOrInterfaceDeclaration>> findGeneratedByFileName(String fileName);

    void addPrototypeMap(PrototypeDescription<ClassOrInterfaceDeclaration> parsed, Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> prototypeMap);

    void calcPrototypeMaps();

    ProcessingEnvironment getProcessingEnvironment();
    void setProcessingEnvironment(ProcessingEnvironment processingEnv);

    CustomDescription createCustomDescription(String id);
    CustomDescription getCustomDescription(String id);
    Collection<CustomDescription> custom();

}
