package net.binis.codegen.enrich.handler.base;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2023 Binis Belev
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
import net.binis.codegen.annotation.type.GenerationStrategy;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;

import java.util.function.Consumer;

public abstract class BaseStrategyEnricher extends BaseEnricher {

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        withStrategy(description, this::internalEnrich);
    }

    @Override
    public void finalizeEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        withStrategy(description, this::internalFinalizeEnrich);
    }

    @Override
    public void postProcess(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        withStrategy(description, this::internalPostProcess);
    }

    protected abstract void internalEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description);

    public void internalFinalizeEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        //Do nothing
    }

    public void internalPostProcess(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        //Do nothing
    }


    protected void withStrategy(PrototypeDescription<ClassOrInterfaceDeclaration> description, Consumer<PrototypeDescription<ClassOrInterfaceDeclaration>> consumer) {
        if (description.getProperties().getStrategy().equals(supportedStrategy())) {
            consumer.accept(description);
        }
    }

    public abstract GenerationStrategy supportedStrategy();


}
