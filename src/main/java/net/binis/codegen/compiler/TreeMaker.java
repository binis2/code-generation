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

import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;
import net.binis.codegen.compiler.base.JavaCompilerObject;
import net.binis.codegen.factory.CodeFactory;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.*;

public class TreeMaker extends JavaCompilerObject {

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
        if (nonNull(env)) {
            trees = Trees.instance(env);
        } else {
            trees = Trees.instance(CodeFactory.create(JavacTask.class));
        }
    }

    public Trees getTrees() {
        return trees;
    }

    public TreeMaker at(int pos) {
        invoke("at", instance, pos);
        return this;
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

    public CGIdent Ident(CGName name) {
        var method = findMethod("Ident", instance.getClass(), CGName.theClass());
        return new CGIdent(invoke(method, instance, name.getInstance()));
    }

    public CGLiteral Literal(CGTypeTag tag, Object value) {
        var method = findMethod("Literal", instance.getClass(), CGTypeTag.theClass(), Object.class);
        return new CGLiteral(invoke(method, instance, tag.getInstance(), value));
    }

    public CGFieldAccess Select(CGExpression selected, CGName selector) {
        var method = findMethod("Select", instance.getClass(), CGExpression.theClass(), CGName.theClass());
        return new CGFieldAccess(invoke(method, instance, selected.getInstance(), selector.getInstance()));
    }

    public CGExpression Select(CGExpression base, CGSymbol sym) {
        var method = findMethod("Select", instance.getClass(), CGExpression.theClass(), CGSymbol.theClass());
        return new CGFieldAccess(invoke(method, instance, base.getInstance(), sym.getInstance()));
    }

    public CGStatement Call(CGExpression apply) {
        var method = findMethod("Call", instance.getClass(), CGExpression.theClass());
        return new CGStatement(invoke(method, instance, apply.getInstance()));
    }

    public CGStatement Assignment(CGSymbol v, CGExpression rhs) {
        var method = findMethod("Assignment", instance.getClass(), CGSymbol.theClass(), CGExpression.theClass());
        return new CGStatement(invoke(method, instance, v.getInstance(), rhs.getInstance()));
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

    public CGArrayTypeTree TypeArray(CGExpression elemtype) {
        var method = findMethod("TypeArray", instance.getClass(), CGExpression.theClass());
        return new CGArrayTypeTree(invoke(method, instance, elemtype.getInstance()));
    }

    public CGTypeCast TypeCast(CGTree clazz, CGExpression expr) {
        var method = findMethod("TypeCast", instance.getClass(), CGTree.theClass(), CGExpression.theClass());
        return new CGTypeCast(invoke(method, instance, clazz.getInstance(), expr.getInstance()));
    }

    public CGVariableDecl VarDef(CGModifiers mods, CGName name, CGExpression vartype, CGExpression init) {
        var method = findMethod("VarDef", instance.getClass(), CGModifiers.theClass(), CGName.theClass(), CGExpression.theClass(), CGExpression.theClass());
        return new CGVariableDecl(invoke(method, instance, mods.getInstance(), name.getInstance(), vartype.getInstance(), nonNull(init) ? init.getInstance() : null));
    }

    public CGVariableDecl VarDef(CGModifiers mods, CGName name, CGExpression vartype, CGExpression init, boolean declaredUsingVar) {
        var method = findMethod("VarDef", instance.getClass(), CGModifiers.theClass(), CGName.theClass(), CGExpression.theClass(), CGExpression.theClass(), boolean.class);
        return new CGVariableDecl(invoke(method, instance, mods.getInstance(), name.getInstance(), vartype.getInstance(), init.getInstance(), declaredUsingVar));
    }

    public CGVariableDecl ReceiverVarDef(CGModifiers mods, CGName name, CGExpression vartype) {
        var method = findMethod("ReceiverVarDef", instance.getClass(), CGModifiers.theClass(), CGName.theClass(), CGExpression.theClass());
        return new CGVariableDecl(invoke(method, instance, mods.getInstance(), name.getInstance(), vartype.getInstance()));
    }

    public CGVariableDecl VarDef(CGVarSymbol symbol, CGExpression init) {
        var method = findMethod("VarDef", instance.getClass(), CGVarSymbol.theClass(), CGExpression.theClass());
        return new CGVariableDecl(invoke(method, instance, symbol.getInstance(), nonNull(init) ? init.getInstance() : null));
    }

    public CGVariableDecl Param(CGName name, CGType argtype, CGSymbol owner) {
        var method = findMethod("Param", instance.getClass(), CGName.theClass(), CGType.theClass(), CGSymbol.theClass());
        return new CGVariableDecl(invoke(method, instance, name.getInstance(), argtype.getInstance(), owner.getInstance()));
    }

    public CGMethodDeclaration MethodDef(CGModifiers mods,
                                         CGName name,
                                         CGExpression restype,
                                         CGList<CGTypeParameter> typarams,
                                         CGList<CGVariableDecl> params,
                                         CGList<CGExpression> thrown,
                                         CGBlock body,
                                         CGExpression defaultValue) {
        var method = findMethod("MethodDef", instance.getClass(), CGModifiers.theClass(), CGName.theClass(), CGExpression.theClass(), CGList.theClass(), CGList.theClass(), CGList.theClass(), CGBlock.theClass(), CGExpression.theClass());
        return new CGMethodDeclaration(invoke(method, instance, mods.getInstance(), name.getInstance(), nonNull(restype) ? restype.getInstance() : null, typarams.getInstance(), params.getInstance(), thrown.getInstance(), body.getInstance(), nonNull(defaultValue) ? defaultValue.getInstance() : null));
    }

    public CGModifiers Modifiers(long flags, CGList<CGAnnotation> annotations) {
        var method = findMethod("Modifiers", instance.getClass(), long.class, CGList.theClass());
        return new CGModifiers(invoke(method, instance, flags, nonNull(annotations) ? annotations.getInstance() : CGList.nil(CGAnnotation.class)), true);
    }

    public CGModifiers Modifiers(long flags) {
        var method = findMethod("Modifiers", instance.getClass(), long.class);
        return new CGModifiers(invoke(method, instance, flags), true);
    }

    public CGBlock Block(long flags, CGList<CGStatement> stats) {
        var method = findMethod("Block", instance.getClass(), long.class, CGList.theClass());
        return new CGBlock(invoke(method, instance, flags, stats.getInstance()));
    }

    public CGSymbol getSymbol(String className) {
        var compilerClass = loadClass("com.sun.tools.javac.main.JavaCompiler");
        var compiler = invokeStatic("instance", compilerClass, context);
        return new CGSymbol(invoke("resolveBinaryNameOrIdent", compiler, className));
    }

    public CGMethodInvocation Apply(CGList<CGExpression> typeargs, CGExpression fn, CGList<CGExpression> args)
    {
        var method = findMethod("Apply", instance.getClass(), CGList.theClass(), CGExpression.theClass(), CGList.theClass());
        return new CGMethodInvocation(invoke(method, instance, typeargs.getInstance(), fn.getInstance(), args.getInstance()));
    }

    public CGExpression Type(CGType type) {
        var method = findMethod("Type", instance.getClass(), CGType.theClass());
        return new CGExpression(invoke(method, instance, type.getInstance()));
    }

    public CGTypeApply TypeApply(CGExpression clazz, CGList<CGExpression> arguments) {
        var method = findMethod("TypeApply", instance.getClass(), CGExpression.theClass(), CGList.theClass());
        return new CGTypeApply(invoke(method, instance, clazz.getInstance(), arguments.getInstance()));
    }

    public CGReturn Return(CGExpression expr) {
        var method = findMethod("Return", instance.getClass(), CGExpression.theClass());
        return new CGReturn(invoke(method, instance, expr.getInstance()));
    }


}
