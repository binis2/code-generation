package net.binis.codegen.tools;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2024 Binis Belev
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

import java.util.List;
import java.util.Map;

public class Interpolator extends BaseStringInterpolator<Interpolator.Context> {

    protected Context context;
    protected Interpolator.Context expression;

    public static Interpolator build(String string) {
        var result = new Interpolator();
        result.expression = result.buildExpression(string);
        return result;
    }

    public static Interpolator build(char identifier, String string) {
        var result = new Interpolator();
        result.identifier = identifier;
        result.expression = result.buildExpression(string);
        return result;
    }


    public Built with(Context context) {
        this.context = context;
        return () -> expression.interpolate("");
    }

    public Built params(Map<String, Object> map) {
        this.context = param -> map.getOrDefault(param, param).toString();
        return () -> expression.interpolate("");
    }


    protected Context buildConstantExpression(String exp) {
        return message -> exp;
    }

    protected Context buildComplexExpression(List<Context> list) {
        return message -> {
            var result = new StringBuilder();
            for (var exp : list) {
                result.append(exp.interpolate(message));
            }
            return result.toString();
        };
    }

    protected Context buildParamExpression(String exp) {
        return message ->
            context.interpolate(exp);
    }

    @FunctionalInterface
    public interface Context {
        String interpolate(String message);
    }

    @FunctionalInterface
    public interface Built {
        String interpolate();
    }

}
