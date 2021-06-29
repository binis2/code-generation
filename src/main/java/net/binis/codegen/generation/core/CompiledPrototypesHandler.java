package net.binis.codegen.generation.core;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.enrich.PrototypeEnricher;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.List;

import static net.binis.codegen.generation.core.Generator.generateCodeForClass;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.tools.Tools.notNull;

@Slf4j
public abstract class CompiledPrototypesHandler {

    private static final JavaParser parser = new JavaParser();

    private CompiledPrototypesHandler() {
        //Do nothing
    }

    public static void handleCompiledPrototype(String compiledPrototype) {
        notNull(loadClass(compiledPrototype), c -> {
            notNull(c.getAnnotation(CodePrototype.class), ann -> {
                log.info("{}", ann);

                var declaration = new CompilationUnit().setPackageDeclaration(c.getPackageName()).addClass(c.getSimpleName()).setInterface(true);
                handleAnnotations(c, declaration);
                handleFields(c, declaration);

                var parsed = Structures.Parsed.<ClassOrInterfaceDeclaration>builder()
                        .compiled(c)
                        .declaration(declaration);

                lookup.registerParsed(compiledPrototype, parsed.build());
                generateCodeForClass(declaration.findCompilationUnit().get());

                //TODO: Implement annotations
                //TODO: Implement class annotations
                //TODO: Implement fields
            });
        });
    }

    private static void handleFields(Class<?> c, ClassOrInterfaceDeclaration declaration) {
        for (var method : c.getDeclaredMethods()) {
            if (!method.isDefault() && method.getParameterCount() == 0 && !Void.class.equals(method.getReturnType())) {
                declaration.addMethod(method.getName()).setType(method.getReturnType().getSimpleName()).setBody(null);
                if (method.getReturnType().isPrimitive()) {
                    declaration.findCompilationUnit().get().addImport(method.getReturnType().getCanonicalName());
                }
            }
        }
    }

    private static void handleAnnotations(Class<?> cls, ClassOrInterfaceDeclaration declaration) {
        for (var ann : cls.getAnnotations()) {
            parser.parseAnnotation(ann.toString()).getResult().ifPresent(annotation -> {
                declaration.findCompilationUnit().get().addImport(ann.annotationType().getCanonicalName());
                annotation.setName(ann.annotationType().getSimpleName());
                declaration.addAnnotation(annotation);
            });
        }
    }

    private static Structures.PrototypeDataHandler buildProperties(Class<?> cls, ClassOrInterfaceDeclaration declaration, CodePrototype ann) {
        var result = Structures.PrototypeDataHandler.builder()
                .generateConstructor(true)
                .generateInterface(true)
                .generateImplementation(true)
                .classGetters(true)
                .classSetters(true)
                .interfaceSetters(true)
                .classPackage(defaultClassPackage(declaration))
                .interfacePackage(defaultInterfacePackage(declaration))
                .modifierName(Constants.MODIFIER_INTERFACE_NAME);

        if (StringUtils.isNotBlank(ann.name())) {
            var intf = ann.name().replace("Entity", "");
            result.name(ann.name())
                    .className(ann.name())
                    .interfaceName(intf)
                    .longModifierName(intf + "." + Constants.MODIFIER_INTERFACE_NAME);
        }

        if (StringUtils.isNotBlank(ann.interfaceName())) {
            result.interfaceName(ann.interfaceName());
        }

        result.generateConstructor(ann.generateConstructor())
                .generateImplementation(ann.generateImplementation())
                .generateInterface(ann.generateInterface())
                .base(ann.base())
                .interfaceSetters(ann.interfaceSetters())
                .classGetters(ann.classGetters())
                .classSetters(ann.classSetters())
                .baseModifierClass(ann.baseModifierClass().getCanonicalName())
                .mixInClass(ann.mixInClass().getCanonicalName())
                .classPackage(ann.implementationPackage())
                .basePath(ann.basePath())
                .enrichers(handleEnrichers(ann.enrichers()))
                .inheritedEnrichers(handleEnrichers(ann.inheritedEnrichers()));

        return result.build();
    }

    private static List<PrototypeEnricher> handleEnrichers(Class<?>[] enrichers) {
        return null;
    }

}
