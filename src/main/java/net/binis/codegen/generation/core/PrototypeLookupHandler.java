package net.binis.codegen.generation.core;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 Binis Belev
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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.enrich.CustomDescription;
import net.binis.codegen.enrich.PrototypeLookup;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Tools.nullCheck;

@Slf4j
public class PrototypeLookupHandler implements PrototypeLookup {

    private final Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> parsed = new HashMap<>();
    private final Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> generated = new HashMap<>();
    private final Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> external = new HashMap<>();
    private final Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> enums = new HashMap<>();
    private final Map<String, CustomDescription> custom = new HashMap<>();
    private final List<Pair<Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>>, PrototypeDescription<ClassOrInterfaceDeclaration>>> prototypeMaps = new ArrayList<>();

    @Getter
    private final JavaParser parser = new JavaParser();
    private UnaryOperator<String> externalLookup;

    @Getter
    @Setter
    private ProcessingEnvironment processingEnvironment;

    @SuppressWarnings("unchecked")
    @Override
    public void registerParsed(String prototype, PrototypeDescription<?> parsed) {
        this.parsed.put(prototype, (PrototypeDescription) parsed);
        if (parsed.isCodeEnum()) {
            enums.put(parsed.getInterfaceFullName(), (PrototypeDescription) parsed);
        }
    }

    @Override
    public void registerGenerated(String prototype, PrototypeDescription<ClassOrInterfaceDeclaration> generated) {
        this.generated.put(prototype, generated);
    }

    @Override
    public void registerExternalLookup(UnaryOperator<String> lookup) {
        externalLookup = lookup;
    }

    @Override
    public CustomDescription createCustomDescription(String id) {
        return custom.computeIfAbsent(id, k -> Structures.CustomParsed.bldr()
                .id(id)
                .properties(Structures.defaultBuilder().build())
                .build());
    }

    @Override
    public CustomDescription getCustomDescription(String id) {
        return custom.get(id);
    }

    public Collection<CustomDescription> custom() {
        return custom.values();
    }

    @Override
    public PrototypeDescription<ClassOrInterfaceDeclaration> findParsed(String prototype) {
        var result = parsed.get(prototype);
        if (isNull(result)) {
            handleExternal(prototype);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void handleExternal(String prototype) {
        if (nonNull(externalLookup) && !external.containsKey(prototype)) {
            var code = externalLookup.apply(prototype);
            if (nonNull(code)) {
                var res = parser.parse(code).getResult();
                if (res.isPresent() && res.get().getType(0).isTypeDeclaration()) {
                    external.put(prototype, Structures.Parsed.builder()
                            .declaration(res.get().getType(0).asTypeDeclaration())
                            .spec(res.get().getType(0).isClassOrInterfaceDeclaration() ? res.get().getType(0).asClassOrInterfaceDeclaration() : null)
                            .build());
                }
            }
        }
    }

    @Override
    public PrototypeDescription<ClassOrInterfaceDeclaration> findGenerated(String prototype) {
        return generated.get(prototype);
    }

    @Override
    public PrototypeDescription<ClassOrInterfaceDeclaration> findExternal(String prototype) {
        handleExternal(prototype);
        return external.get(prototype);
    }

    @Override
    public PrototypeDescription<ClassOrInterfaceDeclaration> findByInterfaceName(String name) {
        return parsed.values().stream().filter(p -> p.getInterfaceName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public PrototypeDescription<ClassOrInterfaceDeclaration> findEnum(String generated) {
        return enums.get(generated);
    }

    @Override
    public Optional<PrototypeField> findField(String prototype, String name) {
        return nullCheck(findParsed(prototype), parsed ->
                parsed.findField(name));
    }

    @Override
    public boolean isParsed(String prototype) {
        return parsed.containsKey(prototype);
    }

    @Override
    public boolean isGenerated(String prototype) {
        return generated.containsKey(prototype);
    }

    @Override
    public boolean isExternal(String prototype) {
        handleExternal(prototype);
        return external.containsKey(prototype);
    }

    @Override
    public Collection<PrototypeDescription<ClassOrInterfaceDeclaration>> parsed() {
        return parsed.values();
    }

    @Override
    public Collection<PrototypeDescription<ClassOrInterfaceDeclaration>> generated() {
        return generated.values();
    }

    @Override
    public void addPrototypeMap(PrototypeDescription<ClassOrInterfaceDeclaration> parsed, Map<String, PrototypeDescription<ClassOrInterfaceDeclaration>> prototypeMap) {
        prototypeMaps.add(Pair.of(prototypeMap, parsed));
    }

    @Override
    public void calcPrototypeMaps() {
        prototypeMaps.forEach(p ->
                p.getLeft().put(p.getRight().getInterfaceName(), p.getRight()));
    }

    @Override
    public List<PrototypeDescription<ClassOrInterfaceDeclaration>> findGeneratedByFileName(String fileName) {
        return generated.values().stream().filter(g -> fileName.equals(g.getPrototypeFileName())).collect(Collectors.toList());
    }

    public void clean() {
        parsed.clear();
        generated.clear();
        enums.clear();
        external.clear();
        prototypeMaps.clear();
    }

}
