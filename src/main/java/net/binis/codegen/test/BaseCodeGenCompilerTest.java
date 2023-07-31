package net.binis.codegen.test;

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

import com.github.javaparser.ast.body.TypeDeclaration;
import net.binis.codegen.CodeGen;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.Structures;
import net.binis.codegen.objects.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class BaseCodeGenCompilerTest extends BaseCodeTest {

    @BeforeEach
    public void beforeEach() {
        Helpers.cleanUp();
        CodeFactory.registerType(BaseCodeGenCompilerTest.class, () -> this);
    }

    @AfterEach
    public void afterEach() {
        CodeFactory.unregisterType(BaseCodeGenCompilerTest.class);
    }

    public TestClassLoader testSingle(String path) {
        var list = newList();
        load(list, path);
        TestClassLoader loader = new TestClassLoader();
        assertTrue(compile(loader, list, null, true,"-Xplugin:BinisCodeGen"));
        return loader;
    }

    public Class<?> testSingle(String path, String cls) {
        var loader = testSingle(path);
        return loader.findClass(cls);
    }

    public TestClassLoader testMulti(String... paths) {
        var list = newList();
        for (var path : paths) {
            load(list, path);
        }
        TestClassLoader loader = new TestClassLoader();
        assertTrue(compile(loader, list, null, true,"-Xplugin:BinisCodeGen"));
        return loader;
    }



}
