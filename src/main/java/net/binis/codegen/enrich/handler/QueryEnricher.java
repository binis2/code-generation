package net.binis.codegen.enrich.handler;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.CollectionsHandler;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;

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
    private static final String QUERY_SELECT_GENERIC = "QS";
    private static final String QUERY_ORDER_GENERIC = "QO";
    private static final String QUERY_FUNCTIONS = "QueryFunctions";
    private static final String QUERY_NAME = "QueryName";
    private static final String QUERY_IMPL = "Impl";

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var spec = getImplementation(description);
        var intf = getInterface(description);
        var entity = description.getProperties().getInterfaceName();

        with(intf.findCompilationUnit().get(), unit -> {
            unit.addImport("java.util.List")
                    .addImport("net.binis.codegen.creator.EntityCreator")
                    .addImport("net.binis.codegen.spring.query.*")
                    .addImport("java.util.Optional");
        });
        with(spec.findCompilationUnit().get(), unit -> {
            unit.addImport("java.util.List")
                    .addImport("net.binis.codegen.factory.CodeFactory")
                    .addImport("net.binis.codegen.creator.EntityCreator")
                    .addImport("net.binis.codegen.spring.query.*")
                    .addImport("net.binis.codegen.spring.query.executor.QueryExecutor")
                    .addImport("net.binis.codegen.spring.query.executor.QueryOrderer")
                    .addImport("java.util.Optional");
        });

        addFindMethod(intf);
        addQueryStart(entity, intf, spec);
        addQuerySelectOrderName(description, intf, spec);
    }

    private void addQuerySelectOrderName(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration spec) {
        var entity = description.getProperties().getInterfaceName();
        var select = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_SELECT)
                .addExtendedType(QUERY_EXECUTE + "<" + QUERY_GENERIC + ">")
                .addExtendedType("QueryModifiers<" + entity + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">>")
                .addTypeParameter(QUERY_GENERIC);
        select.addMethod("order").setType(entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">").setBody(null);
        intf.addMember(select);

        var impl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, QUERY_EXECUTOR + QUERY_IMPL)
                .addModifier(PROTECTED)
                .addModifier(STATIC)
                .addExtendedType(QUERY_EXECUTOR + "<Object, " + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                .addImplementedType(entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">")
                .addTypeParameter(QUERY_GENERIC);
        impl.addConstructor(PROTECTED)
                .addParameter("Class<" + QUERY_GENERIC + ">", "returnClass")
                .setBody(new BlockStmt()
                        .addStatement("super(returnClass);")
                        .addStatement("order = new " + entity + QUERY_ORDER + QUERY_IMPL + "<>(this);"));
        impl.addMethod("order").setType(entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt()
                        .addStatement("orderStart();")
                        .addStatement(new ReturnStmt("order")));
        spec.addMember(impl);

        impl.addMethod("not", PUBLIC).setType(entity + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                .setBody(new BlockStmt()
                        .addStatement("doNot();")
                        .addStatement("var result = new " + entity + QUERY_NAME + QUERY_IMPL + "<>();")
                        .addStatement("result.setParent(\"u\", this);")
                        .addStatement(new ReturnStmt("(" + entity + "." + QUERY_NAME + ") result")));

        impl.addMethod("lower", PUBLIC).setType(entity + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                .setBody(new BlockStmt()
                        .addStatement("doLower();")
                        .addStatement("var result = new " + entity + QUERY_NAME + QUERY_IMPL + "<>();")
                        .addStatement("result.setParent(\"u\", this);")
                        .addStatement(new ReturnStmt("(" + entity + "." + QUERY_NAME + ") result")));

        impl.addMethod("upper", PUBLIC).setType(entity + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                .setBody(new BlockStmt()
                        .addStatement("doUpper();")
                        .addStatement("var result = new " + entity + QUERY_NAME + QUERY_IMPL + "<>();")
                        .addStatement("result.setParent(\"u\", this);")
                        .addStatement(new ReturnStmt("(" + entity + "." + QUERY_NAME + ") result")));

        impl.addMethod("trim", PUBLIC).setType(entity + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                .setBody(new BlockStmt()
                        .addStatement("doTrim();")
                        .addStatement("var result = new " + entity + QUERY_NAME + QUERY_IMPL + "<>();")
                        .addStatement("result.setParent(\"u\", this);")
                        .addStatement(new ReturnStmt("(" + entity + "." + QUERY_NAME + ") result")));

        impl.addMethod("substring", PUBLIC).setType(entity + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                .addParameter("int", "start")
                .setBody(new BlockStmt()
                        .addStatement("doSubstring(start);")
                        .addStatement("var result = new " + entity + QUERY_NAME + QUERY_IMPL + "<>();")
                        .addStatement("result.setParent(\"u\", this);")
                        .addStatement(new ReturnStmt("(" + entity + "." + QUERY_NAME + ") result")));

        impl.addMethod("substring", PUBLIC).setType(entity + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                .addParameter("int", "start")
                .addParameter("int", "len")
                .setBody(new BlockStmt()
                        .addStatement("doSubstring(start, len);")
                        .addStatement("var result = new " + entity + QUERY_NAME + QUERY_IMPL + "<>();")
                        .addStatement("result.setParent(\"u\", this);")
                        .addStatement(new ReturnStmt("(" + entity + "." + QUERY_NAME + ") result")));

        impl.addMethod("length", PUBLIC).setType(entity + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                .setBody(new BlockStmt()
                        .addStatement("doLen();")
                        .addStatement("var result = new " + entity + QUERY_NAME + QUERY_IMPL + "<>();")
                        .addStatement("result.setParent(\"u\", this);")
                        .addStatement(new ReturnStmt("(" + entity + "." + QUERY_NAME + ") result")));

        impl.addMethod("replace", PUBLIC).setType(entity + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                .addParameter("String", "what")
                .addParameter("String", "withWhat")
                .setBody(new BlockStmt()
                        .addStatement("doReplace(what, withWhat);")
                        .addStatement("var result = new " + entity + QUERY_NAME + QUERY_IMPL + "<>();")
                        .addStatement("result.setParent(\"u\", this);")
                        .addStatement(new ReturnStmt("(" + entity + "." + QUERY_NAME + ") result")));

        var orderImpl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, entity + QUERY_ORDER + QUERY_IMPL)
                .addTypeParameter(QUERY_GENERIC)
                .addModifier(PROTECTED)
                .addExtendedType(QUERY_ORDER + "er<" + QUERY_GENERIC + ">")
                .addImplementedType(entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">");

        orderImpl.addConstructor(PROTECTED)
                .addParameter(QUERY_EXECUTOR + QUERY_IMPL, "executor")
                .setBody(new BlockStmt().addStatement("super(executor);"));

        impl.addMember(orderImpl);

        var qName = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_NAME)
                .addTypeParameter(QUERY_SELECT_GENERIC)
                .addTypeParameter(QUERY_ORDER_GENERIC)
                .addTypeParameter(QUERY_GENERIC);
        intf.addMember(qName);

        var qNameImpl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, entity + QUERY_NAME + QUERY_IMPL)
                .addModifier(PROTECTED)
                .addModifier(STATIC)
                .addImplementedType(entity + "." + QUERY_NAME + "<" + QUERY_SELECT_GENERIC + ", " + QUERY_ORDER_GENERIC + ", " + QUERY_GENERIC + ">")
                .addImplementedType("QueryEmbed")
                .addTypeParameter(QUERY_SELECT_GENERIC)
                .addTypeParameter(QUERY_ORDER_GENERIC)
                .addTypeParameter(QUERY_GENERIC);
        spec.addMember(qNameImpl);

        qNameImpl.addField("String", "name", PRIVATE);
        qNameImpl.addField(QUERY_EXECUTOR + "<Object, " + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">", "executor", PRIVATE);

        qNameImpl.addMethod("setParent", PUBLIC)
                .addParameter("String", "name")
                .addParameter("Object", "executor")
                .setBody(new BlockStmt()
                        .addStatement("this.name = name;")
                        .addStatement("this.executor = (" + QUERY_EXECUTOR + ") executor;")
                        .addStatement("this.executor.embedded(name);"));

        var initializer = spec.getChildNodes().stream().filter(n -> n instanceof InitializerDeclaration).map(n -> ((InitializerDeclaration) n).asInitializerDeclaration().getBody()).findFirst().orElseGet(spec::addInitializer);
        initializer
                .addStatement(new MethodCallExpr()
                        .setName("CodeFactory.registerType")
                        .addArgument(entity + "." + QUERY_NAME + ".class")
                        .addArgument(entity + QUERY_NAME + QUERY_IMPL + "::new")
                        .addArgument("null"));

        var order = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_ORDER)
                .addExtendedType(QUERY_EXECUTE + "<" + QUERY_GENERIC + ">")
                .addTypeParameter(QUERY_GENERIC);
        intf.addMember(order);

        with(description.getBase(), base ->
                base.getFields().forEach(desc ->
                        declareField(entity, desc, select, impl, orderImpl, order, qName, qNameImpl)));

        description.getFields().forEach(desc ->
                declareField(entity, desc, select, impl, orderImpl, order, qName, qNameImpl));
    }

    private void addQueryStart(String entity, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration spec) {
        var start = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_START);
        intf.addMember(start);

        start.addMethod("by").setType(entity + "." + QUERY_SELECT + "<" + intf.getNameAsString() + ">").setBody(null);
        start.addMethod("nativeQuery").setType(QUERY_EXECUTE + "<" + intf.getNameAsString() + ">")
                .addParameter("String", "query")
                .setBody(null);
        start.addMethod("query").setType(QUERY_EXECUTE + "<" + intf.getNameAsString() + ">")
                .addParameter("String", "query")
                .setBody(null);

        var startImpl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, entity + QUERY_START + QUERY_IMPL)
                .addModifier(PROTECTED)
                .addModifier(STATIC)
                .addImplementedType(entity + "." + QUERY_START);
        spec.addMember(startImpl);

        startImpl.addMethod("by")
                .setType(entity + "." + QUERY_SELECT + "<" + intf.getNameAsString() + ">")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(" + entity + "." + QUERY_SELECT + ") new " + QUERY_EXECUTOR + QUERY_IMPL + "(" + intf.getNameAsString() + ".class).by()")));
        startImpl.addMethod("nativeQuery").setType(QUERY_EXECUTE + "<" + intf.getNameAsString() + ">")
                .addParameter("String", "query")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(" + entity + "." + QUERY_SELECT + ") new " + QUERY_EXECUTOR + QUERY_IMPL + "(" + intf.getNameAsString() + ".class).nativeQuery(query)")));
        startImpl.addMethod("query").setType(QUERY_EXECUTE + "<" + intf.getNameAsString() + ">")
                .addParameter("String", "query")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(" + entity + "." + QUERY_SELECT + ") new " + QUERY_EXECUTOR + QUERY_IMPL + "(" + intf.getNameAsString() + ".class).query(query)")));

        var initializer = spec.getChildNodes().stream().filter(n -> n instanceof InitializerDeclaration).map(n -> ((InitializerDeclaration) n).asInitializerDeclaration().getBody()).findFirst().orElseGet(spec::addInitializer);
        initializer
                .addStatement(new MethodCallExpr()
                        .setName("CodeFactory.registerType")
                        .addArgument(entity + "." + QUERY_START + ".class")
                        .addArgument(entity + QUERY_START + QUERY_IMPL + "::new")
                        .addArgument("null"));
    }

    private void addFindMethod(ClassOrInterfaceDeclaration intf) {
        intf.addMethod("find", STATIC)
                .setType(QUERY_START)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("EntityCreator.create(" + intf.getNameAsString() + "." + QUERY_START + ".class)")));
    }

    private void declareField(String entity, PrototypeField desc, ClassOrInterfaceDeclaration select, ClassOrInterfaceDeclaration impl, ClassOrInterfaceDeclaration orderImpl, ClassOrInterfaceDeclaration order, ClassOrInterfaceDeclaration qName, ClassOrInterfaceDeclaration qNameImpl) {
        var field = desc.getDeclaration();
        var name = field.getVariable(0).getNameAsString();

        if (desc.isCollection()) {
            var subType = CollectionsHandler.getCollectionType(field.getCommonType());

            select.addMethod(name)
                    .setType(QUERY_SELECT + "Operation<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                    .setBody(null)
                    .addParameter(subType, "in");

            impl.addMethod(name)
                    .setType(QUERY_SELECT + "Operation<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                    .addModifier(PUBLIC)
                    .addParameter(subType, "in")
                    .setBody(new BlockStmt()
                            .addStatement("collection(\"" + name + "\", in);")
                            .addStatement(new ReturnStmt("this")));
        } else {
            select.addMethod(name)
                    .setType(QUERY_SELECT + "Operation<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                    .setBody(null)
                    .addParameter(field.getCommonType(), name);

            impl.addMethod(name)
                    .setType(QUERY_SELECT + "Operation<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                    .addModifier(PUBLIC)
                    .addParameter(field.getCommonType(), name)
                    .setBody(new BlockStmt()
                            .addStatement("identifier(\"" + name + "\", " + name + ");")
                            .addStatement(new ReturnStmt("this")));

            order.addMethod(name)
                    .setType(QUERY_ORDER + "Operation<" + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                    .setBody(null);

            orderImpl.addMethod(name)
                    .setType(QUERY_ORDER + "Operation<" + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                    .addModifier(PUBLIC)
                    .setBody(new BlockStmt()
                            .addStatement("QueryExecutorImpl.this.orderIdentifier(\"" + name + "\");")
                            .addStatement(new ReturnStmt("(" + QUERY_ORDER + "Operation) QueryExecutorImpl.this")));

            if (checkQueryName(entity, desc)) {
                select.addMethod(name).setType(desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">").setBody(null);
                impl.addMethod(name, PUBLIC).setType(desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                        .setBody(new BlockStmt()
                                .addStatement("var result = EntityCreator.create(" + desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + ".class);")
                                .addStatement("((QueryEmbed) result).setParent(\"" + name + "\", this);")
                                .addStatement(new ReturnStmt("result")));

                qName.addMethod(name).setType(desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + "<" + QUERY_SELECT_GENERIC + ", " + QUERY_ORDER_GENERIC + ", " + QUERY_GENERIC + ">").setBody(null);
                qNameImpl.addMethod(name, PUBLIC).setType(desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + "<" + QUERY_SELECT_GENERIC + ", " + QUERY_ORDER_GENERIC + ", " + QUERY_GENERIC + ">")
                        .setBody(new BlockStmt()
                                .addStatement("var result = EntityCreator.create(" + desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + ".class);")
                                .addStatement("((QueryEmbed) result).setParent(\"" + name + "\", executor);")
                                .addStatement(new ReturnStmt("result")));

            } else {
                select.addMethod(name).setType(QUERY_FUNCTIONS + "<" + Helpers.handleGenericPrimitiveType(field.getCommonType()) + "," + QUERY_SELECT + "Operation<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">>").setBody(null);
                impl.addMethod(name, PUBLIC).setType(QUERY_FUNCTIONS + "<" + Helpers.handleGenericPrimitiveType(field.getCommonType()) + "," + QUERY_SELECT + "Operation<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">>")
                        .setBody(new BlockStmt()
                                .addStatement("identifier(\"" + name + "\");")
                                .addStatement(new ReturnStmt("(" + QUERY_FUNCTIONS + ") this")));

                qName.addMethod(name)
                        .setType(QUERY_FUNCTIONS + "<" + Helpers.handleGenericPrimitiveType(field.getCommonType()) + ", " + QUERY_SELECT + "Operation<" + QUERY_SELECT_GENERIC + ", " + QUERY_ORDER_GENERIC + ", " + QUERY_GENERIC + ">>")
                        .setBody(null);

                qNameImpl.addMethod(name)
                        .setType(QUERY_FUNCTIONS + "<" + Helpers.handleGenericPrimitiveType(field.getCommonType()) + ", " + QUERY_SELECT + "Operation<" + QUERY_SELECT_GENERIC + ", " + QUERY_ORDER_GENERIC + ", " + QUERY_GENERIC + ">>")
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt()
                                .addStatement("executor.identifier(\"" + name + "\");")
                                .addStatement(new ReturnStmt("(" + QUERY_FUNCTIONS + ") executor")));
            }

            qName.addMethod(name)
                    .setType(QUERY_SELECT + "Operation<" + QUERY_SELECT_GENERIC + ", " + QUERY_ORDER_GENERIC + ", " + QUERY_GENERIC + ">")
                    .setBody(null)
                    .addParameter(field.getCommonType(), name);

            qNameImpl.addMethod(name)
                    .setType(QUERY_SELECT + "Operation<" + QUERY_SELECT_GENERIC + ", " + QUERY_ORDER_GENERIC + ", " + QUERY_GENERIC + ">")
                    .addModifier(PUBLIC)
                    .addParameter(field.getCommonType(), name)
                    .setBody(new BlockStmt()
                            .addStatement("executor.identifier(\"" + name + "\", " + name + ");")
                            .addStatement(new ReturnStmt("(" + QUERY_SELECT + "Operation) executor")));

        }
    }

    private boolean checkQueryName(String entity, PrototypeField desc) {
        return nonNull(desc.getPrototype()) && (desc.getPrototype().getProperties().getEnrichers().stream().anyMatch(e -> QueryEnricher.class.equals(e.getClass())) ||
                nonNull(desc.getPrototype().getBase()) &&
                        desc.getPrototype().getBase().getProperties().getInheritedEnrichers().stream().anyMatch(e -> QueryEnricher.class.equals(e.getClass())));
    }

}
