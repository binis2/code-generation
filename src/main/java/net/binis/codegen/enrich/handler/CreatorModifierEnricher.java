package net.binis.codegen.enrich.handler;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import net.binis.codegen.codegen.interfaces.PrototypeDescription;
import net.binis.codegen.enrich.handler.base.BaseEnricher;

import static com.github.javaparser.ast.Modifier.Keyword.STATIC;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class CreatorModifierEnricher extends BaseEnricher {

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var properties = description.getProperties();
        var spec = getImplementation(description);
        var intf = getInterface(description);
        var modifier = description.getModifierIntf();

        var creatorClass = "EntityCreatorModifier";

        spec.findCompilationUnit().get().addImport(creatorClass);

        if (properties.isGenerateModifier()) {
            var type = intf.getNameAsString() + "." + modifier.getNameAsString();
            if (isNull(properties.getMixInClass())) {
                intf.addMethod("create", STATIC)
                        .setType(type)
                        .setBody(new BlockStmt().addStatement(new ReturnStmt("(" + type + ") " + creatorClass + ".create(" + intf.getNameAsString() + ".class).with()")));
            } else {
                intf.addMethod("create", STATIC)
                        .setType(type)
                        .setBody(new BlockStmt().addStatement(new ReturnStmt("((" + intf.getNameAsString() + ") " + creatorClass + ".create(" + intf.getNameAsString() + ".class)).as" + intf.getNameAsString() + "()")));
            }
        } else {
            creatorClass = "EntityCreator";
            intf.addMethod("create", STATIC)
                    .setType(intf.getNameAsString())
                    .setBody(new BlockStmt().addStatement(new ReturnStmt(creatorClass + ".create(" + intf.getNameAsString() + ".class)")));
        }

        intf.findCompilationUnit().get().addImport("net.binis.codegen.creator." + creatorClass);

        if (!properties.isBase()) {
            var embedded = description.getEmbeddedModifier();

            var type = spec;
            if (nonNull(properties.getMixInClass())) {
                type = description.getMixIn().getSpec();
            }
            type.findCompilationUnit().get().addImport("net.binis.codegen.factory.CodeFactory");
            var typeName = type.getNameAsString();
            var initializer = type.getChildNodes().stream().filter(n -> n instanceof InitializerDeclaration).map(n -> ((InitializerDeclaration) n).asInitializerDeclaration().getBody()).findFirst().orElseGet(type::addInitializer);
            initializer
                    .addStatement(new MethodCallExpr()
                            .setName("CodeFactory.registerType")
                            .addArgument(intf.getNameAsString() + ".class")
                            .addArgument(typeName + "::new")
                            .addArgument(nonNull(embedded) ? "(p, v) -> new " + embedded.getNameAsString() + "<>(p, (" + typeName + ") v)" : "null"));
        }
    }

}