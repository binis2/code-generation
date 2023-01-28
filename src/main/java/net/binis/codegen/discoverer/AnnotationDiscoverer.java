package net.binis.codegen.discoverer;

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

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.discovery.Discoverer;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Objects.isNull;

@Slf4j
public abstract class AnnotationDiscoverer extends Discoverer {

    private static InputStream read;
    private static OutputStream write;
    private static final SortedSet<String> allServices = Sets.newTreeSet();

    private static void loadDefault(List<DiscoveredService> list) {
        list.add(DiscoveredService.builder().type(TEMPLATE).name("net.binis.codegen.annotation.CodePrototype").build());
        list.add(DiscoveredService.builder().type(TEMPLATE).name("net.binis.codegen.annotation.EnumPrototype").build());
        list.add(DiscoveredService.builder().type(TEMPLATE).name("net.binis.codegen.annotation.builder.CodeBuilder").build());
        list.add(DiscoveredService.builder().type(TEMPLATE).name("net.binis.codegen.spring.annotation.builder.CodeQueryBuilder").build());
        list.add(DiscoveredService.builder().type(TEMPLATE).name("net.binis.codegen.annotation.builder.CodeValidationBuilder").build());
        list.add(DiscoveredService.builder().type(TEMPLATE).name("net.binis.codegen.annotation.builder.CodeRequest").build());
    }

    public static List<DiscoveredService> findAnnotations() {
        var result = new ArrayList<DiscoveredService>();
        try {
            loadResources(RESOURCE_PATH, Discoverer.class.getClassLoader()).forEach(s -> Discoverer.processResource(s, result));
        } catch (Exception e) {
            log.error("Unable to discover annotations!");
            loadDefault(result);
        }
        return result;
    }


    public static void writeTemplate(Filer filer, String className) {
        writeEntry(TEMPLATE, filer, className);
    }

    public static void writeConfig(Filer filer, String className) {
        writeEntry(CONFIG, filer, className);
    }

    public static void writeEntry(String type, Filer filer, String className) {
        try {
            if (isNull(read)) {
                try {
                    var file = filer.getResource(StandardLocation.CLASS_OUTPUT, "", RESOURCE_PATH);
                    read = file.openInputStream();
                    Set<String> oldServices = readServiceFile(read);
                    allServices.addAll(oldServices);
                } catch (IOException e) {
                    //No old file present.
                }
            }

            var service = type + ":" + className;

            allServices.add(service);

            if (isNull(write)) {
                write = filer.createResource(StandardLocation.CLASS_OUTPUT, "", RESOURCE_PATH).openOutputStream();
            }
            writeService(service, write);
        } catch (IOException e) {
            log.error("Failed to write annotations resource!", e);
        }
    }

    protected static void writeServiceFile(Collection<String> services, OutputStream output) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        for (String service : services) {
            writer.write(service);
            writer.newLine();
        }
        writer.flush();
    }

    protected static void writeService(String service, OutputStream output) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        writer.write(service);
        writer.newLine();
        writer.flush();
    }


}
