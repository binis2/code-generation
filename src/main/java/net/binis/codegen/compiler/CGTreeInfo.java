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

import net.binis.codegen.compiler.base.JavaCompilerObject;

import java.lang.reflect.Method;

import static net.binis.codegen.tools.Reflection.*;

public class CGTreeInfo {

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.tree.TreeInfo");
    }

    protected static Method firstStartPos = findMethod("firstStatPos", theClass(), CGTree.theClass());;
    protected static Method endPos = findMethod("endPos", theClass(), CGTree.theClass());;
    protected static Method getStartPos = findMethod("getStartPos", theClass(), CGTree.theClass());;

    public static int firstStatPos(JavaCompilerObject tree) {
        return (int) invokeStatic(firstStartPos, tree.getInstance());
    }

    public static int endPos(JavaCompilerObject tree) {
        return (int) invokeStatic(endPos, tree.getInstance());
    }

    public static int getStartPos(JavaCompilerObject tree) {
        return (int) invokeStatic(getStartPos, tree.getInstance());
    }

}
