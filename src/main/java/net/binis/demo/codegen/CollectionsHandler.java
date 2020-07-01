package net.binis.demo.codegen;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

import static com.github.javaparser.ast.Modifier.Keyword.*;
import static net.binis.demo.codegen.Generator.getGenericsList;
import static net.binis.demo.codegen.Helpers.getFieldName;
import static net.binis.demo.codegen.Helpers.methodExists;

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
        if (!methodExists(spec, declaration)) {
            var type = declaration.getType().asClassOrInterfaceType();
            var collection = getCollectionType(declaration.findCompilationUnit().get(), spec.findCompilationUnit().get(), type);
            spec.findCompilationUnit().ifPresent(u -> {
                u.addImport(collection.getInterfaceImport());
                u.addImport(collection.getImplementorInterface());
            });
            var method = spec
                    .addMethod(declaration.getNameAsString())
                    .setType(collection.getType() + "<" + (collection.isPrototypeParam() ? collection.getGeneric() + ".EmbeddedModify<Modify>, ": "") + collection.getGeneric() + ", " + modifierName + ">");
            if (isClass) {
                var parent = className + ".this." + declaration.getName();
                var block = new BlockStmt()
                        .addStatement(new IfStmt().setCondition(new NameExpr().setName(parent + " == null")).setThenStmt(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName(parent)).setValue(new NameExpr().setName("new " + collection.getImplementor() + "<>()")))));
                if (collection.isPrototypeParam()) {
                    block
                        .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection.getClassType() + "<>(this, " + parent + ", " + collection.getGeneric() +".class)")));
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
        if (!methodExists(spec, declaration)) {
            var type = declaration.getParameter(0).getType().asClassOrInterfaceType();
            var collection = getCollectionType(declaration.findCompilationUnit().get(), spec.findCompilationUnit().get(), type);
            spec.findCompilationUnit().ifPresent(u -> {
                u.addImport(collection.getInterfaceImport());
                u.addImport(collection.getImplementorInterface());
            });
            var method = spec
                    .addMethod(field)
                    .setType(collection.getType() + "<" + collection.getGeneric() + ", " + modifierName + ">");
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
        if (!methodExists(spec, declaration)) {
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
                        .prototypeParam(!type.getTypeArguments().get().get(0).toString().equals(generic));
                break;
            case "Set":
            case "CodeSet":
                builder
                        .type("CodeSet")
                        .classType("CodeSetImpl")
                        .implementor("java.util.HashSet")
                        .implementorInterface("java.util.Set")
                        .prototypeParam(!type.getTypeArguments().get().get(0).toString().equals(generic));
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

        result.setInterfaceImport("net.binis.demo.collection." + result.getType());
        result.setClassImport("net.binis.demo.collection." + result.getClassType());

        return result;
    }

    public static void handleEmbeddedModifier(String type, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf) {
        var list = intf.getMembers().stream().filter(BodyDeclaration::isClassOrInterfaceDeclaration).map(BodyDeclaration::asClassOrInterfaceDeclaration).collect(Collectors.toList());
        if (list.size() == 1) {
            var actualModifier = list.get(0);
            var actualModifierClass = spec.getMembers().stream().filter(BodyDeclaration::isClassOrInterfaceDeclaration).map(BodyDeclaration::asClassOrInterfaceDeclaration).findFirst().get();
            var modifier = new ClassOrInterfaceDeclaration(
                    Modifier.createModifierList(PUBLIC), false, "Embedded" + actualModifier.getNameAsString())
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
                    .setType("EmbeddedCodeCollection<EmbeddedModify<T>, " + intf.getNameAsString() + ", T>")
                    .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName("(EmbeddedCodeCollection) parent"))));

            spec.addMember(modifierClass);
            intf.addMember(modifier);

            intf.findCompilationUnit().get().addImport("net.binis.demo.collection.EmbeddedCodeCollection");
            spec.findCompilationUnit().ifPresent(u -> {
                u.addImport("net.binis.demo.factory.CodeFactory");
                u.addImport("net.binis.demo.collection.EmbeddedCodeCollection");
            });
            spec.addStaticInitializer();
        }
    }

    public static CompilationUnit finalizeEmbeddedModifier(CompilationUnit unit) {
        var list = unit.getType(0).getMembers().stream().filter(BodyDeclaration::isClassOrInterfaceDeclaration).map(BodyDeclaration::asClassOrInterfaceDeclaration).collect(Collectors.toList());
        if (list.size() == 2) {
            //var prefix = unit.getType(0).asClassOrInterfaceDeclaration().getFullyQualifiedName();
            var modifier = list.get(0).asClassOrInterfaceDeclaration();
            var embedded = list.get(1).asClassOrInterfaceDeclaration();
            embedded.setExtendedTypes(modifier.getExtendedTypes());

            var intf = modifier.getNameAsString();
            var eIntf = embedded.getNameAsString() + "<T>";
            if (modifier.getImplementedTypes().isNonEmpty()) {
                intf = modifier.getImplementedTypes(0).toString();
                eIntf = embedded.getImplementedTypes(0).toString();
            }

            var intfName = "<void>";

            for (var old : modifier.getMethods()) {
                if (!"done".equals(old.getNameAsString())) {
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
                    } else {
                        method.setType(old.getType());
                        method.setBody(old.getBody().orElse(null));
                    }
                } else {
                    intfName = old.getType().asClassOrInterfaceType().getNameAsString();
                }
            }

            var initilizer = unit.getType(0).getMembers().stream().filter(BodyDeclaration::isInitializerDeclaration).map(BodyDeclaration::asInitializerDeclaration).findFirst().orElse(null);
            if (initilizer != null) {
                initilizer.getBody()
                        .addStatement(new MethodCallExpr()
                                .setName("CodeFactory.registerEmbeddableType")
                                .addArgument(intfName + ".class")
                                .addArgument(unit.getType(0).asClassOrInterfaceDeclaration().getNameAsString() + ".class")
                                .addArgument(embedded.getNameAsString() + ".class"));
            }
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
        private String generic;
        private String implementor;
        private String implementorInterface;
        private boolean prototypeParam;
    }

}
