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
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.Embeddable;
import net.binis.codegen.annotation.type.EmbeddedModifierType;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.CollectionsHandler;
import net.binis.codegen.generation.core.Constants;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.javaparser.ast.Modifier.Keyword.PROTECTED;
import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Constants.*;
import static net.binis.codegen.generation.core.Generator.handleType;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.tools.Tools.notNull;
import static net.binis.codegen.tools.Tools.with;

@Slf4j
public class ModifierEnricherHandler extends BaseEnricher implements ModifierEnricher {

    private static final String TYPE_PARAMETER = "T";
    private static final String RETURN_PARAMETER = "R";

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        calcEmbeddedModifiersType(description);
        var embeddedType = description.getEmbeddedModifierType();

        var spec = description.getSpec();
        if (nonNull(description.getProperties().getMixInClass())) {
            spec = description.getMixIn().getSpec();
        }
        var intf = description.getIntf();
        var properties = description.getProperties();
        var entity = description.getProperties().getInterfaceName();

        var modifier = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, properties.getModifierName());
        description.registerClass(MODIFIER_INTF_KEY, modifier);

        ClassOrInterfaceDeclaration embeddedModifier;
        ClassOrInterfaceDeclaration embeddedModifierSolo = null;
        ClassOrInterfaceDeclaration embeddedModifierCollection = null;

        ClassOrInterfaceDeclaration modifierClass;
        ClassOrInterfaceDeclaration embeddedModifierClass;
        ClassOrInterfaceDeclaration embeddedModifierSoloClass;
        ClassOrInterfaceDeclaration embeddedModifierCollectionClass;

        if (!EmbeddedModifierType.NONE.equals(embeddedType)) {
            intf.findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.modifier.BaseModifier"));
            embeddedModifier = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, EMBEDDED + properties.getModifierName())
                    .addTypeParameter(TYPE_PARAMETER)
                    .addTypeParameter(RETURN_PARAMETER)
                    .addExtendedType("BaseModifier<T, R>");
            description.registerClass(EMBEDDED_MODIFIER_INTF_KEY, embeddedModifier);
            intf.addMember(embeddedModifier);
            if (embeddedType.isSolo()) {
                embeddedModifierSolo = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, EMBEDDED + SOLO + properties.getModifierName())
                        .addTypeParameter(RETURN_PARAMETER)
                        .addExtendedType(entity + "." + EMBEDDED + properties.getModifierName() + "<" + entity + "." + EMBEDDED + SOLO + properties.getModifierName() + "<" + RETURN_PARAMETER + ">, " + RETURN_PARAMETER + ">");
                description.registerClass(EMBEDDED_SOLO_MODIFIER_INTF_KEY, embeddedModifierSolo);
                intf.addMember(embeddedModifierSolo);
            }
            if (embeddedType.isCollection()) {
                embeddedModifierCollection = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, EMBEDDED + COLLECTION + properties.getModifierName())
                        .addTypeParameter(RETURN_PARAMETER)
                        .addExtendedType(entity + "." + EMBEDDED + properties.getModifierName() + "<" + entity + "." + EMBEDDED + COLLECTION + properties.getModifierName() + "<" + RETURN_PARAMETER + ">, " + RETURN_PARAMETER + ">");
                description.registerClass(EMBEDDED_COLLECTION_MODIFIER_INTF_KEY, embeddedModifierCollection);
                embeddedModifierCollection.addMethod("_and").setType("EmbeddedCodeCollection<" + entity + ".EmbeddedCollectionModify<" + RETURN_PARAMETER + ">, " + entity + ", " + RETURN_PARAMETER + ">").setBody(null);
                intf.findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.collection.EmbeddedCodeCollection"));
                intf.addMember(embeddedModifierCollection);
            }
        }

        var methodName = isNull(properties.getMixInClass()) ? Constants.MODIFIER_METHOD_NAME : Constants.MIXIN_MODIFYING_METHOD_PREFIX + intf.getNameAsString();
        if (!description.getProperties().isBase()) {
            modifierClass = new ClassOrInterfaceDeclaration(Modifier.createModifierList(PROTECTED), false, defaultModifierClassName(properties.getClassName()))
                    .addImplementedType(entity + "." + modifier.getNameAsString());
            addConstructor(entity, modifierClass);
            description.registerClass(MODIFIER_KEY, modifierClass);
            spec.addMember(modifierClass);
            intf.addMember(modifier);
            if (!EmbeddedModifierType.NONE.equals(embeddedType)) {
                embeddedModifierClass = new ClassOrInterfaceDeclaration(Modifier.createModifierList(PROTECTED), false, defaultModifierClassName(properties.getClassName() + EMBEDDED))
                        .addTypeParameter(TYPE_PARAMETER)
                        .addTypeParameter(RETURN_PARAMETER)
                        .addImplementedType(entity + "." + EMBEDDED + properties.getModifierName() + "<" + TYPE_PARAMETER + ", " + RETURN_PARAMETER + ">");
                addConstructor(RETURN_PARAMETER, embeddedModifierClass);
                description.registerClass(EMBEDDED_MODIFIER_KEY, embeddedModifierClass);
                spec.addMember(embeddedModifierClass);
                if (embeddedType.isSolo()) {
                    embeddedModifierSoloClass = new ClassOrInterfaceDeclaration(Modifier.createModifierList(PROTECTED), false, defaultModifierClassName(properties.getClassName() + SOLO))
                            .addExtendedType(embeddedModifierClass.getNameAsString())
                            .addImplementedType(entity + "." + embeddedModifierSolo.getNameAsString());
                    addConstructor("Object", embeddedModifierSoloClass);
                    description.registerClass(EMBEDDED_SOLO_MODIFIER_KEY, embeddedModifierSoloClass);
                    spec.addMember(embeddedModifierSoloClass);
                }
                if (embeddedType.isCollection()) {
                    embeddedModifierCollectionClass = new ClassOrInterfaceDeclaration(Modifier.createModifierList(PROTECTED), false, defaultModifierClassName(properties.getClassName() + COLLECTION))
                            .addExtendedType(embeddedModifierClass.getNameAsString())
                            .addImplementedType(entity + "." + embeddedModifierCollection.getNameAsString());
                    addConstructor("Object", embeddedModifierCollectionClass);
                    embeddedModifierCollectionClass.addMethod("_and", PUBLIC).setType("EmbeddedCodeCollection").setBody(description.getParser().parseBlock("{return (EmbeddedCodeCollection) parent;}").getResult().get());
                    spec.findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.collection.EmbeddedCodeCollection"));
                    description.registerClass(EMBEDDED_COLLECTION_MODIFIER_KEY, embeddedModifierCollectionClass);
                    spec.addMember(embeddedModifierCollectionClass);
                }
            }

            addModifyMethod(methodName, spec, properties.getLongModifierName(), modifierClass.getNameAsString(), true);
            addModifyMethod(methodName, intf, properties.getLongModifierName(), null, false);
            spec.findCompilationUnit().get().addImport("net.binis.codegen.modifier.Modifiable");
        }
        if (isNull(description.getMixIn()) && !description.getProperties().isBase()) {
            spec.addImplementedType("Modifiable<" + intf.getNameAsString() + "." + modifier.getNameAsString() + ">");
        }
    }

    private void addConstructor(String entity, ClassOrInterfaceDeclaration cls) {
        cls.addConstructor(PROTECTED).addParameter(entity, "parent").getBody().addStatement(lookup.getParser().parseStatement("super(parent);").getResult().get());
    }

    @Override
    public void finalizeEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var imports = new ArrayList<Pair<CompilationUnit, String>>();
        var intf = description.getIntf();
        var properties = description.getProperties();

        var modifier = description.getRegisteredClass(MODIFIER_INTF_KEY);
        var modifierClass = description.getRegisteredClass(MODIFIER_KEY);

        var modifierFields = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, "Fields")
                .addTypeParameter(MODIFIER_FIELD_GENERIC);

        for (var field : description.getFields()) {
            declare(description, properties, modifierFields, field, description.getDeclaration(), imports);
        }

        if (nonNull(description.getBase())) {
            var fields = description.getBase().getRegisteredClass(MODIFIER_FIELDS_KEY);
            if (nonNull(fields)) {
                modifierFields.addExtendedType(description.getBase().getInterfaceName() + "." + fields.getNameAsString() + "<" + MODIFIER_FIELD_GENERIC + ">");
                for (var field : description.getBase().getFields()) {
                    declare(description, properties, null, field, description.getBase().getDeclaration(), imports);
                }
            } else {
                for (var field : description.getBase().getFields()) {
                    declare(description, properties, modifierFields, field, description.getBase().getDeclaration(), imports);
                }
            }
        }

        if (nonNull(description.getMixIn())) {
            var fields = description.getMixIn().getRegisteredClass(MODIFIER_FIELDS_KEY);
            if (nonNull(fields)) {
                modifierFields.addExtendedType(description.getMixIn().getInterfaceName() + "." + fields.getNameAsString() + "<" + MODIFIER_FIELD_GENERIC + ">");
                for (var field : description.getMixIn().getFields()) {
                    declare(description, properties, null, field, description.getMixIn().getDeclaration(), imports);
                }
            } else {
                for (var field : description.getMixIn().getFields()) {
                    declare(description, properties, modifierFields, field, description.getMixIn().getDeclaration(), imports);
                }
            }

            if (nonNull(description.getMixIn().getBase())) {
                var flds = description.getMixIn().getBase().getRegisteredClass(MODIFIER_FIELDS_KEY);
                if (nonNull(flds)) {
                    var type = description.getMixIn().getBase().getInterfaceName() + "." + flds.getNameAsString() + "<" + MODIFIER_FIELD_GENERIC + ">";
                    if (fields.getExtendedTypes().stream().noneMatch(t -> t.toString().equals(type))) {
                        modifierFields.addExtendedType(type);
                    }
                    for (var field : description.getMixIn().getBase().getFields()) {
                        declare(description, properties, null, field, description.getMixIn().getBase().getDeclaration(), imports);
                    }
                } else {
                    for (var field : description.getMixIn().getBase().getFields()) {
                        declare(description, properties, modifierFields, field, description.getMixIn().getBase().getDeclaration(), imports);
                    }
                }
            }
        }

        var embedded = description.getRegisteredClass(EMBEDDED_MODIFIER_INTF_KEY);
        var baseModifier = handleBaseModifierClass(description);
        if (!modifierFields.isEmpty()) {
            if (nonNull(embedded)) {
                embedded.addExtendedType(intf.getNameAsString() + "." + modifierFields.getNameAsString() + "<" + TYPE_PARAMETER + ">");
            } else {
                modifier.addExtendedType(intf.getNameAsString() + "." + modifierFields.getNameAsString() + "<" + intf.getNameAsString() + "." + modifier.getNameAsString() + ">");
            }
            intf.addMember(modifierFields);
            description.registerClass(MODIFIER_FIELDS_KEY, modifierFields);
            intf.findCompilationUnit().ifPresent(dest ->
                    imports.forEach(pair ->
                            notNull(getExternalClassNameIfExists(pair.getKey(), pair.getValue()), dest::addImport)));
        }

        if (nonNull(embedded)) {
            description.getRegisteredClass(EMBEDDED_MODIFIER_KEY).addExtendedType(baseModifier + "<" + TYPE_PARAMETER + ", " + RETURN_PARAMETER + ">");
            modifier.addExtendedType(EMBEDDED + properties.getModifierName() + "<" + description.getInterfaceName() + "." + properties.getModifierName() + ", " + description.getInterfaceName() + ">");
            modifierClass.addExtendedType(description.getRegisteredClass(EMBEDDED_MODIFIER_KEY).getNameAsString() + "<" + description.getInterfaceName() + "." + properties.getModifierName() + ", " + description.getInterfaceName() + ">");
        } else {
            if (nonNull(modifierClass)) {
                modifierClass.addExtendedType(baseModifier + "<" + description.getInterfaceName() + "." + properties.getModifierName() + ", " + description.getInterfaceName() + ">");
                modifierClass.addMethod("done", PUBLIC).setType(description.getInterfaceName()).setBody(description.getParser().parseBlock("{return " + (isNull(description.getMixIn()) ? description.getProperties().getClassName() : description.getMixIn().getProperties().getClassName()) + ".this;}").getResult().get());
            }
        }
        baseModifier = handleBaseModifier(description);
        if (!"BaseModifier".equals(baseModifier) || isNull(embedded)) {
            modifier.addExtendedType(baseModifier + "<" + description.getInterfaceName() + "." + properties.getModifierName() + ", " + description.getInterfaceName() + ">");
        }
    }

    private String handleBaseModifierClass(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var baseModifier = description.getProperties().getBaseModifierClass();
        if (isNull(baseModifier)) {
            if (!description.getProperties().isBase()) {
                var spec = description.getSpec();
                if (nonNull(description.getMixIn())) {
                        spec = description.getMixIn().getSpec();
                }
                spec.findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.modifier.impl.BaseModifierImpl"));
            }
            return "BaseModifierImpl";
        }
        var full = getExternalClassNameIfExists(description.getDeclaration().findCompilationUnit().get(), baseModifier);
        if (isNull(full)) {
            full = baseModifier;
            baseModifier = full.substring(Math.max(0, full.lastIndexOf('.') + 1));
        }
        var path = full.substring(0, full.indexOf(baseModifier)) + "impl." + baseModifier + "Impl";
        if (!description.getProperties().isBase()) {
            description.getSpec().findCompilationUnit().ifPresent(u -> u.addImport(path));
        }
        return baseModifier + "Impl";
    }

    private String handleBaseModifier(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var baseModifier = description.getProperties().getBaseModifierClass();
        if (isNull(baseModifier)) {
            if (!description.getProperties().isBase()) {
                description.getIntf().findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.modifier.BaseModifier"));
            }
            return "BaseModifier";
        }
        if (!description.getProperties().isBase()) {
            var full = getExternalClassNameIfExists(description.getDeclaration().findCompilationUnit().get(), baseModifier);
            if (isNull(full)) {
                full = baseModifier;
                baseModifier = full.substring(Math.max(0, full.lastIndexOf('.') + 1));
            }
            var path = full;
            description.getIntf().findCompilationUnit().ifPresent(u -> u.addImport(path));
        }
        return baseModifier;
    }

    private void calcEmbeddedModifiersType(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var ann = description.getDeclaration().getAnnotationByClass(Embeddable.class);
        if (ann.isPresent()) {
            description.setEmbeddedModifier(EmbeddedModifierType.valueOf(Helpers.getAnnotationValue(ann.get())));
        } else {
            lookup.parsed().forEach(parsed -> {
                parsed.getFields().stream()
                        .filter(field -> nonNull(field.getPrototype()))
                        .filter(field -> field.getPrototype().equals(description))
                        .forEach(field -> {
                            if (field.isCollection()) {
                                description.addEmbeddedModifier(EmbeddedModifierType.COLLECTION);
                            } else {
                                description.addEmbeddedModifier(EmbeddedModifierType.SINGLE);
                            }
                        });
                if (nonNull(parsed.getBase()) && parsed.getBase().getDeclaration().asClassOrInterfaceDeclaration().getTypeParameters().isNonEmpty()) {
                    var base = parsed.getBase().getDeclaration().asClassOrInterfaceDeclaration();
                    var proto = parsed.getDeclaration().asClassOrInterfaceDeclaration();
                    var generics = buildGenerics(proto.getExtendedTypes().stream().filter(t -> t.getNameAsString().equals(base.getNameAsString())).findFirst().get(), base);
                    parsed.getBase().getFields().forEach(field ->
                            with(generics.get(field.getType()), t -> with(getExternalClassName(proto.findCompilationUnit().get(), t.asString()), n -> with(lookup.findParsed(n), p -> {
                                if (p.equals(description)) {
                                    if (field.isCollection()) {
                                        description.addEmbeddedModifier(EmbeddedModifierType.COLLECTION);
                                    } else {
                                        description.addEmbeddedModifier(EmbeddedModifierType.SINGLE);
                                    }
                                }
                            }))));
                }
            });
        }
    }

    private void declare(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeData properties, ClassOrInterfaceDeclaration modifierFields, PrototypeField field, TypeDeclaration<ClassOrInterfaceDeclaration> classDeclaration, List<Pair<CompilationUnit, String>> imports) {
        if (!field.getIgnores().isForModifier()) {
            var pair = getFieldType(description, field);
            var type = pair.getKey();
            var modifierClass = description.getRegisteredClass(EMBEDDED_MODIFIER_KEY);
            var modifier = description.getRegisteredClass(EMBEDDED_MODIFIER_INTF_KEY);
            var returnType = TYPE_PARAMETER;
            var cast = TYPE_PARAMETER;
            if (isNull(modifierClass)) {
                modifierClass = description.getRegisteredClass(MODIFIER_KEY);
                returnType = properties.getLongModifierName();
                cast = null;
            }

            if (nonNull(modifierClass)) {
                addModifier(modifierClass, field, isNull(properties.getMixInClass()) ? properties.getClassName() : description.getMixIn().getParsedName(), returnType, true, type, cast);
            }

            if (CollectionsHandler.isCollection(type)) {
                if (isNull(modifier)) {
                    modifier = description.getRegisteredClass(MODIFIER_INTF_KEY);
                    returnType = properties.getModifierName();
                }
                addModifier(modifier, field, null, returnType, false, type, cast);
                var proto = nonNull(pair.getValue()) ? pair.getValue() : field.getPrototype();
                if (nonNull(proto) && proto.getEmbeddedModifierType().isCollection()) {
                    CollectionsHandler.addModifier(description, modifierClass, field, properties.getLongModifierName(), isNull(properties.getMixInClass()) ? properties.getClassName() : description.getMixIn().getParsedName(), true);
                    CollectionsHandler.addModifier(description, modifier, field, description.getInterfaceName(), null, false);
                }
            } else {
                addField(field, modifierFields, imports, type);
                var proto = nonNull(pair.getValue()) ? pair.getValue() : field.getPrototype();
                if (nonNull(proto) && proto.getEmbeddedModifierType().isSolo()) {
                    if (isNull(modifier)) {
                        modifier = description.getRegisteredClass(MODIFIER_INTF_KEY);
                    }
                    returnType = Helpers.calcType(modifier);
                    modifier.addMethod(field.getName()).setType(proto.getInterfaceName() + ".EmbeddedSoloModify<" + returnType + ">").setBody(null);
                    var className = isNull(description.getMixIn()) ? description.getProperties().getClassName() : description.getMixIn().getProperties().getClassName();
                    modifierClass.addMethod(field.getName(), PUBLIC).setType(proto.getInterfaceName() + ".EmbeddedSoloModify<" + ((ClassOrInterfaceDeclaration) modifier.getParentNode().get()).getNameAsString() + "." + returnType + ">").setBody(description.getParser().parseBlock(
                            "{ if (" + className + ".this." + field.getName() + " == null) {" +
                                    className + ".this." + field.getName() + " = CodeFactory.create(" + proto.getInterfaceName() + ".class);}" +
                                    "return CodeFactory.modify(this, " + className + ".this." + field.getName() + ", " + proto.getInterfaceName() + ".class); }").getResult().get());
                    modifierClass.findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.factory.CodeFactory"));

                    with(description.getRegisteredClass(MODIFIER_INTF_KEY), cls -> {
                        cls.addMethod(field.getName() + "$").setType(properties.getModifierName()).addParameter("Consumer<" + proto.getInterfaceName() + ".Modify>", "init").setBody(null);
                        cls.findCompilationUnit().ifPresent(u -> u.addImport(Consumer.class));
                    });

                    with(description.getRegisteredClass(MODIFIER_KEY), cls -> {
                        var methodName = isNull(proto.getMixIn()) ? Constants.MODIFIER_METHOD_NAME : Constants.MIXIN_MODIFYING_METHOD_PREFIX + proto.getInterfaceName();
                        cls.addMethod(field.getName() + "$", PUBLIC).setType(properties.getInterfaceName() + "." + properties.getModifierName()).addParameter("Consumer<" + proto.getInterfaceName() + ".Modify>", "init").setBody(description.getParser().parseBlock(
                                "{ if (" + className + ".this." + field.getName() + " == null) {" +
                                        className + ".this." + field.getName() + " = CodeFactory.create(" + proto.getInterfaceName() + ".class);}" +
                                        "init.accept(" + className + ".this." + field.getName() + "." + methodName + "());" +
                                        "return this;}"
                        ).getResult().get());
                        cls.findCompilationUnit().ifPresent(u -> u.addImport(Consumer.class));
                    });

                }
            }
        }
    }

    private void addField(PrototypeField field, ClassOrInterfaceDeclaration modifierFields, List<Pair<CompilationUnit, String>> imports, Type generic) {
        if (nonNull(modifierFields)) {
            var type = nonNull(generic) ? generic : field.getDeclaration().getVariable(0).getType();
            modifierFields.addMethod(field.getName())
                    .setType(MODIFIER_FIELD_GENERIC)
                    .addParameter(type, field.getName())
                    .setBody(null);
            field.getDeclaration().findCompilationUnit().ifPresent(source ->
                    imports.add(Pair.of(source, type.asString())));
        }
    }

    private static void addModifyMethod(String methodName, ClassOrInterfaceDeclaration spec, String modifierName, String modifierClassName, boolean isClass) {
        var method = spec
                .addMethod(methodName)
                .setType(modifierName);
        if (isClass) {
            method
                    .addModifier(PUBLIC)
                    .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + modifierClassName + "(this)"))));
        } else {
            method.setBody(null);
        }
    }

    private static void addDoneMethod(ClassOrInterfaceDeclaration spec, String parentName, String parentClassName, boolean isClass) {
        var method = spec
                .addMethod("done")
                .setType(parentName);
        if (isClass) {
            method
                    .addModifier(PUBLIC)
                    .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName(parentClassName + ".this"))));
        } else {
            method.setBody(null);
        }
    }

    @Override
    public int order() {
        return 10000;
    }

    private void addModifier(ClassOrInterfaceDeclaration spec, PrototypeField declaration, String modifierClassName, String modifierName, boolean isClass, Type generic, String cast) {
        var type = declaration.isGenericField() ? generic.asString() : declaration.isGenericMethod() ? "Object" : declaration.getType();
        if (isNull(type)) {
            type = isNull(declaration.getDescription()) || "dummy".equals(declaration.getDescription().findCompilationUnit().get().getPackageDeclaration().get().getNameAsString()) ?
                    handleType(declaration.getDeclaration().findCompilationUnit().get(), spec.findCompilationUnit().get(), declaration.getDeclaration().getVariables().get(0).getType()) :
                    (declaration.getDescription().getTypeParameters().isEmpty() ? handleType(declaration.getDescription().findCompilationUnit().get(), spec.findCompilationUnit().get(), declaration.getDeclaration().getVariable(0).getType()) : "Object");
        } else {
            spec.findCompilationUnit().get().addImport(declaration.getFullType());
        }
        var method = new MethodDeclaration().setName(declaration.getName())
                .setType(modifierName)
                .addParameter(new Parameter().setName(declaration.getName()).setType(type));
        if (isClass) {
            method
                    .addModifier(PUBLIC)
                    .setBody(new BlockStmt()
                            .addStatement(new AssignExpr().setTarget(new NameExpr().setName(modifierClassName + ".this." + declaration.getName())).setValue(new NameExpr().setName(declaration.getName())))
                            .addStatement(new ReturnStmt().setExpression(new NameExpr().setName((nonNull(cast) ? "(" + cast + ") " : "") + "this"))));
            declaration.addModifier(method);
        } else {
            method.setBody(null);
        }
        if (!methodExists(spec, method, isClass)) {
            spec.addMember(method);
        }
    }

}
