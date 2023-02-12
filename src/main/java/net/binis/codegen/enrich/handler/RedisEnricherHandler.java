package net.binis.codegen.enrich.handler;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 Binis Belev
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import net.binis.codegen.enrich.Enricher;
import net.binis.codegen.enrich.ModifierEnricher;
import net.binis.codegen.enrich.RedisEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;

import java.io.Serializable;
import java.util.List;

import static com.github.javaparser.ast.Modifier.Keyword.*;
import static net.binis.codegen.generation.core.EnrichHelpers.*;
import static net.binis.codegen.generation.core.Helpers.getCustomValue;

public class RedisEnricherHandler extends BaseEnricher implements RedisEnricher {

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var intf = description.getInterface();
        var impl = description.getImplementation();
        impl.addImplementedType(Serializable.class);

        addKey(intf, impl);
        addPrefix(description, impl);
        addConstructor(description, intf, impl);
        addLoad(description, intf);
        Helpers.addDefaultCreation(description, null);
        Helpers.addInitializer(description, intf, (LambdaExpr) expression("params -> new " + impl.getNameAsString() + "((String) params[0])"), false);
    }

    private void addConstructor(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration impl) {
        description.getInterfaceUnit().addImport(CodeFactory.class);
        intf.addMethod("create", STATIC)
                .addParameter(String.class, "key")
                .setType(intf.getNameAsString() + ".Modify")
                .setBody(new BlockStmt().addStatement(new ReturnStmt("CodeFactory.create(" + intf.getNameAsString() + ".class, key).with()")));

        impl.addConstructor(PUBLIC).addParameter(String.class, "key").setBody(block("{ _key = key; }"));
    }

    private void addLoad(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration intf) {
        description.getInterfaceUnit().addImport("net.binis.codegen.redis.Redis");
        intf.addMethod("load", STATIC)
                .addParameter(String.class, "key")
                .setType(intf.getNameAsString())
                .setBody(new BlockStmt().addStatement(new ReturnStmt("Redis.load(key, " + intf.getNameAsString() + ".class)")));

        intf.addMethod("load", STATIC)
                .setType(intf.getNameAsString())
                .setBody(new BlockStmt().addStatement(new ReturnStmt("Redis.load(" + intf.getNameAsString() + ".class)")));
    }

    private void addKey(ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration impl) {
        intf.addMethod("key").setType(String.class).setBody(null);
        impl.addProtectedField(String.class, "_key");
        impl.addMethod("key", PUBLIC).setType(String.class)
                .setBody(returnBlock("PREFIX + _key"));
    }

    private void addPrefix(PrototypeDescription<ClassOrInterfaceDeclaration> description, ClassOrInterfaceDeclaration impl) {
        var value = getCustomValue("prefix", description.getProperties());
        var prefix = value instanceof String s ? s : "";
        impl.addField(String.class, "PREFIX", PUBLIC, STATIC, FINAL).getVariable(0).setInitializer('"' + prefix + '"');
    }

    @Override
    public int order() {
        return 500;
    }

    @Override
    public List<Class<? extends Enricher>> dependencies() {
        return List.of(ModifierEnricher.class);
    }

}
