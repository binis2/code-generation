package net.binis.codegen.generation.core;

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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import net.binis.codegen.generation.core.interfaces.PrototypeData;

import static net.binis.codegen.annotation.type.GenerationStrategy.PLAIN;
import static net.binis.codegen.annotation.type.GenerationStrategy.PROTOTYPE;
import static net.binis.codegen.tools.Tools.in;

public class ErrorHelpers {

    public static String calculatePrototypeAnnotationError(ClassOrInterfaceDeclaration type, PrototypeData properties) {
        if (in(properties.getStrategy(), PROTOTYPE, PLAIN) && !type.isInterface()) {
            return "@" + properties.getPrototypeAnnotation().getSimpleName() + " is allowed only on interfaces!";
        }
        return null;
    }
    private ErrorHelpers() {
        //Do nothing
    }

}
