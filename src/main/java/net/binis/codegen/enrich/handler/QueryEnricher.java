package net.binis.codegen.enrich.handler;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import net.binis.codegen.generation.core.CollectionsHandler;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.interfaces.PrototypeField;

import static com.github.javaparser.ast.Modifier.Keyword.*;
import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Tools.with;
import static net.binis.codegen.tools.Tools.withRes;

public class QueryEnricher extends BaseEnricher {

    private static final String QUERY_START = "QueryStart";
    private static final String QUERY_SELECT = "QuerySelect";
    private static final String QUERY_ORDER = "QueryOrder";
    private static final String QUERY_EXECUTE = "QueryExecute";
    private static final String QUERY_EXECUTOR = "QueryExecutor";
    private static final String QUERY_GENERIC = "QR";
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
                    .addImport("net.binis.codegen.spring.query.QueryExecute")
                    .addImport("net.binis.codegen.spring.query.QueryOrderOperation")
                    .addImport("net.binis.codegen.spring.query.QuerySelectOperation")
                    .addImport("java.util.Optional");
        });
        with(spec.findCompilationUnit().get(), unit -> {
            unit.addImport("java.util.List")
                    .addImport("net.binis.codegen.factory.CodeFactory")
                    .addImport("net.binis.codegen.spring.query.QueryOrderOperation")
                    .addImport("net.binis.codegen.spring.query.QuerySelectOperation")
                    .addImport("net.binis.codegen.spring.query.QueryExecute")
                    .addImport("net.binis.codegen.spring.query.executor.QueryExecutor")
                    .addImport("net.binis.codegen.spring.query.executor.QueryOrderer")
                    .addImport("java.util.Optional");
        });

        addFindMethod(intf);
        addQueryStartIntf(entity, intf, spec);
        addQuerySelectAndOrderIntf(description, intf, spec);
    }

    private void addQuerySelectAndOrderIntf(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration spec) {
        var entity = description.getProperties().getInterfaceName();
        var select = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_SELECT)
                .addExtendedType(QUERY_EXECUTE + "<" + QUERY_GENERIC + ">")
                .addTypeParameter(QUERY_GENERIC);
        select.addMethod("order").setType(entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">").setBody(null);
        intf.addMember(select);

        var impl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, QUERY_EXECUTOR + QUERY_IMPL)
                .addModifier(PROTECTED)
                .addModifier(STATIC)
                .addExtendedType(QUERY_EXECUTOR + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
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

        var orderImpl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, entity + QUERY_ORDER + QUERY_IMPL)
                .addTypeParameter(QUERY_GENERIC)
                .addModifier(PROTECTED)
                .addExtendedType(QUERY_ORDER + "er<" + QUERY_GENERIC + ">")
                .addImplementedType(entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">");

        orderImpl.addConstructor(PROTECTED)
                .addParameter("QueryExecutorImpl", "executor")
                .setBody(new BlockStmt().addStatement("super(executor);"));

        impl.addMember(orderImpl);

        var order = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_ORDER)
                .addExtendedType(QUERY_EXECUTE + "<" + QUERY_GENERIC + ">")
                .addTypeParameter(QUERY_GENERIC);
        intf.addMember(order);

        with(description.getBase(), base ->
                base.getFields().forEach(desc ->
                        declareField(entity, desc, select, impl, orderImpl, order)));

        description.getFields().forEach(desc ->
                declareField(entity, desc, select, impl, orderImpl, order));
    }

    private void addQueryStartIntf(String entity, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration spec) {
        var start = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_START);
        intf.addMember(start);

        start.addMethod("by").setType(entity + "." + QUERY_SELECT + "<Optional<" + intf.getNameAsString() + ">>").setBody(null);
        start.addMethod("all").setType(entity + "." + QUERY_SELECT + "<List<" + intf.getNameAsString() + ">>").setBody(null);
        start.addMethod("count").setType(entity + "." + QUERY_SELECT + "<Long>").setBody(null);
        start.addMethod("nativeQuery").setType(QUERY_EXECUTE + "<List<" + QUERY_GENERIC + ">>")
                .addTypeParameter(QUERY_GENERIC)
                .addParameter("String", "query")
                .addParameter("Class<" + QUERY_GENERIC + ">", "cls")
                .setBody(null);
        start.addMethod("query").setType(QUERY_EXECUTE + "<List<" + QUERY_GENERIC + ">>")
                .addTypeParameter(QUERY_GENERIC)
                .addParameter("String", "query")
                .addParameter("Class<" + QUERY_GENERIC + ">", "cls")
                .setBody(null);
        start.addMethod("top").setType(entity + "." + QUERY_SELECT + "<List<" + intf.getNameAsString() + ">>")
                .addParameter("long", "records")
                .setBody(null);

        var startImpl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, entity + QUERY_START + QUERY_IMPL)
                .addModifier(PROTECTED)
                .addModifier(STATIC)
                .addImplementedType(entity + "." + QUERY_START);
        spec.addMember(startImpl);

        startImpl.addMethod("by")
                .setType(entity + "." + QUERY_SELECT + "<Optional<" + intf.getNameAsString() + ">>")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(" + entity + "." + QUERY_SELECT + ") new " + QUERY_EXECUTOR + QUERY_IMPL + "(" + intf.getNameAsString() + ".class).by()")));
        startImpl.addMethod("all")
                .setType(entity + "." + QUERY_SELECT + "<List<" + intf.getNameAsString() + ">>")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(" + entity + "." + QUERY_SELECT + ") new " + QUERY_EXECUTOR + QUERY_IMPL + "(" + intf.getNameAsString() + ".class).all()")));
        startImpl.addMethod("count")
                .setType(entity + "." + QUERY_SELECT + "<Long>")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(" + entity + "." + QUERY_SELECT + ") new " + QUERY_EXECUTOR + QUERY_IMPL + "(" + intf.getNameAsString() + ".class).count()")));
        startImpl.addMethod("nativeQuery").setType(QUERY_EXECUTE + "<List<" + QUERY_GENERIC + ">>")
                .addTypeParameter(QUERY_GENERIC)
                .addParameter("String", "query")
                .addParameter("Class<" + QUERY_GENERIC + ">", "cls")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(" + entity + "." + QUERY_SELECT + ") new " + QUERY_EXECUTOR + QUERY_IMPL + "(" + intf.getNameAsString() + ".class).nativeQuery(query, cls)")));
        startImpl.addMethod("query").setType(QUERY_EXECUTE + "<List<" + QUERY_GENERIC + ">>")
                .addTypeParameter(QUERY_GENERIC)
                .addParameter("String", "query")
                .addParameter("Class<" + QUERY_GENERIC + ">", "cls")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(" + entity + "." + QUERY_SELECT + ") new " + QUERY_EXECUTOR + QUERY_IMPL + "(" + intf.getNameAsString() + ".class).query(query, cls)")));
        startImpl.addMethod("top").setType(entity + "." + QUERY_SELECT + "<List<" + intf.getNameAsString() + ">>")
                .addParameter("long", "records")
                .addModifier(PUBLIC)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("(" + entity + "." + QUERY_SELECT + ") new " + QUERY_EXECUTOR + QUERY_IMPL + "(" + intf.getNameAsString() + ".class).top(records)")));


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

    private void declareField(String entity, PrototypeField desc, ClassOrInterfaceDeclaration select, ClassOrInterfaceDeclaration impl, ClassOrInterfaceDeclaration orderImpl, ClassOrInterfaceDeclaration order) {
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
                select.addMethod(name).setType(desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + "<" + QUERY_GENERIC + ">").setBody(null);
                impl.addMethod(name, PUBLIC).setType(desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + "<" + QUERY_GENERIC + ">")
                        .setBody(new BlockStmt()
                                .addStatement("var result = EntityCreator.create(" + desc.getPrototype().getInterfaceName() + "." + QUERY_NAME + ".class);")
                                .addStatement("((QueryEmbed) result).setParent(\"" + name + "\", this);")
                                .addStatement(new ReturnStmt("result")));
            }

        }
    }

    private void declareQueryName(PrototypeDescription<ClassOrInterfaceDeclaration> prototype) {
        if (prototype.getProperties().isBase()) {
            return;
        }

        var spec = getImplementation(prototype);
        var intf = getInterface(prototype);
        var entity = prototype.getProperties().getInterfaceName();

        with(spec.findCompilationUnit().get(), unit -> unit
                .addImport("net.binis.codegen.spring.query.QueryEmbed"));

        var qName = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_NAME)
                .addTypeParameter(QUERY_GENERIC);
        intf.addMember(qName);

        var qNameImpl = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), false, entity + QUERY_NAME + QUERY_IMPL)
                .addModifier(PROTECTED)
                .addModifier(STATIC)
                .addImplementedType(entity + "." + QUERY_NAME + "<" + QUERY_GENERIC + ">")
                .addImplementedType("QueryEmbed")
                .addTypeParameter(QUERY_GENERIC);
        spec.addMember(qNameImpl);

        qNameImpl.addField("String", "name", PRIVATE);
        qNameImpl.addField(QUERY_EXECUTOR + "<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">", "executor", PRIVATE);

        qNameImpl.addMethod("setParent", PUBLIC)
                .addParameter("String", "name")
                .addParameter("Object", "executor")
                .setBody(new BlockStmt()
                        .addStatement("this.name = name;")
                        .addStatement("this.executor = (QueryExecutor) executor;"));

        var initializer = spec.getChildNodes().stream().filter(n -> n instanceof InitializerDeclaration).map(n -> ((InitializerDeclaration) n).asInitializerDeclaration().getBody()).findFirst().orElseGet(spec::addInitializer);
        initializer
                .addStatement(new MethodCallExpr()
                        .setName("CodeFactory.registerType")
                        .addArgument(entity + "." + QUERY_NAME + ".class")
                        .addArgument(entity + QUERY_NAME + QUERY_IMPL + "::new")
                        .addArgument("null"));

        with(prototype.getBase(), base ->
                base.getFields().forEach(field ->
                        declareNameField(entity, field, qName, qNameImpl)));

        prototype.getFields().forEach(field ->
                declareNameField(entity, field, qName, qNameImpl));

    }

    private void declareNameField(String entity, PrototypeField desc, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration impl) {
        if (!desc.isCollection()) {
            var field = desc.getDeclaration();
            var name = field.getVariable(0).getNameAsString();
            intf.addMethod(name)
                    .setType(QUERY_SELECT + "Operation<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                    .setBody(null)
                    .addParameter(field.getCommonType(), name);

            impl.addMethod(name)
                    .setType(QUERY_SELECT + "Operation<" + entity + "." + QUERY_SELECT + "<" + QUERY_GENERIC + ">, " + entity + "." + QUERY_ORDER + "<" + QUERY_GENERIC + ">, " + QUERY_GENERIC + ">")
                    .addModifier(PUBLIC)
                    .addParameter(field.getCommonType(), name)
                    .setBody(new BlockStmt()
                            .addStatement("executor.identifier(name + \"." + name + "\", " + name + ");")
                            .addStatement(new ReturnStmt("executor")));
        }
    }

    private boolean checkQueryName(String entity, PrototypeField desc) {
        if (nonNull(desc.getPrototype())) {
            if (desc.getPrototype().getProperties().getEnrichers().stream().anyMatch(e -> QueryEnricher.class.equals(e.getClass())) ||
                    nonNull(desc.getPrototype().getBase()) &&
                            desc.getPrototype().getBase().getProperties().getInheritedEnrichers().stream().anyMatch(e -> QueryEnricher.class.equals(e.getClass()))) {
                if (getInterface(lookup.findGenerated(desc.getPrototype().getParsedFullName())).stream().noneMatch(n -> n instanceof ClassOrInterfaceDeclaration && QUERY_NAME.equals(((ClassOrInterfaceDeclaration) n).getNameAsString()))) {
                    declareQueryName(desc.getPrototype());
                }

                with(desc.getDeclaration().findCompilationUnit().get(), unit -> unit
                        .addImport("net.binis.codegen.spring.query.QueryEmbed")
                        .addImport("net.binis.codegen.creator.EntityCreator"));

                return true;
            }
        }
        return false;
    }

}
