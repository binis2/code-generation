package net.binis.codegen.compiler.utils;

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

import net.binis.codegen.compiler.*;

import javax.lang.model.element.Element;

import static java.util.Objects.nonNull;

public class ElementFieldUtils extends ElementUtils {

    public static CGVariableDecl addField(Element element, String name, Class<?> type, long flags, CGExpression init) {
        var maker = TreeMaker.create();
        var declaration = getDeclaration(element, maker);
        var def = maker.VarDef(maker.Modifiers(flags) , CGName.create(name), chainDotsString(type.getCanonicalName()), nonNull(init) ? init : null);
        declaration.getDefs().append(def);
        return def;
    }

    public static CGVariableDecl addField(Element element, String name, Class<?> type, long flags) {
        return addField(element, name, type, flags, null);
    }



}
