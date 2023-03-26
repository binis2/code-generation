package net.binis.codegen.enrich.handler;

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
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.interfaces.MethodDescription;
import net.binis.codegen.tools.Interpolator;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class ValidatorEnricher extends BaseEnricher {

    public static final String TEMPLATE = """
                package ${package};
                import net.binis.codegen.annotation.CodeAnnotation;
                import net.binis.codegen.validation.annotation.ValidateLambda;
                
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Target;

                @CodeAnnotation
                @Target({ElementType.METHOD, ElementType.TYPE_USE})
                @ValidateLambda("${class}::${methodName}")
                public @interface ${name} {
                    String message() default "${message}";
                }
            """;

    public static final String DEFAULT_MESSAGE = "({field}) Invalid value!";

    protected static Interpolator interpolator = Interpolator.build('$', TEMPLATE);

    @Override
    public void enrichMethod(MethodDescription method) {
        if (method.getMethod().isStatic() && "boolean".equals(method.getMethod().getType().asString()) && method.getMethod().getParameters().size() == 1) {
            var parent = ((ClassOrInterfaceDeclaration) method.getMethod().getParentNode().get());
            var pack = parent.findCompilationUnit().get().getPackageDeclaration().get().getNameAsString() + ".annotation.validation";
            var name = "Validate" + StringUtils.capitalize(method.getMethod().getNameAsString());
            var cls = interpolator.params(Map.of(
                    "methodName", method.getMethod().getNameAsString(),
                    "name", name,
                    "message", method.getPrototype() instanceof NormalAnnotationExpr exp ?
                            exp.getPairs().stream()
                                    .filter(p -> p.getNameAsString().equals("message"))
                                    .map(p -> p.getValue().asStringLiteralExpr().asString())
                                    .findFirst()
                                    .orElse(DEFAULT_MESSAGE) :
                            DEFAULT_MESSAGE,
                    "class", ((ClassOrInterfaceDeclaration) method.getMethod().getParentNode().get()).getFullyQualifiedName().get(),
                    "package", pack
            )).interpolate();

            var unit = lookup.getParser().parse(cls).getResult().get();
            method.getDescription().addCustomFile(pack + '.' + name).setJavaClass(unit.getType(0));
        } else {
            error(method.getMethod().getNameAsString() + " is not valid @Validator target! (example 'public static boolean method(Type value)')");
        }
    }

    @Override
    public int order() {
        return 0;
    }

}
