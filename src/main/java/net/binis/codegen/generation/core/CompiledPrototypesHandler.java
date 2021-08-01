package net.binis.codegen.generation.core;

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
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.CodePrototype;

import static net.binis.codegen.generation.core.Generator.generateCodeForClass;
import static net.binis.codegen.generation.core.Helpers.lookup;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.notNull;

@Slf4j
public abstract class CompiledPrototypesHandler {

    private static final JavaParser parser = new JavaParser();

    private CompiledPrototypesHandler() {
        //Do nothing
    }

    public static void handleCompiledPrototype(String compiledPrototype) {
        notNull(loadClass(compiledPrototype), c -> {
            notNull(c.getAnnotation(CodePrototype.class), ann -> {
                var declaration = new CompilationUnit().setPackageDeclaration(c.getPackageName()).addClass(c.getSimpleName()).setInterface(true);
                handleAnnotations(c, declaration);
                handleFields(c, declaration);

                var parsed = Structures.Parsed.<ClassOrInterfaceDeclaration>builder()
                        .compiled(c)
                        .parser(parser)
                        .declaration(declaration);

                lookup.registerParsed(compiledPrototype, parsed.build());
                generateCodeForClass(declaration.findCompilationUnit().get());

                //TODO: Implement field annotations
                //TODO: Implement class annotations
            });
        });
    }

    private static void handleFields(Class<?> c, ClassOrInterfaceDeclaration declaration) {
        for (var method : c.getDeclaredMethods()) {
            if (!method.isDefault() && method.getParameterCount() == 0 && !Void.class.equals(method.getReturnType())) {
                declaration.addMethod(method.getName()).setType(method.getReturnType().getSimpleName()).setBody(null);
                if (method.getReturnType().isPrimitive()) {
                    declaration.findCompilationUnit().get().addImport(method.getReturnType().getCanonicalName());
                }
            }
        }
    }

    private static void handleAnnotations(Class<?> cls, ClassOrInterfaceDeclaration declaration) {
        for (var ann : cls.getAnnotations()) {
            parser.parseAnnotation(ann.toString()).getResult().ifPresent(annotation -> {
                declaration.findCompilationUnit().get().addImport(ann.annotationType().getCanonicalName());
                annotation.setName(ann.annotationType().getSimpleName());
                declaration.addAnnotation(annotation);
            });
        }
    }

}
