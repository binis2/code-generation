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
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.Final;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.CollectionsHandler;
import net.binis.codegen.generation.core.Constants;
import net.binis.codegen.generation.core.Generator;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.javaparser.ast.Modifier.Keyword.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Constants.*;
import static net.binis.codegen.generation.core.Generator.handleType;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.notNull;
import static net.binis.codegen.tools.Tools.nullCheck;

@Slf4j
public class ModifierEnricherHandler extends BaseEnricher implements ModifierEnricher {

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var imports = new ArrayList<Pair<CompilationUnit, String>>();
        var spec = description.getSpec();
        if (nonNull(description.getProperties().getMixInClass())) {
            spec = description.getMixIn().getSpec();
        }
        var intf = description.getIntf();
        var properties = description.getProperties();
        var entity = description.getProperties().getInterfaceName();

        var modifier = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, properties.getModifierName());
        ClassOrInterfaceDeclaration modifierClass = null;
        var modifierFields = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, "Fields")
                .addTypeParameter(MODIFIER_FIELD_GENERIC);
        description.registerClass(MODIFIER_INTF_KEY, modifier);

        var methodName = isNull(properties.getMixInClass()) ? Constants.MODIFIER_METHOD_NAME : Constants.MIXIN_MODIFYING_METHOD_PREFIX + intf.getNameAsString();
        if (!description.getProperties().isBase()) {
            modifierClass = new ClassOrInterfaceDeclaration(Modifier.createModifierList(PROTECTED), false, defaultModifierClassName(properties.getClassName()));
            modifierClass.addImplementedType(properties.getLongModifierName());
            description.registerClass(MODIFIER_KEY, modifierClass);
            spec.addMember(modifierClass);
            intf.addMember(modifier);
            addModifyMethod(methodName, spec, properties.getLongModifierName(), modifierClass.getNameAsString(), true);
            addModifyMethod(methodName, intf, properties.getLongModifierName(), null, false);
            addDoneMethod(modifierClass, properties.getInterfaceName(), isNull(properties.getMixInClass()) ? properties.getClassName() : description.getMixIn().getParsedName(), true);
            addDoneMethod(modifier, properties.getInterfaceName(), null, false);
            handleModifierBaseImplementation(isNull(properties.getMixInClass()) ? description : description.getMixIn(), spec, intf, modifier, modifierClass);
            spec.findCompilationUnit().get().addImport("net.binis.codegen.modifier.Modifiable");
        }
        if (isNull(description.getMixIn()) && !description.getProperties().isBase()) {
            spec.addImplementedType("Modifiable<" + intf.getNameAsString() + "." + modifier.getNameAsString() + ">");
        }

        for (var field : description.getFields()) {
            declare(description, properties, modifier, modifierClass, modifierFields, field, description.getDeclaration(), imports);
        }

        if (nonNull(description.getBase())) {
            var fields = description.getBase().getRegisteredClass(MODIFIER_FIELDS_KEY);
            if (nonNull(fields)) {
                modifierFields.addExtendedType(description.getBase().getInterfaceName() + "." + fields.getNameAsString() + "<" + MODIFIER_FIELD_GENERIC + ">");
                for (var field : description.getBase().getFields()) {
                    declare(description, properties, modifier, modifierClass, null, field, description.getBase().getDeclaration(), imports);
                }
            } else {
                for (var field : description.getBase().getFields()) {
                    declare(description, properties, modifier, modifierClass, modifierFields, field, description.getBase().getDeclaration(), imports);
                }
            }
        }

        if (nonNull(description.getMixIn())) {
            var fields = description.getMixIn().getRegisteredClass(MODIFIER_FIELDS_KEY);
            if (nonNull(fields)) {
                modifierFields.addExtendedType(description.getMixIn().getInterfaceName() + "." + fields.getNameAsString() + "<" + MODIFIER_FIELD_GENERIC + ">");
                for (var field : description.getMixIn().getFields()) {
                    declare(description, properties, modifier, modifierClass, null, field, description.getMixIn().getDeclaration(), imports);
                }
            } else {
                for (var field : description.getMixIn().getFields()) {
                    declare(description, properties, modifier, modifierClass, modifierFields, field, description.getMixIn().getDeclaration(), imports);
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
                        declare(description, properties, modifier, modifierClass, null, field, description.getMixIn().getBase().getDeclaration(), imports);
                    }
                } else {
                    for (var field : description.getMixIn().getBase().getFields()) {
                        declare(description, properties, modifier, modifierClass, modifierFields, field, description.getMixIn().getBase().getDeclaration(), imports);
                    }
                }
            }
        }

        if (!modifierFields.isEmpty()) {
            intf.addMember(modifierFields);
            description.registerClass(MODIFIER_FIELDS_KEY, modifierFields);
            modifier.addExtendedType(intf.getNameAsString() + "." + modifierFields.getNameAsString() + "<" + intf.getNameAsString() + "." + modifier.getNameAsString() + ">");
            intf.findCompilationUnit().ifPresent(dest ->
                    imports.forEach(pair ->
                            notNull(getExternalClassNameIfExists(pair.getKey(), pair.getValue()), dest::addImport)));
        }
    }

    private void declare(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeData properties, ClassOrInterfaceDeclaration modifier, ClassOrInterfaceDeclaration modifierClass, ClassOrInterfaceDeclaration modifierFields, PrototypeField field, TypeDeclaration<ClassOrInterfaceDeclaration> classDeclaration, List<Pair<CompilationUnit, String>> imports) {
        if (!field.getIgnores().isForModifier()) {
            var type = getFieldType(field);
            if (nonNull(modifierClass)) {
                addModifier(modifierClass, field, isNull(properties.getMixInClass()) ? properties.getClassName() : description.getMixIn().getParsedName(), properties.getLongModifierName(), true);
            }
            if (CollectionsHandler.isCollection(type)) {
                addModifier(modifier, field, null, properties.getModifierName(), false);
                CollectionsHandler.addModifier(modifierClass, field, properties.getLongModifierName(), isNull(properties.getMixInClass()) ? properties.getClassName() : description.getMixIn().getParsedName(), true);
                CollectionsHandler.addModifier(modifier, field, properties.getModifierName(), null, false);
                notNull(lookup.findParsed(CollectionsHandler.getFullCollectionType(type)), parsed ->
                        lookup.generateEmbeddedModifier(parsed));
            } else {
                addField(field, modifierFields, imports);
            }
        }
    }

    private Type getFieldType(PrototypeField field) {
        if (field.getDescription().getTypeParameters().isEmpty()) {
            return isNull(field.getDescription()) ? field.getDeclaration().getVariables().get(0).getType() : field.getDescription().getType();
        } else {
            return new ClassOrInterfaceType().setName("Object");
        }
    }

    private void addField(PrototypeField field, ClassOrInterfaceDeclaration modifierFields, List<Pair<CompilationUnit, String>> imports) {
        if (nonNull(modifierFields)) {
            var type = field.getDeclaration().getVariable(0).getType();
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
        notNull(Generator.findInheritanceProperty(parse.getDeclaration().asClassOrInterfaceDeclaration(), parse.getProperties(), (s, p) -> nullCheck(p.getBaseModifierClass(), prp -> getExternalClassName(s.findCompilationUnit().get(), prp))), baseClass ->
                notNull(loadClass(baseClass), cls -> {
                    if (net.binis.codegen.modifier.Modifier.class.isAssignableFrom(cls)) {
                        modifierClass.addConstructor(PROTECTED).setBody(new BlockStmt().addStatement("setObject(" + parse.getProperties().getClassName() + ".this);"));
                        spec.findCompilationUnit().get().addImport(net.binis.codegen.modifier.Modifier.class);
                    }
                    spec.findCompilationUnit().get().addImport(baseClass);
                    var intfName = intf.getNameAsString();
                    var modName = intfName + "." + modifier.getNameAsString();
                    var signature = parseGenericClassSignature(cls);
                    if (signature.size() != 2) {
                        log.error("BaseModifier ({}) should have two generic params!", cls.getCanonicalName());
                    }
                    var clsSignature = Map.<String, String>of(
                            signature.get(0), modName,
                            signature.get(1), intfName
                    );
                    modifierClass.addExtendedType(cls.getSimpleName() + "<" + modName + ", " + intfName + ">");

                    for (var method : cls.getDeclaredMethods()) {
                        if (java.lang.reflect.Modifier.isPublic(method.getModifiers()) && !java.lang.reflect.Modifier.isStatic(method.getModifiers()) && !"setObject".equals(method.getName())) {
                            var ann = method.getAnnotation(Final.class);
                            if (nonNull(ann)) {
                                if (StringUtils.isBlank(ann.description())) {
                                    Generator.addMethod(modifier, method, clsSignature, intfName);
                                } else {
                                    Generator.addMethod(modifier, method, clsSignature, modName, intfName, ann);
                                }
                            } else {
                                Generator.addMethod(modifier, method, clsSignature, modName);
                            }
                        }
                    }

                    if (nonNull(cls.getSuperclass()) && net.binis.codegen.modifier.Modifier.class.isAssignableFrom(cls)) {
                        handleSuperModifierBaseImplementation(parse, spec, intf, modifier, modifierClass, cls.getSuperclass(), clsSignature);
                    }
                })
        );
    }

    private static void handleSuperModifierBaseImplementation(PrototypeDescription<ClassOrInterfaceDeclaration> parse, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration modifier, ClassOrInterfaceDeclaration modifierClass, Class<?> cls, Map<String, String> clsSignature) {

        var intfName = intf.getNameAsString();
        var modName = intfName + "." + modifier.getNameAsString();

        for (var method : cls.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(method.getModifiers()) && !java.lang.reflect.Modifier.isStatic(method.getModifiers()) && !"setObject".equals(method.getName())) {
                var ann = method.getAnnotation(Final.class);
                if (nonNull(ann)) {
                    if (StringUtils.isBlank(ann.description())) {
                        Generator.addMethod(modifier, method, clsSignature, intfName);
                    } else {
                        Generator.addMethod(modifier, method, clsSignature, modName, intfName, ann);
                    }
                } else {
                    Generator.addMethod(modifier, method, clsSignature, modName);
                }
            }
        }

        if (nonNull(cls.getSuperclass()) && net.binis.codegen.modifier.Modifier.class.isAssignableFrom(cls)) {
            handleSuperModifierBaseImplementation(parse, spec, intf, modifier, modifierClass, cls.getSuperclass(), clsSignature);
        }
    }

    @Override
    public void finalize(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        if (lookup.embeddedModifierRequested(description)) {
            buildEmbeddedModifier(description, true);
            buildEmbeddedModifier(description, false);
        }
    }

    @Override
    public int order() {
        return 10000;
    }

    public static void buildEmbeddedModifier(PrototypeDescription<ClassOrInterfaceDeclaration> parse, boolean isClass) {
        var unit = parse.getFiles().get(isClass ? 0 : 1);
        var modifier = parse.getRegisteredClass(MODIFIER_INTF_KEY);
        var embedded = parse.getRegisteredClass(EMBEDDED_MODIFIER_INTF_KEY);
        if (isClass) {
            modifier = parse.getRegisteredClass(MODIFIER_KEY);
            embedded = parse.getRegisteredClass(EMBEDDED_MODIFIER_KEY);
        }
        var intfName = unit.getType(0).asClassOrInterfaceDeclaration().isInterface() ? unit.getType(0).getNameAsString() : "void";

        if (isNull(embedded)) {
            handleEmbeddedModifier(parse, parse.getSpec(), parse.getIntf());
            embedded = isClass ? parse.getRegisteredClass(EMBEDDED_MODIFIER_KEY) : parse.getRegisteredClass(EMBEDDED_MODIFIER_INTF_KEY);
        }

        if (nonNull(embedded)) {
            if (isClass) {
                embedded.setExtendedTypes(modifier.getExtendedTypes());
            } else {
                embedded.addExtendedType(parse.getIntf().getNameAsString() + ".Fields<" + parse.getIntf().getNameAsString() + ".EmbeddedModify<" + MODIFIER_FIELD_GENERIC + ">>");
            }

            var intf = modifier.getNameAsString();
            var eIntf = embedded.getNameAsString() + "<T>";
            if (modifier.getImplementedTypes().isNonEmpty()) {
                intf = modifier.getImplementedTypes(0).toString();
                eIntf = embedded.getImplementedTypes(0).toString();
            }

            for (var old : modifier.getMethods()) {
                if (Constants.MODIFIER_INTERFACE_NAME.equals(old.getType().toString()) || (old.getType().toString().endsWith(".Modify")) || old.getTypeAsString().startsWith("EmbeddedCodeCollection<")) {
                    var method = embedded.addMethod(old.getNameAsString())
                            .setModifiers(old.getModifiers())
                            .setParameters(old.getParameters());

                    if (old.getType().asString().equals(intf)) {
                        method.setType(eIntf);
                        if (old.getBody().isPresent()) {
                            method.setBody(new BlockStmt()
                                    .addStatement(new AssignExpr().setTarget(new NameExpr().setName("entity." + method.getNameAsString())).setValue(new NameExpr().setName(method.getNameAsString())))
                                    .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("this"))));
                            //TODO: Register modifier to field prototype
                        } else {
                            method.setBody(null);
                        }
                    } else if (CollectionsHandler.isCollection(old.getType())) {
                        method.setType(old.getType().toString().replace(intf, eIntf));
                        if (old.getBody().isPresent()) {
                            var collection = CollectionsHandler.getCollectionType(unit, unit, old.getType().asClassOrInterfaceType());
                            var parent = "entity." + method.getNameAsString();

                            method.setBody(new BlockStmt()
                                    .addStatement(new IfStmt().setCondition(new NameExpr().setName(parent + " != null")).setThenStmt(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName(parent)).setValue(new NameExpr().setName("new " + collection.getImplementor() + "<>()")))))
                                    .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection.getClassType() + "<>(this, " + parent + ")"))));
                        } else {
                            method.setBody(null);
                        }
                    } else if (old.getTypeAsString().startsWith("EmbeddedCodeCollection<")) {
                        if (old.getBody().isPresent()) {
                            method.setType(old.getType().toString().replace(", " + intf, ", " + eIntf));
                            var split = ((NameExpr) old.getBody().get().getChildNodes().get(1).getChildNodes().get(0)).getNameAsString().split("[\\s<.]");
                            var collection = split[1];
                            var cls = split[6];
                            var collectionType = old.getBody().get().getChildNodes().get(0).getChildNodes().get(1).getChildNodes().get(0).toString().split("[\\s<]")[3];
                            var parent = "entity." + method.getNameAsString();

                            method.setBody(new BlockStmt()
                                    .addStatement(new IfStmt().setCondition(new NameExpr().setName(parent + " != null")).setThenStmt(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName(parent)).setValue(new NameExpr().setName("new " + collectionType + "<>()")))))
                                    .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection + "<>(this, " + parent + ", " + cls + ".class)"))));
                        } else {
                            method.setType(old.getType().toString().replace(", Modify>", ", " + intfName + ".EmbeddedModify<T>>"));
                            method.setBody(null);
                        }
                    } else {
                        method.setType(old.getType());
                        method.setBody(old.getBody().orElse(null));
                    }
                } else {
                    intfName = old.getType().asClassOrInterfaceType().getNameAsString();
                }
            }
        }
    }

    private static void handleEmbeddedModifier(PrototypeDescription<ClassOrInterfaceDeclaration> parse, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf) {
        var actualModifier = parse.getRegisteredClass(MODIFIER_INTF_KEY);
        if (nonNull(actualModifier) && isNull(parse.getRegisteredClass(EMBEDDED_MODIFIER_INTF_KEY))) {
            if (nonNull(parse.getProperties().getMixInClass())) {
                spec = parse.getMixIn().getSpec();
            }

            var actualModifierClass = parse.getRegisteredClass(MODIFIER_KEY);
            var modifier = new ClassOrInterfaceDeclaration(
                    Modifier.createModifierList(), false, "Embedded" + actualModifier.getNameAsString())
                    .addTypeParameter("T")
                    .setInterface(true);
            modifier.addMethod("and")
                    .setType("EmbeddedCodeCollection<EmbeddedModify<T>, " + intf.getNameAsString() + ", T>")
                    .setBody(null);

            var modifierClass = new ClassOrInterfaceDeclaration(
                    Modifier.createModifierList(PROTECTED, STATIC), false, "Embedded" + actualModifierClass.getNameAsString())
                    .addTypeParameter("T")
                    .addImplementedType(intf.getNameAsString() + "." + modifier.getNameAsString() + "<T>");
            modifierClass.addField("T", "parent", PROTECTED);
            modifierClass.addField(spec.getNameAsString(), "entity", PROTECTED);
            modifierClass.addConstructor(PROTECTED)
                    .addParameter("T", "parent")
                    .addParameter(spec.getNameAsString(), "entity")
                    .setBody(new BlockStmt()
                            .addStatement(new AssignExpr().setTarget(new NameExpr().setName("this.parent")).setValue(new NameExpr().setName("parent")))
                            .addStatement(new AssignExpr().setTarget(new NameExpr().setName("this.entity")).setValue(new NameExpr().setName("entity"))));
            modifierClass.addMethod("and", PUBLIC)
                    .setType("EmbeddedCodeCollection<" + intf.getNameAsString() + ".EmbeddedModify<T>, " + intf.getNameAsString() + ", T>")
                    .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName("(EmbeddedCodeCollection) parent"))));

            spec.addMember(modifierClass);
            intf.addMember(modifier);

            parse.registerClass(EMBEDDED_MODIFIER_KEY, modifierClass);
            parse.registerClass(EMBEDDED_MODIFIER_INTF_KEY, modifier);

            intf.findCompilationUnit().get().addImport("net.binis.codegen.collection.EmbeddedCodeCollection");
            spec.findCompilationUnit().ifPresent(u -> {
                u.addImport("net.binis.codegen.factory.CodeFactory");
                u.addImport("net.binis.codegen.collection.EmbeddedCodeCollection");
            });
        }
    }

    private void addModifier(ClassOrInterfaceDeclaration spec, PrototypeField declaration, String modifierClassName, String modifierName, boolean isClass) {
        var type = declaration.isGenericMethod() ? "Object" : declaration.getType();
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
                            .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("this"))));
            declaration.addModifier(method);
        } else {
            method.setBody(null);
        }
        if (!methodExists(spec, method, isClass)) {
            spec.addMember(method);
        }
    }

}
