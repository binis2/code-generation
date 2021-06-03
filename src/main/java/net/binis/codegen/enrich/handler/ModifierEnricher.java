package net.binis.codegen.enrich.handler;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.Final;
import net.binis.codegen.enrich.handler.base.BaseEnricher;
import net.binis.codegen.generation.core.CollectionsHandler;
import net.binis.codegen.generation.core.Constants;
import net.binis.codegen.generation.core.Generator;
import net.binis.codegen.generation.core.Structures;
import net.binis.codegen.generation.core.interfaces.PrototypeDescription;
import net.binis.codegen.generation.core.interfaces.PrototypeField;

import static com.github.javaparser.ast.Modifier.Keyword.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.generation.core.Constants.*;
import static net.binis.codegen.generation.core.Generator.handleType;
import static net.binis.codegen.generation.core.Helpers.*;
import static net.binis.codegen.generation.core.Helpers.parseGenericClassSignature;
import static net.binis.codegen.tools.Tools.notNull;
import static net.binis.codegen.tools.Tools.nullCheck;

@Slf4j
public class ModifierEnricher extends BaseEnricher {

    @Override
    public void enrich(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        var spec = description.getSpec();
        if (nonNull(description.getProperties().getMixInClass())) {
            spec = description.getMixIn().getSpec();
        }
        var intf = description.getIntf();
        var properties = description.getProperties();
        var entity = description.getProperties().getInterfaceName();

        var modifier = new ClassOrInterfaceDeclaration(Modifier.createModifierList(), true, properties.getModifierName());
        var modifierClass = new ClassOrInterfaceDeclaration(Modifier.createModifierList(PROTECTED), false, defaultModifierClassName(properties.getClassName()));

        description.registerClass(MODIFIER_KEY, modifierClass);
        description.registerClass(MODIFIER_INTF_KEY, modifier);

        spec.addMember(modifierClass);
        intf.addMember(modifier);
        modifierClass.addImplementedType(properties.getLongModifierName());

        var methodName = isNull(properties.getMixInClass()) ? Constants.MODIFIER_METHOD_NAME : Constants.MIXIN_MODIFYING_METHOD_PREFIX + intf.getNameAsString();
        addModifyMethod(methodName, spec, properties.getLongModifierName(), modifierClass.getNameAsString(), true);
        addModifyMethod(methodName, intf, properties.getLongModifierName(), null, false);
        addDoneMethod(modifierClass, properties.getInterfaceName(), isNull(properties.getMixInClass()) ? properties.getClassName() : description.getMixIn().getParsedName(), true);
        addDoneMethod(modifier, properties.getInterfaceName(), null, false);
        handleModifierBaseImplementation(isNull(properties.getMixInClass()) ? description : description.getMixIn(), spec, intf, modifier, modifierClass);
        spec.findCompilationUnit().get().addImport("net.binis.codegen.modifier.Modifiable");
        if (isNull(description.getMixIn())) {
            spec.addImplementedType("Modifiable<" + intf.getNameAsString() + "." + modifier.getNameAsString() + ">");
        }

        for (var field : description.getFields()) {
            declare(description, properties, modifier, modifierClass, field);
        }

        if (nonNull(description.getBase())) {
            for (var field : description.getBase().getFields()) {
                declare(description, properties, modifier, modifierClass, field);
            }
        }

        if (nonNull(description.getMixIn())) {
            for (var field : description.getMixIn().getFields()) {
                declare(description, properties, modifier, modifierClass, field);
            }

            if (nonNull(description.getMixIn().getBase())) {
                for (var field : description.getMixIn().getBase().getFields()) {
                    declare(description, properties, modifier, modifierClass, field);
                }
            }
        }
    }

    private void declare(PrototypeDescription<ClassOrInterfaceDeclaration> description, net.binis.codegen.generation.core.interfaces.PrototypeData properties, ClassOrInterfaceDeclaration modifier, ClassOrInterfaceDeclaration modifierClass, PrototypeField field) {
        if (!field.getIgnores().isForModifier()) {
            var type = field.getDeclaration().getVariables().get(0).getType();
            addModifier(modifierClass, field, isNull(properties.getMixInClass()) ? properties.getClassName() : description.getMixIn().getParsedName(), properties.getLongModifierName(), true);
            addModifier(modifier, field, null, properties.getModifierName(), false);
            if (CollectionsHandler.isCollection(type)) {
                CollectionsHandler.addModifier(modifierClass, field, properties.getLongModifierName(), isNull(properties.getMixInClass()) ? properties.getClassName() : description.getMixIn().getParsedName(), true);
                CollectionsHandler.addModifier(modifier, field, properties.getModifierName(), null, false);
                notNull(lookup.findParsed(CollectionsHandler.getFullCollectionType(type)), parsed ->
                        lookup.generateEmbeddedModifier(parsed));
            }
        }
    }

    private static void addModifyMethod(String methodName, ClassOrInterfaceDeclaration spec, String modifierName, String modifierClassName, boolean isClass) {
        var method = spec
                .addMethod(methodName)
                .setType(modifierName);
        if (isClass) {
            method
                    .addModifier(PUBLIC)
                    .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + modifierClassName + "()"))));
        } else {
            method.setBody(null);
        }
    }

    private static void addDoneMethod(ClassOrInterfaceDeclaration spec, String parentName, String parentClassName, boolean isClass) {
        var method = spec
                .addMethod("done")
                .setType(parentName);
        if (isClass) {
            method
                    .addModifier(PUBLIC)
                    .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName(parentClassName + ".this"))));
        } else {
            method.setBody(null);
        }
    }

    private static void handleModifierBaseImplementation(PrototypeDescription<ClassOrInterfaceDeclaration> parse, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration modifier, ClassOrInterfaceDeclaration modifierClass) {
        notNull(Generator.findInheritanceProperty(parse.getDeclaration().asClassOrInterfaceDeclaration(), parse.getProperties(), (s, p) -> nullCheck(p.getBaseModifierClass(), prp -> getExternalClassName(s.findCompilationUnit().get(), prp))), baseClass ->
                notNull(loadClass(baseClass), cls -> {
                    if (net.binis.codegen.modifier.Modifier.class.isAssignableFrom(cls)) {
                        modifierClass.addConstructor(PROTECTED).setBody(new BlockStmt().addStatement("setObject(" + parse.getProperties().getClassName() + ".this);"));
                        spec.findCompilationUnit().get().addImport(net.binis.codegen.modifier.Modifier.class);
                    }
                    spec.findCompilationUnit().get().addImport(baseClass);
                    var intfName = intf.getNameAsString();
                    var modName = intfName + "." + modifier.getNameAsString();
                    var clsSignature = parseGenericClassSignature(cls);
                    if (clsSignature.size() != 2) {
                        log.error("BaseModifier ({}) should have two generic params!", cls.getCanonicalName());
                    }
                    modifierClass.addExtendedType(cls.getSimpleName() + "<" + modName + ", " + intfName + ">");

                    for (var method : cls.getDeclaredMethods()) {
                        if (java.lang.reflect.Modifier.isPublic(method.getModifiers()) && !"setObject".equals(method.getName())) {
                            if (nonNull(method.getAnnotation(Final.class))) {
                                Generator.addMethod(modifier, method, clsSignature, intfName);
                            } else {
                                Generator.addMethod(modifier, method, clsSignature, modName);
                            }
                        }
                    }
                })
        );
    }

    @Override
    public void finalize(PrototypeDescription<ClassOrInterfaceDeclaration> description) {
        if (lookup.embeddedModifierRequested(description)) {
            buildEmbeddedModifier(description, true);
            buildEmbeddedModifier(description, false);
        }
    }

    @Override
    public int order() {
        return 10000;
    }

    public static void buildEmbeddedModifier(PrototypeDescription<ClassOrInterfaceDeclaration> parse, boolean isClass) {
        var unit = parse.getFiles().get(isClass ? 0 : 1);
        var modifier = parse.getRegisteredClass(MODIFIER_INTF_KEY);
        var embedded = parse.getRegisteredClass(EMBEDDED_MODIFIER_INTF_KEY);
        if (isClass) {
            modifier = parse.getRegisteredClass(MODIFIER_KEY);
            embedded = parse.getRegisteredClass(EMBEDDED_MODIFIER_KEY);
        }
        var intfName = unit.getType(0).asClassOrInterfaceDeclaration().isInterface() ? unit.getType(0).getNameAsString() : "void";

        if (isNull(embedded)) {
            handleEmbeddedModifier(parse, parse.getSpec(), parse.getIntf());
            embedded = isClass ? parse.getRegisteredClass(EMBEDDED_MODIFIER_KEY) : parse.getRegisteredClass(EMBEDDED_MODIFIER_INTF_KEY);
        }

        if (nonNull(embedded)) {
            //var prefix = unit.getType(0).asClassOrInterfaceDeclaration().getFullyQualifiedName();
            embedded.setExtendedTypes(modifier.getExtendedTypes());

            var intf = modifier.getNameAsString();
            var eIntf = embedded.getNameAsString() + "<T>";
            if (modifier.getImplementedTypes().isNonEmpty()) {
                intf = modifier.getImplementedTypes(0).toString();
                eIntf = embedded.getImplementedTypes(0).toString();
            }

            for (var old : modifier.getMethods()) {
                if (Constants.MODIFIER_INTERFACE_NAME.equals(old.getType().toString()) || (old.getType().toString().endsWith(".Modify")) || old.getTypeAsString().startsWith("EmbeddedCodeCollection<")) {
                    var method = embedded.addMethod(old.getNameAsString())
                            .setModifiers(old.getModifiers())
                            .setParameters(old.getParameters());

                    if (old.getType().asString().equals(intf)) {
                        method.setType(eIntf);
                        if (old.getBody().isPresent()) {
                            method.setBody(new BlockStmt()
                                    .addStatement(new AssignExpr().setTarget(new NameExpr().setName("entity." + method.getNameAsString())).setValue(new NameExpr().setName(method.getNameAsString())))
                                    .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("this"))));
                        } else {
                            method.setBody(null);
                        }
                    } else if (CollectionsHandler.isCollection(old.getType())) {
                        method.setType(old.getType().toString().replace(intf, eIntf));
                        if (old.getBody().isPresent()) {
                            var collection = CollectionsHandler.getCollectionType(unit, unit, old.getType().asClassOrInterfaceType());
                            var parent = "entity." + method.getNameAsString();

                            method.setBody(new BlockStmt()
                                    .addStatement(new IfStmt().setCondition(new NameExpr().setName(parent + " != null")).setThenStmt(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName(parent)).setValue(new NameExpr().setName("new " + collection.getImplementor() + "<>()")))))
                                    .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection.getClassType() + "<>(this, " + parent + ")"))));
                        } else {
                            method.setBody(null);
                        }
                    } else if (old.getTypeAsString().startsWith("EmbeddedCodeCollection<")) {
                        if (old.getBody().isPresent()) {
                            method.setType(old.getType().toString().replace(", " + intf, ", " + eIntf));
                            var split = ((NameExpr) old.getBody().get().getChildNodes().get(1).getChildNodes().get(0)).getNameAsString().split("[\\s<.]");
                            var collection = split[1];
                            var cls = split[6];
                            var collectionType = old.getBody().get().getChildNodes().get(0).getChildNodes().get(1).getChildNodes().get(0).toString().split("[\\s<]")[3];
                            var parent = "entity." + method.getNameAsString();

                            method.setBody(new BlockStmt()
                                    .addStatement(new IfStmt().setCondition(new NameExpr().setName(parent + " != null")).setThenStmt(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName(parent)).setValue(new NameExpr().setName("new " + collectionType + "<>()")))))
                                    .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + collection + "<>(this, " + parent + ", " + cls + ".class)"))));
                        } else {
                            method.setType(old.getType().toString().replace(", Modify>", ", " + intfName + ".EmbeddedModify<T>>"));
                            method.setBody(null);
                        }
                    } else {
                        method.setType(old.getType());
                        method.setBody(old.getBody().orElse(null));
                    }
                } else {
                    intfName = old.getType().asClassOrInterfaceType().getNameAsString();
                }
            }
        }
    }

    private static void handleEmbeddedModifier(PrototypeDescription<ClassOrInterfaceDeclaration> parse, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf) {
        var actualModifier = parse.getRegisteredClass(MODIFIER_INTF_KEY);
        if (nonNull(actualModifier) && isNull(parse.getRegisteredClass(EMBEDDED_MODIFIER_INTF_KEY))) {
            if (nonNull(parse.getProperties().getMixInClass())) {
                spec = parse.getMixIn().getSpec();
            }

            var actualModifierClass = parse.getRegisteredClass(MODIFIER_KEY);
            var modifier = new ClassOrInterfaceDeclaration(
                    Modifier.createModifierList(), false, "Embedded" + actualModifier.getNameAsString())
                    .addTypeParameter("T")
                    .setInterface(true);
            modifier.addMethod("and")
                    .setType("EmbeddedCodeCollection<EmbeddedModify<T>, " + intf.getNameAsString() + ", T>")
                    .setBody(null);

            var modifierClass = new ClassOrInterfaceDeclaration(
                    Modifier.createModifierList(PROTECTED, STATIC), false, "Embedded" + actualModifierClass.getNameAsString())
                    .addTypeParameter("T")
                    .addImplementedType(intf.getNameAsString() + "." + modifier.getNameAsString() + "<T>");
            modifierClass.addField("T", "parent", PROTECTED);
            modifierClass.addField(spec.getNameAsString(), "entity", PROTECTED);
            modifierClass.addConstructor(PROTECTED)
                    .addParameter("T", "parent")
                    .addParameter(spec.getNameAsString(), "entity")
                    .setBody(new BlockStmt()
                            .addStatement(new AssignExpr().setTarget(new NameExpr().setName("this.parent")).setValue(new NameExpr().setName("parent")))
                            .addStatement(new AssignExpr().setTarget(new NameExpr().setName("this.entity")).setValue(new NameExpr().setName("entity"))));
            modifierClass.addMethod("and", PUBLIC)
                    .setType("EmbeddedCodeCollection<" + intf.getNameAsString() + ".EmbeddedModify<T>, " + intf.getNameAsString() + ", T>")
                    .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName("(EmbeddedCodeCollection) parent"))));

            spec.addMember(modifierClass);
            intf.addMember(modifier);

            parse.registerClass(EMBEDDED_MODIFIER_KEY, modifierClass);
            parse.registerClass(EMBEDDED_MODIFIER_INTF_KEY, modifier);

            intf.findCompilationUnit().get().addImport("net.binis.codegen.collection.EmbeddedCodeCollection");
            spec.findCompilationUnit().ifPresent(u -> {
                u.addImport("net.binis.codegen.factory.CodeFactory");
                u.addImport("net.binis.codegen.collection.EmbeddedCodeCollection");
            });
        }
    }

    private void addModifier(ClassOrInterfaceDeclaration spec, PrototypeField declaration, String modifierClassName, String modifierName, boolean isClass) {
        var type = isNull(declaration.getDescription()) ?
                handleType(declaration.getDeclaration().findCompilationUnit().get(), spec.findCompilationUnit().get(), declaration.getDeclaration().getVariables().get(0).getType(), false) :
                handleType(declaration.getDescription().findCompilationUnit().get(), spec.findCompilationUnit().get(), declaration.getDescription().getType(), false);
        var method = new MethodDeclaration().setName(declaration.getName())
                .setType(modifierName)
                .addParameter(new Parameter().setName(declaration.getName()).setType(type));
        if (isClass) {
            method
                    .addModifier(PUBLIC)
                    .setBody(new BlockStmt()
                            .addStatement(new AssignExpr().setTarget(new NameExpr().setName(modifierClassName + ".this." + declaration.getName())).setValue(new NameExpr().setName(declaration.getName())))
                            .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("this"))));
        } else {
            method.setBody(null);
        }
        if (!methodExists(spec, method, isClass)) {
            spec.addMember(method);
        }
    }

}
