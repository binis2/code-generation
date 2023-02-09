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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.enrich.JacksonEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.options.Options;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class JacksonEnricherHandler extends BaseEnricher implements JacksonEnricher {

    private static final boolean IS_JACKSON_AVAILABLE = nonNull(loadClass("com.fasterxml.jackson.databind.annotation.JsonDeserialize"));

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        if (generate(description)) {
            var spec = description.getImplementation();
            spec.findCompilationUnit().ifPresent(unit -> {
                unit.addImport("com.fasterxml.jackson.databind.annotation", false, true);
                description.getFields().forEach(f -> enrichField(unit, f));
            });
        }
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 10000;
    }

    private void enrichField(CompilationUnit unit, PrototypeField field) {
        if (field.isCollection()) {
            if (!field.getTypePrototypes().isEmpty()) {
                var ann = field.getDeclaration().addAndGetAnnotation("JsonDeserialize");
                if ("java.util.Map".equals(field.getFullType())) {
                    if (field.getDeclaration().getCommonType().isClassOrInterfaceType()) {
                        field.getDeclaration().getCommonType().asClassOrInterfaceType().getTypeArguments().ifPresent(args -> {
                            unit.addImport("java.util.HashMap");
                            ann.addPair("as", "HashMap.class");
                            var key = field.getTypePrototypes().get(args.get(0).asString());
                            if (nonNull(key)) {
                                ann.addPair("keyAs", key.getInterfaceName() + ".class");
                            }
                            var content = field.getTypePrototypes().get(args.get(1).asString());
                            if (nonNull(content)) {
                                ann.addPair("contentAs", content.getInterfaceName() + ".class");
                            }
                        });
                    }
                } else {
                    var proto = field.getTypePrototypes().values().iterator().next();
                    ann.addPair("contentAs", proto.getInterfaceName() + ".class");
                }
            }
        }
    }

    private boolean generate(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        return description.hasOption(Options.HANDLE_JACKSON_ALWAYS) || (IS_JACKSON_AVAILABLE && description.hasOption(Options.HANDLE_JACKSON_IF_AVAILABLE));
    }
}
