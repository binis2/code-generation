package net.binis.codegen.compiler.utils;

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

import net.binis.codegen.compiler.*;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ElementAnnotationUtils extends ElementUtils {

    public static CGAnnotation findAnnotation(Element element, Class<? extends Annotation> annotation) {
        return findAnnotation(element, ann ->
                ann.isAnnotation(annotation));
    }

    public static CGAnnotation findAnnotation(CGDeclaration declaration, Class<? extends Annotation> annotation) {
        return findAnnotation(declaration, ann ->
                ann.isAnnotation(annotation));
    }


    public static CGAnnotation findAnnotation(Element element, Predicate<CGAnnotation> filter) {
        return findAnnotation(getDeclaration(element), filter);
    }

    public static CGAnnotation findAnnotation(CGDeclaration declaration, Predicate<CGAnnotation> filter) {
        for (var ann : declaration.getModifiers().getAnnotations()) {
            if (filter.test(ann)) {
                return ann;
            }
        }
        return null;
    }

    public static List<CGAnnotation> findAnnotations(Element element, Predicate<CGAnnotation> filter) {
        return findAnnotations(getDeclaration(element), filter);
    }

    public static List<CGAnnotation> findAnnotations(CGDeclaration declaration, Predicate<CGAnnotation> filter) {
        var result = new ArrayList<CGAnnotation>();
        for (var ann : declaration.getModifiers().getAnnotations()) {
            if (filter.test(ann)) {
                result.add(ann);
            }
        }
        return result;
    }


    public static CGAnnotation createAnnotation(Class<? extends Annotation> annotation, Map<String, Object> attributes) {
        if (isNull(attributes)) {
            attributes = Map.of();
        }

        var maker = TreeMaker.create();

        var list = CGList.nil(CGExpression.class);
        for (var attr : attributes.entrySet()) {
            list = list.append(maker.Assign(maker.Ident(CGName.create(attr.getKey())), calcExpression(maker, attr.getValue())));
        }

        return maker.Annotation(maker.QualIdent(maker.getSymbol(annotation.getCanonicalName())), list);
    }

    public static CGAnnotation addAnnotation(Element element, Class<? extends Annotation> annotation) {
        return addAnnotation(element, annotation, Map.of());
    }

    public static CGAnnotation addAnnotation(Element element, Class<? extends Annotation> annotation, Map<String, Object> attributes) {
        return addAnnotation(getDeclaration(element), annotation, attributes);
    }

    public static CGAnnotation addAnnotation(CGDeclaration declaration, Class<? extends Annotation> annotation, Map<String, Object> attributes) {
        var ann = createAnnotation(annotation, attributes);
        declaration.getModifiers().getAnnotations().append(ann);
        return ann;
    }

    public static CGAnnotation addAnnotation(Element element, Class<? extends Annotation> annotation, CGList<CGExpression> attributes) {
        return addAnnotation(getDeclaration(element), annotation, attributes);
    }

    public static CGAnnotation addAnnotation(CGDeclaration declaration, Class<? extends Annotation> annotation, CGList<CGExpression> attributes) {
        var maker = TreeMaker.create();

        var ann = maker.Annotation(maker.QualIdent(maker.getSymbol(annotation.getCanonicalName())), attributes);

        declaration.getModifiers().getAnnotations().append(ann);
        return ann;
    }

    public static CGAnnotation addAnnotation(Element element, Class<? extends Annotation> annotation, CGExpression... attributes) {
        return addAnnotation(element, annotation, expressionToList(attributes));
    }

    public static CGAnnotation addAnnotation(CGDeclaration declaration, Class<? extends Annotation> annotation, CGExpression... attributes) {
        return addAnnotation(declaration, annotation, expressionToList(attributes));
    }

    public static CGAnnotation addOrReplaceAnnotation(Element element, Class<? extends Annotation> annotation) {
        return addOrReplaceAnnotation(element, annotation, Map.of());
    }

    public static CGAnnotation addOrReplaceAnnotation(CGDeclaration declaration, Class<? extends Annotation> annotation) {
        return addOrReplaceAnnotation(declaration, annotation, Map.of());
    }

    public static CGAnnotation addOrReplaceAnnotation(Element element, Class<? extends Annotation> annotation, Map<String, Object> attributes) {
        removeAnnotation(element, annotation);
        return addAnnotation(element, annotation, attributes);
    }

    public static CGAnnotation addOrReplaceAnnotation(CGDeclaration declaration, Class<? extends Annotation> annotation, Map<String, Object> attributes) {
        removeAnnotation(declaration, annotation);
        return addAnnotation(declaration, annotation, attributes);
    }

    public static CGAnnotation addOrReplaceAnnotation(Element element, Class<? extends Annotation> annotation, CGExpression... attributes) {
        removeAnnotation(element, annotation);
        return addAnnotation(element, annotation, attributes);
    }

    public static CGAnnotation addOrReplaceAnnotation(CGDeclaration declaration, Class<? extends Annotation> annotation, CGExpression... attributes) {
        removeAnnotation(declaration, annotation);
        return addAnnotation(declaration, annotation, attributes);
    }

    public static CGAnnotation getAnnotation(Element element, Class<? extends Annotation> annotation) {
        return getAnnotation(getDeclaration(element), annotation);
    }

    public static CGAnnotation getAnnotation(CGDeclaration declaration, Class<? extends Annotation> annotation) {
        for (var ann : declaration.getModifiers().getAnnotations()) {
            if (ann.isAnnotation(annotation)) {
                return ann;
            }
        }
        return null;
    }

    public static CGAnnotation replaceAnnotation(Element element, CGAnnotation oldAnnotation, Class<? extends Annotation> annotation, Map<String, Object> attributes) {
        removeAnnotation(element, oldAnnotation);
        return addAnnotation(element, annotation, attributes);
    }

    public static CGAnnotation replaceAnnotation(CGDeclaration declaration, CGAnnotation oldAnnotation, Class<? extends Annotation> annotation, Map<String, Object> attributes) {
        removeAnnotation(declaration, oldAnnotation);
        return addAnnotation(declaration, annotation, attributes);
    }

    public static CGAnnotation replaceAnnotation(Element element, CGAnnotation oldAnnotation, Class<? extends Annotation> annotation, CGExpression... attributes) {
        removeAnnotation(element, oldAnnotation);
        return addAnnotation(element, annotation, attributes);
    }

    public static CGAnnotation replaceAnnotation(CGDeclaration declaration, CGAnnotation oldAnnotation, Class<? extends Annotation> annotation, CGExpression... attributes) {
        removeAnnotation(declaration, oldAnnotation);
        return addAnnotation(declaration, annotation, attributes);
    }

    public static CGAnnotation replaceAnnotationWithAttributes(Element element, CGAnnotation oldAnnotation, Class<? extends Annotation> annotation) {
        removeAnnotation(element, oldAnnotation);
        return addAnnotation(element, annotation, oldAnnotation.getArguments());
    }

    public static CGAnnotation replaceAnnotationWithAttributes(CGDeclaration declaration, CGAnnotation oldAnnotation, Class<? extends Annotation> annotation) {
        removeAnnotation(declaration, oldAnnotation);
        return addAnnotation(declaration, annotation, oldAnnotation.getArguments());
    }

    public static CGAnnotation removeAnnotation(Element element, Class<? extends Annotation> annotation) {
        return removeAnnotation(getDeclaration(element), annotation);
    }

    public static CGAnnotation removeAnnotation(CGDeclaration declaration, Class<? extends Annotation> annotation) {
        CGAnnotation result = null;
        var list = CGList.nil(CGAnnotation.class);
        for (var ann : declaration.getModifiers().getAnnotations()) {
            if (!ann.isAnnotation(annotation)) {
                list = list.append(ann);
            } else {
                result = ann;
            }
        }
        declaration.getModifiers().setAnnotations(list);
        return result;
    }

    public static CGAnnotation removeAnnotation(Element element, CGAnnotation annotation) {
        return removeAnnotation(getDeclaration(element), annotation);
    }

    public static CGAnnotation removeAnnotation(CGDeclaration declaration, CGAnnotation annotation) {
        CGAnnotation result = null;
        var list = CGList.nil(CGAnnotation.class);
        for (var ann : declaration.getModifiers().getAnnotations()) {
            if (!ann.getInstance().equals(annotation.getInstance())) {
                list = list.append(ann);
            } else {
                result = ann;
            }
        }
        declaration.getModifiers().setAnnotations(list);
        return result;
    }

    public static void addAnnotationAttribute(Element element, Class<? extends Annotation> annotation, String name, Object value) {
        addAnnotationAttribute(getDeclaration(element), annotation, name, value);
    }

    public static void addAnnotationAttribute(CGDeclaration declaration, Class<? extends Annotation> annotation, String name, Object value) {
        for (var ann : declaration.getModifiers().getAnnotations()) {
            if (ann.isAnnotation(annotation)) {
                addAnnotationAttribute(ann, name, value);
                break;
            }
        }
    }

    public static void addAnnotationAttribute(CGAnnotation annotation, String name, Object value) {
        var maker = TreeMaker.create();
        annotation.getArguments().append(maker.Assign(maker.Ident(CGName.create(name)), calcExpression(maker, value)));
    }

    public static void removeAnnotationAttribute(Element element, Class<? extends Annotation> annotation, String name) {
        removeAnnotationAttribute(getDeclaration(element), annotation, name);
    }

    public static void removeAnnotationAttribute(CGDeclaration declaration, Class<? extends Annotation> annotation, String name) {
        for (var ann : declaration.getModifiers().getAnnotations()) {
            if (ann.isAnnotation(annotation)) {
                removeAnnotationAttribute(ann, name);
                break;
            }
        }
    }

    public static void removeAnnotationAttribute(CGAnnotation annotation, String name) {
        var list = CGList.nil(CGExpression.class);
        for (var attr : annotation.getArguments()) {
            if (attr.getInstance().getClass().equals(CGAssign.theClass())) {
                var assign = new CGAssign(attr.getInstance());
                if (!assign.getVariable().getInstance().toString().equals(name)) {
                    list.append(attr);
                }
            } else {
                list.append(attr);
            }
        }
        annotation.setArguments(list);
    }

    public static void replaceAnnotationAttribute(Element element, Class<? extends Annotation> annotation, String name, Object value) {
        replaceAnnotationAttribute(getDeclaration(element), annotation, name, value);
    }

    public static void replaceAnnotationAttribute(CGDeclaration declaration, Class<? extends Annotation> annotation, String name, Object value) {
        for (var ann : declaration.getModifiers().getAnnotations()) {
            if (ann.isAnnotation(annotation)) {
                replaceAnnotationAttribute(ann, name, value);
                break;
            }
        }
    }

    public static void replaceAnnotationAttribute(CGAnnotation annotation, String name, Object value) {
        var maker = TreeMaker.create();

        var list = CGList.nil(CGExpression.class);
        for (var attr : annotation.getArguments()) {
            if (attr.getInstance().getClass().equals(CGAssign.theClass())) {
                var assign = new CGAssign(attr.getInstance());
                if (!assign.getVariable().getInstance().toString().equals(name)) {
                    list.append(attr);
                } else {
                    list.append(maker.Assign(maker.Ident(CGName.create(name)), calcExpression(maker, value)));
                }
            } else {
                list.append(attr);
            }
        }
        annotation.setArguments(list);
    }

    public static void addIfMissingAnnotationAttribute(CGAnnotation annotation, String name, Object value) {
        var maker = TreeMaker.create();

        for (var attr : annotation.getArguments()) {
            if (attr.getInstance().getClass().equals(CGAssign.theClass())) {
                var assign = new CGAssign(attr.getInstance());
                if (assign.getVariable().getInstance().toString().equals(name)) {
                    return;
                }
            }
        }
        annotation.getArguments().append(maker.Assign(maker.Ident(CGName.create(name)), calcExpression(maker, value)));
    }

    protected static CGList<CGExpression> expressionToList(CGExpression... expressions) {
        var list = CGList.nil(CGExpression.class);
        if (nonNull(expressions)) {
            for (var attr : expressions) {
                if (nonNull(attr)) {
                    list = list.append(attr);
                }
            }
        }
        return list;
    }

}
