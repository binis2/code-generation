package net.binis.codegen.tools;

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

import java.util.List;

import static java.util.Objects.isNull;

public class ContextInterpolator extends BaseStringInterpolator<ContextInterpolator.Context> {

    protected final Context context;

    public ContextInterpolator(Context context) {
        this.context = context;
    }

    public static ContextInterpolator of(Context context) {
        return new ContextInterpolator(context);
    }

    public String interpolate(String string) {
        return buildExpression(string).interpolate(string);
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
        var param = context.interpolate(exp);
        if (isNull(param)) {
            param = exp;
        }

        return buildConstantExpression("{" + param + "}");
    }

    @FunctionalInterface
    public interface Context {
        String interpolate(String message);
    }

}
