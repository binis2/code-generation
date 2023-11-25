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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.PrimitiveType;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.enrich.HibernateEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.objects.base.enumeration.CodeEnum;

import java.lang.reflect.Method;

import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.EnrichHelpers.annotation;
import static net.binis.codegen.generation.core.Helpers.hasAnnotation;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.with;
import static net.binis.codegen.tools.Tools.withRes;

@Slf4j
public class HibernateEnricherHandler extends BaseEnricher implements HibernateEnricher {

    protected static final Method EQUALS = initEqualsMethod();

    protected static final Method HASH_CODE = initHashCode();
    protected static final String TYPE = "org.hibernate.annotations.Type";

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        description.getFields().stream()
                .filter(field ->
                        field.isCollection() ||
                                (nonNull(field.getPrototype()) && field.getPrototype().isCodeEnum()) ||
                                nonNull(lookup.findEnum(field.getFullType())) ||
                                withRes(loadClass(field.getFullType()), CodeEnum.class::isAssignableFrom, false))
                .filter(field -> !hasAnnotation(field.getDeclaration(), TYPE))
                .forEach(this::processField);
    }

    private void processField(PrototypeField field) {
        var declUnit = field.getDeclaration().findCompilationUnit();
        if (field.isCollection()) {
            var types = field.getType().asClassOrInterfaceType().getTypeArguments().orElse(null);
            if (nonNull(types)) {
                if (types.size() == 1) {
                    var type = types.get(0).toString();
                    var proto = nonNull(field.getTypePrototypes()) ? field.getTypePrototypes().get(type) : null;
                    if ((nonNull(proto) && proto.isCodeEnum()) ||
                            withRes(lookup.findGenerated(Helpers.getExternalClassName(declUnit.get(), type)), PrototypeDescription::isCodeEnum, false)) {
                        declUnit.ifPresent(unit ->
                                unit.addImport("jakarta.persistence.ElementCollection"));
                        field.getDeclaration().addAnnotation(annotation("@ElementCollection"));
                    } else {
                        return;
                    }
                } else {
                    log.warn("Collection type has more than one type argument, case not implemented!");
                    return;
                }
            } else {
                return;
            }
        }

        declUnit.ifPresent(unit ->
                unit.addImport(TYPE));

        field.getDeclaration().addAnnotation(annotation("@Type(net.binis.codegen.hibernate.CodeEnumType.class)"));

        //Silencing missing equals and hashCode Hibernate warnings.
        with(field.getPrototype(), prototype ->

        {
            var intf = prototype.getInterface();
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
