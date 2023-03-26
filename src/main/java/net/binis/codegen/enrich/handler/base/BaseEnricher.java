package net.binis.codegen.enrich.handler.base;

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
import net.binis.codegen.enrich.Enricher;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.enrich.PrototypeLookup;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.generation.core.interfaces.MethodDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;

import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;

public abstract class BaseEnricher implements PrototypeEnricher {

    protected PrototypeLookup lookup;
    protected JavaParser parser;

    @Override
    public void init(PrototypeLookup lookup) {
        this.lookup = lookup;
        parser = lookup.getParser();
    }

    @Override
    public void setup(PrototypeData properties) {
        //Do nothing
    }

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        //Do nothing
    }

    @Override
    public void finalizeEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        //Do nothing
    }

    @Override
    public void postProcess(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        //Do nothing
    }

    @Override
    public List<Class<? extends Enricher>> dependencies() {
        return Collections.emptyList();
    }

    @Override
    public void enrichMethod(MethodDescription method) {
        //Do nothing
    }

    protected void error(String message) {
        if (nonNull(lookup.getProcessingEnvironment())) {
            lookup.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR, message);
        } else {
            throw new GenericCodeGenException(message);
        }
    }

}
