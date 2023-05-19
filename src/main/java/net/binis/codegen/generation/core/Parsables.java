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

import javax.lang.model.element.Element;
import java.util.*;

public class Parsables extends HashMap<String, Parsables.Entry> implements Iterable<Map.Entry<String, Parsables.Entry>> {

    public static Parsables create() {
        return new Parsables();
    }

    public Entry file(String name) {
        return computeIfAbsent(name, k -> new Entry());
    }

    @Override
    public Iterator<Map.Entry<String, Entry>> iterator() {
        return entrySet().iterator();
    }

    public static class Entry implements Iterable<Element> {
        protected List<Element> elements = new ArrayList<>();

        protected Entry() {
            //Do nothing
        }

        public void add(Element element) {
            elements.add(element);
        }

        @Override
        public Iterator<Element> iterator() {
            return elements.iterator();
        }

        public List<Element> getElements() {
            return Collections.unmodifiableList(elements);
        }
    }

}
