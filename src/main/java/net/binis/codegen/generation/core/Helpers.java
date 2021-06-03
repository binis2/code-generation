package net.binis.codegen.generation.core;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithVariables;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.enrich.PrototypeEnricher;
import net.binis.codegen.generation.core.interfaces.PrototypeData;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.enrich.PrototypeLookup;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import net.binis.codegen.tools.Holder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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

    public static final Set<String> primitiveTypes = Set.of("byte", "short", "int", "long", "float", "double", "boolean", "char");

    public static final PrototypeLookup lookup = new PrototypeLookupHandler();
    public static final Map<String, PrototypeDescription<EnumDeclaration>> enumParsed = new HashMap<>();
    public static final Map<String, PrototypeDescription<EnumDeclaration>> enumGenerated = new HashMap<>();
    public static final Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> constantParsed = new HashMap<>();
    public static final Map<String, List<Pair<String, String>>> declaredConstants = new HashMap<>();
    public static final Map<String, Structures.ProcessingType> processingTypes = new HashMap<>();
    public static final List<Triple<PrototypeDescription<ClassOrInterfaceDeclaration>, CompilationUnit, ClassExpr>> recursiveExpr = new LinkedList<>();

    public static final Method classSignature = initClassSignature();
    public static final Method methodSignature = initMethodSignature();
    public static final Pattern methodSignatureMatcher = Pattern.compile("T(.*?);");

    private static Method initMethodSignature() {
        try {
            var method = Method.class.getDeclaredMethod("getGenericSignature");
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            log.error("Unable to initialize Generic Method Signature getter!");
        }
        return null;
    }

    private static Method initClassSignature() {
        try {
            var method = Class.class.getDeclaredMethod("getGenericSignature0");
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            log.error("Unable to initialize Generic Class Signature getter!");
        }
        return null;
    }


    public static String defaultPackage(TypeDeclaration<?> type, String name) {
        var result = type.findCompilationUnit().get().getPackageDeclaration().get().getNameAsString();
        if (nonNull(name)) {
            return result.replace("prototype", name);
        } else {
            return result.replace(".prototype", "");
        }
    }

    public static String defaultInterfacePackage(ClassOrInterfaceDeclaration type) {
        return defaultPackage(type, null);
    }

    public static String defaultClassPackage(ClassOrInterfaceDeclaration type) {
        return defaultPackage(type, null);
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
        var result = Holder.blank();
        type.findCompilationUnit().flatMap(CompilationUnit::getPackageDeclaration).ifPresent(p -> {
            result.set(p.getName().asString());
        });

        return result.get() + "." + type.getNameAsString();
    }

    public static String getClassName(ClassOrInterfaceType type) {
        var result = Holder.blank();
        type.findCompilationUnit().flatMap(CompilationUnit::getPackageDeclaration).ifPresent(p -> {
            result.set(p.getName().asString());
        });

        return result.get() + "." + type.getNameAsString();
    }

    public static String getExternalClassName(CompilationUnit unit, String type) {
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
                    .filter(name -> lookup.isParsed(name) || classExists(name))
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
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(methodName) &&
                                m.getParameters().size() == declaration.getParameters().size() &&
                                m.getType().equals(declaration.getType())
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
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(declaration.getName()) &&
                                m.getParameters().size() == 1 &&
                                m.getType().equals(declaration.getDeclaration().getVariable(0).getType())
                        //TODO: Match parameter types also
                ) || !isClass && ancestorMethodExists(spec, declaration, declaration.getName());
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
        //TODO: Check for params and return type
        var unit = spec.findCompilationUnit().get();
        return spec.getExtendedTypes().stream()
                .map(t -> loadClass(getExternalClassName(unit, t.getNameAsString())))
                .filter(Objects::nonNull)
                .anyMatch(c -> Arrays.stream(c.getMethods()).anyMatch(
                        m -> m.getName().equals(methodName)
                ));
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

    public static boolean fieldExists(ClassOrInterfaceDeclaration spec, String field) {
        return nonNull(findField(spec, field));
    }

    public static FieldDeclaration findField(ClassOrInterfaceDeclaration spec, String field) {
        return spec.getFields().stream().filter(f -> f.getVariable(0).getNameAsString().equals(field)).findFirst().orElse(null);
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
            result.put(types.get(i), generics.get(i));
        }
        return result;
    }

    public static List<String> parseGenericClassSignature(Class<?> cls) {
        try {
            return parseGenericClassSignature((String) classSignature.invoke(cls));
        } catch (Exception e) {
            log.error("Unable to get class signature for {}", cls.getName());
        }
        return Collections.emptyList();
    }

    public static List<String> parseGenericClassSignature(String signature) {
        var result = new ArrayList<String>();
        var split = signature.split(":");
        for (var i = 0; i < split.length - 1; i++) {
            result.add(String.valueOf(split[i].charAt(split[i].length() - 1)));
        }
        return result;
    }

    public static String parseMethodSignature(Method method) {
        try {
            var match = methodSignatureMatcher.matcher((String) methodSignature.invoke(method));
            if (match.find()) {
                return match.group(1);
            }
        } catch (Exception e) {
            log.error("Unable to get method signature for {}", method.getName());
        }
        return "Object";
    }

    public static String parseMethodSignature(MethodDeclaration method) {
        return "Not Implemented";
    }

    public static Structures.Ignores getIgnores(BodyDeclaration<?> member) {
        var result = Structures.Ignores.builder();
        member.getAnnotations().stream().filter(a -> "Ignore".equals(a.getNameAsString())).findFirst().ifPresent(annotation ->
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
                }));
        return result.build();
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

    @SuppressWarnings("unchecked")
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

    public static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
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
        var list = new ArrayList<PrototypeEnricher>();

        notNull(parsed.getBase(), base ->
                notNull(base.getProperties().getInheritedEnrichers(), list::addAll));

        notNull(parsed.getMixIn(), mixIn ->
                notNull(mixIn.getBase(), base ->
                        notNull(base.getProperties().getInheritedEnrichers(), list::addAll)));

        notNull(parsed.getProperties().getEnrichers(), list::addAll);

        list.sort(Comparator.comparingInt(PrototypeEnricher::order).reversed());
        return list;
    }

    public static void handleEnrichers(PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        getEnrichersList(parsed).forEach(e -> e.enrich(parsed));
    }

    public static void finalizeEnrichers(PrototypeDescription<ClassOrInterfaceDeclaration> parsed) {
        getEnrichersList(parsed).forEach(e -> e.finalize(parsed));
    }

    public static boolean isJavaType(String type) {
        return primitiveTypes.contains(type) || classExists("java.lang." + type);
    }


}
