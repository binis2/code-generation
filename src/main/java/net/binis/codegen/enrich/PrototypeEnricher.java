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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import net.binis.codegen.generation.core.interfaces.MethodDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;

import java.util.List;

public interface PrototypeEnricher extends Enricher {

    void init(PrototypeLookup lookup);
    void setup(PrototypeData properies);
    void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description);
    void finalizeEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description);
    void postProcess(PrototypeDescription<ClassOrInterfaceDeclaration> description);
    int order();

    List<Class<? extends Enricher>> dependencies();

    void enrichMethod(MethodDescription method);
}
