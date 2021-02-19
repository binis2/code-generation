package net.binis.codegen.enrich.handler;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import net.binis.codegen.codegen.interfaces.PrototypeDescription;
import net.binis.codegen.enrich.handler.base.BaseEnricher;

import static com.github.javaparser.ast.Modifier.Keyword.*;

public class QueryEnricher extends BaseEnricher {

    private static final String QUERY_START = "QueryStart";
    private static final String QUERY_SELECT = "QuerySelect";
    private static final String QUERY_ORDER = "QueryOrder";
    private static final String QUERY_EXECUTE = "QueryExecute";
    private static final String QUERY_GENERIC = "QR";

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var properties = description.getProperties();
        var spec = getImplementation(description);
        var intf = getInterface(description);

        addFindMethod(intf);
        addQueryStartIntf(intf);
        addQuerySelectAndOrderIntf(intf, spec);

    }

    private void addQuerySelectAndOrderIntf(ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration spec) {
        intf.findCompilationUnit().get().addImport("net.binis.codegen.spring.query.QueryExecute");

        var select = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_SELECT)
                .addExtendedType(QUERY_EXECUTE + "<" + QUERY_GENERIC + ">")
                .addTypeParameter(QUERY_GENERIC);
        select.addMethod("order").setType(QUERY_ORDER + "<" + QUERY_GENERIC + ">").setBody(null);
        intf.addMember(select);

        var order = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_ORDER)
                .addExtendedType(QUERY_EXECUTE + "<" + QUERY_GENERIC + ">")
                .addTypeParameter(QUERY_GENERIC);
        intf.addMember(order);

        spec.findAll(FieldDeclaration.class).forEach(field -> {
            var name = field.getVariable(0).getNameAsString();
            select.addMethod(name)
                    .setType(QUERY_SELECT + "Operation<" + QUERY_GENERIC +", " + QUERY_SELECT + "<" + QUERY_GENERIC + ">>")
                    .setBody(null)
            .addParameter(field.getCommonType(), name);

            order.addMethod(name)
                    .setType(QUERY_ORDER + "Operation<" + QUERY_GENERIC +", " + QUERY_ORDER + "<" + QUERY_GENERIC + ">>")
                    .setBody(null)
                    .addParameter(field.getCommonType(), name);

        });
    }

    private void addQueryStartIntf(ClassOrInterfaceDeclaration intf) {
        var start = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, QUERY_START);
        intf.addMember(start);

        start.addMethod("by").setType(QUERY_SELECT + "<" + intf.getNameAsString() + ">").setBody(null);
        start.addMethod("all").setType(QUERY_SELECT + "<List<" + intf.getNameAsString() + ">>").setBody(null);
        start.addMethod("count").setType(QUERY_SELECT + "<Long>").setBody(null);
    }

    private void addFindMethod(ClassOrInterfaceDeclaration intf) {
        intf.addMethod("find", STATIC)
                .setType(QUERY_START)
                .setBody(new BlockStmt().addStatement(new ReturnStmt("new " + QUERY_START + "()")));

    }

}
