package net.binis.codegen.enrich.handler;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.enrich.handler.base.BaseEnricher;

import static com.github.javaparser.ast.Modifier.Keyword.*;
import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Tools.with;

public class QueryEnricher extends BaseEnricher {

    private static final String QUERY_START = "QueryStart";
    private static final String QUERY_SELECT = "QuerySelect";
    private static final String QUERY_ORDER = "QueryOrder";
    private static final String QUERY_EXECUTE = "QueryExecute";
    private static final String QUERY_EXECUTOR = "QueryExecutor";
    private static final String QUERY_GENERIC = "QR";
    private static final String QUERY_IMPL = "Impl";

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var spec = getImplementation(description);
        var intf = getInterface(description);

        with(intf.findCompilationUnit().get(), unit -> {
            unit.addImport("java.util.List")
                    .addImport("net.binis.codegen.creator.EntityCreator")
                    .addImport("net.binis.codegen.spring.query.QueryExecute")
                    .addImport("net.binis.codegen.spring.query.QueryOrderOperation")
                    .addImport("net.binis.codegen.spring.query.QuerySelectOperation");
        });
        with(spec.findCompilationUnit().get(), unit -> {
            unit.addImport("java.util.List")
                    .addImport("net.binis.codegen.factory.CodeFactory")
                    .addImport("net.binis.codegen.spring.query.QueryOrderOperation")
                    .addImport("net.binis.codegen.spring.query.QuerySelectOperation")
                    .addImport("net.binis.codegen.spring.query.executor.QueryExecutor")
                    .addImport("net.binis.codegen.spring.query.executor.QueryOrderer");
        });


        addFindMethod(intf);
        addQueryStartIntf(intf, spec);
        addQuerySelectAndOrderIntf(intf, spec);
    }

    private void addQuerySelectAndOrderIntf(ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration spec) {
        var select = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_SELECT)
                .addExtendedType(QUERY_EXECUTE + "<" + QUERY_GENERIC + ">")
                .addTypeParameter(QUERY_GENERIC);
        select.addMethod("order").setType(QUERY_ORDER + "<" + QUERY_GENERIC + ">").setBody(null);
        intf.addMember(select);

        var impl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, QUERY_EXECUTOR + QUERY_IMPL)
                .addModifier(PROTECTED)
                .addModifier(STATIC)
                .addExtendedType(QUERY_EXECUTOR + "<" + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                .addImplementedType(QUERY_SELECT + "<" + QUERY_GENERIC + ">")
                .addTypeParameter(QUERY_GENERIC);
        impl.addConstructor(PROTECTED)
                .addParameter("Class<" + QUERY_GENERIC + ">", "returnClass")
                .setBody(new BlockStmt()
                        .addStatement("super(returnClass);")
                        .addStatement("order = new " + QUERY_ORDER + QUERY_IMPL + "<" + QUERY_GENERIC + ">(this);"));
        impl.addMethod("order").setType(QUERY_ORDER + "<" + QUERY_GENERIC + ">")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt()
                        .addStatement("orderStart();")
                        .addStatement(new ReturnStmt("order")));
        spec.addMember(impl);

        var orderImpl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, QUERY_ORDER + QUERY_IMPL)
                .addTypeParameter(QUERY_GENERIC)
                .addModifier(PROTECTED)
                .addExtendedType(QUERY_ORDER + "er<" + QUERY_GENERIC + ">")
                .addImplementedType(QUERY_ORDER + "<" + QUERY_GENERIC + ">");

        orderImpl.addConstructor(PROTECTED)
                .addParameter("QueryExecutorImpl", "executor")
                .setBody(new BlockStmt().addStatement("super(executor);"));

        impl.addMember(orderImpl);

        var order = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_ORDER)
                .addExtendedType(QUERY_EXECUTE + "<" + QUERY_GENERIC + ">")
                .addTypeParameter(QUERY_GENERIC);
        intf.addMember(order);

        spec.findAll(FieldDeclaration.class).forEach(field -> {
            var name = field.getVariable(0).getNameAsString();
            select.addMethod(name)
                    .setType(QUERY_SELECT + "Operation<" + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                    .setBody(null)
                    .addParameter(field.getCommonType(), name);

            order.addMethod(name)
                    .setType(QUERY_ORDER + "Operation<" + QUERY_ORDER + "<" + QUERY_GENERIC + ">, "+ QUERY_GENERIC + ">")
                    .setBody(null);

            impl.addMethod(name)
                    .setType(QUERY_SELECT + "Operation<" + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                    .addModifier(PUBLIC)
                    .addParameter(field.getCommonType(), name)
                    .setBody(new BlockStmt()
                            .addStatement("identifier(\"" + name + "\", " + name + ");")
                            .addStatement(new ReturnStmt("(" + QUERY_SELECT + "Operation) this")));

            orderImpl.addMethod(name)
                    .setType(QUERY_ORDER + "Operation<" + QUERY_ORDER + "<" + QUERY_GENERIC + ">, "+ QUERY_GENERIC + ">")
                    .addModifier(PUBLIC)
                    .setBody(new BlockStmt()
                            .addStatement("QueryExecutorImpl.this.orderIdentifier(\"" + name + "\");")
                            .addStatement(new ReturnStmt("(" + QUERY_ORDER + "Operation) QueryExecutorImpl.this")));
        });
    }

    private void addQueryStartIntf(ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration spec) {
        var start = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_START);
        intf.addMember(start);

        start.addMethod("by").setType(QUERY_SELECT + "<" + intf.getNameAsString() + ">").setBody(null);
        start.addMethod("all").setType(QUERY_SELECT + "<List<" + intf.getNameAsString() + ">>").setBody(null);
        start.addMethod("count").setType(QUERY_SELECT + "<Long>").setBody(null);

        var startImpl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, QUERY_START + QUERY_IMPL)
                .addModifier(PROTECTED)
                .addModifier(STATIC)
                .addImplementedType(QUERY_START);
        spec.addMember(startImpl);

        startImpl.addMethod("by")
                .setType(QUERY_SELECT + "<" + intf.getNameAsString() + ">")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(QuerySelect) new " + QUERY_EXECUTOR + QUERY_IMPL + "(" + intf.getNameAsString() + ".class).by()")));
        startImpl.addMethod("all")
                .setType(QUERY_SELECT + "<List<" + intf.getNameAsString() + ">>")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(QuerySelect) new " + QUERY_EXECUTOR + QUERY_IMPL + "(" + intf.getNameAsString() + ".class).all()")));
        startImpl.addMethod("count")
                .setType(QUERY_SELECT + "<Long>")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(QuerySelect) new " + QUERY_EXECUTOR + QUERY_IMPL + "(" + intf.getNameAsString() + ".class).count()")));

        var initializer = spec.getChildNodes().stream().filter(n -> n instanceof InitializerDeclaration).map(n -> ((InitializerDeclaration) n).asInitializerDeclaration().getBody()).findFirst().orElseGet(spec::addInitializer);
        initializer
                .addStatement(new MethodCallExpr()
                        .setName("CodeFactory.registerType")
                        .addArgument(QUERY_START + ".class")
                        .addArgument(QUERY_START + QUERY_IMPL + "::new")
                        .addArgument("null"));
    }

    private void addFindMethod(ClassOrInterfaceDeclaration intf) {
        intf.addMethod("find", STATIC)
                .setType(QUERY_START)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("EntityCreator.create(" + intf.getNameAsString() + ".QueryStart.class)")));
        //.setBody(new BlockStmt().addStatement(new ReturnStmt("new " + QUERY_START + "()")));

    }

}
