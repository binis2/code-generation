package net.binis.codegen.compiler;

import com.sun.source.util.Trees;
import net.binis.codegen.compiler.base.BaseJavaCompilerObject;

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
        //TODO: Optimize it!
        var method = findMethod("QualIdent", instance.getClass(), CGSymbol.theClass());
        return new CGExpression(invoke(method, instance, sym.getInstance()));
    }

    public CGAnnotation Annotation(CGExpression annotationType, CGList<CGExpression> args) {
        //TODO: Optimize it!
        var method = findMethod("Annotation", instance.getClass(), loadClass("com.sun.tools.javac.tree.JCTree"), CGList.theClass());
        return new CGAnnotation(invoke(method, instance, annotationType.getInstance(), args.getInstance()));
    }

    public CGAssign Assign(CGExpression lhs, CGExpression rhs) {
        //TODO: Optimize it!
        var method = findMethod("Assign", instance.getClass(), CGExpression.theClass(), CGExpression.theClass());
        return new CGAssign(invoke(method, instance, lhs.getInstance(), rhs.getInstance()));
    }

    public CGIdent Ident(Name name) {
        //TODO: Optimize it!
        var method = findMethod("Ident", instance.getClass(), Name.theClass());
        return new CGIdent(invoke(method, instance, name.getInstance()));
    }

    public CGLiteral Literal(CGTypeTag tag, Object value) {
        //TODO: Optimize it!
        var method = findMethod("Literal", instance.getClass(), CGTypeTag.theClass(), Object.class);
        return new CGLiteral(invoke(method, instance, tag.getInstance(), value));
    }

    public CGFieldAccess Select(CGExpression selected, Name selector) {
        //TODO: Optimize it!
        var method = findMethod("Select", instance.getClass(), CGExpression.theClass(), Name.theClass());
        return new CGFieldAccess(invoke(method, instance, selected.getInstance(), selector.getInstance()));
    }


    public CGSymbol getSymbol(String className) {
        //TODO: Optimize it!
        var compilerClass = loadClass("com.sun.tools.javac.main.JavaCompiler");
        var compiler = invokeStatic("instance", compilerClass, context);
        return new CGSymbol(invoke("resolveBinaryNameOrIdent", compiler, className));
    }

}
