package net.binis.codegen.generation.core;

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

import lombok.Builder;
import lombok.Data;

import javax.lang.model.element.Element;
import java.util.*;

public class Parsables extends HashMap<String, Parsables.Entry> implements Iterable<Map.Entry<String, Parsables.Entry>> {

    public static Parsables create() {
        return new Parsables();
    }

    public Entry file(String source) {
        return computeIfAbsent(source, k -> new Entry());
    }

    @Override
    public Iterator<Map.Entry<String, Entry>> iterator() {
        return entrySet().iterator();
    }

    public static class Entry implements Iterable<Entry.Bag> {
        protected List<Entry.Bag> elements = new ArrayList<>();

        protected Entry() {
            //Do nothing
        }

        public void add(Element element, Object annotation, String fileName) {
            elements.add(Bag.builder().element(element).annotation(annotation).fileName(fileName).build());
        }

        public String getFileName() {
            if (elements.isEmpty()) {
                return "unknown";
            } else {
                return elements.get(0).getFileName();
            }
        }

        @Override
        public Iterator<Entry.Bag> iterator() {
            return elements.iterator();
        }

        public List<Entry.Bag> getElements() {
            return Collections.unmodifiableList(elements);
        }

        @Data
        @Builder
        public static class Bag {
            private Element element;
            private Object annotation;
            private String fileName;
        }

    }


}
