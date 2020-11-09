package net.binis.codegen.codegen;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.annotation.CodeAnnotation;
import net.binis.codegen.annotation.CodeFieldAnnotations;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.tools.Holder;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.javaparser.ast.Modifier.Keyword.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.codegen.Helpers.*;
import static net.binis.codegen.codegen.Structures.Parsed;
import static net.binis.codegen.codegen.Structures.PrototypeData;
import static net.binis.codegen.tools.Tools.*;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
public class Generator {

    public static void generateCodeForClass(CompilationUnit parser) {

        for (var type : parser.getTypes()) {
            if (type.isClassOrInterfaceDeclaration() && type.asClassOrInterfaceDeclaration().isInterface()) {
                type.getAnnotationByName("CodePrototype").ifPresent(prototype -> {
                    var typeDeclaration = type.asClassOrInterfaceDeclaration();

                    log.info("Processing - {}", typeDeclaration.getNameAsString());

                    var properties = getProperties(prototype);
                    addProcessingType(typeDeclaration.getNameAsString(), properties.getInterfacePackage(), properties.getInterfaceName(), properties.getClassPackage(), properties.getClassName());
                    ensureParsedParents(typeDeclaration, properties);

                    var unit = new CompilationUnit();
                    var spec = unit.addClass(properties.getClassName());
                    unit.setPackageDeclaration(properties.getClassPackage());
                    spec.addModifier(PUBLIC);

                    if (properties.isGenerateConstructor()) {
                        spec.addConstructor(PUBLIC);
                    }

                    var iUnit = new CompilationUnit();
                    var intf = iUnit.addClass(properties.getInterfaceName()).setInterface(true);
                    iUnit.setPackageDeclaration(properties.getInterfacePackage());
                    intf.addModifier(PUBLIC);

                    var modifier = new ClassOrInterfaceDeclaration(Modifier.createModifierList(PUBLIC), false, properties.getModifierName()).setInterface(true);
                    var modifierClass = new ClassOrInterfaceDeclaration(Modifier.createModifierList(PROTECTED), false, properties.getClassName() + "ModifyImpl");
                    if (properties.isGenerateModifier()) {
                        spec.addMember(modifierClass);
                        intf.addMember(modifier);
                        modifierClass.addImplementedType(properties.getLongModifierName());
                    }

                    typeDeclaration.getExtendedTypes().forEach(t -> {
                        var parsed = getParsed(t);

                        if (nonNull(parsed)) {

                            if (parsed.getProperties().isBase()) {
                                properties.setBaseClassName(parsed.getParsedName());
                                spec.addExtendedType(parsed.getParsedName());
                                implementModifier(properties, modifier, modifierClass, parsed.getDeclaration().asClassOrInterfaceDeclaration());

                                if (parsed.getProperties().isGenerateConstructor() && properties.isGenerateConstructor()) {
                                    spec.findFirst(ConstructorDeclaration.class).ifPresent(c ->
                                            c.getBody().addStatement("super();")
                                    );
                                }
                            } else {
                                implementInterface(properties, spec, modifier, modifierClass, parsed.getFiles().get(0).getType(0).asClassOrInterfaceDeclaration());
                            }
                            if (StringUtils.isNotBlank(parsed.getInterfaceName())) {
                                iUnit.addImport(parsed.getInterfaceFullName());
                                intf.addExtendedType(parsed.getInterfaceName());
                            }
                        } else {
                            handleExternalInterface(properties, typeDeclaration, spec, intf, t, modifier, modifierClass);
                        }
                    });

                    if (properties.isGenerateInterface()) {
                        spec.addImplementedType(properties.getInterfaceName());
                        unit.addImport(getClassName(intf));
                        if (properties.isGenerateModifier()) {
                            addModifyMethod(spec, properties.getLongModifierName(), modifierClass.getNameAsString(), true, false);
                            addModifyMethod(intf, properties.getLongModifierName(), null, false, false);
                            addDoneMethod(modifierClass, properties.getInterfaceName(), properties.getClassName(), true, false);
                            addDoneMethod(modifier, properties.getInterfaceName(), null, false, false);
                            handleModifierBaseImplementation(properties, typeDeclaration, spec, intf, modifier, modifierClass);
                            spec.findCompilationUnit().get().addImport("net.binis.codegen.modifier.Modifiable");
                            spec.addImplementedType("Modifiable<" + intf.getNameAsString() + "." + modifier.getNameAsString() + ">");
                        }
                        handleCreatorBaseImplementation(properties, spec, intf, modifier);
                    }

                    for (var member : type.getMembers()) {
                        if (member.isMethodDeclaration()) {
                            var declaration = member.asMethodDeclaration();

                            if (!declaration.isDefault()) {
                                var ignore = getIgnores(member);
                                if (!ignore.isForField()) {
                                    addField(typeDeclaration, spec, declaration, null);
                                }
                                if (!ignore.isForClass()) {
                                    if (properties.isClassGetters()) {
                                        addGetter(typeDeclaration, spec, declaration, true);
                                    }
                                    if (properties.isClassSetters()) {
                                        addSetter(typeDeclaration, spec, declaration, true);
                                    }
                                }
                                if (!ignore.isForInterface()) {
                                    addGetter(typeDeclaration, intf, declaration, false);
                                    if (properties.isInterfaceSetters()) {
                                        addSetter(typeDeclaration, intf, declaration, false);
                                    }
                                }
                                if (properties.isGenerateModifier() && !ignore.isForModifier()) {
                                    addModifier(modifierClass, declaration, properties.getClassName(), properties.getLongModifierName(), true);
                                    addModifier(modifier, declaration, null, properties.getModifierName(), false);
                                    if (CollectionsHandler.isCollection(declaration.getType())) {
                                        CollectionsHandler.addModifier(modifierClass, declaration, properties.getLongModifierName(), properties.getClassName(), true);
                                        CollectionsHandler.addModifier(modifier, declaration, properties.getModifierName(), null, false);
                                    }
                                }
                            } else {
                                handleDefaultMethod(typeDeclaration, spec, declaration);
                            }
                        } else if (member.isClassOrInterfaceDeclaration()) {
                            processInnerClass(spec, member.asClassOrInterfaceDeclaration());
                        } else if (member.isFieldDeclaration()) {
                            processConstant(typeDeclaration, spec, intf, member.asFieldDeclaration());
                        } else {
                            log.error("Can't process method " + member.toString());
                        }
                    }

                    unit.setComment(new BlockComment("Generated code."));
                    iUnit.setComment(new BlockComment("Generated code."));

                    var parse = parsed.get(getClassName(typeDeclaration));

                    parse.setParsedName(spec.getNameAsString());
                    parse.setParsedFullName(spec.getFullyQualifiedName().get());
                    parse.setInterfaceName(intf.getNameAsString());
                    parse.setInterfaceFullName(intf.getFullyQualifiedName().get());
                    parse.setModifierName(modifier.getNameAsString());
                    parse.setModifierClassName(modifierClass.getNameAsString());
                    parse.setProperties(properties);
                    parse.setFiles(List.of(unit, iUnit));
                    generated.put(getClassName(spec), parse);

                    handleClassAnnotations(typeDeclaration, spec);
                    checkForDeclaredConstants(spec);
                    checkForDeclaredConstants(intf);

                    handleMixin(parse);

                    handleImports(typeDeclaration, spec);
                    handleImports(typeDeclaration, intf);

                    processingTypes.remove(typeDeclaration.getNameAsString());
                });
            } else {
                log.error("Invalid type " + type.getNameAsString());
            }
        }
    }

    private static void handleDefaultMethod(ClassOrInterfaceDeclaration typeDeclaration, ClassOrInterfaceDeclaration spec, MethodDeclaration declaration) {
        var method = declaration.clone().removeModifier(DEFAULT).addModifier(PUBLIC);

        declaration.getBody().ifPresent(b -> {
            var body = b.clone();
            handleDefaultMethodBody(typeDeclaration, body, declaration.getNameAsString());
            method.setBody(body);
        });

        spec.addMember(method);
    }

    private static void handleDefaultMethodBody(ClassOrInterfaceDeclaration typeDeclaration, Node node, String name) {
        for (var i = 0; i < node.getChildNodes().size(); i++) {
            var n = node.getChildNodes().get(i);
            if (n instanceof MethodCallExpr) {
                var method = (MethodCallExpr) n;
                if (typeDeclaration.getMembers().stream().anyMatch(m -> m.isMethodDeclaration() && m.asMethodDeclaration().getNameAsString().equals(name))) {
                    node.replace(method, new FieldAccessExpr().setName(method.getName()));
                }
            } else {
                handleDefaultMethodBody(typeDeclaration, n, name);
            }
        }
    }

    private static void handleImports(ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration type) {
        System.out.println("Imports for: " + declaration.getNameAsString());
        declaration.findCompilationUnit().ifPresent(decl ->
                type.findCompilationUnit().ifPresent(unit -> {
                    findUsedTypes(type).stream().map(t -> getClassImport(decl, t)).filter(Objects::nonNull).forEach(unit::addImport);
                    decl.getImports().stream().filter(ImportDeclaration::isAsterisk).forEach(unit::addImport);
                }));
    }

    private static Set<String> findUsedTypes(ClassOrInterfaceDeclaration type) {
        var result = new HashSet<String>();
        findUsedTypesInternal(result, type);

        result.forEach(t -> System.out.println(t));

        return result;
    }

    private static void findUsedTypesInternal(Set<String> types, Node node) {
        if (node instanceof ClassOrInterfaceType) {
            types.add(((ClassOrInterfaceType) node).getNameAsString());
        } else if (node instanceof AnnotationExpr) {
            types.add(((AnnotationExpr) node).getNameAsString());
        } else if (node instanceof NameExpr) {
            types.add(((NameExpr) node).getNameAsString());
        }
        node.getChildNodes().forEach(n -> findUsedTypesInternal(types, n));
    }

    private static void checkForDeclaredConstants(Node type) {
        //TODO: Handle more cases
        for (var node : type.getChildNodes()) {
            if (node instanceof FieldAccessExpr) {
                var expr = (FieldAccessExpr) node;
                if (expr.getChildNodes().size() > 1 && expr.getChildNodes().get(0) instanceof NameExpr && expr.getChildNodes().get(1) instanceof SimpleName) {
                    var namespace = (NameExpr) expr.getChildNodes().get(0);
                    var name = ((SimpleName) expr.getChildNodes().get(1)).asString();

                    var decl = declaredConstants.get(namespace.getNameAsString());
                    if (nonNull(decl)) {
                        decl.stream().filter(p -> p.getValue().equals(name)).findFirst().ifPresent(
                                c -> namespace.setName(c.getKey())
                        );
                    }
                }
            }
            checkForDeclaredConstants(node);
        }
    }

    private static void processConstant(ClassOrInterfaceDeclaration prototype, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, FieldDeclaration field) {
        var consts = getConstants(field);
        var f = field.clone();
        var name = field.getVariable(0).getNameAsString();
        f.getAnnotationByName("CodeConstant").ifPresent(f::remove);
        if (consts.isForInterface()) {
            intf.addMember(f);
            addDeclaredConstant(prototype.getNameAsString(), intf.getNameAsString(), name);
        } else {
            spec.addMember(f.addModifier(consts.isForPublic() ? PUBLIC : "serialVersionUID".equals(name) ? PRIVATE : PROTECTED).addModifier(STATIC).addModifier(FINAL));
            addDeclaredConstant(prototype.getNameAsString(), spec.getNameAsString(), name);
        }
    }

    private static PrototypeData getProperties(AnnotationExpr prototype) {
        var type = (ClassOrInterfaceDeclaration) prototype.getParentNode().get();
        var iName = Holder.of(defaultInterfaceName(type));
        var cName = defaultClassName(type);

        var builder = PrototypeData.builder()
                .generateConstructor(true)
                .generateInterface(true)
                .classGetters(true)
                .classSetters(true)
                .interfaceSetters(true)
                .classPackage(defaultClassPackage(type))
                .interfacePackage(defaultInterfacePackage(type))
                .modifierName("Modify");
        prototype.getChildNodes().forEach(node -> {
            if (node instanceof MemberValuePair) {
                var pair = (MemberValuePair) node;
                var name = pair.getNameAsString();
                switch (name) {
                    case "name":
                        var value = pair.getValue().asStringLiteralExpr().asString();
                        var intf = value.replace("Entity", "");
                        builder.name(value)
                                .className(value)
                                .classPackage("net.binis.codegen.entities")
                                .interfaceName(intf)
                                .interfacePackage("net.binis.codegen.objects")
                                .modifierName("Modify")
                                .longModifierName(intf + ".Modify");
                        break;
                    case "generateConstructor":
                        builder.generateConstructor(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "generateInterface":
                        builder.generateInterface(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "interfaceName":
                        iName.set(pair.getValue().asStringLiteralExpr().asString());
                        break;
                    case "classGetters":
                        builder.classGetters(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "classSetters":
                        builder.classSetters(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "interfaceSetters":
                        builder.interfaceSetters(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "generateModifier":
                        builder.generateModifier(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "base":
                        builder.base(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "creatorClass":
                        builder.creatorClass(pair.getValue().asClassExpr().getTypeAsString());
                        break;
                    case "creatorModifier":
                        builder.creatorModifier(pair.getValue().asBooleanLiteralExpr().getValue());
                        break;
                    case "baseModifierClass":
                        builder.baseModifierClass(pair.getValue().asClassExpr().getTypeAsString());
                        break;
                    case "mixInClass":
                        builder.mixInClass(pair.getValue().asClassExpr().getTypeAsString());
                        break;
                    case "implementationPackage":
                        builder.classPackage(pair.getValue().asStringLiteralExpr().asString());
                        break;
                    default:
                }
            }
        });

        if (cName.equals(iName.get())) {
            cName = iName.get() + "Impl";
        }

        builder.className(cName).interfaceName(iName.get()).longModifierName(iName.get() + ".Modify");

        var result = builder.build();
        if (result.isBase() && result.isGenerateModifier()) {
            result.setGenerateModifier(false);
        }

        return result;
    }

    private static void ensureParsedParents(ClassOrInterfaceDeclaration declaration, PrototypeData properties) {
        for (var extended : declaration.getExtendedTypes()) {
            notNull(getParsed(extended), parse ->
                    ifNull(parse.getFiles(), () -> generateCodeForClass(parse.getDeclaration().findCompilationUnit().get())));
        }

        notNull(properties.getMixInClass(), c ->
                notNull(getExternalClassName(declaration.findCompilationUnit().get(), c),
                        name -> notNull(parsed.get(name), parse ->
                                ifNull(parse.getFiles(), () -> generateCodeForClass(parse.getDeclaration().findCompilationUnit().get())))));
    }

    private static void ensureParsedParents(EnumDeclaration declaration, PrototypeData properties) {
        notNull(properties.getMixInClass(), c ->
                notNull(getExternalClassName(declaration.findCompilationUnit().get(), c),
                        name -> notNull(enumParsed.get(name), parse ->
                                ifNull(parse.getFiles(), () -> generateCodeForEnum(parse.getDeclaration().findCompilationUnit().get())))));
    }


    private static void implementModifier(PrototypeData properties, ClassOrInterfaceDeclaration modifier, ClassOrInterfaceDeclaration modifierClass, ClassOrInterfaceDeclaration declaration) {
        if (properties.isGenerateModifier()) {
            declaration.findCompilationUnit().ifPresent(unit -> {
                for (var method : declaration.getMethods()) {
                    if (!method.isDefault() && !getIgnores(method).isForModifier()) {
                        notNull(getClassImport(unit, method.getType().asString()), i -> {
                            modifierClass.findCompilationUnit().get().addImport(i);
                            modifier.findCompilationUnit().get().addImport(i);
                        });
                        addModifier(modifierClass, method, properties.getClassName(), properties.getLongModifierName(), true);
                        addModifier(modifier, method, null, properties.getModifierName(), false);
                        if (CollectionsHandler.isCollection(method.getType())) {
                            CollectionsHandler.addModifier(modifierClass, method, properties.getLongModifierName(), properties.getClassName(), true);
                            CollectionsHandler.addModifier(modifier, method, properties.getModifierName(), null, false);
                        }
                    }
                }
            });
        }
    }

    private static void implementInterface(PrototypeData properties, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration modifier, ClassOrInterfaceDeclaration modifierClass, ClassOrInterfaceDeclaration declaration) {
        for (var method : declaration.getMethods()) {
            if (method.getNameAsString().startsWith("get")) {
                addFieldFromGetter(spec, method, null);
                addGetterFromGetter(spec, method, true);
            } else if (method.getNameAsString().startsWith("set")) {
                addSetterFromSetter(spec, method, true);
                if (properties.isGenerateModifier()) {
                    if (CollectionsHandler.isCollection(method.getType())) {
                        CollectionsHandler.addModifier(modifierClass, method, properties.getLongModifierName(), properties.getClassName(), true);
                        CollectionsHandler.addModifier(modifier, method, properties.getModifierName(), null, false);
                    } else {
                        addModifierFromSetter(modifierClass, method, properties.getClassName(), properties.getLongModifierName(), true);
                        addModifierFromSetter(modifier, method, null, properties.getModifierName(), false);
                    }
                }
            }
        }
    }

    private static void handleExternalInterface(PrototypeData properties, ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, ClassOrInterfaceType type, ClassOrInterfaceDeclaration modifier, ClassOrInterfaceDeclaration modifierClass) {
        var className = getExternalClassName(declaration.findCompilationUnit().get(), type.getNameAsString());
        if (nonNull(className)) {
            var cls = loadClass(className);
            if (nonNull(cls)) {
                if (cls.isInterface()) {
                    for (var i : cls.getInterfaces()) {
                        handleExternalInterface(properties, declaration, spec, i, modifier, modifierClass, type.getTypeArguments().orElse(null));
                    }
                    handleExternalInterface(properties, declaration, spec, cls, modifier, modifierClass, type.getTypeArguments().orElse(null));
                    intf.addExtendedType(type);
                } else {
                    log.error("{} is not interface!", className);
                }
            }
        } else {
            log.error("Can't process interface {} cause can't find its type!", type.getNameAsString());
        }
    }

    private static void handleExternalInterface(PrototypeData properties, ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration spec, Class<?> cls, ClassOrInterfaceDeclaration modifier, ClassOrInterfaceDeclaration modifierClass, NodeList<Type> generics) {
        Map<String, Type> generic = null;
        if (nonNull(generics)) {
            generic = processGenerics(cls, generics);
        }

        Arrays.stream(cls.getInterfaces()).forEach(i -> handleExternalInterface(properties, declaration, spec, i, modifier, modifierClass, generics));

        for (var method : cls.getDeclaredMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers()) && !methodExists(declaration, method, false)) {
                if (method.getName().startsWith("get")) {
                    addFieldFromGetter(spec, method, generic);
                    addGetterFromGetter(spec, method, true, generic);
                } else if (method.getName().startsWith("set")) {
                    addSetterFromSetter(spec, method, true, generic);
                    if (properties.isGenerateModifier()) {
                        if (CollectionsHandler.isCollection(method.getParameterTypes()[0])) {
                            CollectionsHandler.addModifier(modifierClass, method, properties.getLongModifierName(), properties.getClassName(), true);
                            CollectionsHandler.addModifier(modifier, method, properties.getModifierName(), null, false);
                        } else {
                            addModifierFromSetter(modifierClass, method, properties.getClassName(), properties.getLongModifierName(), true, generic);
                            addModifierFromSetter(modifier, method, null, properties.getModifierName(), false, generic);
                        }
                    }
                } else {
                    log.error("Method {} of {} is nor getter or setter. Not implemented!", method.getName(), cls.getCanonicalName());
                }
            }
        }
    }

    private static void handleCreatorBaseImplementation(PrototypeData properties, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration modifier) {
        notNull(properties.getCreatorClass(), creatorClass -> {
            spec.findCompilationUnit().get().addImport(creatorClass);
            spec.addInitializer().addStatement(creatorClass + ".register(" + intf.getNameAsString() + ".class, " + spec.getNameAsString() + ".class);");

            intf.findCompilationUnit().get().addImport(creatorClass);

            if (properties.isGenerateModifier() && properties.isCreatorModifier()) {
                var type = intf.getNameAsString() + "." + modifier.getNameAsString();
                intf.addMethod("create", STATIC)
                        .setType(type)
                        .setBody(new BlockStmt().addStatement(new ReturnStmt("(" + type + ") " + creatorClass + ".create(" + intf.getNameAsString() + ".class).with()")));
            } else {
                intf.addMethod("create", STATIC)
                        .setType(intf.getNameAsString())
                        .setBody(new BlockStmt().addStatement(new ReturnStmt(creatorClass + ".create(" + intf.getNameAsString() + ".class)")));
            }
        });
    }

    private static void handleModifierBaseImplementation(PrototypeData properties, ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration intf, ClassOrInterfaceDeclaration modifier, ClassOrInterfaceDeclaration modifierClass) {
        notNull(findInheritanceProperty(declaration, properties, (s, p) -> nullCheck(p.getBaseModifierClass(), prp -> getExternalClassName(s.findCompilationUnit().get(), prp))), baseClass ->
                notNull(loadClass(baseClass), cls -> {
                    if (net.binis.codegen.modifier.Modifier.class.isAssignableFrom(cls)) {
                        modifierClass.addConstructor(PROTECTED).setBody(new BlockStmt().addStatement("setObject(" + properties.getClassName() + ".this);"));
                        spec.findCompilationUnit().get().addImport(net.binis.codegen.modifier.Modifier.class);
                    }
                    spec.findCompilationUnit().get().addImport(baseClass);
                    var intfName = intf.getNameAsString() + "." +  modifier.getNameAsString();
                    var clsSignature = parseGenericClassSignature(cls);
                    if (clsSignature.size() != 1) {
                        log.error("BaseModifier ({}) should have only one generic!", cls.getCanonicalName());
                    }
                    modifierClass.addExtendedType(cls.getSimpleName() + "<" + intfName + ">");

                    for (var method : cls.getDeclaredMethods()) {
                        if (java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                            addMethod(modifier, method, clsSignature, intfName);
                        }
                    }
                })
        );
    }

    private static void handleMixin(Parsed<ClassOrInterfaceDeclaration> parse) {
        if (nonNull(parse.getProperties().getMixInClass())) {
            var parent = parsed.get(getExternalClassName(parse.getDeclaration().findCompilationUnit().get(), parse.getProperties().getMixInClass()));
            if (parent != null) {
                var spec = parse.getFiles().get(0).getType(0).asClassOrInterfaceDeclaration();
                var intf = parse.getFiles().get(1).getType(0).asClassOrInterfaceDeclaration();
                var parentSpec = parent.getFiles().get(0).getType(0).asClassOrInterfaceDeclaration();
                var parentIntf = parent.getFiles().get(1).getType(0).asClassOrInterfaceDeclaration();
                intf.findCompilationUnit().get().addImport(parentIntf.getFullyQualifiedName().get());
                intf.addExtendedType(parentIntf.getNameAsString());
                parentSpec.findCompilationUnit().get().addImport(intf.getFullyQualifiedName().get());
                parentSpec.addImplementedType(intf.getNameAsString());
                mergeTypes(spec, parentSpec, m -> !m.isMethodDeclaration() || !Constants.MODIFIER_METHOD_NAME.equals(m.asMethodDeclaration().getNameAsString()));
                handleImports(parse.getDeclaration().asClassOrInterfaceDeclaration(), spec);
                handleImports(parse.getDeclaration().asClassOrInterfaceDeclaration(), intf);
                var modifier = findModifier(intf);
                if (parse.getProperties().isGenerateModifier() && parent.getProperties().isGenerateModifier()) {
                    mergeModifierTypes(parse, parent.getProperties(), modifier, findModifier(spec), findModifier(parentIntf), findModifier(parentSpec));
                }
                intf.getMembers().remove(modifier);
                intf.findFirst(MethodDeclaration.class).ifPresent(m -> intf.getMembers().remove(m));
            }
        }
    }

    public static String handleType(ClassOrInterfaceDeclaration source, ClassOrInterfaceDeclaration destination, Type type, boolean isCollection) {
        return handleType(source.findCompilationUnit().get(), destination.findCompilationUnit().get(), type, isCollection);
    }

    public static String handleType(CompilationUnit source, CompilationUnit destination, Type type, boolean isCollection) {
        var result = type.toString();
        if (type.isClassOrInterfaceType()) {
            var generic = handleGenericTypes(source, destination, type.asClassOrInterfaceType());
            if (!isEmpty(generic)) {
                result = type.asClassOrInterfaceType().getNameAsString() + "<" + String.join(",", generic) + ">";
            }
        }

        return handleType(source, destination, result, isCollection);
    }

    public static List<String> handleGenericTypes(CompilationUnit source, CompilationUnit destination, ClassOrInterfaceType type) {
        var result = new ArrayList<String>();
        var arguments = type.getTypeArguments();
        if (arguments.isEmpty() || arguments.get().isEmpty()) {
            return result;
        } else {
            return arguments.get().stream().map(n -> handleType(source, destination, n.toString(), false)).collect(Collectors.toList());
        }
    }

    public static String getGenericsList(CompilationUnit source, CompilationUnit destination, ClassOrInterfaceType type, boolean isCollection) {
        var arguments = type.getTypeArguments();
        if (arguments.isEmpty() || arguments.get().isEmpty()) {
            return "Object";
        } else {
            return arguments.get().stream().map(n -> handleType(source, destination, n.toString(), isCollection)).collect(Collectors.joining(", "));
        }
    }

    public static String handleType(CompilationUnit source, CompilationUnit destination, String type, boolean isCollection) {
        var parse = parsed.get(getExternalClassName(source, type));

        if (nonNull(parse)) {
            var processing = processingTypes.get(type);

            if (isNull(processing)) {
                if (isNull(parse.getFiles())) {
                    generateCodeForClass(parse.getDeclaration().findCompilationUnit().get());
                }

                var intf = parse.getFiles().get(1).getType(0);
                destination.addImport(intf.getFullyQualifiedName().get());

                if (isCollection && parse.getProperties().isGenerateModifier()) {
                    CollectionsHandler.handleEmbeddedModifier(type, parse.getFiles().get(0).getType(0).asClassOrInterfaceDeclaration(), intf.asClassOrInterfaceDeclaration());
                }

                return intf.getNameAsString();
            } else {
                destination.addImport(processing.getInterfacePackage() + "." + processing.getInterfaceName());
                return processing.getInterfaceName();
            }
        } else {
            return type;
        }
    }

    private static void handleFieldAnnotations(CompilationUnit unit, FieldDeclaration field, MethodDeclaration method) {
        method.getAnnotations().forEach(a ->
                notNull(getExternalClassName(unit, a.getNameAsString()), name -> {
                    var ann = loadClass(name);
                    if (nonNull(ann)) {
                        if (isNull(ann.getAnnotation(CodeAnnotation.class))) {
                            var target = ann.getAnnotation(Target.class);
                            if (target == null || target.toString().contains("FIELD")) {
                                field.addAnnotation(a);
                            }
                        } else {
                            if (CodeFieldAnnotations.class.isAssignableFrom(ann)) {
                                a.getChildNodes().stream().filter(n -> n instanceof ArrayInitializerExpr).findFirst().ifPresent(e ->
                                        e.getChildNodes().forEach(n ->
                                                field.addAnnotation(((StringLiteralExpr) n).asStringLiteralExpr().asString())));
                            }
                        }
                    } else {
                        log.warn("Can't process annotation {}", name);
                    }
                })
        );
    }

    private static void handleMethodAnnotations(CompilationUnit unit, MethodDeclaration method, MethodDeclaration declaration) {
        declaration.getAnnotations().forEach(a ->
                notNull(getExternalClassName(unit, a.getNameAsString()), name -> {
                    var ann = loadClass(name);
                    if (nonNull(ann) && !CodeAnnotation.class.isAssignableFrom(ann)) {
                        var target = ann.getAnnotation(Target.class);
                        if (target != null && !target.toString().contains("FIELD")) {
                            method.addAnnotation(a);
                        }
                    } else {
                        log.warn("Can't process annotation {}", name);
                    }
                })
        );
    }

    private static void handleClassAnnotations(ClassOrInterfaceDeclaration declaration, ClassOrInterfaceDeclaration spec) {
        declaration.findCompilationUnit().ifPresent(unit ->
                declaration.getAnnotations().forEach(a ->
                        notNull(getExternalClassName(unit, a.getNameAsString()), name -> {
                            var ann = loadClass(name);
                            if (nonNull(ann) && !CodeAnnotation.class.isAssignableFrom(ann)) {
                                var target = ann.getAnnotation(Target.class);
                                if (target != null && !target.toString().equals("TYPE")) {
                                    spec.addAnnotation(a);
                                }
                            } else {
                                log.warn("Can't process annotation {}", name);
                            }
                        })
                ));
    }

    private static <T> T findInheritanceProperty(ClassOrInterfaceDeclaration spec, PrototypeData properties, BiFunction<ClassOrInterfaceDeclaration, PrototypeData, T> func) {
        var data = func.apply(spec, properties);
        if (isNull(data)) {
            for (var type : spec.getExtendedTypes()) {
                var parse = generated.get(getClassName(type));
                if (nonNull(parse)) {
                    data = findInheritanceProperty(parse.getDeclaration().asClassOrInterfaceDeclaration(), parse.getProperties(), func);
                    if (nonNull(data)) {
                        break;
                    }
                }
            }
        }
        return data;
    }

    private static void processInnerClass(ClassOrInterfaceDeclaration spec, ClassOrInterfaceDeclaration cls) {
        notNull(spec.getAnnotationByName("CodeClassAnnotations"), a ->
                cls.getAnnotations().forEach(ann -> {
                    if (!"CodeClassAnnotations".equals(ann.getNameAsString())) {
                        spec.addAnnotation(ann.clone());
                    }
                })
        );
    }

    private static void addModifyMethod(ClassOrInterfaceDeclaration spec, String modifierName, String modifierClassName, boolean isClass, boolean isAbstract) {
        var method = spec
                .addMethod(Constants.MODIFIER_METHOD_NAME)
                .setType(modifierName);
        if (isClass) {
            method
                    .addModifier(PUBLIC)
                    .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName("new " + modifierClassName + "()"))));
        } else {
            if (isAbstract) {
                method.addModifier(ABSTRACT).addModifier(PUBLIC);
            }
            method.setBody(null);
        }
    }

    private static void addDoneMethod(ClassOrInterfaceDeclaration spec, String parentName, String parentClassName, boolean isClass, boolean isAbstract) {
        var method = spec
                .addMethod("done")
                .setType(parentName);
        if (isClass) {
            method
                    .addModifier(PUBLIC)
                    .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName(parentClassName + ".this"))));
        } else {
            if (isAbstract) {
                method.addModifier(ABSTRACT).addModifier(PUBLIC);
            }
            method.setBody(null);
        }
    }


    private static void addField(ClassOrInterfaceDeclaration type, ClassOrInterfaceDeclaration spec, MethodDeclaration method, Type generic) {
        var fieldName = method.getNameAsString();
        var field = findField(spec, fieldName);
        if (isNull(field)) {
            if (nonNull(generic)) {
                field = spec.addField(generic, fieldName, PROTECTED);
            } else {
                field = spec.addField(handleType(type, spec, method.getType(), false), fieldName, PROTECTED);
            }
        }
        handleFieldAnnotations(type.findCompilationUnit().get(), field, method);
    }

    private static void addFieldFromGetter(ClassOrInterfaceDeclaration spec, MethodDeclaration method, Map<String, Type> generic) {
        var field = getFieldName(method.getNameAsString());
        if (!fieldExists(spec, field)) {
            if (nonNull(generic)) {
                spec.addField(generic.get(parseMethodSignature(method)), getFieldName(method.getNameAsString()), PROTECTED);
            } else {
                spec.addField(method.getType(), getFieldName(method.getNameAsString()), PROTECTED);
            }
        }
    }

    private static void addFieldFromGetter(ClassOrInterfaceDeclaration spec, Method method, Map<String, Type> generic) {
        System.out.println(spec.getNameAsString() + " - " + method.getName());
        var field = getFieldName(method.getName());
        if (!fieldExists(spec, field)) {
            if (nonNull(generic)) {
                spec.addField(generic.get(parseMethodSignature(method)), field, PROTECTED);
            } else {
                spec.addField(method.getReturnType(), field, PROTECTED);
            }
            if (!method.getReturnType().isPrimitive() && !method.getReturnType().getCanonicalName().startsWith("java.lang.")) {
                spec.findCompilationUnit().get().addImport(method.getReturnType().getCanonicalName());
            }
        }
    }

    private static void addGetter(ClassOrInterfaceDeclaration type, ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass) {
        var name = getGetterName(declaration.getNameAsString(), declaration.getType().asString());
        if (!methodExists(spec, declaration, name, isClass)) {
            var method = spec
                    .addMethod(name)
                    .setType(handleType(type, spec, declaration.getType(), false));
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName(declaration.getName()))));
                handleMethodAnnotations(spec.findCompilationUnit().get(), method, declaration);
            } else {
                method.setBody(null);
            }
        }
    }

    private static void addGetterFromGetter(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass) {
        if (!methodExists(spec, declaration, isClass)) {
            var method = spec
                    .addMethod(declaration.getNameAsString())
                    .setType(declaration.getType());
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName(getFieldName(declaration.getNameAsString())))));
            } else {
                method.setBody(null);
            }
        }
    }

    private static void addGetterFromGetter(ClassOrInterfaceDeclaration spec, Method declaration, boolean isClass, Map<String, Type> generic) {
        if (!methodExists(spec, declaration, isClass)) {
            var method = spec.addMethod(declaration.getName());
            if (nonNull(generic)) {
                method.setType(generic.get(parseMethodSignature(declaration)));
            } else {
                method.setType(declaration.getReturnType());
            }
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new ReturnStmt().setExpression(new NameExpr().setName(getFieldName(declaration.getName())))));
            } else {
                method.setBody(null);
            }
        }
    }

    private static void addSetter(ClassOrInterfaceDeclaration type, ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass) {
        var name = getSetterName(declaration.getNameAsString());
        if (!methodExists(spec, declaration, name, isClass)) {
            var method = spec
                    .addMethod(name)
                    .addParameter(new Parameter().setName(declaration.getName()).setType(handleType(type, spec, declaration.getType(), false)));
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName("this." + declaration.getName())).setValue(new NameExpr().setName(declaration.getName()))));
            } else {
                method.setBody(null);
            }
        }
    }

    private static void addSetterFromSetter(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, boolean isClass) {
        if (!methodExists(spec, declaration, isClass)) {
            var method = spec
                    .addMethod(getSetterName(declaration.getNameAsString()))
                    .addParameter(new Parameter().setName(declaration.getName()).setType(declaration.getParameter(0).getType()));
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName("this." + getFieldName(declaration.getNameAsString()))).setValue(new NameExpr().setName(declaration.getName()))));
            } else {
                method.setBody(null);
            }
        }
    }

    private static void addSetterFromSetter(ClassOrInterfaceDeclaration spec, Method declaration, boolean isClass, Map<String, Type> generic) {
        if (!methodExists(spec, declaration, isClass)) {
            var field = getFieldName(declaration.getName());
            var method = spec.addMethod(declaration.getName());
            if (nonNull(generic)) {
                method.addParameter(new Parameter().setName(field).setType(generic.get(parseMethodSignature(declaration))));
            } else {
                method.addParameter(new Parameter().setName(field).setType(declaration.getParameterTypes()[0]));
            }
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt().addStatement(new AssignExpr().setTarget(new NameExpr().setName("this." + field)).setValue(new NameExpr().setName(field))));
            } else {
                method.setBody(null);
            }
        }
    }

    private static void addModifier(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, String modifierClassName, String modifierName, boolean isClass) {
        var method = new MethodDeclaration().setName(declaration.getNameAsString())
                .setType(modifierName)
                .addParameter(new Parameter().setName(declaration.getName()).setType(handleType(declaration.findCompilationUnit().get(), spec.findCompilationUnit().get(), declaration.getType(), false)));
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

    private static void addModifierFromModifier(ClassOrInterfaceDeclaration type, ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, String modifierClassName, String modifierName, boolean isClass) {
        if (!methodExists(spec, declaration, isClass) && declaration.getParameters().isNonEmpty()) {
            var method = spec
                    .addMethod(declaration.getNameAsString())
                    .setType(modifierName)
                    .addParameter(new Parameter().setName(declaration.getName()).setType(handleType(type, spec, declaration.getParameter(0).getType(), false)));
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt()
                                .addStatement(new AssignExpr().setTarget(new NameExpr().setName(modifierClassName + ".this." + declaration.getName())).setValue(new NameExpr().setName(declaration.getName())))
                                .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("this"))));
            } else {
                method.setBody(null);
            }
        }
    }

    private static void addModifierFromSetter(ClassOrInterfaceDeclaration spec, MethodDeclaration declaration, String modifierClassName, String modifierName, boolean isClass) {
        var field = getFieldName(declaration.getNameAsString());
        if (!methodExists(spec, declaration, isClass)) {
            var method = spec
                    .addMethod(field)
                    .setType(modifierName)
                    .addParameter(new Parameter().setName(field).setType(declaration.getParameter(0).getType()));
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt()
                                .addStatement(new AssignExpr().setTarget(new NameExpr().setName(modifierClassName + ".this." + field)).setValue(new NameExpr().setName(field)))
                                .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("this"))));
            } else {
                method.setBody(null);
            }
        }
    }

    private static void addModifierFromSetter(ClassOrInterfaceDeclaration spec, Method declaration, String modifierClassName, String modifierName, boolean isClass, Map<String, Type> generic) {
        var field = getFieldName(declaration.getName());
        if (!methodExists(spec, field, declaration, isClass)) {
            var method = spec
                    .addMethod(field)
                    .setType(modifierName);
            if (nonNull(generic)) {
                method.addParameter(new Parameter().setName(field).setType(generic.get(parseMethodSignature(declaration))));
            } else {
                method.addParameter(new Parameter().setName(field).setType(declaration.getParameterTypes()[0]));
            }
            if (isClass) {
                method
                        .addModifier(PUBLIC)
                        .setBody(new BlockStmt()
                                .addStatement(new AssignExpr().setTarget(new NameExpr().setName(modifierClassName + ".this." + field)).setValue(new NameExpr().setName(field)))
                                .addStatement(new ReturnStmt().setExpression(new NameExpr().setName("this"))));
            } else {
                method.setBody(null);
            }
        }
    }

    private static void addMethod(ClassOrInterfaceDeclaration spec, Method declaration, List<String> signature, String name) {
        if (!methodExists(spec, declaration, false)) {
            var method = spec.addMethod(declaration.getName());
            method.setType(signature.contains(parseMethodSignature(declaration)) ? name : declaration.getReturnType().toString());
            for (var i = 0; i < declaration.getParameterCount(); i++) {
                var param = declaration.getParameters()[i];
                method.addParameter(param.getType().getCanonicalName(), param.getName());
            }
            method.setBody(null);
        }
    }

    private static void mergeTypes(ClassOrInterfaceDeclaration source, ClassOrInterfaceDeclaration destination, Predicate<BodyDeclaration<?>> filter) {
        for (var member : source.getMembers()) {
            if (filter.test(member)) {
                if (member instanceof FieldDeclaration) {
                    var field = findField(destination, member.asFieldDeclaration().getVariable(0).getNameAsString());
                    if (isNull(field)) {
                        destination.addMember(member);
                    } else {
                        mergeAnnotations(member, field);
                    }
                } else if (member instanceof MethodDeclaration) {
                    var method = findMethod(destination, member.asMethodDeclaration().toString());
                    if (isNull(method)) {
                        destination.addMember(member);
                    } else {
                        mergeAnnotations(member, method);
                    }
                }
            }
            //TODO: Merge annotations too!
        }
    }

    private static void mergeAnnotations(BodyDeclaration<?> source, BodyDeclaration<?> destination) {
        log.warn("Merging nnotations is not implemented yet!");
    }

    private static void mergeModifierTypes(Parsed parsed, PrototypeData parentProperties, ClassOrInterfaceDeclaration modifier, ClassOrInterfaceDeclaration modifierClass, ClassOrInterfaceDeclaration parentModifier, ClassOrInterfaceDeclaration parentModifierClass) {
        for (var member : modifier.getMembers()) {
            if (member.isMethodDeclaration()) {
                var method = member.asMethodDeclaration();
                if (CollectionsHandler.isCollection(method.getType())) {
                    method.getType().asClassOrInterfaceType().getTypeArguments().get().removeLast();
                    CollectionsHandler.addModifier(parentModifierClass, method, parentProperties.getLongModifierName(), parentProperties.getClassName(), true);
                    CollectionsHandler.addModifier(parentModifier, method, parentProperties.getModifierName(), null, false);
                } else {
                    addModifierFromModifier(parsed.getDeclaration().asClassOrInterfaceDeclaration(), parentModifierClass, method, parentProperties.getClassName(), parentProperties.getLongModifierName(), true);
                    addModifierFromModifier(parsed.getDeclaration().asClassOrInterfaceDeclaration(), parentModifier, method, null, parentProperties.getModifierName(), false);
                }
            }
        }
    }

    public static void generateCodeForEnum(CompilationUnit parser) {
        for (var type : parser.getTypes()) {
            if (type.isEnumDeclaration()) {
                type.getAnnotationByName("EnumPrototype").ifPresent(prototype -> {
                    var typeDeclaration = type.asEnumDeclaration();

                    log.info("Processing - {}", prototype.toString());

                    var properties = getEnumProperties(prototype);
                    ensureParsedParents(typeDeclaration, properties);

                    var unit = new CompilationUnit();
                    var spec = unit.addEnum(properties.getClassName());
                    unit.setPackageDeclaration(properties.getClassPackage());
                    mergeEnums(typeDeclaration, spec);

                    var parse = enumParsed.get(getClassName(typeDeclaration));
                    parse.setParsedName(spec.getNameAsString());
                    parse.setParsedFullName(spec.getFullyQualifiedName().get());
                    parse.setProperties(properties);
                    parse.setFiles(List.of(unit));
                    enumGenerated.put(getClassName(spec), parse);

                    notNull(properties.getMixInClass(), c ->
                            notNull(getExternalClassName(typeDeclaration.findCompilationUnit().get(), c),
                                    name -> notNull(enumParsed.get(name), p ->
                                            mergeEnums(spec, p.getFiles().get(0).findFirst(EnumDeclaration.class).get()))));
                });
            } else {
                log.error("Invalid type " + type.getNameAsString());
            }
        }
    }

    private static void mergeEnums(EnumDeclaration source, EnumDeclaration destination) {
        mergeImports(source.findCompilationUnit().get(), destination.findCompilationUnit().get());

        source.getEntries().forEach(entry -> condition(destination.getEntries().stream().noneMatch(e -> e.getNameAsString().equals(entry.getNameAsString())), () -> destination.addEntry(entry)));
        source.getImplementedTypes().forEach(entry -> condition(destination.getImplementedTypes().stream().noneMatch(e -> e.getNameAsString().equals(entry.getNameAsString())), () -> destination.addImplementedType(entry)));
        source.getMembers().forEach(member -> mergeEnumMember(member, destination));
        source.getModifiers().forEach(m -> destination.addModifier(m.getKeyword()));
        source.getAnnotations().stream().filter(a -> !"EnumPrototype".equals(a.getNameAsString())).forEach(destination::addAnnotation);
        source.getComment().ifPresent(destination::setComment);
        source.getOrphanComments().forEach(destination::addOrphanComment);
    }

    private static void mergeEnumMember(BodyDeclaration<?> member, EnumDeclaration destination) {
        if (member.isConstructorDeclaration()) {
            if (destination.getConstructors().isEmpty()) {
                var constructor = destination.addConstructor();
                var source = member.asConstructorDeclaration();
                constructor.setModifiers(source.getModifiers());
                constructor.setParameters(source.getParameters());
                constructor.setBody(source.getBody());
                constructor.setAnnotations(source.getAnnotations());
            }
        } else if (member.isFieldDeclaration()) {
            if (destination.getFieldByName(member.asFieldDeclaration().getVariables().get(0).getNameAsString()).isEmpty()) {
                destination.addMember(member);
            }
        } else if (member.isMethodDeclaration()) {
            if (destination.getMethodsByName(member.asMethodDeclaration().getNameAsString()).isEmpty()) {
                destination.addMember(member);
            }
        } else {
            throw new GenericCodeGenException("TODO: Unhandled enum mix in type!");
        }
    }

    private static PrototypeData getEnumProperties(AnnotationExpr prototype) {
        var type = (EnumDeclaration) prototype.getParentNode().get();
        var builder = PrototypeData.builder()
                .className(defaultClassName(type))
                .classPackage(defaultPackage(type, null));
        prototype.getChildNodes().forEach(node -> {
            if (node instanceof MemberValuePair) {
                var pair = (MemberValuePair) node;
                var name = pair.getNameAsString();
                switch (name) {
                    case "name":
                        builder.name(pair.getValue().asStringLiteralExpr().asString());
                        break;
                    case "mixIn":
                        builder.mixInClass(pair.getValue().asClassExpr().getTypeAsString());
                        break;
                    default:
                }
            }
        });

        return builder.build();
    }

    private static PrototypeData getConstnatProperties(AnnotationExpr prototype) {
        var type = (ClassOrInterfaceDeclaration) prototype.getParentNode().get();
        var builder = PrototypeData.builder()
                .className(defaultClassName(type))
                .classPackage(defaultPackage(type, null));
        prototype.getChildNodes().forEach(node -> {
            if (node instanceof MemberValuePair) {
                var pair = (MemberValuePair) node;
                var name = pair.getNameAsString();
                switch (name) {
                    case "mixIn":
                        builder.mixInClass(pair.getValue().asClassExpr().getTypeAsString());
                        break;
                    default:
                }
            }
        });

        return builder.build();
    }

    public static CompilationUnit generateCodeForConstants() {
        if (!constantParsed.isEmpty()) {
            var result = new CompilationUnit();
            var parent = result.addClass("Constants");
            parent.addConstructor(PRIVATE);

            for (var entry : constantParsed.entrySet()) {
                var type = entry.getValue().getDeclaration();
                if (type.isClassOrInterfaceDeclaration()) {
                    type.getAnnotationByName("ConstantPrototype").ifPresent(prototype -> {
                        var typeDeclaration = type.asClassOrInterfaceDeclaration();

                        log.info("Processing - {}", prototype.toString());

                        var properties = getConstnatProperties(prototype);

                        var name = Holder.of(defaultClassName(entry.getValue().getDeclaration()));
                        if (nonNull(properties.getMixInClass())) {
                            name.set(defaultClassName(properties.getMixInClass()));
                        }

                        var cls = (ClassOrInterfaceDeclaration) parent.getMembers().stream().filter(c -> c.isClassOrInterfaceDeclaration() && c.asClassOrInterfaceDeclaration().getNameAsString().equals(name.get())).findFirst().orElse(null);
                        if (isNull(cls)) {
                            cls = (new ClassOrInterfaceDeclaration()).setName(name.get()).setModifiers(PUBLIC, STATIC);
                            cls.addConstructor(PRIVATE);
                            parent.addMember(cls);
                        }

                        mergeConstants(typeDeclaration, cls);
                    });
                }
            }

            return result;
        }
        return null;
    }

    private static void mergeConstants(ClassOrInterfaceDeclaration source, ClassOrInterfaceDeclaration destination) {
        mergeImports(source.findCompilationUnit().get(), destination.findCompilationUnit().get());

        for (var member : source.getMembers()) {
            if (member.isFieldDeclaration()) {
                var type = member.asFieldDeclaration();
                var field = new FieldDeclaration();
                field.setModifiers(type.getModifiers());
                type.getVariables().forEach(v -> {
                    var variable = new VariableDeclarator().setName(v.getName());
                    if (v.getType().isClassOrInterfaceType()) {
                        var enm = getEnumNameFromPrototype(source, v.getType().asClassOrInterfaceType().getNameAsString());
                        if (nonNull(enm)) {
                            variable.setType(enm);
                        } else {
                            variable.setType(v.getType());
                        }
                    } else {
                        variable.setType(v.getType());
                    }

                    v.getInitializer().ifPresent(i -> {
                        if (i.isFieldAccessExpr()) {
                            var expr = i.asFieldAccessExpr();
                            if (expr.getScope().isNameExpr()) {
                                var enm = getEnumNameFromPrototype(source, expr.getScope().asNameExpr().getNameAsString());
                                if (nonNull(enm)) {
                                    variable.setInitializer(new FieldAccessExpr().setName(expr.getName()).setScope(new NameExpr(enm)));
                                } else {
                                    variable.setInitializer(i);
                                }
                            } else {
                                variable.setInitializer(i);
                            }
                        } else {
                            variable.setInitializer(i);
                        }
                    });

                    field.addVariable(variable);
                });
                destination.addMember(field);
            }
        }

    }
}
