package net.binis.codegen.enrich.handler.base;

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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import net.binis.codegen.generation.core.interfaces.ElementDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;

public abstract class BaseConditionalEnricher extends BaseEnricher {

    protected boolean shouldEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        return true;
    }

    protected boolean shouldEnrich(ElementDescription description) {
        return true;
    }

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        if (shouldEnrich(description)) {
            super.enrich(description);
        }
    }

    @Override
    public void finalizeEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        if (shouldEnrich(description)) {
            super.finalizeEnrich(description);
        }
    }

    @Override
    public void postProcess(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        if (shouldEnrich(description)) {
            super.postProcess(description);
        }
    }

    @Override
    public void enrichElement(ElementDescription description) {
        if (shouldEnrich(description)) {
            super.enrichElement(description);
        }
    }

}
