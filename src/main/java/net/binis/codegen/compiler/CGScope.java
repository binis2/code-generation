package net.binis.codegen.compiler;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2026 Binis Belev
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

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.JavaCompilerObject;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.tools.Reflection;

import java.lang.reflect.Method;
import java.util.Iterator;

import static java.util.Objects.isNull;
import static net.binis.codegen.tools.Reflection.invoke;
import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
@SuppressWarnings("unchecked")
public class CGScope extends JavaCompilerObject {

    protected static Method _getSymbols;
    protected static Method _getSymbolsByName;

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.code.Scope");
    }

    public CGScope(Object instance) {
        super();
        this.instance = instance;
    }

    @Override
    protected void init() {
        cls = theClass();
    }

    public Iterator<CGSymbol> getSymbols() {
        if (isNull(_getSymbols)) {
            _getSymbols = Reflection.findMethod("getSymbols", cls);
        }
        return new ScopeIterator(((Iterable)invoke(_getSymbols, instance)).iterator());
    }


    public Iterator<CGSymbol> getSymbolsByName(String name) {
        if (isNull(_getSymbolsByName)) {
            _getSymbolsByName = Reflection.findMethod("getSymbolsByName", cls, CGName.theClass());
        }
        return new ScopeIterator(((Iterable)invoke(_getSymbolsByName, instance, CGName.create(name).getInstance())).iterator());
    }

    public static class ScopeIterator implements Iterator<CGSymbol> {

        protected final Iterator it;

        public ScopeIterator(Iterator it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public CGSymbol next() {
            var obj = it.next();
            return (CGSymbol) CodeFactory.create(obj.getClass(), obj);
        }
    }

}
