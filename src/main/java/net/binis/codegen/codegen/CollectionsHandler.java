package net.binis.codegen.codegen;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.codegen.interfaces.PrototypeDescription;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.javaparser.ast.Modifier.Keyword.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.codegen.Generator.getGenericsList;
import static net.binis.codegen.codegen.Helpers.*;

@Slf4j
public class CollectionsHandler {

    public static boolean isCollection(Type type) {
        if (type.isClassOrInterfaceType()) {
            return isCollection(type.asClassOrInterfaceType().getNameAsString());
        }

        return false;
    }

    public static boolean isCollection(Class<?> type) {
        return isCollection(type.getName());
    }

    public static boolean isListOrSet(String type) {
        return ("List".equals(type) || "Set".equals(type) || "CodeList".equals(type) || "CodeSet".equals(type));
    }

    public static boolean isCollection(String type) {
        return isListOrSet(type) || "Map".equals(type) || "CodeMap".equals(type);
    }

    public static void addModifier(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, String modifierName, String className, boolean isClass) {
        if (!methodExists(spec, declaration, isClass)) {
            var type = declaration.getType().asClassOrInterfaceType();
            var collection = getCollectionType(declaration.findCompilationUnit().get(), spec.findCompilationUnit().get(), type);
            var generic = collection.getGeneric().stream().map(Pair::getLeft).collect(Collectors.joining(", "));
            spec.findCompilationUnit().ifPresent(u -> {
                u.addImport(collection.getInterfaceImport());
                u.addImport(collection.getImplementorInterface());
            });
            var method = spec
                    .addMethod(declaration.getNameAsString())
                    .setType(collection.getType() + "<" + (collection.isPrototypeParam() ? generic + ".EmbeddedModify<" + generic + ".Modify>, " : "") + generic + ", " + modifierName + ">");
            if (isClass) {
                var parent = className + ".this." + declaration.getName();
                var block = new BlockStmt()
                        .addStatement(new IfStmt().setCondition(new NameExpr().setName(parent + " == null")).setThenStmt(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName(parent)).setValue(new NameExpr().setName("new " + collection.getImplementor() + "<>()")))));
                if (collection.isPrototypeParam()) {
                    block.addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection.getClassType() + "<>(this, " + parent + ", " + generic + ".class)")));
                } else {
                    block.addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection.getClassType() + "<>(this, " + parent + ")")));
                }
                method
                        .addModifier(PUBLIC)
                        .setBody(block);
                spec.findCompilationUnit().get().addImport(collection.getClassImport());
            } else {
                method.setBody(null);
            }
        }
    }

    public static void addModifierFromSetter(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, String modifierName, String className, boolean isClass) {
        var field = getFieldName(declaration.getNameAsString());
        if (!methodExists(spec, declaration, isClass)) {
            var type = declaration.getParameter(0).getType().asClassOrInterfaceType();
            var collection = getCollectionType(declaration.findCompilationUnit().get(), spec.findCompilationUnit().get(), type);
            spec.findCompilationUnit().ifPresent(u -> {
                u.addImport(collection.getInterfaceImport());
                u.addImport(collection.getImplementorInterface());
            });
            var method = spec
                    .addMethod(field)
                    .setType(collection.getType() + "<" + collection.getGeneric().stream().map(Pair::getLeft).collect(Collectors.joining(", ")) + ", " + modifierName + ">");
            if (isClass) {
                var parent = className + ".this." + field;
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt()
                                .addStatement(new IfStmt().setCondition(new NameExpr().setName(parent + " != null")).setThenStmt(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName(parent)).setValue(new NameExpr().setName("new " + collection.getImplementor() + "<>()")))))
                                .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection.getClassType() + "<>(this, " + parent + ")"))));
                spec.findCompilationUnit().get().addImport(collection.getClassImport());
            } else {
                method.setBody(null);
            }
        }
    }

    public static void addModifier(ClassOrInterfaceDeclaration spec, Method declaration, String modifierName, String className, boolean isClass) {
        if (!methodExists(spec, declaration, isClass)) {
            throw new NotImplementedException("addModifier");
//            var type = declaration.getType().asClassOrInterfaceType();
//            var collection = getCollectionType(type, modifierName);
//            spec.findCompilationUnit().get().addImport(collection.getInterfaceImport());
//            var method = spec
//                    .addMethod(declaration.getName())
//                    .setType(collection.getType() + "<" + collection.getGeneric() + ", " + modifierName + ">");
//            if (isClass) {
//                var parent = className + ".this." + declaration.getName();
//                method
//                        .addModifier(PUBLIC)
//                        .setBody(new BlockStmt()
//                                .addStatement(new IfStmt().setCondition(new NameExpr().setName(parent + " != null")).setThenStmt(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName(parent)).setValue(new NameExpr().setName("new " + collection.getImplementor() + "<>()")))))
//                                .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection.getClassType() + "<>(this, " + parent + ")"))));
//                spec.findCompilationUnit().get().addImport(collection.getClassImport());
//            } else {
//                method.setBody(null);
//            }
        }
    }

    private static CollectionType getCollectionType(CompilationUnit source, CompilationUnit destination, ClassOrInterfaceType type) {
        var generic = getGenericsList(source, destination, type, true);

        var builder = CollectionType.builder().generic(generic);
        switch (type.getNameAsString()) {
            case "List":
            case "CodeList":
                builder
                        .type("CodeList")
                        .classType("CodeListImpl")
                        .implementor("java.util.ArrayList")
                        .implementorInterface("java.util.List")
                        .prototypeParam(!type.getTypeArguments().get().get(0).toString().equals(generic.get(0).getKey()));
                break;
            case "Set":
            case "CodeSet":
                builder
                        .type("CodeSet")
                        .classType("CodeSetImpl")
                        .implementor("java.util.HashSet")
                        .implementorInterface("java.util.Set")
                        .prototypeParam(!type.getTypeArguments().get().get(0).toString().equals(generic.get(0).getKey()));
                break;
            case "Map":
            case "CodeMap":
                builder
                        .type("CodeMap")
                        .classType("CodeMapImpl")
                        .implementor("java.util.HashMap")
                        .implementorInterface("java.util.Map");
                break;
            default:
                builder
                        .type("Unknown")
                        .classType("UnknownImpl");
        }

        var result = builder.build();

        if (result.isPrototypeParam()) {
            result.setType("EmbeddedCodeCollection");
            result.setClassType("Embedded" + result.getClassType());
        }

        result.setInterfaceImport("net.binis.codegen.collection." + result.getType());
        result.setClassImport("net.binis.codegen.collection." + result.getClassType());

        return result;
    }

    public static void handleEmbeddedModifier(PrototypeDescription<ClassOrInterfaceDeclaration> parse, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf) {
        var actualModifier = parse.getModifierIntf();
        if (nonNull(actualModifier) && isNull(parse.getEmbeddedModifierIntf())) {
            if (nonNull(parse.getProperties().getMixInClass())) {
                spec = parse.getMixIn().getSpec();
            }

            var actualModifierClass = parse.getModifier();
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

            ((Structures.Parsed) parse).setEmbeddedModifier(modifierClass);
            ((Structures.Parsed) parse).setEmbeddedModifierIntf(modifier);

            intf.findCompilationUnit().get().addImport("net.binis.codegen.collection.EmbeddedCodeCollection");
            spec.findCompilationUnit().ifPresent(u -> {
                u.addImport("net.binis.codegen.factory.CodeFactory");
                u.addImport("net.binis.codegen.collection.EmbeddedCodeCollection");
            });
        }
    }

    public static CompilationUnit finalizeEmbeddedModifier(PrototypeDescription<ClassOrInterfaceDeclaration> parse, boolean isClass) {
        var unit = parse.getFiles().get(isClass ? 0 : 1);
        var modifier = parse.getModifierIntf();
        var embedded = parse.getEmbeddedModifierIntf();
        if (isClass) {
            modifier = parse.getModifier();
            embedded = parse.getEmbeddedModifier();
        }
        var intfName = unit.getType(0).asClassOrInterfaceDeclaration().isInterface() ? unit.getType(0).getNameAsString() : "void";

        if (isNull(embedded) && recursiveEmbeddedModifiers.containsKey(parse.getIntf().getNameAsString())) {
            handleEmbeddedModifier(parse, parse.getSpec(), parse.getIntf());
            embedded = isClass ? parse.getEmbeddedModifier() : parse.getEmbeddedModifierIntf();
        }

        if (nonNull(embedded)) {

            //var prefix = unit.getType(0).asClassOrInterfaceDeclaration().getFullyQualifiedName();
            embedded.setExtendedTypes(modifier.getExtendedTypes());

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
                        } else {
                            method.setBody(null);
                        }
                    } else if (isCollection(old.getType())) {
                        method.setType(old.getType().toString().replace(intf, eIntf));
                        if (old.getBody().isPresent()) {
                            var collection = getCollectionType(unit, unit, old.getType().asClassOrInterfaceType());
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
                                    .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection + "<>(this, " + parent + ", " + cls +".class)"))));
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

        if (!parse.getProperties().isBase() && isClass) {
            var type = unit.getType(0).asClassOrInterfaceDeclaration();
            if (nonNull(parse.getProperties().getMixInClass())) {
                type = parse.getMixIn().getSpec();
            }
            if ("void".equals(intfName)) {
                intfName = parse.getInterfaceName();
            }
            type.findCompilationUnit().get().addImport("net.binis.codegen.factory.CodeFactory");
            var typeName = type.getNameAsString();
            var initializer = type.getChildNodes().stream().filter(n -> n instanceof InitializerDeclaration).map(n -> ((InitializerDeclaration) n).asInitializerDeclaration().getBody()).findFirst().orElseGet(type::addInitializer);
            initializer
                    .addStatement(new MethodCallExpr()
                            .setName("CodeFactory.registerType")
                            .addArgument(intfName + ".class")
                            .addArgument(typeName + "::new")
                            .addArgument(nonNull(embedded) ? "(p, v) -> new " + embedded.getNameAsString() + "<>(p, (" + typeName + ") v)" : "null"));
        }

        return unit;
    }

    @Data
    @Builder
    private static class CollectionType {
        private String type;
        private String classType;
        private String classImport;
        private String interfaceImport;
        private List<Pair<String, Boolean>> generic;
        private String implementor;
        private String implementorInterface;
        private boolean prototypeParam;
    }

}
