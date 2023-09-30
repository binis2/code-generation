package net.binis.codegen.enrich.handler.constructor;

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

import net.binis.codegen.compiler.CGVariableDecl;
import net.binis.codegen.enrich.constructor.RequiredArgsConstructorEnricher;

import java.util.stream.Stream;

public class NoArgsConstructorEnricherHandler extends BaseArgsConstructorEnricherHandler implements RequiredArgsConstructorEnricher {


    @Override
    protected Stream<CGVariableDecl> applyFieldsFilter(Stream<CGVariableDecl> stream) {
        return stream.filter(f -> false);
    }

    @Override
    protected String getName() {
        return "No";
    }
}
