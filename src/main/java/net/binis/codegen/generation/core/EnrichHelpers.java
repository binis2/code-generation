package net.binis.codegen.generation.core;

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

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

import static net.binis.codegen.generation.core.Helpers.lookup;

public class EnrichHelpers {

    public static BlockStmt block(String code) {
        return lookup.getParser().parseBlock(code).getResult().get();
    }

    public static BlockStmt returnBlock(String variable) {
        return block("{return " + variable + ";}");
    }

    public static Statement statement(String code) {
        return lookup.getParser().parseStatement(code).getResult().get();
    }

    public static Expression expression(String code) {
        return lookup.getParser().parseExpression(code).getResult().get();
    }

    private EnrichHelpers() {
        //Do nothing
    }

}
