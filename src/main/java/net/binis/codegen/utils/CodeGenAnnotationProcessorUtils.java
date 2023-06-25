package net.binis.codegen.utils;

/*-
 * #%L
 * code-generator-annotation
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

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.tools.Reflection;
import net.binis.codegen.utils.dummy.Parent;
import sun.misc.Unsafe;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.getUnsafe;
import static net.binis.codegen.tools.Reflection.loadClass;

@Slf4j
public class CodeGenAnnotationProcessorUtils {

    public static void addOpensForCodeGen(Class cls) {
        Class<?> cModule;
        try {
            cModule = Class.forName("java.lang.Module");
        } catch (ClassNotFoundException e) {
            return; //jdk8-; this is not needed.
        }

        var unsafe = getUnsafe();
        var jdkCompilerModule = getJdkCompilerModule();
        var ownModule = getOwnModule(cls);
        String[] allPkgs = {
                "com.sun.tools.javac.code",
                "com.sun.tools.javac.comp",
                "com.sun.tools.javac.file",
                "com.sun.tools.javac.main",
                "com.sun.tools.javac.model",
                "com.sun.tools.javac.parser",
                "com.sun.tools.javac.processing",
                "com.sun.tools.javac.tree",
                "com.sun.tools.javac.util",
                "com.sun.tools.javac.jvm",
        };

        try {
            Method m = cModule.getDeclaredMethod("implAddOpens", String.class, cModule);
            long firstFieldOffset = getFirstFieldOffset(unsafe);
            unsafe.putBooleanVolatile(m, firstFieldOffset, true);
            for (String p : allPkgs) {
                m.invoke(jdkCompilerModule, p, ownModule);
            }
        } catch (Exception ignore) {
        }
    }

    private static long getFirstFieldOffset(Unsafe unsafe) {
        try {
            return unsafe.objectFieldOffset(Parent.class.getDeclaredField("first"));
        } catch (NoSuchFieldException e) {
            // can't happen.
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            // can't happen
            throw new RuntimeException(e);
        }
    }

    private static Object getJdkCompilerModule() {
        try {
            Class<?> cModuleLayer = Class.forName("java.lang.ModuleLayer");
            Method mBoot = cModuleLayer.getDeclaredMethod("boot");
            Object bootLayer = mBoot.invoke(null);
            Class<?> cOptional = Class.forName("java.util.Optional");
            Method mFindModule = cModuleLayer.getDeclaredMethod("findModule", String.class);
            Object oCompilerO = mFindModule.invoke(bootLayer, "jdk.compiler");
            return cOptional.getDeclaredMethod("get").invoke(oCompilerO);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getOwnModule(Class cls) {
        try {
            Method m = Reflection.findMethod("getModule", Class.class);
            return m.invoke(cls);
        } catch (Exception e) {
            return null;
        }
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
            return Reflection.getFieldValue(instance, "delegate");
        } catch (Exception e) {
            return null;
        }
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
