package net.binis.codegen.enrich.handler;

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
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.interfaces.ElementDescription;
import net.binis.codegen.tools.Interpolator;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class SanitizerEnricher extends BaseEnricher {

    public static final String TEMPLATE = """
                package ${package};
                import net.binis.codegen.annotation.CodeAnnotation;
                import net.binis.codegen.validation.annotation.SanitizeLambda;
                
                import java.lang.annotation.ElementType;
                import java.lang.annotation.Target;

                @CodeAnnotation
                @Target({ElementType.METHOD, ElementType.TYPE_USE})
                @SanitizeLambda("${class}::${methodName}")
                public @interface ${name} {
                    String message() default "${message}";
                }
            """;

    public static final String DEFAULT_MESSAGE = "({field}) Invalid value!";

    protected static Interpolator interpolator = Interpolator.build('$', TEMPLATE);

    @Override
    public void enrichElement(ElementDescription description) {
        if (description.getNode() instanceof MethodDeclaration method && method.isStatic() && method.getParameters().size() == 1 && method.getType().asString().equals(method.getParameter(0).getTypeAsString())) {
            var parent = ((ClassOrInterfaceDeclaration) method.getParentNode().get());
            var pack = parent.findCompilationUnit().get().getPackageDeclaration().get().getNameAsString() + ".annotation.validation";
            var name = "Sanitize" + StringUtils.capitalize(method.getNameAsString());
            var cls = interpolator.params(Map.of(
                    "methodName", method.getNameAsString(),
                    "name", name,
                    "message", description.getPrototype() instanceof NormalAnnotationExpr exp ?
                            exp.getPairs().stream()
                                    .filter(p -> p.getNameAsString().equals("message"))
                                    .map(p -> p.getValue().asStringLiteralExpr().asString())
                                    .findFirst()
                                    .orElse(DEFAULT_MESSAGE) :
                            DEFAULT_MESSAGE,
                    "class", ((ClassOrInterfaceDeclaration) method.getParentNode().get()).getFullyQualifiedName().get(),
                    "package", pack
            )).interpolate();

            var unit = lookup.getParser().parse(cls).getResult().get();
            description.getDescription().addCustomFile(pack + '.' + name).setJavaClass(unit.getType(0));
        } else {
            error(description.getNode() instanceof NodeWithSimpleName<?> name ? name.getNameAsString() : "Element" + " is not valid @Sanitizer target! (example 'public static Type method(Type value)')", description.getElement());
        }
    }

    @Override
    public int order() {
        return 0;
    }

}
