package net.binis.codegen.compiler;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2023 Binis Belev
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

import com.sun.source.util.Trees;
import net.binis.codegen.compiler.base.BaseJavaCompilerObject;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.*;

public class TreeMaker extends BaseJavaCompilerObject {

    public static TreeMaker create() {
        return new TreeMaker();
    }

    public TreeMaker() {
        super();
    }

    protected Trees trees;

    @Override
    protected void init() {
        cls = loadClass("com.sun.tools.javac.tree.TreeMaker");
        instance = invokeStatic("instance", cls, context);
        trees = Trees.instance(env);
    }

    public Trees getTrees() {
        return trees;
    }

    public CGExpression QualIdent(CGSymbol sym) {
        var method = findMethod("QualIdent", instance.getClass(), CGSymbol.theClass());
        return new CGExpression(invoke(method, instance, sym.getInstance()));
    }

    public CGAnnotation Annotation(CGExpression annotationType, CGList<CGExpression> args) {
        var method = findMethod("Annotation", instance.getClass(), CGTree.theClass(), CGList.theClass());
        return new CGAnnotation(invoke(method, instance, annotationType.getInstance(), args.getInstance()));
    }

    public CGAssign Assign(CGExpression lhs, CGExpression rhs) {
        var method = findMethod("Assign", instance.getClass(), CGExpression.theClass(), CGExpression.theClass());
        return new CGAssign(invoke(method, instance, lhs.getInstance(), rhs.getInstance()));
    }

    public CGIdent Ident(Name name) {
        var method = findMethod("Ident", instance.getClass(), Name.theClass());
        return new CGIdent(invoke(method, instance, name.getInstance()));
    }

    public CGLiteral Literal(CGTypeTag tag, Object value) {
        var method = findMethod("Literal", instance.getClass(), CGTypeTag.theClass(), Object.class);
        return new CGLiteral(invoke(method, instance, tag.getInstance(), value));
    }

    public CGFieldAccess Select(CGExpression selected, Name selector) {
        var method = findMethod("Select", instance.getClass(), CGExpression.theClass(), Name.theClass());
        return new CGFieldAccess(invoke(method, instance, selected.getInstance(), selector.getInstance()));
    }

    public CGExpression Select(CGExpression base, CGSymbol sym) {
        var method = findMethod("Select", instance.getClass(), CGExpression.theClass(), CGSymbol.theClass());
        return new CGFieldAccess(invoke(method, instance, base.getInstance(), sym.getInstance()));
    }

    public CGNewArray NewArray(CGExpression elemtype,
                               CGList<CGExpression> dims,
                               CGList<CGExpression> elems) {
        var method = findMethod("NewArray", instance.getClass(), CGExpression.theClass(), CGList.theClass(), CGList.theClass());
        return new CGNewArray(invoke(method, instance, nonNull(elemtype) ? elemtype.getInstance() : null, dims.getInstance(), elems.getInstance()));
    }

    public CGPrimitiveTypeTree TypeIdent(CGTypeTag typetag) {
        var method = findMethod("TypeIdent", instance.getClass(), CGTypeTag.theClass());
        return new CGPrimitiveTypeTree(invoke(method, instance, typetag.getInstance()));
    }

    public CGTypeCast TypeCast(CGTree clazz, CGExpression expr) {
        var method = findMethod("TypeCast", instance.getClass(), CGTree.theClass(), CGExpression.theClass());
        return new CGTypeCast(invoke(method, instance, clazz.getInstance(), expr.getInstance()));
    }

    public CGSymbol getSymbol(String className) {
        var compilerClass = loadClass("com.sun.tools.javac.main.JavaCompiler");
        var compiler = invokeStatic("instance", compilerClass, context);
        return new CGSymbol(invoke("resolveBinaryNameOrIdent", compiler, className));
    }

}
