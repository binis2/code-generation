package net.binis.codegen.compiler.utils;

import net.binis.codegen.compiler.*;

public class ElementMethodUtils extends ElementUtils {

    public static CGExpression createStaticMethodInvocation(Class<?> cls, String methodName, CGExpression... params) {
        return createStaticMethodInvocation(cls.getCanonicalName(), methodName, params);
    }

    public static CGExpression createStaticMethodInvocation(String cls, String methodName, CGExpression... params) {
        var maker = TreeMaker.create();
        var method = chainDotsString(maker, cls + "." + methodName);
        return maker.Apply(CGList.nil(CGExpression.class), method, CGList.from(params, CGExpression.class));
    }

    public static CGMethodInvocation createClassMethodInvocation(Class<?> cls, String methodName, CGExpression... params) {
        var maker = TreeMaker.create();
        var method = maker.Select(toType(cls), maker.toName(methodName));
        return maker.Apply(CGList.nil(CGExpression.class), method, CGList.from(params, CGExpression.class));
    }

    public static CGMethodInvocation createClassMethodInvocation(String cls, String methodName, CGExpression... params) {
        var maker = TreeMaker.create();
        var method = chainDotsString(maker, cls + ".class." + methodName);
        return maker.Apply(CGList.nil(CGExpression.class), method, CGList.from(params, CGExpression.class));
    }

}
