package net.binis.demo.codegen;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.Method;

import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
import static net.binis.demo.codegen.Generator.getGenericsList;
import static net.binis.demo.codegen.Helpers.getFieldName;
import static net.binis.demo.codegen.Helpers.methodExists;

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

    public static boolean isCollection(String type) {
        return ("List".equals(type) || "Set".equals(type) || "Map".equals(type) || "CodeList".equals(type) || "CodeSet".equals(type) || "CodeMap".equals(type));
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
                    .setType(collection.getType() + "<" + collection.getGeneric() + ", " + modifierName + ">");
            if (isClass) {
                var parent = className + ".this." + declaration.getName();
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
        var builder = CollectionType.builder().generic(getGenericsList(source, destination, type));
        switch (type.getNameAsString()) {
            case "List":
            case "CodeList":
                builder
                        .type("CodeList")
                        .classType("CodeListImpl")
                        .implementor("java.util.ArrayList")
                        .implementorInterface("java.util.List");
                break;
            case "Set":
            case "CodeSet":
                builder
                        .type("CodeSet")
                        .classType("CodeSetImpl")
                        .implementor("java.util.HashSet")
                        .implementorInterface("java.util.Set");
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

        result.setClassImport("net.binis.demo.collection." + result.classType);
        result.setInterfaceImport("net.binis.demo.collection." + result.type);

        return result;
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
    }

}
