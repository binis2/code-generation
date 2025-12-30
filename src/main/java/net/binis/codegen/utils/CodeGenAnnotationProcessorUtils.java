package net.binis.codegen.utils;

/*-
 * #%L
 * code-generator-annotation
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
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.tools.Reflection;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CodeGenAnnotationProcessorUtils {

    public static boolean isPrototypeTest() {
        var cls = loadClass("net.binis.codegen.test.BaseCodeGenTest");
        return nonNull(cls) && CodeFactory.isRegisteredType(cls) && nonNull(CodeFactory.create(cls));
    }

    public static boolean isElementTest() {
        var cls = loadClass("net.binis.codegen.test.BaseCodeGenElementTest");
        return nonNull(cls) && CodeFactory.isRegisteredType(cls) && nonNull(CodeFactory.create(cls));
    }


    public static void addOpensForCodeGen(boolean openCompiler) {
        try {
            var cModule = loadClass("java.lang.Module");
            if (isNull(cModule)) {
                return; //jdk8-; this is not needed.
            }


            var method = findMethod("implAddExportsOrOpens", cModule, String.class, cModule, boolean.class, boolean.class);
            var module = getStaticFieldValue(cModule, "EVERYONE_MODULE");
            //var module = getStaticFieldValue(cModule, "ALL_UNNAMED_MODULE");
            //var module = findMethod("getModule", Class.class).invoke(CodeGenAnnotationProcessorUtils.class);

            openRuntimeModules(method, module);

            if (openCompiler) {
                openCompilerModules(method, module);
            }
        } catch (Exception e) {
            log.error("Failed to open modules", e);
        }
    }

    private static void openRuntimeModules(Method method, Object module) {
        var javaBaseModule = Reflection.invoke("getModule", String.class);
        invoke(method, javaBaseModule, "jdk.internal.loader", module, true, true);
        invoke(method, javaBaseModule, "jdk.internal.module", module, true, true);
        invoke(method, javaBaseModule, "jdk.internal.vm", module, true, true);
        invoke(method, javaBaseModule, "jdk.internal.vm.annotation", module, true, true);
        invoke(method, javaBaseModule, "java.lang", module, true, true);
        invoke(method, javaBaseModule, "java.lang.invoke", module, true, true);
        invoke(method, javaBaseModule, "java.lang.module", module, true, true);
        invoke(method, javaBaseModule, "java.lang.reflect", module, true, true); // for jailbreak
        invoke(method, javaBaseModule, "java.net", module, true, true);

        Class<?> desktop = loadClass("java.awt.Desktop");
        if (desktop == null) {
            return;
        }
        var javaDesktop = invoke("getModule", desktop);
        invoke(method, javaDesktop, "sun.awt", module, true, true);
    }

    private static void openCompilerModules(Method method, Object module) {
        var jdkCompilerModule = invoke("getModule", loadClass("com.sun.tools.javac.code.Symbol"));
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.api", module, true, true);
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.code", module, true, true);
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.comp", module, true, true);
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.file", module, true, true);
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.jvm", module, true, true);
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.main", module, true, true);
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.model", module, true, true);
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.parser", module, true, true);
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.platform", module, true, true);
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.processing", module, true, true);
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.resources", module, true, true);
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.tree", module, true, true);
        invoke(method, jdkCompilerModule, "com.sun.tools.javac.util", module, true, true);
    }

    public static Object getJavacProcessingEnvironment(ProcessingEnvironment processingEnv, Object procEnv) {
        var cls = loadClass("com.sun.tools.javac.processing.JavacProcessingEnvironment");
        if (nonNull(cls)) {

            if (cls.isAssignableFrom(procEnv.getClass())) {
                return procEnv;
            }

            // try to find a "delegate" field in the object, and use this to try to obtain a JavacProcessingEnvironment
            for (Class<?> procEnvClass = procEnv.getClass(); procEnvClass != null; procEnvClass = procEnvClass.getSuperclass()) {
                Object delegate = tryGetDelegateField(procEnvClass, procEnv);
                if (delegate == null) delegate = tryGetProxyDelegateToField(procEnvClass, procEnv);
                if (delegate == null) delegate = tryGetProcessingEnvField(procEnvClass, procEnv);

                if (delegate != null) return getJavacProcessingEnvironment(processingEnv, delegate);
                // delegate field was not found, try on superclass
            }

            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "Can't get the delegate of the gradle IncrementalProcessingEnvironment. Lombok won't work.");
        }
        return null;
    }

    /**
     * Gradle incremental processing
     */
    private static Object tryGetDelegateField(Class<?> delegateClass, Object instance) {
        try {
            var field = findField(instance.getClass(), "delegate");
            if (nonNull(field)) {
                return getFieldValue(field, instance);
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * IntelliJ IDEA >= 2020.3
     */
    private static Object tryGetProxyDelegateToField(Class<?> delegateClass, Object instance) {
        try {
            InvocationHandler handler = Proxy.getInvocationHandler(instance);
            return Reflection.getFieldValue(handler, "val$delegateTo");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Kotlin incremental processing
     */
    private static Object tryGetProcessingEnvField(Class<?> delegateClass, Object instance) {
        try {
            return Reflection.getFieldValue(delegateClass, instance, "processingEnv");
        } catch (Exception e) {
            return null;
        }
    }

}
