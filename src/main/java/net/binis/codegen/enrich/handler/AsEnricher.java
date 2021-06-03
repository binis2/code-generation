package net.binis.codegen.enrich.handler;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.enrich.handler.base.BaseEnricher;

import static com.github.javaparser.ast.Modifier.Keyword.ABSTRACT;
import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
import static net.binis.codegen.generation.core.Constants.MIXIN_MODIFYING_METHOD_PREFIX;

public class AsEnricher extends BaseEnricher {

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        addAsMethod(description.getSpec(), true);
        addAsMethod(description.getIntf(), false);
    }

    @Override
    public int order() {
        return 0;
    }

    private static void addAsMethod(ClassOrInterfaceDeclaration spec, boolean isClass) {
        var method = spec
                .addMethod(MIXIN_MODIFYING_METHOD_PREFIX)
                .setType("T")
                .addParameter("Class<T>", "cls")
                .addTypeParameter("T");
        if (isClass) {
            method
                    .addModifier(PUBLIC)
                    .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName("cls.cast(this)"))));
        } else {
            method.setBody(null);
        }
    }

}
