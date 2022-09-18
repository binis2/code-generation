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

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.PrimitiveType;
import net.binis.codegen.enrich.HibernateEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;

import java.lang.reflect.Method;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Tools.with;

public class HibernateEnricherHandler extends BaseEnricher implements HibernateEnricher {

    private static final Method EQUALS = initEqualsMethod();


    private static final Method HASH_CODE = initHashCode();

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        description.getFields().stream()
                .filter(field ->
                        (nonNull(field.getPrototype()) && field.getPrototype().isCodeEnum()) || nonNull(lookup.findEnum(field.getFullType())))
                .forEach(this::processField);
    }

    private void processField(PrototypeField field) {
        field.getDeclaration().findCompilationUnit().ifPresent(unit ->
                unit.addImport("org.hibernate.annotations.Type"));

        field.getDeclaration().addAnnotation(StaticJavaParser.parseAnnotation("@Type(type = \"net.binis.codegen.hibernate.CodeEnumType\")"));

        //Silencing missing equals and hashCode Hibernate warning.
        with(field.getPrototype(), prototype -> {
            var intf = prototype.getIntf();
            if (!Helpers.methodExists(intf, EQUALS, false)) {
                intf.addMethod("equals")
                        .setType(PrimitiveType.booleanType())
                        .addParameter("Object", "o")
                        .setBody(null);
            }
            if (!Helpers.methodExists(intf, HASH_CODE, false)) {
                intf.addMethod("hashCode")
                        .setType(PrimitiveType.intType())
                        .setBody(null);
            }
        });

    }

    @Override
    public int order() {
        return 0;
    }

    private static Method initEqualsMethod() {
        try {
            return Object.class.getMethod("equals", Object.class);
        } catch (NoSuchMethodException e) {
            throw new GenericCodeGenException(e);
        }
    }


    private static Method initHashCode() {
        try {
            return Object.class.getMethod("hashCode");
        } catch (NoSuchMethodException e) {
            throw new GenericCodeGenException(e);
        }
    }


}
