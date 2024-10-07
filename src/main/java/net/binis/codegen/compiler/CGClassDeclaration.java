package net.binis.codegen.compiler;

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

import com.sun.source.util.Trees;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.JavaCompilerObject;

import javax.lang.model.element.Element;
import java.lang.reflect.Modifier;
import java.util.List;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGClassDeclaration extends CGDeclaration {

    protected final int ENUM = getStaticFieldValue(Modifier.class, "ENUM");
    protected final int ANNOTATION = getStaticFieldValue(Modifier.class, "ANNOTATION");

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.tree.JCTree$JCClassDecl");
    }

    protected CGClassDeclaration(Object instance) {
        super(instance);
    }


    public static CGClassDeclaration create(Trees trees, Element element) {
        return new CGClassDeclaration(trees, element);
    }

    public CGClassDeclaration(Trees trees, Element element) {
        super(trees, element);
    }

    @Override
    protected void init() {
        cls = CGClassDeclaration.theClass();
    }

    public boolean isInterface() {
        var flags = getModifiers().flags();
        return (flags & Modifier.INTERFACE) == Modifier.INTERFACE && (flags & ANNOTATION) != ANNOTATION;
    }

    public List<CGMethodDeclaration> getMethods() {
        return getDefs().stream().filter(CGMethodDeclaration.class::isInstance).map(CGMethodDeclaration.class::cast).toList();
    }

    public List<CGVariableDecl> getFields() {
        return getDefs().stream().filter(CGVariableDecl.class::isInstance).map(CGVariableDecl.class::cast).toList();
    }

    public CGExpression getExtending() {
        var value = getFieldValue(instance, "extending");
        return nonNull(value) ? CGIdent.theClass().isInstance(value) ? new CGIdent(value) : CGFieldAccess.theClass().isInstance(value) ? new CGFieldAccess(value) : new CGExpression(value) : null;
    }

    public void setExtending(CGExpression expr) {
        setFieldValue(instance, "extending", expr.getInstance());
    }

    public CGList<CGExpression> getImplementing() {
        return new CGList<>(getFieldValue(instance, "implementing"), this::onImplModify, CGExpression.class);
    }

    protected <T extends JavaCompilerObject> void onImplModify(CGList<T> list) {
        setFieldValue(instance, "implementing", list.getInstance());
    }

    public boolean isEnum() {
        return (getModifiers().flags() & ENUM) == ENUM;
    }

    public boolean isAnnotation() {
        return (getModifiers().flags() & ANNOTATION) == ANNOTATION;
    }
}
