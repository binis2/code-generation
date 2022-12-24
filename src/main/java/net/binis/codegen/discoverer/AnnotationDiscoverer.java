package net.binis.codegen.discoverer;

import com.google.common.collect.Sets;
import com.google.common.io.Closer;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class AnnotationDiscoverer {

    public static final String TEMPLATE = "template";
    protected static String RESOURCE_PATH = "binis/annotations";

    protected static List<InputStream> loadResources(
            final String name, final ClassLoader classLoader) throws IOException {
        var list = new ArrayList<InputStream>();
        var systemResources =
                (classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader)
                        .getResources(name);
        while (systemResources.hasMoreElements()) {
            list.add(systemResources.nextElement().openStream());
        }
        return list;
    }

    public static List<DiscoveredAnnotation> findAnnotations() {
        var result = new ArrayList<DiscoveredAnnotation>();
        try {
            loadResources(RESOURCE_PATH, AnnotationDiscoverer.class.getClassLoader()).forEach(s -> AnnotationDiscoverer.processResource(s, result));
        } catch (Exception e) {
            log.error("Unable to discover annotations!");
            loadDefault(result);
        }
        return result;
    }

    private static void loadDefault(List<DiscoveredAnnotation> list) {
        list.add(DiscoveredAnnotation.builder().type(TEMPLATE).name("net.binis.codegen.annotation.CodePrototype").build());
        list.add(DiscoveredAnnotation.builder().type(TEMPLATE).name("net.binis.codegen.annotation.EnumPrototype").build());
        list.add(DiscoveredAnnotation.builder().type(TEMPLATE).name("net.binis.codegen.annotation.builder.CodeBuilder").build());
        list.add(DiscoveredAnnotation.builder().type(TEMPLATE).name("net.binis.codegen.spring.annotation.builder.CodeQueryBuilder").build());
        list.add(DiscoveredAnnotation.builder().type(TEMPLATE).name("net.binis.codegen.annotation.builder.CodeValidationBuilder").build());
        list.add(DiscoveredAnnotation.builder().type(TEMPLATE).name("net.binis.codegen.annotation.builder.CodeRequest").build());
    }

    @SuppressWarnings("unchecked")
    protected static void processResource(InputStream stream, List<DiscoveredAnnotation> annotations) {
        var reader = new BufferedReader(new InputStreamReader(stream));
        try {
            while (reader.ready()) {
                var line = reader.readLine();
                var parts = line.split(":");
                if (parts.length == 2) {
                    if (TEMPLATE.equals(parts[0])) {
                        var cls = loadClass(parts[1]);
                        if (nonNull(cls)) {
                            if (Annotation.class.isAssignableFrom(cls)) {
                                annotations.add(DiscoveredAnnotation.builder().type(parts[0]).name(parts[1]).cls((Class<? extends Annotation>) cls).build());
                            }
                        } else {
                            log.warn("Can't load class: {}!", parts[1]);
                        }
                    } else {
                        log.warn("Invalid descriptor type: {}!", parts[0]);
                    }
                } else {
                    log.warn("Invalid descriptor line: {}!", line);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to process stream!", e);
        }
    }

    public static void writeTemplate(Filer filer, String className) {
        try {
            SortedSet<String> allServices = Sets.newTreeSet();
            try {
                var existingFile =
                        filer.getResource(StandardLocation.CLASS_OUTPUT, "", RESOURCE_PATH);
                Set<String> oldServices = readServiceFile(existingFile.openInputStream());
                allServices.addAll(oldServices);
            } catch (IOException e) {
                //No old file present.
            }

            allServices.add(TEMPLATE + ":" + className);

            FileObject fileObject =
                    filer.createResource(StandardLocation.CLASS_OUTPUT, "", RESOURCE_PATH);
            try (OutputStream out = fileObject.openOutputStream()) {
                writeServiceFile(allServices, out);
            }
        } catch (IOException e) {
            log.error("Failed to write annotations resource!", e);
        }
    }

    protected static Set<String> readServiceFile(InputStream input) throws IOException {
        var serviceClasses = new HashSet<String>();
        var closer = Closer.create();
        try {
            BufferedReader r = closer.register(new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)));
            String line;
            while ((line = r.readLine()) != null) {
                int commentStart = line.indexOf('#');
                if (commentStart >= 0) {
                    line = line.substring(0, commentStart);
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    serviceClasses.add(line);
                }
            }
            return serviceClasses;
        } catch (Throwable t) {
            throw closer.rethrow(t);
        } finally {
            closer.close();
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


    @Builder
    @Data
    public static class DiscoveredAnnotation {
        protected String type;
        protected String name;
        protected Class<? extends Annotation> cls;
    }
}
