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
import java.lang.reflect.Array;
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

    public static CGAnnotation findAnnotation(Element element, Predicate<CGAnnotation> filter) {
        var decl = getDeclaration(element);
        for (var ann : decl.getModifiers().getAnnotations()) {
            if (filter.test(ann)) {
                return ann;
            }
        }
        return null;
    }

    public static List<CGAnnotation> findAnnotations(Element element, Predicate<CGAnnotation> filter) {
        var result = new ArrayList<CGAnnotation>();
        var decl = getDeclaration(element);
        for (var ann : decl.getModifiers().getAnnotations()) {
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
        var maker = TreeMaker.create();
        var decl = getDeclaration(element, maker);
        var ann = createAnnotation(annotation, attributes);
        decl.getModifiers().getAnnotations().append(ann);
        return ann;
    }

    public static CGAnnotation addAnnotation(Element element, Class<? extends Annotation> annotation, CGList<CGExpression> attributes) {
        var maker = TreeMaker.create();

        var decl = getDeclaration(element, maker);
        var ann = maker.Annotation(maker.QualIdent(maker.getSymbol(annotation.getCanonicalName())), attributes);

        decl.getModifiers().getAnnotations().append(ann);
        return ann;
    }

    public static CGAnnotation addAnnotation(Element element, Class<? extends Annotation> annotation, CGExpression... attributes) {
        return addAnnotation(element, annotation, expressionToList(attributes));
    }

    public static CGAnnotation addOrReplaceAnnotation(Element element, Class<? extends Annotation> annotation) {
        return addOrReplaceAnnotation(element, annotation, Map.of());
    }

    public static CGAnnotation addOrReplaceAnnotation(Element element, Class<? extends Annotation> annotation, Map<String, Object> attributes) {
        removeAnnotation(element, annotation);
        return addAnnotation(element, annotation, attributes);
    }

    public static CGAnnotation addOrReplaceAnnotation(Element element, Class<? extends Annotation> annotation, CGExpression... attributes) {
        removeAnnotation(element, annotation);
        return addAnnotation(element, annotation, attributes);
    }

    public static CGAnnotation replaceAnnotation(Element element, CGAnnotation oldAnnotation, Class<? extends Annotation> annotation, Map<String, Object> attributes) {
        removeAnnotation(element, oldAnnotation);
        return addAnnotation(element, annotation, attributes);
    }

    public static CGAnnotation replaceAnnotation(Element element, CGAnnotation oldAnnotation, Class<? extends Annotation> annotation, CGExpression... attributes) {
        removeAnnotation(element, oldAnnotation);
        return addAnnotation(element, annotation, attributes);
    }

    public static CGAnnotation replaceAnnotationWithAttributes(Element element, CGAnnotation oldAnnotation, Class<? extends Annotation> annotation) {
        removeAnnotation(element, oldAnnotation);
        return addAnnotation(element, annotation, oldAnnotation.getArguments());
    }

    public static CGAnnotation removeAnnotation(Element element, Class<? extends Annotation> annotation) {
        CGAnnotation result = null;
        var decl = getDeclaration(element);
        var list = CGList.nil(CGAnnotation.class);
        for (var ann : decl.getModifiers().getAnnotations()) {
            if (!ann.isAnnotation(annotation)) {
                list = list.append(ann);
            } else {
                result = ann;
            }
        }
        decl.getModifiers().setAnnotations(list);
        return result;
    }

    public static CGAnnotation removeAnnotation(Element element, CGAnnotation annotation) {
        CGAnnotation result = null;
        var decl = getDeclaration(element);
        var list = CGList.nil(CGAnnotation.class);
        for (var ann : decl.getModifiers().getAnnotations()) {
            if (!ann.getInstance().equals(annotation.getInstance())) {
                list = list.append(ann);
            } else {
                result = ann;
            }
        }
        decl.getModifiers().setAnnotations(list);
        return result;
    }

    public static void addAnnotationAttribute(Element element, Class<? extends Annotation> annotation, String name, Object value) {
        var decl = getDeclaration(element);
        for (var ann : decl.getModifiers().getAnnotations()) {
            if (ann.isAnnotation(annotation)) {
                addAnnotationAttribute(element, ann, name, value);
                break;
            }
        }
    }

    public static void addAnnotationAttribute(Element element, CGAnnotation annotation, String name, Object value) {
        var maker = TreeMaker.create();
        annotation.getArguments().append(maker.Assign(maker.Ident(CGName.create(name)), calcExpression(maker, value)));
    }

    public static void removeAnnotationAttribute(Element element, Class<? extends Annotation> annotation, String name) {
        var decl = getDeclaration(element);
        for (var ann : decl.getModifiers().getAnnotations()) {
            if (ann.isAnnotation(annotation)) {
                removeAnnotationAttribute(element, ann, name);
                break;
            }
        }
    }

    public static void removeAnnotationAttribute(Element element, CGAnnotation annotation, String name) {
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
        var decl = getDeclaration(element);
        for (var ann : decl.getModifiers().getAnnotations()) {
            if (ann.isAnnotation(annotation)) {
                replaceAnnotationAttribute(element, ann, name, value);
                break;
            }
        }
    }

    public static void replaceAnnotationAttribute(Element element, CGAnnotation annotation, String name, Object value) {
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
