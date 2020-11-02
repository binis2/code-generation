package net.binis.demo.codegen;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import lombok.extern.slf4j.Slf4j;
import net.binis.demo.tools.Holder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.demo.codegen.Structures.Parsed;
import static net.binis.demo.tools.Tools.nullCheck;

@Slf4j
public class Helpers {

    public static final Map<String, Parsed<ClassOrInterfaceDeclaration>> parsed = new HashMap<>();
    public static final Map<String, Parsed<ClassOrInterfaceDeclaration>> generated = new HashMap<>();
    public static final Map<String, Parsed<EnumDeclaration>> enumParsed = new HashMap<>();
    public static final Map<String, Parsed<EnumDeclaration>> enumGenerated = new HashMap<>();

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
        return defaultPackage(type, "objects");
    }

    public static String defaultClassPackage(ClassOrInterfaceDeclaration type) {
        return defaultPackage(type, "entities");
    }

    public static String defaultInterfaceName(ClassOrInterfaceDeclaration type) {
        return defaultClassName(type).replace("Entity", "");
    }

    public static String defaultClassName(TypeDeclaration<?> type) {
        return type.getNameAsString().replace("Prototype", "");
    }

    public static String getGetterName(String name) {
        return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String getSetterName(String name) {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String getFieldName(String name) {
        return name.substring(3, 4).toLowerCase() + name.substring(4);
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
        var rType = Holder.of(type);
        var idx = type.indexOf('.');
        if (idx > -1) {
            rType.set(type.substring(0, idx));
        }
        var result = unit.getImports()
                .stream()
                .filter(i -> i.getNameAsString().endsWith("." + rType.get()))
                .findFirst()
                .map(NodeWithName::getNameAsString)
                .orElse(null);

        if (nonNull(result) && idx > -1) {
            result += type.substring(idx).replace(".", "$");
        }

        if (isNull(result)) {
            result = unit.getPackageDeclaration().get().getNameAsString() + "." + type;
        }

        return result;
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, Method declaration) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(declaration.getName()) &&
                                m.getParameters().size() == declaration.getParameterCount() &&
                                m.getType().asString().equals(declaration.getReturnType().getSimpleName())
                        //TODO: Match parameter types also
                );
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(declaration.getNameAsString()) &&
                                m.getParameters().size() == declaration.getParameters().size() &&
                                m.getType().equals(declaration.getType())
                        //TODO: Match parameter types also
                );
    }

    public static boolean methodExists(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, String methodName) {
        return spec.getMethods().stream()
                .anyMatch(m -> m.getNameAsString().equals(methodName) &&
                                m.getParameters().size() == declaration.getParameters().size() &&
                                m.getType().equals(declaration.getType())
                        //TODO: Match parameter types also
                );
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
        source.getImports().stream().filter(i -> !i.getNameAsString().startsWith("net.binis.demo.annotation")).forEach(destination::addImport);
    }

    public static ClassOrInterfaceDeclaration findModifier(ClassOrInterfaceDeclaration intf) {
        return intf.findFirst(ClassOrInterfaceDeclaration.class, m -> nullCheck(m.getNameAsString(), name -> name.equals("Modify") || name.endsWith("ModifyImpl"))).orElseThrow();
    }

    public static Parsed<ClassOrInterfaceDeclaration> getParsed(ClassOrInterfaceType type) {
        return parsed.get(getClassName(type));
    }

    public static Map<String, Type> processGenerics(Class<?> cls, NodeList<Type> generics) {
        Map<String, Type> result = null;
        try {
            var types = parseGenericClassSignature((String) classSignature.invoke(cls));

            if (types.size() != generics.size()) {
                log.warn("Generic types miss match for {}", cls.getName());
            }

            result = new HashMap<>();

            for (var i = 0; i < types.size(); i++) {
                result.put(types.get(i), generics.get(i));
            }
        } catch (Exception e) {
            log.error("Unable to get class signature for {}", cls.getName());
        }
        return result;
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

    public static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
