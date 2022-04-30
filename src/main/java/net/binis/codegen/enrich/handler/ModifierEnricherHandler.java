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
import net.binis.codegen.collection.EmbeddedCodeCollection;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.generation.core.CollectionsHandler;
import net.binis.codegen.generation.core.Constants;
import net.binis.codegen.generation.core.Generator;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        if (nonNull(properties.getBaseModifierClass())) {
            modifier.addExtendedType(properties.getBaseModifierClass() + "<" + entity + "." + properties.getModifierName() + ", " + entity + ">");
        }
        description.registerClass(MODIFIER_INTF_KEY, modifier);

        ClassOrInterfaceDeclaration embeddedModifier = null;
        ClassOrInterfaceDeclaration embeddedModifierSolo = null;
        ClassOrInterfaceDeclaration embeddedModifierCollection = null;

        ClassOrInterfaceDeclaration modifierClass;
        ClassOrInterfaceDeclaration embeddedModifierClass = null;
        ClassOrInterfaceDeclaration embeddedModifierSoloClass = null;
        ClassOrInterfaceDeclaration embeddedModifierCollectionClass = null;

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
            description.registerClass(MODIFIER_KEY, modifierClass);
            spec.addMember(modifierClass);
            intf.addMember(modifier);
            if (!EmbeddedModifierType.NONE.equals(embeddedType)) {
                embeddedModifierClass = new ClassOrInterfaceDeclaration(Modifier.createModifierList(PROTECTED), false, defaultModifierClassName(properties.getClassName() + EMBEDDED))
                        .addTypeParameter(TYPE_PARAMETER)
                        .addTypeParameter(RETURN_PARAMETER)
                        .addImplementedType(entity + "." + EMBEDDED + properties.getModifierName() + "<" + TYPE_PARAMETER + ", " + RETURN_PARAMETER + ">");
                embeddedModifierClass.addConstructor(PROTECTED).addParameter("Object", "parent").setBody(description.getParser().parseBlock("{this.parent = (R) parent;}").getResult().get());
                embeddedModifierClass.addConstructor(PROTECTED).setBody(description.getParser().parseBlock("{setObject((R) " + properties.getClassName() + ".this);}").getResult().get());
                description.registerClass(EMBEDDED_MODIFIER_KEY, embeddedModifierClass);
                spec.addMember(embeddedModifierClass);
                if (embeddedType.isSolo()) {
                    embeddedModifierSoloClass = new ClassOrInterfaceDeclaration(Modifier.createModifierList(PROTECTED), false, defaultModifierClassName(properties.getClassName() + SOLO))
                            .addExtendedType(embeddedModifierClass.getNameAsString())
                            .addImplementedType(entity + "." + embeddedModifierSolo.getNameAsString());
                    embeddedModifierSoloClass.addConstructor(PROTECTED).addParameter("Object", "parent").getBody().addStatement(description.getParser().parseStatement("super(parent);").getResult().get());
                    description.registerClass(EMBEDDED_SOLO_MODIFIER_KEY, embeddedModifierSoloClass);
                    spec.addMember(embeddedModifierSoloClass);
                }
                if (embeddedType.isCollection()) {
                    embeddedModifierCollectionClass = new ClassOrInterfaceDeclaration(Modifier.createModifierList(PROTECTED), false, defaultModifierClassName(properties.getClassName() + COLLECTION))
                            .addExtendedType(embeddedModifierClass.getNameAsString())
                            .addImplementedType(entity + "." + embeddedModifierCollection.getNameAsString());
                    embeddedModifierCollectionClass.addConstructor(PROTECTED).addParameter("Object", "parent").getBody().addStatement(description.getParser().parseStatement("super(parent);").getResult().get());
                    embeddedModifierCollectionClass.addMethod("_and", PUBLIC).setType("EmbeddedCodeCollection").setBody(description.getParser().parseBlock("{return (EmbeddedCodeCollection) parent;}").getResult().get());
                    description.registerClass(EMBEDDED_COLLECTION_MODIFIER_KEY, embeddedModifierCollectionClass);
                    spec.addMember(embeddedModifierCollectionClass);
                }
            }

            addModifyMethod(methodName, spec, properties.getLongModifierName(), modifierClass.getNameAsString(), true);
            addModifyMethod(methodName, intf, properties.getLongModifierName(), null, false);
//            addDoneMethod(modifierClass, properties.getInterfaceName(), isNull(properties.getMixInClass()) ? properties.getClassName() : description.getMixIn().getParsedName(), true);
//            addDoneMethod(modifier, properties.getInterfaceName(), null, false);
//            handleModifierBaseImplementation(isNull(properties.getMixInClass()) ? description : description.getMixIn(), spec, intf, modifier, modifierClass);
            spec.findCompilationUnit().get().addImport("net.binis.codegen.modifier.Modifiable");
        }
        if (isNull(description.getMixIn()) && !description.getProperties().isBase()) {
            spec.addImplementedType("Modifiable<" + intf.getNameAsString() + "." + modifier.getNameAsString() + ">");
        }
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
        }
    }

    private String handleBaseModifierClass(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var baseModifier = description.getProperties().getBaseModifierClass();
        if (isNull(baseModifier)) {
            if (!description.getProperties().isBase()) {
                description.getSpec().findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.modifier.impl.BaseModifierImpl"));
            }
            return "BaseModifierImpl";
        }
        var full = getExternalClassName(description.getDeclaration().findCompilationUnit().get(), baseModifier);
        var path = full.substring(0, full.indexOf(baseModifier)) + "impl." + baseModifier + "Impl";
        if (!description.getProperties().isBase()) {
            description.getSpec().findCompilationUnit().ifPresent(u -> u.addImport(path));
        }
        return baseModifier + "Impl";
    }


    private void calcEmbeddedModifiersType(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var ann = description.getDeclaration().getAnnotationByClass(Embeddable.class);
        if (ann.isPresent()) {
            description.setEmbeddedModifier(EmbeddedModifierType.valueOf(Helpers.getAnnotationValue(ann.get())));
        } else {
            lookup.parsed().forEach(parsed ->
                    parsed.getFields().stream()
                            .filter(field -> nonNull(field.getPrototype()))
                            .filter(field -> field.getPrototype().equals(description))
                            .forEach(field -> {
                                if (field.isCollection()) {
                                    description.addEmbeddedModifier(EmbeddedModifierType.COLLECTION);
                                } else {
                                    description.addEmbeddedModifier(EmbeddedModifierType.SINGLE);
                                }
                            })
            );
        }
    }

    private void declare(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeData properties, ClassOrInterfaceDeclaration modifierFields, PrototypeField field, TypeDeclaration<ClassOrInterfaceDeclaration> classDeclaration, List<Pair<CompilationUnit, String>> imports) {
        if (!field.getIgnores().isForModifier()) {
            var type = getFieldType(description, field);
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
                CollectionsHandler.addModifier(modifierClass, field, properties.getLongModifierName(), isNull(properties.getMixInClass()) ? properties.getClassName() : description.getMixIn().getParsedName(), true);
                CollectionsHandler.addModifier(modifier, field, description.getInterfaceName(), null, false);
            } else {
                addField(field, modifierFields, imports, type);
                if (nonNull(field.getPrototype()) && field.getPrototype().getEmbeddedModifierType().isSolo()) {
                    if (isNull(modifier)) {
                        modifier = description.getRegisteredClass(MODIFIER_INTF_KEY);
                    }
                    returnType = field.getPrototype().getInterfaceName() + ".EmbeddedSoloModify<EmbeddedModify<" + TYPE_PARAMETER + ", " + RETURN_PARAMETER + ">>";
                    modifier.addMethod(field.getName()).setType(returnType).setBody(null);
                    modifierClass.addMethod(field.getName(), PUBLIC).setType(returnType).setBody(description.getParser().parseBlock(
                            "{ if (" + description.getProperties().getClassName() + ".this." + field.getName() + " == null) {" +
                                    description.getProperties().getClassName() + ".this." + field.getName() + " = CodeFactory.create(" + field.getPrototype().getInterfaceName() + ".class);}" +
                                    "return CodeFactory.modify(this, " + description.getProperties().getClassName() + ".this." + field.getName() + ", " + field.getPrototype().getInterfaceName() + ".class); }").getResult().get());

                    with(description.getRegisteredClass(MODIFIER_INTF_KEY), cls -> {
                        cls.addMethod(field.getName()).setType(properties.getModifierName()).addParameter("Consumer<" + field.getPrototype().getInterfaceName() + ".Modify>", "init").setBody(null);
                        cls.findCompilationUnit().ifPresent(u -> u.addImport(Consumer.class));
                    });

                    with(description.getRegisteredClass(MODIFIER_KEY), cls -> {
                        cls.addMethod(field.getName(), PUBLIC).setType(properties.getModifierName()).addParameter("Consumer<" + field.getPrototype().getInterfaceName() + ".Modify>", "init").setBody(description.getParser().parseBlock(
                                "{ if (" + description.getProperties().getClassName() + ".this." + field.getName() + " == null) {" +
                                        description.getProperties().getClassName() + ".this." + field.getName() + " = CodeFactory.create(" + field.getPrototype().getInterfaceName() + ".class);}" +
                                        "init.accept(" + description.getProperties().getClassName() + ".this." + field.getName() + ".with());" +
                                        "return this;}"
                        ).getResult().get());
                        cls.findCompilationUnit().ifPresent(u -> u.addImport(Consumer.class));
                    });

                }
            }
        }
    }

//    private boolean hasEmbeddedCollectionModifier(Type type) {
//        var prototype = lookup.findParsed(CollectionsHandler.getFullCollectionType(type));
//        return nonNull(prototype) && nonNull(prototype.getRegisteredClass(EMBEDDED_COLLECTION_MODIFIER_KEY));
//    }

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
                    .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + modifierClassName + "()"))));
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

    private static void handleModifierBaseImplementation(PrototypeDescription<ClassOrInterfaceDeclaration> parse, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration modifier, ClassOrInterfaceDeclaration modifierClass) {
//        notNull(Generator.findInheritanceProperty(parse.getDeclaration().asClassOrInterfaceDeclaration(), parse.getProperties(), (s, p) -> nullCheck(p.getBaseModifierClass(), prp -> getExternalClassName(s.findCompilationUnit().get(), prp))), baseClass ->
//                notNull(loadClass(baseClass), cls -> {
//                    if (net.binis.codegen.modifier.Modifier.class.isAssignableFrom(cls)) {
//                        modifierClass.addConstructor(PROTECTED).setBody(new BlockStmt().addStatement("setObject(" + parse.getProperties().getClassName() + ".this);"));
//                        spec.findCompilationUnit().get().addImport(net.binis.codegen.modifier.Modifier.class);
//                    }
//                    spec.findCompilationUnit().get().addImport(baseClass);
//                    var intfName = intf.getNameAsString();
//                    var modName = intfName + "." + modifier.getNameAsString();
//                    var signature = parseGenericClassSignature(cls);
//                    if (signature.size() != 2) {
//                        log.error("BaseModifier ({}) should have two generic params!", cls.getCanonicalName());
//                    }
//                    var clsSignature = Map.<String, String>of(
//                            signature.get(0), modName,
//                            signature.get(1), intfName
//                    );
//                    modifierClass.addExtendedType(cls.getSimpleName() + "<" + modName + ", " + intfName + ">");
//
//                    for (var method : cls.getDeclaredMethods()) {
//                        if (java.lang.reflect.Modifier.isPublic(method.getModifiers()) && !java.lang.reflect.Modifier.isStatic(method.getModifiers()) && !"setObject".equals(method.getName())) {
//                            var ann = method.getAnnotation(Final.class);
//                            if (nonNull(ann)) {
//                                if (StringUtils.isBlank(ann.description())) {
//                                    Generator.addMethod(modifier, method, clsSignature, intfName);
//                                } else {
//                                    Generator.addMethod(modifier, method, clsSignature, modName, intfName, ann);
//                                }
//                            } else {
//                                Generator.addMethod(modifier, method, clsSignature, modName);
//                            }
//                        }
//                    }
//
//                    if (nonNull(cls.getSuperclass()) && net.binis.codegen.modifier.Modifier.class.isAssignableFrom(cls)) {
//                        handleSuperModifierBaseImplementation(parse, spec, intf, modifier, modifierClass, cls.getSuperclass(), clsSignature);
//                    }
//                })
//        );
    }

//    private static void handleSuperModifierBaseImplementation(PrototypeDescription<ClassOrInterfaceDeclaration> parse, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration modifier, ClassOrInterfaceDeclaration modifierClass, Class<?> cls, Map<String, String> clsSignature) {
//
//        var intfName = intf.getNameAsString();
//        var modName = intfName + "." + modifier.getNameAsString();
//
//        for (var method : cls.getDeclaredMethods()) {
//            if (java.lang.reflect.Modifier.isPublic(method.getModifiers()) && !java.lang.reflect.Modifier.isStatic(method.getModifiers()) && !"setObject".equals(method.getName())) {
//                var ann = method.getAnnotation(Final.class);
//                if (nonNull(ann)) {
//                    if (StringUtils.isBlank(ann.description())) {
//                        Generator.addMethod(modifier, method, clsSignature, intfName);
//                    } else {
//                        Generator.addMethod(modifier, method, clsSignature, modName, intfName, ann);
//                    }
//                } else {
//                    Generator.addMethod(modifier, method, clsSignature, modName);
//                }
//            }
//        }
//
//        if (nonNull(cls.getSuperclass()) && net.binis.codegen.modifier.Modifier.class.isAssignableFrom(cls)) {
//            handleSuperModifierBaseImplementation(parse, spec, intf, modifier, modifierClass, cls.getSuperclass(), clsSignature);
//        }
//    }

    @Override
    public int order() {
        return 10000;
    }

//    public static void buildEmbeddedModifier(PrototypeDescription<ClassOrInterfaceDeclaration> parse, boolean isClass) {
//        var unit = parse.getFiles().get(isClass ? 0 : 1);
//        var modifier = parse.getRegisteredClass(MODIFIER_INTF_KEY);
//        var embedded = parse.getRegisteredClass(EMBEDDED_MODIFIER_INTF_KEY);
//        if (isClass) {
//            modifier = parse.getRegisteredClass(MODIFIER_KEY);
//            embedded = parse.getRegisteredClass(EMBEDDED_MODIFIER_KEY);
//        }
//        var intfName = unit.getType(0).asClassOrInterfaceDeclaration().isInterface() ? unit.getType(0).getNameAsString() : "void";
//
//        if (isNull(embedded)) {
//            handleEmbeddedModifier(parse, parse.getSpec(), parse.getIntf());
//            embedded = isClass ? parse.getRegisteredClass(EMBEDDED_MODIFIER_KEY) : parse.getRegisteredClass(EMBEDDED_MODIFIER_INTF_KEY);
//        }
//
//        if (nonNull(embedded)) {
//            if (isClass) {
//                embedded.setExtendedTypes(modifier.getExtendedTypes());
//            } else {
//                embedded.addExtendedType(parse.getIntf().getNameAsString() + ".Fields<" + parse.getIntf().getNameAsString() + ".EmbeddedModify<" + MODIFIER_FIELD_GENERIC + ">>");
//            }
//
//            var intf = modifier.getNameAsString();
//            var eIntf = embedded.getNameAsString() + "<T>";
//            if (modifier.getImplementedTypes().isNonEmpty()) {
//                intf = modifier.getImplementedTypes(0).toString();
//                eIntf = embedded.getImplementedTypes(0).toString();
//            }
//
//            for (var old : modifier.getMethods()) {
//                if (Constants.MODIFIER_INTERFACE_NAME.equals(old.getType().toString()) || (old.getType().toString().endsWith(".Modify")) || old.getTypeAsString().startsWith("EmbeddedCodeCollection<")) {
//                    var method = embedded.addMethod(old.getNameAsString())
//                            .setModifiers(old.getModifiers())
//                            .setParameters(old.getParameters());
//
//                    if (old.getType().asString().equals(intf)) {
//                        method.setType(eIntf);
//                        if (old.getBody().isPresent()) {
//                            method.setBody(new BlockStmt()
//                                    .addStatement(new AssignExpr().setTarget(new NameExpr().setName("entity." + method.getNameAsString())).setValue(new NameExpr().setName(method.getNameAsString())))
//                                    .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("this"))));
//                            //TODO: Register modifier to field prototype
//                        } else {
//                            method.setBody(null);
//                        }
//                    } else if (CollectionsHandler.isCollection(old.getType())) {
//                        method.setType(old.getType().toString().replace(intf, eIntf));
//                        if (old.getBody().isPresent()) {
//                            var collection = CollectionsHandler.getCollectionType(unit, unit, old.getType().asClassOrInterfaceType());
//                            var parent = "entity." + method.getNameAsString();
//
//                            method.setBody(new BlockStmt()
//                                    .addStatement(new IfStmt().setCondition(new NameExpr().setName(parent + " != null")).setThenStmt(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName(parent)).setValue(new NameExpr().setName("new " + collection.getImplementor() + "<>()")))))
//                                    .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection.getClassType() + "<>(this, " + parent + ")"))));
//                        } else {
//                            method.setBody(null);
//                        }
//                    } else if (old.getTypeAsString().startsWith("EmbeddedCodeCollection<")) {
//                        if (old.getBody().isPresent()) {
//                            method.setType(old.getType().toString().replace(", " + intf, ", " + eIntf));
//                            var split = ((NameExpr) old.getBody().get().getChildNodes().get(1).getChildNodes().get(0)).getNameAsString().split("[\\s<.]");
//                            var collection = split[1];
//                            var cls = split[6];
//                            var collectionType = old.getBody().get().getChildNodes().get(0).getChildNodes().get(1).getChildNodes().get(0).toString().split("[\\s<]")[3];
//                            var parent = "entity." + method.getNameAsString();
//
//                            method.setBody(new BlockStmt()
//                                    .addStatement(new IfStmt().setCondition(new NameExpr().setName(parent + " != null")).setThenStmt(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName(parent)).setValue(new NameExpr().setName("new " + collectionType + "<>()")))))
//                                    .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection + "<>(this, " + parent + ", " + cls + ".class)"))));
//                        } else {
//                            method.setType(old.getType().toString().replace(", Modify>", ", " + intfName + ".EmbeddedModify<T>>"));
//                            method.setBody(null);
//                        }
//                    } else {
//                        method.setType(old.getType());
//                        method.setBody(old.getBody().orElse(null));
//                    }
//                } else {
//                    intfName = old.getType().asClassOrInterfaceType().getNameAsString();
//                }
//            }
//        }
//    }

//    private static void handleEmbeddedModifier(PrototypeDescription<ClassOrInterfaceDeclaration> parse, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf) {
//        var actualModifier = parse.getRegisteredClass(MODIFIER_INTF_KEY);
//        if (nonNull(actualModifier) && isNull(parse.getRegisteredClass(EMBEDDED_MODIFIER_INTF_KEY))) {
//            if (nonNull(parse.getProperties().getMixInClass())) {
//                spec = parse.getMixIn().getSpec();
//            }
//
//            var actualModifierClass = parse.getRegisteredClass(MODIFIER_KEY);
//            var modifier = new ClassOrInterfaceDeclaration(
//                    Modifier.createModifierList(), false, "Embedded" + actualModifier.getNameAsString())
//                    .addTypeParameter("T")
//                    .setInterface(true);
//            modifier.addMethod("and")
//                    .setType("EmbeddedCodeCollection<EmbeddedModify<T>, " + intf.getNameAsString() + ", T>")
//                    .setBody(null);
//
//            var modifierClass = new ClassOrInterfaceDeclaration(
//                    Modifier.createModifierList(parse.isNested() ? new Modifier.Keyword[]{PROTECTED} : new Modifier.Keyword[]{PROTECTED, STATIC}), false, "Embedded" + actualModifierClass.getNameAsString())
//                    .addTypeParameter("T")
//                    .addImplementedType(intf.getNameAsString() + "." + modifier.getNameAsString() + "<T>");
//            modifierClass.addField("T", "parent", PROTECTED);
//            modifierClass.addField(spec.getNameAsString(), "entity", PROTECTED);
//            modifierClass.addConstructor(PROTECTED)
//                    .addParameter("T", "parent")
//                    .addParameter(spec.getNameAsString(), "entity")
//                    .setBody(new BlockStmt()
//                            .addStatement(new AssignExpr().setTarget(new NameExpr().setName("this.parent")).setValue(new NameExpr().setName("parent")))
//                            .addStatement(new AssignExpr().setTarget(new NameExpr().setName("this.entity")).setValue(new NameExpr().setName("entity"))));
//            modifierClass.addMethod("and", PUBLIC)
//                    .setType("EmbeddedCodeCollection<" + intf.getNameAsString() + ".EmbeddedModify<T>, " + intf.getNameAsString() + ", T>")
//                    .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName("(EmbeddedCodeCollection) parent"))));
//
//            spec.addMember(modifierClass);
//            intf.addMember(modifier);
//
//            parse.registerClass(EMBEDDED_MODIFIER_KEY, modifierClass);
//            parse.registerClass(EMBEDDED_MODIFIER_INTF_KEY, modifier);
//
//            intf.findCompilationUnit().get().addImport("net.binis.codegen.collection.EmbeddedCodeCollection");
//            spec.findCompilationUnit().ifPresent(u -> {
//                u.addImport("net.binis.codegen.factory.CodeFactory");
//                u.addImport("net.binis.codegen.collection.EmbeddedCodeCollection");
//            });
//        }
//    }

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
