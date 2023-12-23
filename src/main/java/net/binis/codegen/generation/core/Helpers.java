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
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.Default;
import net.binis.codegen.annotation.Ignore;
import net.binis.codegen.annotation.type.GenerationStrategy;
import net.binis.codegen.annotation.validation.AliasFor;
import net.binis.codegen.enrich.Enricher;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.enrich.PrototypeLookup;
import net.binis.codegen.enrich.handler.*;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.generation.core.interfaces.ElementDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.tools.Holder;
import net.binis.codegen.tools.Reflection;
import net.binis.codegen.tools.Tools;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.lang.model.element.ElementKind;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Constants.*;
import static net.binis.codegen.generation.core.EnrichHelpers.annotation;
import static net.binis.codegen.generation.core.Generator.checkEnrichers;
import static net.binis.codegen.generation.core.Generator.generateCodeForClass;
import static net.binis.codegen.tools.Reflection.instantiate;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.*;

@Slf4j
public class Helpers {

    public static final Class<?> NAME_DISCOVERER = loadClass("org.springframework.core.StandardReflectionParameterNameDiscoverer");

    public static final Set<String> knownClassAnnotations = Set.of(
            "jakarta.persistence.OneToOne",
            "jakarta.persistence.ManyToOne",
            "jakarta.persistence.OneToMany",
            "jakarta.persistence.ManyToMany");
    public static final Map<String, String> knownTypes = Map.of(
            "CodeList", "net.binis.codegen.collection.CodeList",
            "CodeListImpl", "net.binis.codegen.collection.CodeListImpl",
            "EmbeddedCodeListImpl", "net.binis.codegen.collection.EmbeddedCodeListImpl",
            "EmbeddedCodeSetImpl", "net.binis.codegen.collection.EmbeddedCodeSetImpl");

    public static final Set<String> reserved = Set.of(
            "ensure",
            "reference",
            "get",
            "list",
            "references",
            "count",
            "top",

            "page",

            "tuple",
            "tuples",
            "prepare",

            "projection",
            "flush",
            "lock",
            "hint",
            "filter",

            "exists",
            "delete",
            "remove");


    public static final Set<String> primitiveTypes = Set.of("byte", "short", "int", "long", "float", "double", "boolean", "char", "void");

    public static final PrototypeLookup lookup = new PrototypeLookupHandler();
    public static final Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> constantParsed = new HashMap<>();
    public static final Map<String, List<Pair<String, String>>> declaredConstants = new HashMap<>();
    public static final Map<String, Structures.ProcessingType> processingTypes = new HashMap<>();
    public static final List<Triple<PrototypeDescription<ClassOrInterfaceDeclaration>, CompilationUnit, ClassExpr>> recursiveExpr = new LinkedList<>();

    public static String defaultPackage(TypeDeclaration<?> type, String name) {
        var result = type.findCompilationUnit().get().getPackageDeclaration().get().getNameAsString();
        if (nonNull(name)) {
            return result.replace("prototype", name);
        } else {
            if (result.endsWith(".prototype")) {
                return result.replace(".prototype", "");
            } else {
                return result.replace(".prototype.", ".");
            }
        }
    }

    public static String defaultInterfacePackage(TypeDeclaration<?> type) {
        return defaultPackage(type, null);
    }

    public static String defaultClassPackage(TypeDeclaration<?> type) {
        return defaultPackage(type, null);
    }

    public static String defaultInterfaceName(String type) {
        return defaultClassName(type).replace("Entity", "");
    }

    public static String defaultInterfaceName(TypeDeclaration<?> type) {
        return defaultClassName(type).replace("Entity", "");
    }

    public static String defaultClassName(TypeDeclaration<?> type) {
        return defaultClassName(type.getNameAsString());
    }

    public static String defaultClassName(String name) {
        return name.replace("Prototype", "");
    }

    public static String defaultModifierClassName(String className) {
        if (className.endsWith("Impl")) {
            className = className.substring(0, className.length() - 4);
        }
        return className + "ModifyImpl";
    }

    public static String getGetterName(String name, String type) {
        if ("boolean".equals(type)) {
            return "is" + name.substring(0, 1).toUpperCase() + name.substring(1);
        } else {
            return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }

    public static String getGetterName(String name, Type type) {
        if (PrimitiveType.booleanType().equals(type)) {
            return "is" + name.substring(0, 1).toUpperCase() + name.substring(1);
        } else {
            return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }

    public static String getSetterName(String name) {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String getFieldName(String name) {
        if (name.startsWith("is")) {
            return name.substring(2, 3).toLowerCase() + name.substring(3);
        } else {
            return name.substring(3, 4).toLowerCase() + name.substring(4);
        }
    }

    public static String getClassName(TypeDeclaration<?> type) {
        return type.getFullyQualifiedName().orElse(type.getNameAsString());
    }

    public static String getClassName(ClassOrInterfaceType type) {
        var result = Holder.blank();
        type.findCompilationUnit().flatMap(CompilationUnit::getPackageDeclaration).ifPresent(p ->
                result.set(p.getName().asString()));

        return result.get() + "." + type.getNameAsString();
    }

    public static String getExternalClassName(Node node, String type) {
        var unit = node.findCompilationUnit().orElseThrow(() ->
                new GenericCodeGenException("Node is not part of unit!"));
        if (nonNull(lookup.findParsed(type))) {
            return type;
        }

        var result = getExternalClassNameIfExists(unit, type);

        if (isNull(result)) {
            var imprt = forceGetClassImport(unit, type);
            if (nonNull(imprt)) {
                result = imprt.getNameAsString() + '.' + type;
            }
        }

        if (isNull(result)) {
            result = unit.getPackageDeclaration().get().getNameAsString() + "." + type;
        }

        return result;
    }

    public static String getExternalClassName(Node node, ClassOrInterfaceType type) {
        return getExternalClassName(node, type.getNameWithScope());
    }

    public static String getFQName(NodeWithName<?> node) {
        return getExternalClassName((Node) node, node.getNameAsString());
    }

    public static String getExternalClassNameIfExists(Node node, String t) {
        var idx = t.indexOf('<');
        var type = idx == -1 ? t : t.substring(0, idx);
        var c = loadClass(type);
        if (nonNull(c)) {
            return c.getCanonicalName();
        }

        var unit = node.findCompilationUnit().orElseThrow(() -> new GenericCodeGenException("Node is not part of unit!"));

        idx = type.indexOf('.');
        var result = nullCheck(getClassImport(unit, type), i -> i.isAsterisk() ? i.getNameAsString() + "." + type : i.getNameAsString());

        if (idx > -1) {
            if (nonNull(result)) {
                result += type.substring(idx).replace(".", "$");
            } else {
                result = unit.getImports().stream().filter(i -> i.getNameAsString().endsWith("." + type)).map(NodeWithName::getNameAsString).findFirst().orElse(null);
                if (nonNull(result)) {
                    return result.substring(0, result.length() - type.length()) + type.replace('.', '$');
                }
            }
        }

        if (isNull(result)) {
            result = unit.getImports().stream().filter(ImportDeclaration::isAsterisk)
                    .map(i -> i.getNameAsString() + "." + type)
                    .filter(name -> lookup.isParsed(name) || classExists(name) || lookup.isExternal(name))
                    .findFirst().orElse(null);
        }

        if (isNull(result)) {
            result = findLocalType(unit, type);
        }

        if (isNull(result)) {
            var cls = loadClass("java.lang." + type);
            if (nonNull(cls)) {
                result = cls.getCanonicalName();
            }
        }

        if (isNull(result) && primitiveTypes.contains(type)) {
            result = type;
        }

        return result;
    }

    public static Optional<String> getStaticImportIfExists(CompilationUnit unit, String expression) {
        return unit.getImports().stream()
                .filter(ImportDeclaration::isStatic)
                .map(ImportDeclaration::getNameAsString)
                .filter(i -> i.endsWith("." + expression))
                .findFirst();
    }

    public static String findLocalType(CompilationUnit unit, String t) {
        String result = null;
        for (var type : unit.getTypes()) {
            if (t.equals(type.getName().asString())) {
                return type.getFullyQualifiedName().get();
            }
            if (type.isClassOrInterfaceDeclaration()) {
                result = findLocalType(type.asClassOrInterfaceDeclaration(), t);
            }

            if (nonNull(result)) {
                return result;
            }
        }

        return null;
    }

    public static String findLocalType(ClassOrInterfaceDeclaration parent, String t) {
        String result = null;
        for (var member : parent.getMembers()) {
            if (member.isClassOrInterfaceDeclaration()) {
                var type = member.asClassOrInterfaceDeclaration();
                if (t.equals(type.getName().asString())) {
                    return type.getFullyQualifiedName().get();
                }

                result = findLocalType(type, t);

                if (nonNull(result)) {
                    return result;
                }
            }
        }

        return null;
    }

    public static ImportDeclaration getClassImport(Node node, String type) {
        var unit = node.findCompilationUnit().orElseThrow(() -> new GenericCodeGenException("Node is not part of unit!"));
        var known = knownTypes.get(type);
        if (nonNull(known)) {
            return new ImportDeclaration(known, false, false);
        }

        var rType = Holder.of(type);
        var idx = type.indexOf('.');
        if (idx > -1) {
            rType.set(type.substring(0, idx));
        }

        var discovered = unit.getTypes().stream().filter(t -> t.getNameAsString().equals(type)).findFirst();
        if (discovered.isPresent()) {
            return new ImportDeclaration(discovered.get().getFullyQualifiedName().get(), false, false);
        }

        var result = unit.getImports()
                .stream()
                .filter(i -> i.getNameAsString().endsWith("." + rType.get()))
                .findFirst()
                .orElse(null);
        if (nonNull(result)) {
            return result;
        } else {
            return forceGetClassImport(unit, type);
        }
    }

    private static ImportDeclaration forceGetClassImport(CompilationUnit unit, String type) {
        var result = unit.getImports().stream().filter(ImportDeclaration::isAsterisk).filter(i ->
                nonNull(loadClass(i.getNameAsString() + "." + type))).findFirst().orElse(null);
        if (isNull(result)) {
            result = unit.getImports().stream().filter(ImportDeclaration::isAsterisk).filter(i ->
                    nonNull(lookup.findExternal(i.getNameAsString() + "." + type))).findFirst().orElse(null);
        }

        if (isNull(result)) {
            result = unit.getImports().stream().filter(ImportDeclaration::isAsterisk).filter(i ->
                    nonNull(lookup.findGenerated(i.getNameAsString() + "." + type))).findFirst().orElse(null);
        }

        return result;
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, String name, Method declaration, boolean isClass) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(name) &&
                                m.getParameters().size() == declaration.getParameterCount()
                        //TODO: Match parameter types also
                ) || !isClass && ancestorMethodExists(spec, declaration);
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, Method declaration, boolean isClass) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(declaration.getName()) &&
                                m.getParameters().size() == declaration.getParameterCount()
                        //TODO: Match parameter types also
                ) || !isClass && ancestorMethodExists(spec, declaration);
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass, boolean weak) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(declaration.getNameAsString()) &&
                        matchParams(m.getParameters(), declaration.getParameters(), weak)
                ) || !isClass && ancestorMethodExists(spec, declaration, declaration.getNameAsString());
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass) {
        return methodExists(spec, declaration, isClass, true);
    }

    public static boolean matchParams(NodeList<com.github.javaparser.ast.body.Parameter> parameters, NodeList<com.github.javaparser.ast.body.Parameter> parameters1, boolean weak) {
        if (parameters.size() != parameters1.size()) {
            return false;
        }

        for (var i = 0; i < parameters.size(); i++) {
            var par1 = parameters.get(i);
            var par2 = parameters1.get(i);
            var p1 = weak ? par1.getTypeAsString() : getExternalClassName(par1, par1.getTypeAsString());
            var p2 = weak ? par2.getTypeAsString() : getExternalClassName(par2, par2.getTypeAsString());
            if (!p1.equals(p2)) {
                return false;
            }
        }

        return true;
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, String methodName, boolean isClass) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(methodName) &&
                        matchParams(m.getParameters(), declaration.getParameters(), false)
                ) || !isClass && ancestorMethodExists(spec, declaration, methodName);
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, PrototypeField declaration, String methodName, boolean isClass) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(methodName) &&
                                m.getParameters().size() == 1
                        //TODO: Match parameter types also
                ) || !isClass && ancestorMethodExists(spec, declaration, methodName);
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, PrototypeField declaration, boolean isClass) {
        return methodExists(spec, declaration, isClass, declaration.getDeclaration().getVariable(0).getType());
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, PrototypeField declaration, boolean isClass, Type type) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(declaration.getName()) &&
                                m.getParameters().size() == 1 &&
                                m.getType().equals(type)
                        //TODO: Match parameter types also
                ) || !isClass && ancestorMethodExists(spec, declaration, declaration.getName(), type);
    }

    public static boolean methodSignatureExists(ClassOrInterfaceDeclaration spec, PrototypeField declaration, String methodName) {
        return methodSignatureExists(spec, declaration, methodName, declaration.getDeclaration().getVariable(0).getType());
    }

    public static boolean methodSignatureExists(ClassOrInterfaceDeclaration spec, PrototypeField declaration, String methodName, Type returnType) {
        var result = spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(methodName) &&
                        m.getParameters().size() == 1 &&
                        m.getParameter(0).getType().equals(returnType)
                );

        if (!result) {
            for (var type : spec.getExtendedTypes()) {
                if (type.getScope().isPresent()) {
                    var parsed = lookup.findByInterfaceName(type.getScope().get().getNameAsString());
                    if (nonNull(parsed)) {
                        var intf = parsed.getInterface().findAll(ClassOrInterfaceDeclaration.class).stream().filter(c -> c.getNameAsString().equals(type.getNameAsString())).findFirst();
                        if (intf.isPresent()) {
                            result = methodSignatureExists(intf.get(), declaration, methodName, returnType);
                            if (result) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public static boolean ancestorMethodExists(ClassOrInterfaceDeclaration spec, Method declaration) {
        //TODO: Check for params
        var unit = spec.findCompilationUnit().get();
        return spec.getExtendedTypes().stream()
                .map(t -> loadClass(getExternalClassName(unit, t.getNameAsString())))
                .filter(Objects::nonNull)
                .anyMatch(c -> Arrays.stream(c.getMethods()).anyMatch(
                        m -> m.getName().equals(declaration.getName()) && m.getParameterCount() == declaration.getParameterCount()
                ));
    }

    public static boolean ancestorMethodExists(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, String methodName) {
        //TODO: Check for params and return type
        var unit = spec.findCompilationUnit().get();
        return spec.getExtendedTypes().stream()
                .map(t -> loadClass(getExternalClassName(unit, t.getNameAsString())))
                .filter(Objects::nonNull)
                .anyMatch(c -> Arrays.stream(c.getMethods()).anyMatch(
                        m -> m.getName().equals(methodName) && m.getParameterCount() == declaration.getParameters().size()
                ));
    }

    public static boolean ancestorMethodExists(ClassOrInterfaceDeclaration spec, PrototypeField declaration, String methodName) {
        return ancestorMethodExists(spec, declaration, methodName, declaration.getDeclaration().getVariable(0).getType());
    }

    @SuppressWarnings("unchecked")
    public static boolean ancestorMethodExists(ClassOrInterfaceDeclaration spec, PrototypeField declaration, String methodName, Type returnType) {
        //TODO: Check for params and return type
        var unit = spec.findCompilationUnit().get();
        var result = spec.getExtendedTypes().stream()
                .map(t -> loadClass(getExternalClassName(unit, t.getNameAsString())))
                .filter(Objects::nonNull)
                .anyMatch(c -> Arrays.stream(c.getMethods()).anyMatch(
                        m -> m.getName().equals(methodName)
                ));
        if (!result) {
            var parent = spec.findAncestor(ClassOrInterfaceDeclaration.class);
            if (parent.isPresent()) {
                for (var type : spec.getExtendedTypes()) {
                    var inner = parent.get().findAll(ClassOrInterfaceDeclaration.class).stream().filter(c -> c.getNameAsString().equals(type.getNameAsString())).findFirst();
                    if (inner.isPresent() && (type.getScope().isEmpty() || type.getScope().get().getNameAsString().equals(parent.get().getNameAsString()))) {
                        result = methodSignatureExists(inner.get(), declaration, methodName, returnType);
                        if (result) {
                            return true;
                        }
                    }
                }
            }
        }
        return result;
    }

    public static boolean defaultMethodExists(ClassOrInterfaceDeclaration spec, Method method) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.isDefault() &&
                                m.getNameAsString().equals(method.getName()) &&
                                m.getParameters().size() == method.getParameterCount()
                        //TODO: Match parameter types also
                );
    }


    public static String findProperType(PrototypeDescription<ClassOrInterfaceDeclaration> parsed, CompilationUnit unit, ClassExpr expr) {
        var parent = findParentClassOfType(expr, AnnotationExpr.class, a -> knownClassAnnotations.contains(getExternalClassName(expr.findCompilationUnit().get(), a.getNameAsString())));

        if (isNull(parent)) {
            if (nonNull(parsed.getInterfaceName())) {
                return parsed.getInterfaceName();
            } else {
                return expr.getTypeAsString();
            }
        } else {
            if (parsed.isProcessed()) {
                var type = parsed.getFiles().get(0).getType(0);
                expr.findCompilationUnit().ifPresent(u -> u.addImport(type.getFullyQualifiedName().get()));
                return type.getNameAsString();
            } else {
                recursiveExpr.add(Triple.of(parsed, unit, expr));
                return expr.getTypeAsString();
            }
        }
    }

    public static PrototypeDescription<ClassOrInterfaceDeclaration> discoverPrototype(Node node, Type type) {
        PrototypeDescription<ClassOrInterfaceDeclaration> par = null;
        if (type.isClassOrInterfaceType()) {
            par = lookup.findGenerated(type.asString());
            if (isNull(par)) {
                par = lookup.findParsed(type.asString());
            }
        }
        if (isNull(par)) {
            var name = getExternalClassName(node, type.asString());
            par = lookup.findGenerated(name);
            if (isNull(par)) {
                par = lookup.findParsed(name);
            }

        }

        return par;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> T findParentClassOfType(Node node, Class<T> cls, Predicate<T> predicate) {
        var parent = node.getParentNode();
        if (parent.isEmpty()) {
            return null;
        } else if (cls.isAssignableFrom(parent.get().getClass()) && predicate.test((T) parent.get())) {
            return (T) parent.get();
        } else {
            return findParentClassOfType(parent.get(), cls, predicate);
        }
    }

    public static boolean fieldExists(Structures.Parsed<ClassOrInterfaceDeclaration> parsed, String field) {
        return nonNull(findField(parsed, field));
    }

    public static PrototypeField findField(PrototypeDescription<ClassOrInterfaceDeclaration> parsed, String field) {
        var result = parsed.getFields().stream().filter(f -> f.getName().equals(field)).findFirst();
        if (result.isPresent()) {
            return result.get();
        }
        if (nonNull(parsed.getBase())) {
            return findField(parsed.getBase(), field);
        }

        return null;
    }

    public static MethodDeclaration findMethod(ClassOrInterfaceDeclaration spec, String method) {
        return spec.getMethods().stream().filter(m -> m.getNameAsString().equals(method)).findFirst().orElse(null);
    }

    public static void mergeImports(CompilationUnit source, CompilationUnit destination) {
        source.getImports().stream().filter(i -> !i.getNameAsString().startsWith("net.binis.codegen.annotation")).forEach(destination::addImport);
    }

    public static ClassOrInterfaceDeclaration findModifier(ClassOrInterfaceDeclaration intf) {
        return intf.findFirst(ClassOrInterfaceDeclaration.class, m -> nullCheck(m.getNameAsString(), name -> name.equals(Constants.MODIFIER_INTERFACE_NAME) || name.endsWith(Constants.MODIFIER_CLASS_NAME_SUFFIX))).orElseThrow();
    }

    public static PrototypeDescription<ClassOrInterfaceDeclaration> getParsed(ClassOrInterfaceType type) {
        var result = lookup.findParsed(getClassName(type));
        if (isNull(result)) {
            result = lookup.findParsed(getExternalClassName(type.findCompilationUnit().get(), type.getNameAsString()));
        }
        return result;
    }

    public static Map<String, Type> processGenerics(Structures.Parsed<ClassOrInterfaceDeclaration> prsd, Class<?> cls, NodeList<Type> generics) {
        Map<String, Type> result = null;
        var types = parseGenericClassSignature(cls);

        if (types.size() != generics.size()) {
            log.warn("Generic types miss match for {}", cls.getName());
        }

        result = new HashMap<>();

        for (var i = 0; i < types.size(); i++) {
            var generic = generics.get(i);
            if (generic.isClassOrInterfaceType()) {
                var parsed = getParsed(generic.asClassOrInterfaceType());
                if (nonNull(parsed)) {
                    if (!parsed.isProcessed() && !parsed.equals(prsd)) {
                        generateCodeForClass(parsed.getDeclaration().findCompilationUnit().get(), parsed);
                    }
                    generic = new ClassOrInterfaceType(null, parsed.getInterfaceName());
                }
            }
            result.put(types.get(i), generic);
        }
        return result;
    }

    public static Map<String, Type> processGenerics(Class<?> cls, Map<String, Type> parent, java.lang.reflect.Type[] generics) {
        Map<String, Type> result = new HashMap<>();
        var types = parseGenericClassSignature(cls);
        if (types.size() != generics.length) {
            log.warn("Generic types miss match for {}", cls.getName());
        }

        if (nonNull(parent)) {
            result = new HashMap<>();
            for (var i = 0; i < types.size(); i++) {
                Type generic;
                if (generics[i] instanceof TypeVariable) {
                    generic = parent.get(generics[i].getTypeName());
                    var gUnit = generic.findCompilationUnit();
                    if (gUnit.isPresent()) {
                        var parsed = lookup.findParsed(getExternalClassName(gUnit.get(), generic.asString()));
                        if (nonNull(parsed)) {
                            generic = new ClassOrInterfaceType().setName(parsed.getInterface().getNameAsString());
                        }
                    }
                } else {
                    var type = (Class) generics[i];
                    generic = new ClassOrInterfaceType().setName(type.getSimpleName());

                    if (type.isInterface()) {
                        var parsed = lookup.findParsed(type.getCanonicalName());
                        if (nonNull(parsed)) {
                            generic = new ClassOrInterfaceType().setName(parsed.getInterface().getNameAsString());
                        }
                    }
                }
                result.put(types.get(i), generic);
            }
        } else {
            for (var i = 0; i < types.size(); i++) {
                var type = (Class) generics[i];
                var generic = new ClassOrInterfaceType().setName(type.getSimpleName());
                if (type.isInterface()) {
                    var parsed = lookup.findParsed(type.getCanonicalName());
                    if (nonNull(parsed)) {
                        generic = new ClassOrInterfaceType().setName(parsed.getInterface().getNameAsString());
                    }
                }
                result.put(types.get(i), generic);
            }
        }
        return result.isEmpty() ? null : result;
    }


    public static List<String> parseGenericClassSignature(Class<?> cls) {
        return Arrays.stream(cls.getTypeParameters()).map(TypeVariable::getName).collect(Collectors.toList());
    }

    public static String parseMethodSignature(Method method) {
        return method.getGenericReturnType().getTypeName();
    }

    public static String mapGenericMethodSignature(Method method, Map<String, String> types) {
        return mapGenericSignature(method.getGenericReturnType(), types);
    }

    public static String mapGenericSignature(java.lang.reflect.Type type, Map<String, String> types) {
        if (type instanceof TypeVariable) {
            var result = types.get(((TypeVariable<?>) type).getName());
            if (isNull(result)) {
                throw new GenericCodeGenException("Invalid generic type: " + type);
            }
            return result;
        }
        if (type.equals(void.class)) {
            return "void";
        }
        var t = (ParameterizedType) type;
        if (t.getActualTypeArguments().length > 0) {
            return Arrays.stream(t.getActualTypeArguments()).map(tt -> mapGenericSignature(tt, types)).collect(Collectors.joining(",", t.getRawType().getTypeName() + "<", ">"));
        }
        return t.getTypeName();
    }

    public static String parseMethodSignature(MethodDeclaration method) {
        return "Not Implemented";
    }

    public static Structures.Ignores getIgnores(CompilationUnit unit, BodyDeclaration<?> member) {
        var result = Structures.Ignores.builder();
        member.getAnnotations().forEach(annotation -> Tools.with(getExternalClassName(unit, annotation.getNameAsString()), className ->
                Tools.with(loadClass(className), cls -> {
                    if (Ignore.class.equals(cls)) {
                        annotation.getChildNodes().forEach(node -> {
                            if (node instanceof MemberValuePair pair) {
                                var name = pair.getNameAsString();
                                switch (name) {
                                    case "forField" ->
                                            result.forField(pair.getValue().asBooleanLiteralExpr().getValue());
                                    case "forClass" ->
                                            result.forClass(pair.getValue().asBooleanLiteralExpr().getValue());
                                    case "forInterface" ->
                                            result.forInterface(pair.getValue().asBooleanLiteralExpr().getValue());
                                    case "forModifier" ->
                                            result.forModifier(pair.getValue().asBooleanLiteralExpr().getValue());
                                    case "forQuery" ->
                                            result.forQuery(pair.getValue().asBooleanLiteralExpr().getValue());
                                    default -> {
                                        //Do nothing
                                    }
                                }
                            }
                        });
                    } else {
                        Tools.with(cls.getAnnotation(Ignore.class), ann -> result
                                .forField(ann.forField())
                                .forClass(ann.forClass())
                                .forInterface(ann.forInterface())
                                .forModifier(ann.forModifier())
                                .forQuery(ann.forQuery()));
                    }
                })));
        return result.build();
    }


    public static Structures.Ignores getIgnores(BodyDeclaration<?> member) {
        return getIgnores(member.findCompilationUnit().get(), member);
    }

    public static String getDefaultValue(BodyDeclaration<?> member) {
        var result = new Holder<String>();
        member.getAnnotations().stream().filter(a -> "Default".equals(a.getNameAsString())).findFirst().ifPresent(annotation ->
                result.set(annotation.getNameAsString())
        );
        return result.get();
    }

    public static Structures.Constants getConstants(BodyDeclaration<?> member) {
        var result = Structures.Constants.builder().forPublic(true);
        member.getAnnotations().stream().filter(a -> "CodeConstant".equals(a.getNameAsString())).findFirst().ifPresent(annotation ->
                annotation.getChildNodes().forEach(node -> {
                    if (node instanceof MemberValuePair pair) {
                        var name = pair.getNameAsString();
                        switch (name) {
                            case "isPublic" -> result.forPublic(pair.getValue().asBooleanLiteralExpr().getValue());
                            case "forClass" -> result.forClass(pair.getValue().asBooleanLiteralExpr().getValue());
                            case "forInterface" ->
                                    result.forInterface(pair.getValue().asBooleanLiteralExpr().getValue());
                            default -> {
                                //Do nothing
                            }
                        }
                    }
                }));
        return result.build();
    }

    public static void addDeclaredConstant(String namespace, String type, String constant) {
        var decl = declaredConstants.get(namespace);
        if (nonNull(decl)) {
            decl.add(Pair.of(type, constant));
        } else {
            var list = new ArrayList<Pair<String, String>>();
            list.add(Pair.of(type, constant));
            declaredConstants.put(namespace, list);
        }
    }

    public static void addProcessingType(String type, String interfacePackage, String interfaceName, String classPackage, String className) {
        processingTypes.put(type, Structures.ProcessingType.builder()
                .interfaceName(interfaceName)
                .interfacePackage(interfacePackage)
                .className(className)
                .classPackage(classPackage)
                .build());
    }

    public static void sortImports(CompilationUnit unit) {
        unit.getImports().sort((i1, i2) -> i2.getNameAsString().compareTo(i1.getNameAsString()));
    }

    public static void sortClass(ClassOrInterfaceDeclaration cls) {
        cls.getMembers().sort(Helpers::compareMembers);
        cls.getMembers().stream().filter(BodyDeclaration::isClassOrInterfaceDeclaration).map(BodyDeclaration::asClassOrInterfaceDeclaration).forEach(Helpers::sortClass);
    }

    private static int compareMembers(BodyDeclaration<?> m1, BodyDeclaration<?> m2) {
        var result = memberIndex(m2) - memberIndex(m1);
        if (result == 0) {
            if (m1 instanceof NodeWithSimpleName m) {
                return m.getNameAsString().compareTo(((NodeWithSimpleName) m2).getNameAsString());
            } else if (m1 instanceof NodeWithVariables m) {
                return m.getVariable(0).getNameAsString().compareTo(((NodeWithVariables) m2).getVariable(0).getNameAsString());
            }
        }
        return result;
    }

    private static int memberIndex(BodyDeclaration<?> member) {
        if (member.isFieldDeclaration()) {
            var field = member.asFieldDeclaration();
            if (field.isStatic() && field.isFinal()) {
                return 1000;
            } else if (field.isStatic()) {
                return 999;
            } else {
                return 998;
            }
        } else if (member.isInitializerDeclaration()) {
            return 997;
        } else if (member.isConstructorDeclaration()) {
            return 996;
        } else if (member.isMethodDeclaration()) {
            return 995;
        } else if (member.isClassOrInterfaceDeclaration()) {
            return 994;
        }

        return 0;
    }

    public static boolean classExists(String className) {
        return nonNull(loadClass(className));
    }

    public static void cleanUp() {
        with((PrototypeLookupHandler) lookup, PrototypeLookupHandler::clean);
        constantParsed.clear();
        declaredConstants.clear();
        processingTypes.clear();
        recursiveExpr.clear();
        lookup.registerExternalLookup(null);
    }

    @SuppressWarnings("unchecked")
    public static void registerEnricher(Class enricher) {
        var reg = false;
        for (var i : enricher.getInterfaces()) {
            if (Enricher.class.isAssignableFrom(i) && !Enricher.class.equals(i.getClass())) {
                CodeFactory.registerType(i, params -> instantiate(enricher, params), null);
                reg = true;
            }
        }
        if (!reg) {
            throw new GenericCodeGenException(enricher.getCanonicalName() + " is not enricher!");
        }
    }

    public static void registerKnownEnrichers() {
        registerEnricher(AsEnricherHandler.class);
        registerEnricher(LogEnricherHandler.class);
        registerEnricher(CloneEnricherHandler.class);
        registerEnricher(CreatorEnricherHandler.class);
        registerEnricher(CreatorModifierEnricherHandler.class);
        registerEnricher(ModifierEnricherHandler.class);
        registerEnricher(QueryEnricherHandler.class);
        registerEnricher(ValidationEnricherHandler.class);
        registerEnricher(FluentEnricherHandler.class);
        registerEnricher(RegionEnricherHandler.class);
        registerEnricher(OpenApiEnricherHandler.class);
        registerEnricher(JacksonEnricherHandler.class);
        registerEnricher(HibernateEnricherHandler.class);
        registerEnricher(RedisEnricherHandler.class);
    }


    public static String handleGenericPrimitiveType(Type type) {
        if (type.isPrimitiveType()) {
            return type.asPrimitiveType().toBoxedType().asString();
        } else {
            return type.asString();
        }
    }

    public static void handleEnrichersSetup(PrototypeData properties) {
        Tools.with(properties.getEnrichers(), enrichers ->
                enrichers.forEach(e -> e.setup(properties)));
    }

    public static void handleInheritedEnrichersSetup(PrototypeData properties) {
        Tools.with(properties.getInheritedEnrichers(), enrichers ->
                enrichers.forEach(e -> e.setup(properties)));
    }

    private static List<PrototypeEnricher> getEnrichersList(PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        var map = new HashMap<Class<?>, PrototypeEnricher>();

        Tools.with(parsed.getBase(), base ->
                Tools.with(base.getProperties().getInheritedEnrichers(), l ->
                        l.forEach(e -> map.put(e.getClass(), e))));

        Tools.with(parsed.getMixIn(), mixIn ->
                Tools.with(mixIn.getBase(), base ->
                        Tools.with(base.getProperties().getInheritedEnrichers(), l ->
                                l.forEach(e -> map.put(e.getClass(), e)))));

        Tools.with(parsed.getProperties().getEnrichers(), l ->
                l.forEach(e -> map.put(e.getClass(), e)));

        parsed.getAdditionalProperties().forEach(properties ->
                Tools.with(properties.getInheritedEnrichers(), l ->
                        l.forEach(e -> map.put(e.getClass(), e))));

        parsed.getAdditionalProperties().forEach(properties ->
                Tools.with(properties.getEnrichers(), l ->
                        l.forEach(e -> map.put(e.getClass(), e))));

        var list = new ArrayList<>(map.values());
        list.sort(Comparator.comparingInt(PrototypeEnricher::order).reversed());
        return list;
    }

    private static List<PrototypeEnricher> getEnrichersList(ElementDescription method) {
        var map = new HashMap<Class<?>, PrototypeEnricher>();

        Tools.with(method.getProperties().getEnrichers(), l ->
                l.forEach(e -> map.put(e.getClass(), e)));

        var list = new ArrayList<>(map.values());
        list.sort(Comparator.comparingInt(PrototypeEnricher::order).reversed());
        return list;
    }

    public static void handleEnrichers(PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        getEnrichersList(parsed).forEach(e -> safeEnrich(e, parsed));
    }

    @SuppressWarnings("unchecked")
    public static void handleEnrichers(Parsables.Entry entry) {
        for (var bag : entry) {
            try {
                if (bag.getAnnotation() instanceof Class cls) {
                    var prop = Structures.defaultProperties.get(cls.getCanonicalName());
                    if (nonNull(prop)) {
                        var properties = prop.get().build();
                        properties.setPrototypeAnnotation(cls);
                        properties.setEnrichers(new ArrayList<>());
                        Tools.with(properties.getPredefinedEnrichers(), list ->
                                list.forEach(e -> checkEnrichers(properties.getEnrichers(), e)));

                        properties.setInheritedEnrichers(new ArrayList<>());
                        Tools.with(properties.getPredefinedInheritedEnrichers(), list ->
                                list.forEach(e -> checkEnrichers(properties.getInheritedEnrichers(), e)));

                        var desc = Structures.ParsedElementDescription.builder()
                                .element(bag.getElement())
                                .properties(properties)
                                .build();
                        var list = getEnrichersList(desc);
                        var parsed = (PrototypeDescription) Structures.Parsed.builder()
                                .properties(properties)
                                .element(bag.getElement())
                                .prototypeClassName(cls.getCanonicalName())
                                .build();
                        list.forEach(e -> log.info("Enriching {} with {}", bag.getElement().toString(), e.getClass()));
                        list.forEach(e -> safeEnrich(e, parsed));
                        list.forEach(e -> e.finalizeEnrich(parsed));
                        list.forEach(e -> e.postProcess(parsed));
                        list.forEach(e -> safeEnrich(e, desc));
                    }
                }
            } catch (Exception e) {
                log.error("Failed to enrich {} with {}", bag.getElement().toString(), bag.getAnnotation());
            }
        }
    }


    private static void safeEnrich(PrototypeEnricher enricher, PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        try {
            enricher.enrich(parsed);
        } catch (Exception e) {
            log.error("Failed to enrich {} with {}", parsed.getProperties().getPrototypeName(), enricher.getClass(), e);
        }
    }

    public static void finalizeEnrichers(PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        getEnrichersList(parsed).forEach(e -> e.finalizeEnrich(parsed));
    }

    public static void postProcessEnrichers(PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        parsed.processActions();

        parsed.getInitializers().forEach(i -> {
            if (i.getMiddle() instanceof ClassOrInterfaceDeclaration type) {
                getInitializer(isNull(parsed.getMixIn()) ? parsed.getImplementation() : parsed.getMixIn().getImplementation()).addStatement(new MethodCallExpr()
                        .setName("CodeFactory.registerType")
                        .addArgument((i.getLeft().getParentNode().get() instanceof ClassOrInterfaceDeclaration ? ((ClassOrInterfaceDeclaration) i.getLeft().getParentNode().get()).getNameAsString() + "." : "") + i.getLeft().getNameAsString() + ".class")
                        .addArgument(type.getNameAsString() + "::new")
                        .addArgument(calcModifierExpression(i.getRight())));
            } else if (i.getMiddle() instanceof LambdaExpr expr && nonNull(i.getLeft())) {
                getInitializer(isNull(parsed.getMixIn()) ? parsed.getImplementation() : parsed.getMixIn().getImplementation()).addStatement(new MethodCallExpr()
                        .setName("CodeFactory.registerType")
                        .addArgument((i.getLeft().getParentNode().get() instanceof ClassOrInterfaceDeclaration ? ((ClassOrInterfaceDeclaration) i.getLeft().getParentNode().get()).getNameAsString() + "." : "") + i.getLeft().getNameAsString() + ".class")
                        .addArgument(expr)
                        .addArgument("null"));
            }
        });

        parsed.getCustomInitializers().forEach(i -> i.accept(getInitializer(parsed.getImplementation())));

        Helpers.handleImports(parsed.getDeclaration(), parsed.getInterface());
        Helpers.handleImports(parsed.getDeclaration(), parsed.getImplementation());

        getEnrichersList(parsed).forEach(e -> e.postProcess(parsed));
    }

    public static void handleEnrichers(ElementDescription method) {
        getEnrichersList(method).forEach(e -> safeEnrich(e, method));
    }

    private static void safeEnrich(PrototypeEnricher enricher, ElementDescription element) {
        try {
            enricher.enrichElement(element);
        } catch (Exception e) {
            log.error("Failed to enrich {} with {}", element.getNode() instanceof NodeWithSimpleName s ? s.getNameAsString() : element.getNode() instanceof NodeWithName n ? n.getNameAsString() : "element", enricher.getClass(), e);
        }
    }

    private static String calcModifierExpression(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        if (nonNull(description)) {
            var modClass = description.getRegisteredClass(EMBEDDED_MODIFIER_KEY);
            if (nonNull(modClass)) {
                var soloClass = description.getRegisteredClass(EMBEDDED_SOLO_MODIFIER_KEY);
                var collectionClass = description.getRegisteredClass(EMBEDDED_COLLECTION_MODIFIER_KEY);
                var className = isNull(description.getMixIn()) ? description.getProperties().getClassName() : description.getMixIn().getProperties().getClassName();
                if (nonNull(soloClass) && nonNull(collectionClass)) {
                    soloClass.findCompilationUnit().ifPresent(u -> u.addImport("net.binis.codegen.collection.EmbeddedCodeCollection"));
                    return "(p, v, r) -> p instanceof EmbeddedCodeCollection ? ((" + description.getProperties().getClassName() + ") v).new " + collectionClass.getNameAsString() + "(p) : ((" + className + ") v).new " + soloClass.getNameAsString() + "(p)";
                } else {
                    var cls = modClass;
                    if (nonNull(soloClass)) {
                        cls = soloClass;
                    } else if (nonNull(collectionClass)) {
                        cls = collectionClass;
                    }
                    return "(p, v, r) -> ((" + className + ") v).new " + cls.getNameAsString() + "(p)";
                }
            }
        }
        return "null";
    }

    public static BlockStmt getInitializer(ClassOrInterfaceDeclaration type) {
        return type.getChildNodes().stream().filter(InitializerDeclaration.class::isInstance).map(n -> ((InitializerDeclaration) n).asInitializerDeclaration().getBody()).findFirst().orElseGet(type::addInitializer);
    }

    public static boolean isJavaType(String type) {
        return nonNull(type) && primitiveTypes.contains(type) || classExists("java.lang." + type);
    }

    public static String getJavaType(String type) {
        if (primitiveTypes.contains(type)) {
            return type;
        }
        var cls = "java.lang." + type;
        if (classExists(cls)) {
            return cls;
        }

        return null;
    }


    public static boolean isPrimitiveType(String type) {
        return primitiveTypes.contains(type);
    }


    public static void handleImports(Node declaration, ClassOrInterfaceDeclaration type) {
        declaration.findCompilationUnit().ifPresent(decl ->
                Tools.with(type, tt -> type.findCompilationUnit().ifPresent(unit ->
                        findUsedTypes(type).stream().map(t -> getClassImport(decl, t)).filter(Objects::nonNull).forEach(unit::addImport))));
    }

    public static Set<String> findUsedTypes(ClassOrInterfaceDeclaration type) {
        var result = new HashSet<String>();
        findUsedTypesInternal(result, type);
        return result;
    }

    private static void findUsedTypesInternal(Set<String> types, Node node) {
        if (node instanceof ClassOrInterfaceType type) {
            types.add(type.getNameAsString());
            type.getTypeArguments().ifPresent(a -> a.forEach(n -> findUsedTypesInternal(types, n)));
        } else if (node instanceof AnnotationExpr ann) {
            types.add(ann.getNameAsString());
        } else if (node instanceof NameExpr name) {
            types.add(name.getNameAsString());
        } else if (node instanceof SimpleName name) {
            Arrays.stream(name.asString().split("[.()<\\s]")).filter(s -> !"".equals(s)).forEach(types::add);
        } else if (node instanceof VariableDeclarator declarator && declarator.getType() instanceof ClassOrInterfaceType type) {
            types.add(type.getNameAsString());
        }

        node.getChildNodes().forEach(n -> findUsedTypesInternal(types, n));
    }

    public static void importType(Type type, CompilationUnit destination) {
        if (!type.isPrimitiveType()) {
            type.findCompilationUnit().ifPresent(unit -> {
                var full = Helpers.getExternalClassNameIfExists(type.findCompilationUnit().get(), type.asClassOrInterfaceType().getNameAsString());

                if (nonNull(full)) {
                    destination.addImport(full);
                }
            });
        }
    }

    public static void addInitializer(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration type, boolean embedded) {
        addInitializerInternal(description, intf, type, embedded);
    }

    public static void addInitializer(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf, LambdaExpr expr, boolean embedded) {
        addInitializerInternal(description, intf, expr, embedded);
    }

    public static void addDefaultCreation(PrototypeDescription<?> description, PrototypeDescription<?> mixIn) {
        var intf = description.getInterface();
        if (description.getProperties().isGenerateImplementation() && intf.getAnnotationByName("Default").isEmpty()) {
            var name = description.getImplementorFullName();
            if (description.isNested() && nonNull(description.getParentClassName())) {
                name = getBasePackage(description) + '.' + description.getParsedName().replace('.', '$');
            }
            intf.addAnnotation(annotation("@Default(\"" + name + "\")"));
            intf.findCompilationUnit().get().addImport(Default.class.getCanonicalName());
        }
    }

    private static String getBasePackage(PrototypeDescription<?> description) {
        if (description.isNested() && nonNull(description.getParentClassName())) {
            return getBasePackage(lookup.findParsed(description.getParentClassName()));
        }

        return isNull(description.getParentPackage()) || description.getProperties().isClassPackageSet() ? description.getProperties().getClassPackage() : description.getParentPackage();
    }

    private static void addInitializerInternal(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf, Node node, boolean embedded) {
        description.getImplementation().findCompilationUnit().get().addImport(CodeFactory.class);

        var list = description.getInitializers();
        for (var i = 0; i < list.size(); i++) {
            if (list.get(i).getLeft().getFullyQualifiedName().get().equals(intf.getFullyQualifiedName().get())) {
                if (isNull(list.get(i).getRight()) && embedded) {
                    list.set(i, Triple.of(intf, node, description));
                }
                return;
            }
        }

        list.add(Triple.of(intf, node, embedded ? description : null));
    }

    public static boolean hasAnnotation(PrototypeDescription<ClassOrInterfaceDeclaration> parsed, Class<? extends Annotation> annotation) {
        return parsed.getDeclaration().getAnnotationByClass(annotation).isPresent();
    }

    public static boolean hasAnnotation(PrototypeDescription<ClassOrInterfaceDeclaration> parsed, String name) {
        return parsed.getDeclaration().getAnnotations().stream().map(a -> getExternalClassName(parsed.getDeclarationUnit(), a.getNameAsString())).anyMatch(name::equals);
    }

    public static boolean hasAnnotation(NodeWithAnnotations<?> node, String name) {
        if (node instanceof Node n) {
            return node.getAnnotations().stream().map(a -> getExternalClassName(n, a.getNameAsString())).anyMatch(name::equals);
        } else {
            return false;
        }
    }

    public static void importClass(CompilationUnit unit, Class<?> cls) {
        if (!cls.isPrimitive() && !"java.lang".equals(cls.getPackageName())) {
            unit.addImport(cls.getCanonicalName());
        }
    }

    public static void importType(CompilationUnit unit, String type) {
        if (!type.startsWith("java.lang.")) {
            var idx = type.indexOf('<');
            if (idx > 0) {
                type = type.substring(0, idx);
            }
            unit.addImport(type);
        }
    }


    public static Map<String, Type> buildGenerics(ClassOrInterfaceType type, ClassOrInterfaceDeclaration cls) {
        var generics = new HashMap<String, Type>();
        var i = 0;
        for (var g : cls.getTypeParameters()) {
            try {
                var gen = type.getTypeArguments().get().get(i);
                var proto = lookup.findParsed(getExternalClassName(type, gen.asString()));
                if (isNull(proto)) {
                    generics.put(g.getNameAsString(), gen);
                } else {
                    generics.put(g.getNameAsString(), new ClassOrInterfaceType(new ClassOrInterfaceType(null, proto.getProperties().getInterfacePackage()), proto.getProperties().getInterfaceName()));
                }
            } catch (NoSuchElementException e) {
                throw new GenericCodeGenException("Invalid generic type arguments for type " + type.getNameAsString());
            }
            i++;
        }
        return generics.isEmpty() ? null : generics;
    }

    public static Type buildGeneric(String type, ClassOrInterfaceType t, ClassOrInterfaceDeclaration cls) {
        var generics = buildGenerics(t, cls);
        if (nonNull(generics)) {
            return generics.get(type);
        }
        return null;
    }

    public static Pair<Type, PrototypeDescription<ClassOrInterfaceDeclaration>> getFieldType(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field) {
        if (isNull(field.getDescription()) || field.getDescription().getTypeParameters().isEmpty()) {
            if (field.isGenericField()) {
                var intf = field.getParsed().getInterface();
                var type = Holder.<Type>blank();
                var proto = Holder.<PrototypeDescription<ClassOrInterfaceDeclaration>>blank();
                description.getInterface().getExtendedTypes().stream().filter(t -> t.getNameAsString().equals(intf.getNameAsString())).findFirst().ifPresent(t ->
                        type.set(buildGeneric(field.getType().asString(), t, intf)));
                description.getDeclaration().asClassOrInterfaceDeclaration().getExtendedTypes().stream().filter(t -> t.getNameAsString().equals(field.getParsed().getDeclaration().getNameAsString())).findFirst().ifPresent(t ->
                        proto.set(discoverPrototype(field.getDescription(), buildGeneric(field.getType().asString(), t, intf))));
                if (type.isPresent()) {
                    return Pair.of(type.get(), proto.get());
                }
            }
            if (nonNull(field.getGenerics())) {
                var type = field.getGenerics().get(field.getType().asString());
                if (nonNull(type)) {
                    return Pair.of(type, null);
                }
                type = field.getGenerics().get(field.getDescription().getType().asString());
                if (nonNull(type)) {
                    return Pair.of(type, null);
                }
            }

            if (isNull(field.getDescription())) {
                return Pair.of(field.getDeclaration().getVariables().get(0).getType(), null);
            } else {
                var result = field.getDescription().getType();
                var unit = field.getDescription().findCompilationUnit().orElse(null);
                if (nonNull(unit) && nonNull(lookup.findParsed(Helpers.getExternalClassName(unit, result.asString())))) {
                    result = lookup.getParser().parseClassOrInterfaceType(field.getType().asString()).getResult().get();
                } else if (nonNull(field.getDeclaration())) {
                    result = field.getDeclaration().getCommonType();
                }
                return Pair.of(result, null);
            }
        } else {
            return Pair.of(new ClassOrInterfaceType().setName("Object"), null);
        }
    }

    public static int sortForEnrich(PrototypeDescription<ClassOrInterfaceDeclaration> left, PrototypeDescription<ClassOrInterfaceDeclaration> right) {
        return ((nonNull(left.getMixIn()) ? 2 : 0) + (nonNull(left.getBase()) ? 1 : 0)) - ((nonNull(right.getMixIn()) ? 2 : 0) + (nonNull(right.getBase()) ? 1 : 0));
    }

    public static String getAnnotationValue(AnnotationExpr annotation) {
        var result = "BOTH";
        if (annotation.isSingleMemberAnnotationExpr()) {
            result = annotation.asSingleMemberAnnotationExpr().getMemberValue().toString();
        } else if (annotation.isNormalAnnotationExpr()) {
            for (var pair : annotation.asNormalAnnotationExpr().getPairs()) {
                if ("value".equals(pair.getName().asString())) {
                    result = pair.getValue().toString();
                    break;
                }
            }
        }

        return result.substring(Math.max(0, result.lastIndexOf('.') + 1));
    }

    public static String calcType(ClassOrInterfaceDeclaration spec) {
        var result = spec.getNameAsString();
        if (spec.getTypeParameters().isNonEmpty()) {
            result = result + spec.getTypeParameters().stream().map(NodeWithSimpleName::getNameAsString).collect(Collectors.joining(", ", "<", ">"));
        }

        return result;
    }

    public static boolean annotationHasTarget(PrototypeDescription<ClassOrInterfaceDeclaration> parsed, String target) {
        return parsed.getDeclaration().stream().filter(AnnotationExpr.class::isInstance)
                .map(AnnotationExpr.class::cast)
                .filter(an -> "java.lang.annotation.Target".equals(getExternalClassName(parsed.getDeclaration().findCompilationUnit().get(), an.getNameAsString())))
                .findFirst()
                .map(t -> t.stream().filter(ArrayInitializerExpr.class::isInstance)
                        .map(ArrayInitializerExpr.class::cast)
                        .findFirst()
                        .map(arr -> arr.getValues().stream().map(Object::toString)
                                .anyMatch(target::equals))
                        .orElse(false))
                .orElse(true);
    }

    public static String checkReserved(String name) {
        if (reserved.contains(name)) {
            return name + "_";
        }
        return name;
    }

    public static String sanitizeImport(String imprt) {
        return imprt.replace('$', '.');
    }

    @SuppressWarnings("unchecked")
    public static void addSuppressWarningsUnchecked(NodeWithAnnotations node) {
        if (!(node instanceof ClassOrInterfaceDeclaration) && node instanceof Node n) {
            while (n.getParentNode().isPresent()) {
                n = n.getParentNode().get();
                if (n instanceof ClassOrInterfaceDeclaration) {
                    node = (NodeWithAnnotations) n;
                    break;
                }
            }
        }

        if (!node.isAnnotationPresent(SuppressWarnings.class)) {
            node.addSingleMemberAnnotation(SuppressWarnings.class, "\"unchecked\"");
        }
    }

    public static Object getExpressionValue(Node exp) {
        if (exp instanceof StringLiteralExpr s) {
            return s.getValue();
        }
        return exp;
    }

    public static Object getCustomValue(String key, PrototypeData properties) {
        Object value = null;
        if (nonNull(properties.getCustom())) {
            value = properties.getCustom().get(key);
            if (isNull(value)) {
                var ann = properties.getPrototypeAnnotation();
                if (nonNull(ann)) {
                    for (var method : ann.getMethods()) {
                        var alias = method.getAnnotation(AliasFor.class);
                        if (nonNull(alias) && key.equals(alias.value())) {
                            value = properties.getCustom().get(method.getName());
                            if (nonNull(value)) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        return value;
    }

    public static String typeToString(Type type) {
        if (type.isClassOrInterfaceType()) {
            return type.asClassOrInterfaceType().getNameWithScope();
        } else {
            return type.asString();
        }
    }

    public static String[] getParameterNames(Method method) {

        if (nonNull(NAME_DISCOVERER)) {
            var discoverer = CodeFactory.create(NAME_DISCOVERER);
            if (nonNull(discoverer) && Reflection.invoke("getParameterNames", discoverer, method) instanceof String[] names) {
                return names;
            }
        }

        return (String[]) Arrays.stream(method.getParameters()).map(Parameter::getName).toArray();
    }

    @SuppressWarnings("unchecked")
    public static Optional<AnnotationExpr> getAnnotationByFullName(Node type, String name) {
        if (type instanceof NodeWithAnnotations annType) {
            return annType.getAnnotations().stream()
                    .filter(exp ->
                            name.equals(getExternalClassName(type, exp instanceof NodeWithName n ? n.getNameAsString() : exp.toString())))
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    public static CompilationUnit envelopWithDummyClass(MethodDeclaration description, Node original) {
        var dummy = new CompilationUnit();
        dummy.setPackageDeclaration("dummy");
        dummy.addClass("Dummy").addMember(description);
        if (nonNull(original)) {
            original.findCompilationUnit().ifPresent(unit -> unit.getImports().forEach(dummy::addImport));
        }
        return dummy;
    }

    @SuppressWarnings("unchecked")
    public static void handleType(JavaParser parser, TypeDeclaration<?> t, String fileName, List<Parsables.Entry.Bag> elements, boolean external) {
        var pack = t.findCompilationUnit().get().getPackageDeclaration().orElseThrow(() -> new GenericCodeGenException("'" + fileName + "' have no package declaration!"));
        var className = pack.getNameAsString() + '.' + t.getNameAsString();
        if (t.getAnnotationByName("ConstantPrototype").isPresent()) {
            constantParsed.put(getClassName(t.asClassOrInterfaceDeclaration()),
                    Structures.Parsed.builder()
                            .declaration(t.asTypeDeclaration())
                            .declarationUnit(t.findCompilationUnit().orElse(null))
                            .prototypeFileName(fileName)
                            .prototypeClassName(className)
                            .parser(parser)
                            .rawElements(elements)
                            .external(external)
                            .build());
        } else {
            var name = getClassName(t);
            checkForNestedClasses(t.asTypeDeclaration(), fileName, parser, elements);
            lookup.registerParsed(name,
                    Structures.Parsed.builder()
                            .declaration(t.asTypeDeclaration())
                            .declarationUnit(t.findCompilationUnit().orElse(null))
                            .prototypeFileName(fileName)
                            .prototypeClassName(className)
                            .parser(parser)
                            .rawElements(elements)
                            .external(external)
                            .build());
        }
    }

    @SuppressWarnings("unchecked")
    public static void handleType(JavaParser parser, TypeDeclaration<?> t, String fileName, List<Parsables.Entry.Bag> elements) {
        handleType(parser, t, fileName, elements, false);
    }

    @SuppressWarnings("unchecked")
    public static void checkForNestedClasses(TypeDeclaration<?> type, String fileName, JavaParser parser, List<Parsables.Entry.Bag> elements) {
        if (type.isClassOrInterfaceDeclaration()) {
            var properties = Generator.getCodeAnnotationProperties(type.asClassOrInterfaceDeclaration());

            if (properties.isEmpty() || !GenerationStrategy.PROTOTYPE.equals(properties.get().getStrategy())) {
                type.getChildNodes().stream().filter(ClassOrInterfaceDeclaration.class::isInstance).map(ClassOrInterfaceDeclaration.class::cast).forEach(nested -> {
                    var ann = Generator.getCodeAnnotations(nested);
                    if (ann.isPresent()) {
                        if (nested.asClassOrInterfaceDeclaration().isInterface()) {
                            var parent = type.findCompilationUnit().get();
                            var pack = parent.getPackageDeclaration().get().getNameAsString();
                            var unit = new CompilationUnit().setPackageDeclaration(pack + "." + type.getNameAsString());
                            var nestedType = nested.clone();
                            parent.getImports().forEach(unit::addImport);
                            unit.addType(nestedType);
                            var cName = getClassName(nestedType);

                            lookup.registerParsed(cName,
                                    Structures.Parsed.builder()
                                            .declaration(nestedType.asTypeDeclaration())
                                            .declarationUnit(unit)
                                            .prototypeFileName(fileName)
                                            .prototypeClassName(cName)
                                            .parser(parser)
                                            .nested(true)
                                            .parentPackage(pack)
                                            .build());
                        } else {
                            ann.get().forEach(prototype ->
                                    with(ErrorHelpers.calculatePrototypeAnnotationError(nested.asClassOrInterfaceDeclaration(), prototype.getValue()), message ->
                                            lookup.error(message, withRes(elements, el -> el.stream().map(Parsables.Entry.Bag::getElement).filter(e ->
                                                    ElementKind.CLASS.equals(e.getKind()) && e.getSimpleName().toString().equals(nested.getNameAsString())).findFirst().orElse(null)))));
                        }
                    }
                });
            }
        }
    }

    public static String getNodeName(Node node) {
        return node instanceof NodeWithSimpleName<?> s ? s.getNameAsString() : node instanceof NodeWithName<?> n ? n.getNameAsString() : "";
    }

    public static void addImport(Node node, PrototypeField field) {
        var f = nonNull(field.getParent()) ? getFieldParent(field) : field;
        if (nonNull(field.getFullType())) {
            node.findCompilationUnit().ifPresent(unit -> addImport(unit, f.getFullType()));
        }

        if (nonNull(field.getType()) && field.getType().isClassOrInterfaceType()) {
            addImport(node, field.getDescription(), f.getType().asClassOrInterfaceType());
        }
    }

    public static void addImport(Node dest, Node source, ClassOrInterfaceType type) {
        dest.findCompilationUnit().ifPresent(unit -> addImport(unit, getExternalClassName(source, type.getNameAsString())));
        type.getTypeArguments().ifPresent(args ->
                args.stream()
                        .filter(Type::isClassOrInterfaceType)
                        .map(Type::asClassOrInterfaceType)
                        .forEach(arg -> addImport(dest, source, arg)));
    }

    public static void addImport(CompilationUnit unit, String imprt) {
        if (!imprt.startsWith("dummy.") && !isPrimitiveType(imprt)) {
            unit.addImport(imprt);
        }
    }

    public static PrototypeField getFieldParent(PrototypeField parent) {
        if (nonNull(parent)) {
            if (isNull(parent.getParent())) {
                return parent;
            } else {
                return getFieldParent(parent.getParent());
            }
        }
        return null;
    }


}
