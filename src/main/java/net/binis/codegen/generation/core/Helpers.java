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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.Default;
import net.binis.codegen.annotation.Ignore;
import net.binis.codegen.enrich.Enricher;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.enrich.PrototypeLookup;
import net.binis.codegen.enrich.handler.*;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.tools.Holder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Generator.handleType;
import static net.binis.codegen.tools.Reflection.instantiate;
import static net.binis.codegen.tools.Reflection.loadClass;
import static net.binis.codegen.tools.Tools.*;

@Slf4j
public class Helpers {

    public static final Set<String> knownClassAnnotations = Set.of(
            "javax.persistence.OneToOne",
            "javax.persistence.ManyToOne",
            "javax.persistence.OneToMany",
            "javax.persistence.ManyToMany");
    public static final Map<String, String> knownTypes = Map.of(
            "CodeList",
            "net.binis.codegen.collection.CodeList",
            "CodeListImpl",
            "net.binis.codegen.collection.CodeListImpl",
            "EmbeddedCodeListImpl",
            "net.binis.codegen.collection.EmbeddedCodeListImpl",
            "EmbeddedCodeSetImpl",
            "net.binis.codegen.collection.EmbeddedCodeSetImpl");

    public static final Set<String> primitiveTypes = Set.of("byte", "short", "int", "long", "float", "double", "boolean", "char", "void");

    public static final PrototypeLookup lookup = new PrototypeLookupHandler();
    public static final Map<String, PrototypeDescription<EnumDeclaration>> enumParsed = new HashMap<>();
    public static final Map<String, PrototypeDescription<EnumDeclaration>> enumGenerated = new HashMap<>();
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

    public static String defaultInterfacePackage(ClassOrInterfaceDeclaration type) {
        return defaultPackage(type, null);
    }

    public static String defaultClassPackage(ClassOrInterfaceDeclaration type) {
        return defaultPackage(type, null);
    }

    public static String defaultInterfaceName(String type) {
        return defaultClassName(type).replace("Entity", "");
    }

    public static String defaultInterfaceName(ClassOrInterfaceDeclaration type) {
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
        type.findCompilationUnit().flatMap(CompilationUnit::getPackageDeclaration).ifPresent(p -> {
            result.set(p.getName().asString());
        });

        return result.get() + "." + type.getNameAsString();
    }

    public static String getExternalClassName(CompilationUnit unit, String type) {
        if (nonNull(lookup.findParsed(type))) {
            return type;
        }

        var result = getExternalClassNameIfExists(unit, type);
        if (isNull(result)) {
            result = unit.getPackageDeclaration().get().getNameAsString() + "." + type;
        }
        return result;
    }

    public static String getExternalClassNameIfExists(CompilationUnit unit, String t) {
        var idx = t.indexOf('<');
        var type = idx == -1 ? t : t.substring(0, idx);

        idx = type.indexOf('.');
        var result = nullCheck(getClassImport(unit, type), i -> i.isAsterisk() ? i.getNameAsString() + "." + type : i.getNameAsString());

        if (nonNull(result) && idx > -1) {
            result += type.substring(idx).replace(".", "$");
        }

        if (isNull(result)) {
            result = unit.getImports().stream().filter(ImportDeclaration::isAsterisk)
                    .map(i -> i.getNameAsString() + "." + type)
                    .filter(name -> lookup.isParsed(name) || classExists(name) || lookup.isExternal(name))
                    .findFirst().orElse(null);
        }

        return result;
    }

    public static ImportDeclaration getClassImport(CompilationUnit unit, String type) {
        var known = knownTypes.get(type);
        if (nonNull(known)) {
            return new ImportDeclaration(known, false, false);
        }

        var rType = Holder.of(type);
        var idx = type.indexOf('.');
        if (idx > -1) {
            rType.set(type.substring(0, idx));
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
        return unit.getImports().stream().filter(ImportDeclaration::isAsterisk).filter(i ->
                nonNull(loadClass(i.getNameAsString() + "." + type))).findFirst().orElse(null);
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, String name, Method declaration, boolean isClass) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(name) &&
                                m.getParameters().size() == declaration.getParameterCount() &&
                                m.getType().asString().equals(declaration.getReturnType().getSimpleName())
                        //TODO: Match parameter types also
                ) || !isClass && ancestorMethodExists(spec, declaration);
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, Method declaration, boolean isClass) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(declaration.getName()) &&
                                m.getParameters().size() == declaration.getParameterCount() &&
                                m.getType().asString().equals(declaration.getReturnType().getSimpleName())
                        //TODO: Match parameter types also
                ) || !isClass && ancestorMethodExists(spec, declaration);
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(declaration.getNameAsString()) &&
                                m.getParameters().size() == declaration.getParameters().size() &&
                                m.getType().equals(declaration.getType())
                        //TODO: Match parameter types also
                ) || !isClass && ancestorMethodExists(spec, declaration, declaration.getNameAsString());
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, String methodName, boolean isClass) {
        var type = declaration.getType().asString();
        var actual = declaration.getTypeParameters().stream().map(TypeParameter::asString).anyMatch(s -> s.equals(type)) ? type :
                declaration.getParentNode().isPresent() ? handleType((ClassOrInterfaceDeclaration) declaration.getParentNode().get(), spec, declaration.getType()) : type;

        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(methodName) &&
                                m.getParameters().size() == declaration.getParameters().size() &&
                                (m.getType().asString().equals(actual) || declaration.getTypeParameters().isNonEmpty())
                        //TODO: Match parameter types also
                ) || !isClass && ancestorMethodExists(spec, declaration, methodName);
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, PrototypeField declaration, String methodName, boolean isClass) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(methodName) &&
                                m.getParameters().size() == 1 &&
                                m.getType().equals(declaration.getDeclaration().getVariable(0).getType())
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
                        var intf = parsed.getIntf().findAll(ClassOrInterfaceDeclaration.class).stream().filter(c -> c.getNameAsString().equals(type.getNameAsString())).findFirst();
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
                        m -> m.getName().equals(declaration.getName()) && m.getReturnType().equals(declaration.getReturnType())
                ));
    }

    public static boolean ancestorMethodExists(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, String methodName) {
        //TODO: Check for params and return type
        var unit = spec.findCompilationUnit().get();
        return spec.getExtendedTypes().stream()
                .map(t -> loadClass(getExternalClassName(unit, t.getNameAsString())))
                .filter(Objects::nonNull)
                .anyMatch(c -> Arrays.stream(c.getMethods()).anyMatch(
                        m -> m.getName().equals(methodName)
                ));
    }

    public static boolean ancestorMethodExists(ClassOrInterfaceDeclaration spec, PrototypeField declaration, String methodName) {
        return ancestorMethodExists(spec, declaration, methodName, declaration.getDeclaration().getVariable(0).getType());
    }

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
                                m.getParameters().size() == method.getParameterCount() &&
                                m.getTypeAsString().equals(method.getReturnType().getSimpleName())
                        //TODO: Match parameter types also
                );
    }


    public static String findProperType(PrototypeDescription<ClassOrInterfaceDeclaration> parsed, CompilationUnit unit, ClassExpr expr) {
        var parent = findParentClassOfType(expr, AnnotationExpr.class, a -> knownClassAnnotations.contains(getExternalClassName(unit, a.getNameAsString())));

        if (isNull(parent)) {
            return parsed.getFiles().get(1).getType(0).getNameAsString();
        } else {
            var files = parsed.getFiles();
            if (nonNull(files)) {
                var type = files.get(0).getType(0);
                expr.findCompilationUnit().ifPresent(u -> u.addImport(type.getFullyQualifiedName().get()));
                return type.getNameAsString();
            } else {
                recursiveExpr.add(Triple.of(parsed, unit, expr));
                return expr.getTypeAsString();
            }
        }
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
        source.getImports().stream().filter(i -> !i.getNameAsString().startsWith("net.binis.codegen.annotation")).forEach(i -> {
            var enm = enumParsed.get(i.getNameAsString());
            if (nonNull(enm)) {
                if (isNull(enm.getProperties().getMixInClass())) {
                    destination.addImport(enm.getParsedFullName());
                } else {
                    notNull(enumParsed.get(getExternalClassName(enm.getDeclaration().findCompilationUnit().get(), enm.getProperties().getMixInClass())), p ->
                            destination.addImport(p.getParsedFullName()));
                }
            } else {
                destination.addImport(i);
            }
        });
    }

    public static ClassOrInterfaceDeclaration findModifier(ClassOrInterfaceDeclaration intf) {
        return intf.findFirst(ClassOrInterfaceDeclaration.class, m -> nullCheck(m.getNameAsString(), name -> name.equals(Constants.MODIFIER_INTERFACE_NAME) || name.endsWith(Constants.MODIFIER_CLASS_NAME_SUFFIX))).orElseThrow();
    }

    public static String getEnumNameFromPrototype(TypeDeclaration<?> type, String prototype) {
        var result = Holder.<String>blank();

        notNull(enumParsed.get(getExternalClassName(type.findCompilationUnit().get(), prototype)), p ->
                nullCheck(p.getProperties().getMixInClass(), m -> result.update(getEnumNameFromPrototype(p.getDeclaration(), m)), result.update(p.getParsedName())));

        return result.get();
    }

    public static PrototypeDescription<ClassOrInterfaceDeclaration> getParsed(ClassOrInterfaceType type) {
        var result = lookup.findParsed(getClassName(type));
        if (isNull(result)) {
            result = lookup.findParsed(getExternalClassName(type.findCompilationUnit().get(), type.getNameAsString()));
        }
        return result;
    }

    public static Map<String, Type> processGenerics(Class<?> cls, NodeList<Type> generics) {
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
                    generic = new ClassOrInterfaceType().setName(parsed.getIntf().getNameAsString());
                }
            }
            result.put(types.get(i), generic);
        }
        return result;
    }

    public static Map<String, Type> processGenerics(Class<?> cls, java.lang.reflect.Type[] generics) {
        Map<String, Type> result = null;
        var types = parseGenericClassSignature(cls);

        if (types.size() != generics.length) {
            log.warn("Generic types miss match for {}", cls.getName());
        }

        result = new HashMap<>();

        for (var i = 0; i < types.size(); i++) {
            var type = (Class) generics[i];
            var generic = new ClassOrInterfaceType().setName(type.getSimpleName());
            if (type.isInterface()) {
                var parsed = lookup.findParsed(type.getCanonicalName());
                if (nonNull(parsed)) {
                    generic = new ClassOrInterfaceType().setName(parsed.getIntf().getNameAsString());
                }
            }
            result.put(types.get(i), generic);
        }
        return result;
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
        member.getAnnotations().forEach(annotation -> notNull(getExternalClassName(unit, annotation.getNameAsString()), className ->
                notNull(loadClass(className), cls -> {
                    if (Ignore.class.equals(cls)) {
                        annotation.getChildNodes().forEach(node -> {
                            if (node instanceof MemberValuePair) {
                                var pair = (MemberValuePair) node;
                                var name = pair.getNameAsString();
                                switch (name) {
                                    case "forField":
                                        result.forField(pair.getValue().asBooleanLiteralExpr().getValue());
                                        break;
                                    case "forClass":
                                        result.forClass(pair.getValue().asBooleanLiteralExpr().getValue());
                                        break;
                                    case "forInterface":
                                        result.forInterface(pair.getValue().asBooleanLiteralExpr().getValue());
                                        break;
                                    case "forModifier":
                                        result.forModifier(pair.getValue().asBooleanLiteralExpr().getValue());
                                        break;
                                    default:
                                }
                            }
                        });
                    } else {
                        notNull(cls.getAnnotation(Ignore.class), ann -> result
                                .forField(ann.forField())
                                .forClass(ann.forClass())
                                .forInterface(ann.forInterface())
                                .forModifier(ann.forModifier()));
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
                    if (node instanceof MemberValuePair) {
                        var pair = (MemberValuePair) node;
                        var name = pair.getNameAsString();
                        switch (name) {
                            case "isPublic":
                                result.forPublic(pair.getValue().asBooleanLiteralExpr().getValue());
                                break;
                            case "forClass":
                                result.forClass(pair.getValue().asBooleanLiteralExpr().getValue());
                                break;
                            case "forInterface":
                                result.forInterface(pair.getValue().asBooleanLiteralExpr().getValue());
                                break;
                            default:
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
            if (m1 instanceof NodeWithSimpleName) {
                return ((NodeWithSimpleName) m1).getNameAsString().compareTo(((NodeWithSimpleName) m2).getNameAsString());
            } else if (m1 instanceof NodeWithVariables) {
                return ((NodeWithVariables) m1).getVariable(0).getNameAsString().compareTo(((NodeWithVariables) m2).getVariable(0).getNameAsString());
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
        enumParsed.clear();
        enumGenerated.clear();
        constantParsed.clear();
        declaredConstants.clear();
        processingTypes.clear();
        recursiveExpr.clear();
    }

    @SuppressWarnings("unchecked")
    public static void registerEnricher(Class enricher) {
        var reg = false;
        for (var i : enricher.getInterfaces()) {
            if (Enricher.class.isAssignableFrom(i) && !Enricher.class.equals(i.getClass())) {
                CodeFactory.registerType(i, () -> instantiate(enricher), null);
                reg = true;
            }
        }
        if (!reg) {
            throw new GenericCodeGenException(enricher.getCanonicalName() + " is not enricher!");
        }
    }

    public static void registerKnownEnrichers() {
        registerEnricher(AsEnricherHandler.class);
        registerEnricher(CloneEnricherHandler.class);
        registerEnricher(CreatorEnricherHandler.class);
        registerEnricher(CreatorModifierEnricherHandler.class);
        registerEnricher(ModifierEnricherHandler.class);
        registerEnricher(QueryEnricherHandler.class);
        registerEnricher(ValidationEnricherHandler.class);
        registerEnricher(FluentEnricherHandler.class);
        registerEnricher(RegionEnricherHandler.class);
    }


    public static String handleGenericPrimitiveType(Type type) {
        if (type.isPrimitiveType()) {
            return type.asPrimitiveType().toBoxedType().asString();
        } else {
            return type.asString();
        }
    }

    public static void handleEnrichersSetup(PrototypeData properties) {
        notNull(properties.getEnrichers(), enrichers ->
                enrichers.forEach(e -> e.setup(properties)));
    }

    public static void handleInheritedEnrichersSetup(PrototypeData properties) {
        notNull(properties.getInheritedEnrichers(), enrichers ->
                enrichers.forEach(e -> e.setup(properties)));
    }

    private static List<PrototypeEnricher> getEnrichersList(PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        var map = new HashMap<Class<?>, PrototypeEnricher>();

        notNull(parsed.getBase(), base ->
                notNull(base.getProperties().getInheritedEnrichers(), l ->
                        l.forEach(e -> map.put(e.getClass(), e))));

        notNull(parsed.getMixIn(), mixIn ->
                notNull(mixIn.getBase(), base ->
                        notNull(base.getProperties().getInheritedEnrichers(), l ->
                                l.forEach(e -> map.put(e.getClass(), e)))));

        notNull(parsed.getProperties().getEnrichers(), l ->
                l.forEach(e -> map.put(e.getClass(), e)));

        var list = new ArrayList<>(map.values());
        list.sort(Comparator.comparingInt(PrototypeEnricher::order).reversed());
        return list;
    }

    public static void handleEnrichers(PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        getEnrichersList(parsed).forEach(e -> e.enrich(parsed));
    }

    public static void finalizeEnrichers(PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        getEnrichersList(parsed).forEach(e -> e.finalizeEnrich(parsed));

        parsed.processActions();

        parsed.getInitializers().forEach(i -> {
            if (i.getMiddle() instanceof ClassOrInterfaceDeclaration) {
                var type = (ClassOrInterfaceDeclaration) i.getMiddle();
                getInitializer(isNull(parsed.getMixIn()) ? parsed.getSpec() : parsed.getMixIn().getSpec()).addStatement(new MethodCallExpr()
                        .setName("CodeFactory.registerType")
                        .addArgument((i.getLeft().getParentNode().get() instanceof ClassOrInterfaceDeclaration ? ((ClassOrInterfaceDeclaration) i.getLeft().getParentNode().get()).getNameAsString() + "." : "") + i.getLeft().getNameAsString() + ".class")
                        .addArgument(type.getNameAsString() + "::new")
                        .addArgument(nonNull(i.getRight()) ? "(p, v) -> new " + i.getRight().getNameAsString() + "<>(p, (" + type.getNameAsString() + ") v)" : "null"));
            } else if (i.getMiddle() instanceof LambdaExpr && nonNull(i.getLeft())) {
                var expr = (LambdaExpr) i.getMiddle();
                getInitializer(isNull(parsed.getMixIn()) ? parsed.getSpec() : parsed.getMixIn().getSpec()).addStatement(new MethodCallExpr()
                        .setName("CodeFactory.registerType")
                        .addArgument((i.getLeft().getParentNode().get() instanceof ClassOrInterfaceDeclaration ? ((ClassOrInterfaceDeclaration) i.getLeft().getParentNode().get()).getNameAsString() + "." : "") + i.getLeft().getNameAsString() + ".class")
                        .addArgument(expr)
                        .addArgument("null"));
            }
        });

        parsed.getCustomInitializers().forEach(i -> i.accept(getInitializer(parsed.getSpec())));

        Helpers.handleImports(parsed.getDeclaration().asClassOrInterfaceDeclaration(), parsed.getIntf());
        Helpers.handleImports(parsed.getDeclaration().asClassOrInterfaceDeclaration(), parsed.getSpec());

        getEnrichersList(parsed).forEach(e -> e.postProcess(parsed));
    }

    public static BlockStmt getInitializer(ClassOrInterfaceDeclaration type) {
        return type.getChildNodes().stream().filter(InitializerDeclaration.class::isInstance).map(n -> ((InitializerDeclaration) n).asInitializerDeclaration().getBody()).findFirst().orElseGet(type::addInitializer);
    }

    public static boolean isJavaType(String type) {
        return primitiveTypes.contains(type) || classExists("java.lang." + type);
    }

    public static void handleImports(ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration type) {
        declaration.findCompilationUnit().ifPresent(decl ->
                type.findCompilationUnit().ifPresent(unit ->
                        findUsedTypes(type).stream().map(t -> getClassImport(decl, t)).filter(Objects::nonNull).forEach(unit::addImport)));
    }

    public static Set<String> findUsedTypes(ClassOrInterfaceDeclaration type) {
        var result = new HashSet<String>();
        findUsedTypesInternal(result, type);
        return result;
    }

    private static void findUsedTypesInternal(Set<String> types, Node node) {
        if (node instanceof ClassOrInterfaceType) {
            var type = (ClassOrInterfaceType) node;
            types.add(type.getNameAsString());
            type.getTypeArguments().ifPresent(a -> a.forEach(n -> findUsedTypesInternal(types, n)));
        } else if (node instanceof AnnotationExpr) {
            types.add(((AnnotationExpr) node).getNameAsString());
        } else if (node instanceof NameExpr) {
            types.add(((NameExpr) node).getNameAsString());
        } else if (node instanceof SimpleName) {
            Arrays.stream(((SimpleName) node).asString().split("[.()<\\s]")).filter(s -> !"".equals(s)).forEach(types::add);
        } else if (node instanceof VariableDeclarator) {
            var declarator = (VariableDeclarator) node;
            if (declarator.getType() instanceof ClassOrInterfaceType) {
                types.add(((ClassOrInterfaceType) declarator.getType()).getNameAsString());
            }
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

    public static void addInitializer(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration type, ClassOrInterfaceDeclaration embedded) {
        addInitializerInternal(description, intf, type, embedded);
    }

    public static void addInitializer(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf, LambdaExpr expr, ClassOrInterfaceDeclaration embedded) {
        addInitializerInternal(description, intf, expr, embedded);
    }

    public static void addDefaultCreation(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var intf = description.getIntf();
        if (description.getProperties().isGenerateImplementation() && intf.getAnnotationByName("Default").isEmpty()) {
            var name = description.getImplementorFullName();
            if (description.isNested() && nonNull(description.getParentClassName())) {
                name = getBasePackage(description) + '.' + description.getParsedName().replace('.', '$');
            }
            intf.addAnnotation(description.getParser().parseAnnotation("@Default(\"" + name + "\")").getResult().get());
            intf.findCompilationUnit().get().addImport(Default.class.getCanonicalName());
        }
    }

    private static String getBasePackage(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        if (description.isNested() && nonNull(description.getParentClassName())) {
            return getBasePackage(lookup.findParsed(description.getParentClassName()));
        }
        return description.getProperties().getClassPackage();
    }

    private static void addInitializerInternal(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf, Node node, ClassOrInterfaceDeclaration embedded) {
        description.getSpec().findCompilationUnit().get().addImport("net.binis.codegen.factory.CodeFactory");

        var list = description.getInitializers();
        for (var i = 0; i < list.size(); i++) {
            if (list.get(i).getLeft().getFullyQualifiedName().get().equals(intf.getFullyQualifiedName().get())) {
                if (isNull(list.get(i).getRight()) && nonNull(embedded)) {
                    list.set(i, Triple.of(intf, node, embedded));
                }
                return;
            }
        }

        list.add(Triple.of(intf, node, embedded));
    }

    public static boolean hasAnnotation(PrototypeDescription<ClassOrInterfaceDeclaration> parsed, Class<?> annotation) {
        return parsed.getDeclaration().getAnnotations().stream()
                .map(a -> getExternalClassName(parsed.getDeclaration().findCompilationUnit().get(), a.getNameAsString())).anyMatch(a -> annotation.getCanonicalName().equals(a));
    }

    public static void importClass(CompilationUnit unit, Class<?> cls) {
        if (!cls.isPrimitive() && !"java.lang".equals(cls.getPackageName())) {
            unit.addImport(cls.getCanonicalName());
        }
    }

    public static Map<String, Type> buildGenerics(ClassOrInterfaceType type, ClassOrInterfaceDeclaration cls) {
        var generics = new HashMap<String, Type>();
        var i = 0;
        for (var g : cls.getTypeParameters()) {
            try {
                generics.put(g.getNameAsString(), type.getTypeArguments().get().get(i));
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

    public static Type getFieldType(PrototypeDescription<ClassOrInterfaceDeclaration> description, PrototypeField field) {
        if (field.getDescription().getTypeParameters().isEmpty()) {
            if (field.isGenericField()) {
                var intf = field.getParsed().getIntf();
                var type = Holder.<Type>blank();
                description.getIntf().getExtendedTypes().stream().filter(t -> t.getNameAsString().equals(intf.getNameAsString())).findFirst().ifPresent(t ->
                        type.set(buildGeneric(field.getType(), t, intf)));
                if (type.isPresent()) {
                    return type.get();
                }
            }
            if (nonNull(field.getGenerics())) {
                var type = field.getGenerics().get(field.getType());
                if (nonNull(type)) {
                    return type;
                }
                type = field.getGenerics().get(field.getDescription().getType().asString());
                if (nonNull(type)) {
                    return type;
                }
            }

            if (isNull(field.getDescription())) {
                return field.getDeclaration().getVariables().get(0).getType();
            } else {
                var result = field.getDescription().getType();
                if (nonNull(lookup.findParsed(Helpers.getExternalClassName(field.getDescription().findCompilationUnit().get(), result.asString())))) {
                    result = lookup.getParser().parseClassOrInterfaceType(field.getType()).getResult().get();
                }
                return result;
            }
        } else {
            return new ClassOrInterfaceType().setName("Object");
        }
    }


}
