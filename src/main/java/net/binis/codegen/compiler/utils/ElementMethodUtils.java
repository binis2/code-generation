package net.binis.codegen.compiler.utils;

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
