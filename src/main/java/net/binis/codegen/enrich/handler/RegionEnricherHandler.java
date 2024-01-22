package net.binis.codegen.enrich.handler;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2024 Binis Belev
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

import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithStaticModifier;
import net.binis.codegen.enrich.RegionEnricher;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.Helpers;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;

import java.util.function.Predicate;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Tools.with;

public class RegionEnricherHandler extends BaseEnricher implements RegionEnricher {

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        //Do nothing
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 1000;
    }

    @Override
    public void postProcess(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        with(description.getInterface(), intf -> {
            Helpers.sortClass(intf);
            calcRegions(intf, m -> m instanceof MethodDeclaration && !m.asMethodDeclaration().isStatic());
        });

        if (isNull(description.getProperties().getMixInClass())) {
            with(description.getImplementation(), spec -> {
                Helpers.sortClass(spec);
                calcRegions(spec, m -> m instanceof FieldDeclaration && !m.asFieldDeclaration().isFinal() && !m.asFieldDeclaration().isStatic());
            });
        }
    }

    private void calcRegions(ClassOrInterfaceDeclaration spec, Predicate<BodyDeclaration<?>> skipRegionFor) {
        BodyDeclaration<?> region = null;
        String regionDesc = null;
        BodyDeclaration<?> prv = null;
        var added = false;

        for (var member : spec.getMembers()) {
            if (areDifferent(member, region)) {
                regionDesc = getDescription(member);
                if (!skipRegionFor.test(member)) {
                    member.setLineComment("region " + regionDesc);
                    added = true;
                }
                if (nonNull(region) && !skipRegionFor.test(prv)) {
                    prv.addOrphanComment(new LineComment("endregion"));
                }
                region = member;
            }
            prv = member;
        }

        if (nonNull(region) && added) {
            prv.addOrphanComment(new LineComment("endregion"));
        }
    }

    private boolean areDifferent(BodyDeclaration<?> member, BodyDeclaration<?> region) {
        if (isNull(region)) {
            return true;
        }

        if ((member instanceof ConstructorDeclaration || member instanceof InitializerDeclaration) &&
                (region instanceof ConstructorDeclaration || region instanceof InitializerDeclaration)) {
            return false;
        }

        if (member instanceof NodeWithStaticModifier modifier && region instanceof NodeWithStaticModifier regModifier && modifier.isStatic() != regModifier.isStatic() && !member.isClassOrInterfaceDeclaration() && !region.isClassOrInterfaceDeclaration()) {
            return true;
        }

        if (member instanceof MethodDeclaration && region instanceof MethodDeclaration && member.asMethodDeclaration().getNameAsString().startsWith("set") && !region.asMethodDeclaration().getNameAsString().startsWith("set")) {
            return true;
        }

        if (member instanceof FieldDeclaration && region instanceof FieldDeclaration &&
                ((member.asFieldDeclaration().isFinal() && member.asFieldDeclaration().isStatic() && !region.asFieldDeclaration().isFinal() && !region.asFieldDeclaration().isStatic()) ||
                (!member.asFieldDeclaration().isFinal() && !member.asFieldDeclaration().isStatic() && region.asFieldDeclaration().isFinal() && region.asFieldDeclaration().isStatic()))) {
            return true;
        }

        return !member.getClass().equals(region.getClass());
    }

    private String getDescription(BodyDeclaration<?> member) {
        if (member instanceof FieldDeclaration) {
            if (member.asFieldDeclaration().isStatic() && member.asFieldDeclaration().isFinal()) {
                return "constants";
            } else {
                return "fields";
            }
        }

        if (member instanceof InitializerDeclaration) {
            return "constructor & initializer";
        }

        if (member instanceof MethodDeclaration) {
            if (member.asMethodDeclaration().isStatic()) {
                return "starters";
            }

            if (member.asMethodDeclaration().getNameAsString().startsWith("set")) {
                return "setters";
            } else {
                return "getters";
            }
        }

        if (member instanceof ClassOrInterfaceDeclaration) {
            return "inner classes";
        }

        return member.getClass().getSimpleName();
    }

}
