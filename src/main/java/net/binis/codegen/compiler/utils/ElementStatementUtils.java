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

public class ElementStatementUtils extends ElementUtils {

    public static CGTry surroundWithTryCatch(CGBlock block, Class<? extends Throwable> exception, CGBlock exceptionBlock) {
        return surroundWithTryCatch(block, exception, exceptionBlock, null);
    }

    public static CGTry surroundWithTryCatch(CGBlock block, Class<? extends Throwable> exception, CGBlock exceptionBlock, CGBlock finallyBlock) {
        var maker = TreeMaker.create();
        var list = CGList.nil(CGCatch.class).append(maker.Catch(maker.VarDef(maker.Modifiers(CGFlags.PARAMETER), maker.toName("ex"), classToExpression(exception), null), exceptionBlock));
        return maker.Try(block, list, finallyBlock);
    }


}