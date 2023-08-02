package net.binis.codegen.enrich.handler;

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

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.CGClassDeclaration;
import net.binis.codegen.compiler.CGVariableDecl;
import net.binis.codegen.compiler.utils.ElementFieldUtils;
import net.binis.codegen.compiler.utils.ElementMethodUtils;
import net.binis.codegen.enrich.LogEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.interfaces.ElementDescription;

import java.lang.reflect.Modifier;
import java.util.logging.Logger;

import static java.lang.reflect.Modifier.*;
import static net.binis.codegen.compiler.utils.ElementUtils.getDeclaration;

@Slf4j
public class LogEnricherHandler extends BaseEnricher implements LogEnricher {

    @Override
    public void enrichElement(ElementDescription description) {
        var declaration = getDeclaration(description.getElement());
        if (declaration instanceof CGClassDeclaration cls && !cls.isInterface() && !cls.isEnum() && !cls.isAnnotation()) {
            if (cls.getDefs().stream()
                    .filter(CGVariableDecl.class::isInstance)
                    .map(CGVariableDecl.class::cast)
                    .noneMatch(f -> "log".equals(f.getName().toString()))) {
                var factoryMethod = ElementMethodUtils.createStaticMethodInvocation(Logger.class, "getLogger",
                        ElementMethodUtils.createClassMethodInvocation(description.getElement().getSimpleName().toString(), "getName"));
                ElementFieldUtils.addField(description.getElement(), "log", Logger.class, PROTECTED | STATIC | FINAL, factoryMethod);
            } else {
                note("Log field already defined.", description.getElement());
            }
        } else {
            note("Log is applicable only for classes.", description.getElement());
        }
    }
    @Override
    public int order() {
        return 0;
    }

}
