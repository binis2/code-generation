package net.binis.codegen.javaparser;

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

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.modules.*;
import com.github.javaparser.ast.nodeTypes.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.DefaultPrettyPrinterVisitor;
import com.github.javaparser.printer.SourcePrinter;
import com.github.javaparser.printer.configuration.ConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration.ConfigOption;
import com.github.javaparser.printer.configuration.PrinterConfiguration;
import com.github.javaparser.utils.Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.javaparser.ast.Node.Parsedness.UNPARSABLE;
import static com.github.javaparser.utils.Utils.*;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

public class CodeGenPrettyPrinterVisitor extends DefaultPrettyPrinterVisitor {

    public CodeGenPrettyPrinterVisitor(PrinterConfiguration configuration) {
        super(configuration);
    }

    public CodeGenPrettyPrinterVisitor(PrinterConfiguration configuration, SourcePrinter printer) {
        super(configuration, printer);
    }

    @Override
    public String toString() {
        return printer.toString();
    }

    @Override
    protected void printModifiers(final NodeList<Modifier> modifiers) {
        if (!modifiers.isEmpty()) {
            printer.print(modifiers.stream().map(Modifier::getKeyword).map(Modifier.Keyword::asString).collect(joining(" ")) + " ");
        }
    }

    @Override
    protected void printMembers(final NodeList<BodyDeclaration<?>> members, final Void arg) {
        BodyDeclaration<?> prv = null;
        for (final BodyDeclaration<?> member : members) {
            if (isPrints(prv, member)) {
                printer.println();
            }
            member.accept(this, arg);
            printer.println();
            printOrphanCommentsAfterThisChildNode(member);
            prv = member;
        }
    }

    private boolean isPrints(BodyDeclaration<?> prv, BodyDeclaration<?> member) {
        if (isNull(prv) && member.isMethodDeclaration() && member.asMethodDeclaration().getBody().isEmpty()) {
            return false;
        }

        if (nonNull(prv) && member.isMethodDeclaration() && prv.isMethodDeclaration()) {
            var method = member.asMethodDeclaration();
            var prvMethod = prv.asMethodDeclaration();
            return method.getBody().isPresent() || prvMethod.getBody().isPresent() || method.getAnnotations().isNonEmpty() ||
                    (prvMethod.getNameAsString().startsWith("get") && !method.getNameAsString().startsWith("get")) ||
                    (prvMethod.getNameAsString().startsWith("is") && !method.getNameAsString().startsWith("is")) ||
                    (prvMethod.getNameAsString().startsWith("set") && !method.getNameAsString().startsWith("set")) ||
                    (!prvMethod.getNameAsString().startsWith("get") && method.getNameAsString().startsWith("get")) ||
                    (!prvMethod.getNameAsString().startsWith("is") && method.getNameAsString().startsWith("is")) ||
                    (!prvMethod.getNameAsString().startsWith("set") && method.getNameAsString().startsWith("set"));
        } else {
            return true;
        }
    }

    protected void printMemberAnnotations(final NodeList<AnnotationExpr> annotations, final Void arg) {
        if (annotations.isEmpty()) {
            return;
        }
        for (final AnnotationExpr a : annotations) {
            a.accept(this, arg);
            printer.println();
        }
    }

    protected void printAnnotations(final NodeList<AnnotationExpr> annotations, boolean prefixWithASpace,
                                    final Void arg) {
        if (annotations.isEmpty()) {
            return;
        }
        if (prefixWithASpace) {
            printer.print(" ");
        }
        for (AnnotationExpr annotation : annotations) {
            annotation.accept(this, arg);
            printer.print(" ");
        }
    }

    protected void printTypeArgs(final NodeWithTypeArguments<?> nodeWithTypeArguments, final Void arg) {
        NodeList<Type> typeArguments = nodeWithTypeArguments.getTypeArguments().orElse(null);
        if (!isNullOrEmpty(typeArguments)) {
            printer.print("<");
            for (final Iterator<Type> i = typeArguments.iterator(); i.hasNext(); ) {
                final Type t = i.next();
                t.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
            printer.print(">");
        }
    }

    protected void printTypeParameters(final NodeList<TypeParameter> args, final Void arg) {
        if (!isNullOrEmpty(args)) {
            printer.print("<");
            for (final Iterator<TypeParameter> i = args.iterator(); i.hasNext(); ) {
                final TypeParameter t = i.next();
                t.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
            printer.print(">");
        }
    }

    protected <T extends Expression> void printArguments(final NodeList<T> args, final Void arg) {
        this.printer.print("(");
        if (!Utils.isNullOrEmpty(args)) {
            boolean columnAlignParameters = args.size() > 1 && this.getOption(ConfigOption.COLUMN_ALIGN_PARAMETERS).isPresent();
            if (columnAlignParameters) {
                this.printer.indentWithAlignTo(this.printer.getCursor().column);
            }

            Iterator<T> i = args.iterator();

            while (i.hasNext()) {
                T e = i.next();
                e.accept(this, arg);
                if (i.hasNext()) {
                    this.printer.print(",");
                    if (columnAlignParameters) {
                        this.printer.println();
                    } else {
                        this.printer.print(" ");
                    }
                }
            }

            if (columnAlignParameters) {
                this.printer.unindent();
            }
        }

        this.printer.print(")");
    }

    protected void printPrePostFixOptionalList(final NodeList<? extends Visitable> args, final Void arg, String prefix, String separator, String postfix) {
        if (!args.isEmpty()) {
            printer.print(prefix);
            for (final Iterator<? extends Visitable> i = args.iterator(); i.hasNext(); ) {
                final Visitable v = i.next();
                v.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(separator);
                }
            }
            printer.print(postfix);
        }
    }

    protected void printPrePostFixRequiredList(final NodeList<? extends Visitable> args, final Void arg, String prefix, String separator, String postfix) {
        printer.print(prefix);
        if (!args.isEmpty()) {
            for (final Iterator<? extends Visitable> i = args.iterator(); i.hasNext(); ) {
                final Visitable v = i.next();
                v.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(separator);
                }
            }
        }
        printer.print(postfix);
    }

    protected void printComment(final Optional<Comment> comment, final Void arg) {
        comment.ifPresent(c -> c.accept(this, arg));
    }

    protected void printOrphanedComments(List<Comment> orphanComments, Void arg) {
        orphanComments.forEach(c -> c.accept(this, arg));
    }

    @Override
    public void visit(final CompilationUnit n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        if (n.getParsed() == UNPARSABLE) {
            printer.println("???");
            return;
        }

        if (n.getPackageDeclaration().isPresent()) {
            n.getPackageDeclaration().get().accept(this, arg);
        }

        n.getImports().accept(this, arg);
        if (!n.getImports().isEmpty()) {
            printer.println();
        }

        for (final Iterator<TypeDeclaration<?>> i = n.getTypes().iterator(); i.hasNext(); ) {
            i.next().accept(this, arg);
            printer.println();
            if (i.hasNext()) {
                printer.println();
            }
        }

        n.getModule().ifPresent(m -> m.accept(this, arg));

        printOrphanCommentsEnding(n);
    }

    @Override
    public void visit(final PackageDeclaration n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printMemberAnnotations(n.getAnnotations(), arg);
        printer.print("package ");
        n.getName().accept(this, arg);
        printer.println(";");
        printer.println();

        printOrphanCommentsEnding(n);
    }

    @Override
    public void visit(final NameExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getName().accept(this, arg);

        printOrphanCommentsEnding(n);
    }

    @Override
    public void visit(final Name n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        if (n.getQualifier().isPresent()) {
            n.getQualifier().get().accept(this, arg);
            printer.print(".");
        }
        printer.print(n.getIdentifier());

        printOrphanCommentsEnding(n);
    }

    @Override
    public void visit(SimpleName n, Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print(n.getIdentifier());
    }

    @Override
    public void visit(final ClassOrInterfaceDeclaration n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printMemberAnnotations(n.getAnnotations(), arg);
        printModifiers(n.getModifiers());

        if (n.isInterface()) {
            printer.print("interface ");
        } else {
            printer.print("class ");
        }

        n.getName().accept(this, arg);

        printTypeParameters(n.getTypeParameters(), arg);

        if (!n.getExtendedTypes().isEmpty()) {
            printer.print(" extends ");
            for (final Iterator<ClassOrInterfaceType> i = n.getExtendedTypes().iterator(); i.hasNext(); ) {
                final ClassOrInterfaceType c = i.next();
                c.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }

        if (!n.getImplementedTypes().isEmpty()) {
            printer.print(" implements ");
            for (final Iterator<ClassOrInterfaceType> i = n.getImplementedTypes().iterator(); i.hasNext(); ) {
                final ClassOrInterfaceType c = i.next();
                c.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }

        printer.println(" {");
        printer.indent();
        if (!isNullOrEmpty(n.getMembers())) {
            printMembers(n.getMembers(), arg);
        }

        printOrphanCommentsEnding(n);

        printer.unindent();
        printer.print("}");
    }

    @Override
    public void visit(RecordDeclaration n, Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printMemberAnnotations(n.getAnnotations(), arg);
        printModifiers(n.getModifiers());

        printer.print("record ");

        n.getName().accept(this, arg);

        printTypeParameters(n.getTypeParameters(), arg);

        printer.print("(");
        if (!isNullOrEmpty(n.getParameters())) {
            for (final Iterator<Parameter> i = n.getParameters().iterator(); i.hasNext(); ) {
                final Parameter p = i.next();
                p.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }
        printer.print(")");

        if (!n.getImplementedTypes().isEmpty()) {
            printer.print(" implements ");
            for (final Iterator<ClassOrInterfaceType> i = n.getImplementedTypes().iterator(); i.hasNext(); ) {
                final ClassOrInterfaceType c = i.next();
                c.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }

        printer.println(" {");
        printer.indent();
        if (!isNullOrEmpty(n.getMembers())) {
            printMembers(n.getMembers(), arg);
        }

        printOrphanCommentsEnding(n);

        printer.unindent();
        printer.print("}");
    }

    @Override
    public void visit(final JavadocComment n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        if (getOption(ConfigOption.PRINT_COMMENTS).isPresent() && getOption(ConfigOption.PRINT_JAVADOC).isPresent()) {
            printer.println("/**");
            final String commentContent = normalizeEolInTextBlock(n.getContent(), getOption(ConfigOption.END_OF_LINE_CHARACTER).get().asString());
            String[] lines = commentContent.split("\\R");
            List<String> strippedLines = new ArrayList<>();
            for (String line : lines) {
                final String trimmedLine = line.trim();
                if (trimmedLine.startsWith("*")) {
                    line = trimmedLine.substring(1);
                }
                line = trimTrailingSpaces(line);
                strippedLines.add(line);
            }

            boolean skippingLeadingEmptyLines = true;
            boolean prependEmptyLine = false;
            boolean prependSpace = strippedLines.stream().anyMatch(line -> !line.isEmpty() && !line.startsWith(" "));
            for (String line : strippedLines) {
                if (line.isEmpty()) {
                    if (!skippingLeadingEmptyLines) {
                        prependEmptyLine = true;
                    }
                } else {
                    skippingLeadingEmptyLines = false;
                    if (prependEmptyLine) {
                        printer.println(" *");
                        prependEmptyLine = false;
                    }
                    printer.print(" *");
                    if (prependSpace) {
                        printer.print(" ");
                    }
                    printer.println(line);
                }
            }
            printer.println(" */");
        }
    }

    @Override
    public void visit(final ClassOrInterfaceType n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        if (n.getScope().isPresent()) {
            n.getScope().get().accept(this, arg);
            printer.print(".");
        }
        printAnnotations(n.getAnnotations(), false, arg);

        n.getName().accept(this, arg);

        if (n.isUsingDiamondOperator()) {
            printer.print("<>");
        } else {
            printTypeArgs(n, arg);
        }
    }

    @Override
    public void visit(final TypeParameter n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printAnnotations(n.getAnnotations(), false, arg);
        n.getName().accept(this, arg);
        if (!isNullOrEmpty(n.getTypeBound())) {
            printer.print(" extends ");
            for (final Iterator<ClassOrInterfaceType> i = n.getTypeBound().iterator(); i.hasNext(); ) {
                final ClassOrInterfaceType c = i.next();
                c.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(" & ");
                }
            }
        }
    }

    @Override
    public void visit(final PrimitiveType n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printAnnotations(n.getAnnotations(), true, arg);
        printer.print(n.getType().asString());
    }

    @Override
    public void visit(final ArrayType n, final Void arg) {
        final List<ArrayType> arrayTypeBuffer = new LinkedList<>();
        Type type = n;
        while (type instanceof ArrayType) {
            final ArrayType arrayType = (ArrayType) type;
            arrayTypeBuffer.add(arrayType);
            type = arrayType.getComponentType();
        }

        type.accept(this, arg);
        for (ArrayType arrayType : arrayTypeBuffer) {
            printAnnotations(arrayType.getAnnotations(), true, arg);
            printer.print("[]");
        }
    }

    @Override
    public void visit(final ArrayCreationLevel n, final Void arg) {
        printAnnotations(n.getAnnotations(), true, arg);
        printer.print("[");
        if (n.getDimension().isPresent()) {
            n.getDimension().get().accept(this, arg);
        }
        printer.print("]");
    }

    @Override
    public void visit(final IntersectionType n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printAnnotations(n.getAnnotations(), false, arg);
        boolean isFirst = true;
        for (ReferenceType element : n.getElements()) {
            if (isFirst) {
                isFirst = false;
            } else {
                printer.print(" & ");
            }
            element.accept(this, arg);
        }
    }

    @Override
    public void visit(final UnionType n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printAnnotations(n.getAnnotations(), true, arg);
        boolean isFirst = true;
        for (ReferenceType element : n.getElements()) {
            if (isFirst) {
                isFirst = false;
            } else {
                printer.print(" | ");
            }
            element.accept(this, arg);
        }
    }

    @Override
    public void visit(final WildcardType n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printAnnotations(n.getAnnotations(), false, arg);
        printer.print("?");
        if (n.getExtendedType().isPresent()) {
            printer.print(" extends ");
            n.getExtendedType().get().accept(this, arg);
        }
        if (n.getSuperType().isPresent()) {
            printer.print(" super ");
            n.getSuperType().get().accept(this, arg);
        }
    }

    @Override
    public void visit(final UnknownType n, final Void arg) {
        // Nothing to print
    }

    @Override
    public void visit(final FieldDeclaration n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);

        printComment(n.getComment(), arg);
        printMemberAnnotations(n.getAnnotations(), arg);
        printModifiers(n.getModifiers());
        if (!n.getVariables().isEmpty()) {
            Optional<Type> maximumCommonType = n.getMaximumCommonType();
            maximumCommonType.ifPresent(t -> t.accept(this, arg));
            if (maximumCommonType.isEmpty()) {
                printer.print("???");
            }
        }

        printer.print(" ");
        for (final Iterator<VariableDeclarator> i = n.getVariables().iterator(); i.hasNext(); ) {
            final VariableDeclarator v = i.next();
            v.accept(this, arg);
            if (i.hasNext()) {
                printer.print(", ");
            }
        }

        printer.print(";");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(final VariableDeclarator n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getName().accept(this, arg);

        n.findAncestor(NodeWithVariables.class).flatMap(ancestor -> ((NodeWithVariables<?>) ancestor).getMaximumCommonType()).ifPresent(commonType -> {

            final Type type = n.getType();

            ArrayType arrayType = null;

            for (int i = commonType.getArrayLevel(); i < type.getArrayLevel(); i++) {
                if (arrayType == null) {
                    arrayType = (ArrayType) type;
                } else {
                    arrayType = (ArrayType) arrayType.getComponentType();
                }
                printAnnotations(arrayType.getAnnotations(), true, arg);
                printer.print("[]");
            }
        });

        if (n.getInitializer().isPresent()) {
            printer.print(" = ");
            n.getInitializer().get().accept(this, arg);
        }
    }

    @Override
    public void visit(final ArrayInitializerExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("{");
        if (!isNullOrEmpty(n.getValues())) {
            printer.print(" ");
            for (final Iterator<Expression> i = n.getValues().iterator(); i.hasNext(); ) {
                final Expression expr = i.next();
                expr.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
            printer.print(" ");
        }
        printOrphanCommentsEnding(n);
        printer.print("}");
    }

    @Override
    public void visit(final VoidType n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printAnnotations(n.getAnnotations(), false, arg);
        printer.print("void");
    }

    @Override
    public void visit(final VarType n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printAnnotations(n.getAnnotations(), false, arg);
        printer.print("var");
    }

    @Override
    public void visit(Modifier n, Void arg) {
        printer.print(n.getKeyword().asString());
        printer.print(" ");
    }

    @Override
    public void visit(final ArrayAccessExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getName().accept(this, arg);
        printer.print("[");
        n.getIndex().accept(this, arg);
        printer.print("]");
    }

    @Override
    public void visit(final ArrayCreationExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("new ");
        n.getElementType().accept(this, arg);
        for (ArrayCreationLevel level : n.getLevels()) {
            level.accept(this, arg);
        }
        if (n.getInitializer().isPresent()) {
            printer.print(" ");
            n.getInitializer().get().accept(this, arg);
        }
    }

    @Override
    public void visit(final AssignExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getTarget().accept(this, arg);
        if (getOption(ConfigOption.SPACE_AROUND_OPERATORS).isPresent()) {
            printer.print(" ");
        }
        printer.print(n.getOperator().asString());
        if (getOption(ConfigOption.SPACE_AROUND_OPERATORS).isPresent()) {
            printer.print(" ");
        }
        n.getValue().accept(this, arg);
    }


    /**
     * work in progress for issue-545
     */

    @Override
    public void visit(final BinaryExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getLeft().accept(this, arg);
        if (getOption(ConfigOption.SPACE_AROUND_OPERATORS).isPresent()) {
            printer.print(" ");
        }
        printer.print(n.getOperator().asString());
        if (getOption(ConfigOption.SPACE_AROUND_OPERATORS).isPresent()) {
            printer.print(" ");
        }
        n.getRight().accept(this, arg);
    }

    @Override
    public void visit(final CastExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("(");
        n.getType().accept(this, arg);
        printer.print(") ");
        n.getExpression().accept(this, arg);
    }

    @Override
    public void visit(final ClassExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getType().accept(this, arg);
        printer.print(".class");
    }

    @Override
    public void visit(final ConditionalExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getCondition().accept(this, arg);
        printer.print(" ? ");
        n.getThenExpr().accept(this, arg);
        printer.print(" : ");
        n.getElseExpr().accept(this, arg);
    }

    @Override
    public void visit(final EnclosedExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("(");
        n.getInner().accept(this, arg);
        printer.print(")");
    }

    @Override
    public void visit(final FieldAccessExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getScope().accept(this, arg);
        printer.print(".");
        n.getName().accept(this, arg);
    }

    @Override
    public void visit(final InstanceOfExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getExpression().accept(this, arg);
        printer.print(" instanceof ");
        n.getType().accept(this, arg);
        if (n.getName().isPresent()) {
            printer.print(" ");
            n.getName().get().accept(this, arg);
        }
    }

    @Override
    public void visit(final CharLiteralExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("'");
        printer.print(n.getValue());
        printer.print("'");
    }

    @Override
    public void visit(final DoubleLiteralExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print(n.getValue());
    }

    @Override
    public void visit(final IntegerLiteralExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print(n.getValue());
    }

    @Override
    public void visit(final LongLiteralExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print(n.getValue());
    }

    @Override
    public void visit(final StringLiteralExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("\"");
        printer.print(n.getValue());
        printer.print("\"");
    }

    @Override
    public void visit(final TextBlockLiteralExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("\"\"\"");
        printer.indent();
        n.stripIndentOfLines().forEach(line -> {
            printer.println();
            printer.print(line);
        });
        printer.print("\"\"\"");
        printer.unindent();
    }

    @Override
    public void visit(final BooleanLiteralExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print(String.valueOf(n.getValue()));
    }

    @Override
    public void visit(final NullLiteralExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("null");
    }

    @Override
    public void visit(final ThisExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        if (n.getTypeName().isPresent()) {
            n.getTypeName().get().accept(this, arg);
            printer.print(".");
        }
        printer.print("this");
    }

    @Override
    public void visit(final SuperExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        if (n.getTypeName().isPresent()) {
            n.getTypeName().get().accept(this, arg);
            printer.print(".");
        }
        printer.print("super");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(final MethodCallExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);

        // determine whether we do reindenting for aligmnent at all
        // - is it enabled?
        // - are we in a statement where we want the alignment?
        // - are we not directly in the argument list of a method call expression?
        AtomicBoolean columnAlignFirstMethodChain = new AtomicBoolean();
        if (getOption(ConfigOption.COLUMN_ALIGN_FIRST_METHOD_CHAIN).isPresent()) {
            // pick the kind of expressions where vertically aligning method calls is okay.
            if (n.findAncestor(Statement.class).map(p -> p.isReturnStmt()
                    || p.isThrowStmt()
                    || p.isAssertStmt()
                    || p.isExpressionStmt()).orElse(false)) {
                // search for first parent that does not have its child as scope
                Node c = n;
                Optional<Node> p = c.getParentNode();
                while (p.isPresent() && p.filter(NodeWithTraversableScope.class::isInstance)
                        .map(NodeWithTraversableScope.class::cast)
                        .flatMap(NodeWithTraversableScope::traverseScope)
                        .map(c::equals)
                        .orElse(false)) {
                    c = p.get();
                    p = c.getParentNode();
                }

                // check if the parent is a method call and thus we are in an argument list
                columnAlignFirstMethodChain.set(p.filter(MethodCallExpr.class::isInstance).isEmpty());
            }
        }

        // we are at the last method call of a call chain
        // this means we do not start reindenting for alignment or we undo it
        AtomicBoolean lastMethodInCallChain = new AtomicBoolean(true);
        if (columnAlignFirstMethodChain.get()) {
            Node node = n;
            while (node.getParentNode()
                    .filter(NodeWithTraversableScope.class::isInstance)
                    .map(NodeWithTraversableScope.class::cast)
                    .flatMap(NodeWithTraversableScope::traverseScope)
                    .map(node::equals)
                    .orElse(false)) {
                node = node.getParentNode().orElseThrow(AssertionError::new);
                if (node instanceof MethodCallExpr) {
                    lastMethodInCallChain.set(false);
                    break;
                }
            }
        }

        // search whether there is a method call with scope in the scope already
        // this means that we probably started reindenting for alignment there
        AtomicBoolean methodCallWithScopeInScope = new AtomicBoolean();
        if (columnAlignFirstMethodChain.get()) {
            Optional<Expression> s = n.getScope();
            while (s.filter(NodeWithTraversableScope.class::isInstance).isPresent()) {
                Optional<Expression> parentScope = s.map(NodeWithTraversableScope.class::cast)
                        .flatMap(NodeWithTraversableScope::traverseScope);
                if (s.filter(MethodCallExpr.class::isInstance).isPresent() && parentScope.isPresent()) {
                    methodCallWithScopeInScope.set(true);
                    break;
                }
                s = parentScope;
            }
        }

        // we have a scope
        // this means we are not the first method in the chain
        n.getScope().ifPresent(scope -> {
            scope.accept(this, arg);
            if (columnAlignFirstMethodChain.get()) {
                if (methodCallWithScopeInScope.get()) {
                    /* We're a method call on the result of something (method call, property access, ...) that is not stand alone,
                       and not the first one with scope, like:
                       we're x() in a.b().x(), or in a=b().c[15].d.e().x().
                       That means that the "else" has been executed by one of the methods in the scope chain, so that the alignment
                       is set to the "." of that method.
                       That means we will align to that "." when we start a new line: */
                    printer.println();
                } else if (!lastMethodInCallChain.get()) {
                    /* We're the first method call on the result of something in the chain (method call, property access, ...),
                       but we are not at the same time the last method call in that chain, like:
                       we're x() in a().x().y(), or in Long.x().y.z(). That means we get to dictate the indent of following method
                       calls in this chain by setting the cursor to where we are now: just before the "."
                       that start this method call. */
                    printer.reindentWithAlignToCursor();
                }
            }
            printer.print(".");
        });

        printTypeArgs(n, arg);
        n.getName().accept(this, arg);
        printer.duplicateIndent();
        printArguments(n.getArguments(), arg);
        printer.unindent();
        if (columnAlignFirstMethodChain.get() && methodCallWithScopeInScope.get() && lastMethodInCallChain.get()) {
            // undo the aligning after the arguments of the last method call are printed
            printer.reindentToPreviousLevel();
        }
    }

    @Override
    public void visit(final ObjectCreationExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        if (n.hasScope()) {
            n.getScope().get().accept(this, arg);
            printer.print(".");
        }

        printer.print("new ");

        printTypeArgs(n, arg);
        if (!isNullOrEmpty(n.getTypeArguments().orElse(null))) {
            printer.print(" ");
        }

        n.getType().accept(this, arg);

        printArguments(n.getArguments(), arg);

        if (n.getAnonymousClassBody().isPresent()) {
            printer.println(" {");
            printer.indent();
            printMembers(n.getAnonymousClassBody().get(), arg);
            printer.unindent();
            printer.print("}");
        }
    }

    @Override
    public void visit(final UnaryExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        if (n.getOperator().isPrefix()) {
            printer.print(n.getOperator().asString());
        }

        n.getExpression().accept(this, arg);

        if (n.getOperator().isPostfix()) {
            printer.print(n.getOperator().asString());
        }
    }

    @Override
    public void visit(final ConstructorDeclaration n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printMemberAnnotations(n.getAnnotations(), arg);
        printModifiers(n.getModifiers());

        printTypeParameters(n.getTypeParameters(), arg);
        if (n.isGeneric()) {
            printer.print(" ");
        }
        n.getName().accept(this, arg);

        printer.print("(");
        if (!n.getParameters().isEmpty()) {
            for (final Iterator<Parameter> i = n.getParameters().iterator(); i.hasNext(); ) {
                final Parameter p = i.next();
                p.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }
        printer.print(")");

        if (!isNullOrEmpty(n.getThrownExceptions())) {
            printer.print(" throws ");
            for (final Iterator<ReferenceType> i = n.getThrownExceptions().iterator(); i.hasNext(); ) {
                final ReferenceType name = i.next();
                name.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }
        printer.print(" ");
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(final CompactConstructorDeclaration n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printMemberAnnotations(n.getAnnotations(), arg);
        printModifiers(n.getModifiers());

        printTypeParameters(n.getTypeParameters(), arg);
        if (n.isGeneric()) {
            printer.print(" ");
        }
        n.getName().accept(this, arg);

        if (!isNullOrEmpty(n.getThrownExceptions())) {
            printer.print(" throws ");
            for (final Iterator<ReferenceType> i = n.getThrownExceptions().iterator(); i.hasNext(); ) {
                final ReferenceType name = i.next();
                name.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }
        printer.print(" ");
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(final MethodDeclaration n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);

        printComment(n.getComment(), arg);
        printMemberAnnotations(n.getAnnotations(), arg);
        printModifiers(n.getModifiers());
        printTypeParameters(n.getTypeParameters(), arg);
        if (!isNullOrEmpty(n.getTypeParameters())) {
            printer.print(" ");
        }

        n.getType().accept(this, arg);
        printer.print(" ");
        n.getName().accept(this, arg);

        printer.print("(");
        n.getReceiverParameter().ifPresent(rp -> {
            rp.accept(this, arg);
            if (!isNullOrEmpty(n.getParameters())) {
                printer.print(", ");
            }
        });
        if (!isNullOrEmpty(n.getParameters())) {
            for (final Iterator<Parameter> i = n.getParameters().iterator(); i.hasNext(); ) {
                final Parameter p = i.next();
                p.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }
        printer.print(")");

        if (!isNullOrEmpty(n.getThrownExceptions())) {
            printer.print(" throws ");
            for (final Iterator<ReferenceType> i = n.getThrownExceptions().iterator(); i.hasNext(); ) {
                final ReferenceType name = i.next();
                name.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }
        if (n.getBody().isEmpty()) {
            printer.print(";");
        } else {
            printer.print(" ");
            n.getBody().get().accept(this, arg);
        }
    }

    @Override
    public void visit(final Parameter n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printAnnotations(n.getAnnotations(), false, arg);
        printModifiers(n.getModifiers());
        n.getType().accept(this, arg);
        if (n.isVarArgs()) {
            printAnnotations(n.getVarArgsAnnotations(), false, arg);
            printer.print("...");
        }
        if (!(n.getType() instanceof UnknownType)) {
            printer.print(" ");
        }
        n.getName().accept(this, arg);
    }

    @Override
    public void visit(final ReceiverParameter n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printAnnotations(n.getAnnotations(), false, arg);
        n.getType().accept(this, arg);
        printer.print(" ");
        n.getName().accept(this, arg);
    }

    @Override
    public void visit(final ExplicitConstructorInvocationStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        if (n.isThis()) {
            printTypeArgs(n, arg);
            printer.print("this");
        } else {
            if (n.getExpression().isPresent()) {
                n.getExpression().get().accept(this, arg);
                printer.print(".");
            }
            printTypeArgs(n, arg);
            printer.print("super");
        }
        printArguments(n.getArguments(), arg);
        printer.print(";");
    }

    @Override
    public void visit(final VariableDeclarationExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        if (n.getParentNode().map(ExpressionStmt.class::isInstance).orElse(false)) {
            printMemberAnnotations(n.getAnnotations(), arg);
        } else {
            printAnnotations(n.getAnnotations(), false, arg);
        }
        printModifiers(n.getModifiers());

        if (!n.getVariables().isEmpty()) {
            n.getMaximumCommonType().ifPresent(t -> t.accept(this, arg));
        }
        printer.print(" ");

        for (final Iterator<VariableDeclarator> i = n.getVariables().iterator(); i.hasNext(); ) {
            final VariableDeclarator v = i.next();
            v.accept(this, arg);
            if (i.hasNext()) {
                printer.print(", ");
            }
        }
    }

    @Override
    public void visit(final LocalClassDeclarationStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getClassDeclaration().accept(this, arg);
    }

    @Override
    public void visit(final LocalRecordDeclarationStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getRecordDeclaration().accept(this, arg);
    }

    @Override
    public void visit(final AssertStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("assert ");
        n.getCheck().accept(this, arg);
        if (n.getMessage().isPresent()) {
            printer.print(" : ");
            n.getMessage().get().accept(this, arg);
        }
        printer.print(";");
    }

    @Override
    public void visit(final BlockStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.println("{");
        if (n.getStatements() != null) {
            printer.indent();
            for (final Statement s : n.getStatements()) {
                s.accept(this, arg);
                printer.println();
            }
        }
        printOrphanCommentsEnding(n);
        printer.unindent();
        printer.print("}");
    }

    @Override
    public void visit(final LabeledStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getLabel().accept(this, arg);
        printer.print(": ");
        n.getStatement().accept(this, arg);
    }

    @Override
    public void visit(final EmptyStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print(";");
    }

    @Override
    public void visit(final ExpressionStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getExpression().accept(this, arg);
        printer.print(";");
    }

    @Override
    public void visit(final SwitchStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printSwitchNode(n, arg);
    }

    @Override
    public void visit(SwitchExpr n, Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printSwitchNode(n, arg);
    }

    private void printSwitchNode(SwitchNode n, Void arg) {
        printComment(n.getComment(), arg);
        printer.print("switch(");
        n.getSelector().accept(this, arg);
        printer.println(") {");
        if (n.getEntries() != null) {
            indentIf(getOption(ConfigOption.INDENT_CASE_IN_SWITCH).isPresent());
            for (final SwitchEntry e : n.getEntries()) {
                e.accept(this, arg);
            }
            unindentIf(getOption(ConfigOption.INDENT_CASE_IN_SWITCH).isPresent());
        }
        printer.print("}");
    }

    @Override
    public void visit(final SwitchEntry n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);

        final String separator = (n.getType() == SwitchEntry.Type.STATEMENT_GROUP) ? ":" : " ->"; // old/new switch

        if (isNullOrEmpty(n.getLabels())) {
            printer.print("default" + separator);
        } else {
            printer.print("case ");
            for (final Iterator<Expression> i = n.getLabels().iterator(); i.hasNext(); ) {
                final Expression label = i.next();
                label.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
            printer.print(separator);
        }
        printer.println();
        printer.indent();
        if (n.getStatements() != null) {
            for (final Statement s : n.getStatements()) {
                s.accept(this, arg);
                printer.println();
            }
        }
        printer.unindent();
    }

    @Override
    public void visit(final BreakStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("break");
        n.getLabel().ifPresent(l -> printer.print(" ").print(l.getIdentifier()));
        printer.print(";");
    }

    @Override
    public void visit(final YieldStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("yield ");
        n.getExpression().accept(this, arg);
        printer.print(";");
    }

    @Override
    public void visit(final ReturnStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("return");
        if (n.getExpression().isPresent()) {
            printer.print(" ");
            n.getExpression().get().accept(this, arg);
        }
        printer.print(";");
    }

    @Override
    public void visit(final EnumDeclaration n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printMemberAnnotations(n.getAnnotations(), arg);
        printModifiers(n.getModifiers());

        printer.print("enum ");
        n.getName().accept(this, arg);

        if (!n.getImplementedTypes().isEmpty()) {
            printer.print(" implements ");
            for (final Iterator<ClassOrInterfaceType> i = n.getImplementedTypes().iterator(); i.hasNext(); ) {
                final ClassOrInterfaceType c = i.next();
                c.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }

        printer.println(" {");
        printer.indent();
        if (n.getEntries().isNonEmpty()) {
            final boolean alignVertically =
                    // Either we hit the constant amount limit in the configurations, or...
                    n.getEntries().size() > getOption(ConfigOption.MAX_ENUM_CONSTANTS_TO_ALIGN_HORIZONTALLY).get().asInteger() ||
                            // any of the constants has a comment.
                            n.getEntries().stream().anyMatch(e -> e.getComment().isPresent());
            printer.println();
            for (final Iterator<EnumConstantDeclaration> i = n.getEntries().iterator(); i.hasNext(); ) {
                final EnumConstantDeclaration e = i.next();
                e.accept(this, arg);
                if (i.hasNext()) {
                    if (alignVertically) {
                        printer.println(",");
                    } else {
                        printer.print(", ");
                    }
                }
            }
        }
        if (!n.getMembers().isEmpty()) {
            printer.println(";");
            printMembers(n.getMembers(), arg);
        } else {
            if (!n.getEntries().isEmpty()) {
                printer.println();
            }
        }
        printer.unindent();
        printer.print("}");
    }

    @Override
    public void visit(final EnumConstantDeclaration n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printMemberAnnotations(n.getAnnotations(), arg);
        n.getName().accept(this, arg);

        if (!n.getArguments().isEmpty()) {
            printArguments(n.getArguments(), arg);
        }

        if (!n.getClassBody().isEmpty()) {
            printer.println(" {");
            printer.indent();
            printMembers(n.getClassBody(), arg);
            printer.unindent();
            printer.println("}");
        }
    }

    @Override
    public void visit(final InitializerDeclaration n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        if (n.isStatic()) {
            printer.print("static ");
        }
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(final IfStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("if (");
        n.getCondition().accept(this, arg);
        final boolean thenBlock = n.getThenStmt() instanceof BlockStmt;
        if (thenBlock) // block statement should start on the same line
            printer.print(") ");
        else {
            printer.println(")");
            printer.indent();
        }
        n.getThenStmt().accept(this, arg);
        if (!thenBlock)
            printer.unindent();
        if (n.getElseStmt().isPresent()) {
            if (thenBlock)
                printer.print(" ");
            else
                printer.println();
            final boolean elseIf = n.getElseStmt().orElse(null) instanceof IfStmt;
            final boolean elseBlock = n.getElseStmt().orElse(null) instanceof BlockStmt;
            if (elseIf || elseBlock) // put chained if and start of block statement on a same level
                printer.print("else ");
            else {
                printer.println("else");
                printer.indent();
            }
            if (n.getElseStmt().isPresent())
                n.getElseStmt().get().accept(this, arg);
            if (!(elseIf || elseBlock))
                printer.unindent();
        }
    }

    @Override
    public void visit(final WhileStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("while (");
        n.getCondition().accept(this, arg);
        printer.print(") ");
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(final ContinueStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("continue");
        n.getLabel().ifPresent(l -> printer.print(" ").print(l.getIdentifier()));
        printer.print(";");
    }

    @Override
    public void visit(final DoStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("do ");
        n.getBody().accept(this, arg);
        printer.print(" while (");
        n.getCondition().accept(this, arg);
        printer.print(");");
    }

    @Override
    public void visit(final ForEachStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("for (");
        n.getVariable().accept(this, arg);
        printer.print(" : ");
        n.getIterable().accept(this, arg);
        printer.print(") ");
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(final ForStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("for (");
        if (n.getInitialization() != null) {
            for (final Iterator<Expression> i = n.getInitialization().iterator(); i.hasNext(); ) {
                final Expression e = i.next();
                e.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }
        printer.print("; ");
        if (n.getCompare().isPresent()) {
            n.getCompare().get().accept(this, arg);
        }
        printer.print("; ");
        if (n.getUpdate() != null) {
            for (final Iterator<Expression> i = n.getUpdate().iterator(); i.hasNext(); ) {
                final Expression e = i.next();
                e.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }
        printer.print(") ");
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(final ThrowStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("throw ");
        n.getExpression().accept(this, arg);
        printer.print(";");
    }

    @Override
    public void visit(final SynchronizedStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("synchronized (");
        n.getExpression().accept(this, arg);
        printer.print(") ");
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(final TryStmt n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("try ");
        if (!n.getResources().isEmpty()) {
            printer.print("(");
            Iterator<Expression> resources = n.getResources().iterator();
            boolean first = true;
            while (resources.hasNext()) {
                resources.next().accept(this, arg);
                if (resources.hasNext()) {
                    printer.print(";");
                    printer.println();
                    if (first) {
                        printer.indent();
                    }
                }
                first = false;
            }
            if (n.getResources().size() > 1) {
                printer.unindent();
            }
            printer.print(") ");
        }
        n.getTryBlock().accept(this, arg);
        for (final CatchClause c : n.getCatchClauses()) {
            c.accept(this, arg);
        }
        if (n.getFinallyBlock().isPresent()) {
            printer.print(" finally ");
            n.getFinallyBlock().get().accept(this, arg);
        }
    }

    @Override
    public void visit(final CatchClause n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print(" catch (");
        n.getParameter().accept(this, arg);
        printer.print(") ");
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(final AnnotationDeclaration n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printMemberAnnotations(n.getAnnotations(), arg);
        printModifiers(n.getModifiers());

        printer.print("@interface ");
        n.getName().accept(this, arg);
        printer.println(" {");
        printer.indent();
        if (n.getMembers() != null) {
            printMembers(n.getMembers(), arg);
        }
        printer.unindent();
        printer.print("}");
    }

    @Override
    public void visit(final AnnotationMemberDeclaration n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printMemberAnnotations(n.getAnnotations(), arg);
        printModifiers(n.getModifiers());

        n.getType().accept(this, arg);
        printer.print(" ");
        n.getName().accept(this, arg);
        printer.print("()");
        if (n.getDefaultValue().isPresent()) {
            printer.print(" default ");
            n.getDefaultValue().get().accept(this, arg);
        }
        printer.print(";");
    }

    @Override
    public void visit(final MarkerAnnotationExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("@");
        n.getName().accept(this, arg);
    }

    @Override
    public void visit(final SingleMemberAnnotationExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("@");
        n.getName().accept(this, arg);
        printer.print("(");
        n.getMemberValue().accept(this, arg);
        printer.print(")");
    }

    @Override
    public void visit(final NormalAnnotationExpr n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("@");
        n.getName().accept(this, arg);
        printer.print("(");
        if (n.getPairs() != null) {
            for (final Iterator<MemberValuePair> i = n.getPairs().iterator(); i.hasNext(); ) {
                final MemberValuePair m = i.next();
                m.accept(this, arg);
                if (i.hasNext()) {
                    printer.print(", ");
                }
            }
        }
        printer.print(")");
    }

    @Override
    public void visit(final MemberValuePair n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        n.getName().accept(this, arg);
        printer.print(" = ");
        n.getValue().accept(this, arg);
    }

    @Override
    public void visit(final LineComment n, final Void arg) {
        if (getOption(ConfigOption.PRINT_COMMENTS).isEmpty()) {
            return;
        }
        printer
                .print("// ")
                .println(normalizeEolInTextBlock(n.getContent(), "").trim());
    }

    @Override
    public void visit(final BlockComment n, final Void arg) {
        if (getOption(ConfigOption.PRINT_COMMENTS).isEmpty()) {
            return;
        }
        final String commentContent = normalizeEolInTextBlock(n.getContent(), getOption(ConfigOption.END_OF_LINE_CHARACTER).get().asString());
        String[] lines = commentContent.split("\\R", -1); // as BlockComment should not be formatted, -1 to preserve any trailing empty line if present
        printer.print("/*");
        for (int i = 0; i < (lines.length - 1); i++) {
            printer.print(lines[i]);
            printer.print(getOption(ConfigOption.END_OF_LINE_CHARACTER).get().asValue()); // Avoids introducing indentation in blockcomments. ie: do not use println() as it would trigger indentation at the next print call.
        }
        printer.print(lines[lines.length - 1]); // last line is not followed by a newline, and simply terminated with `*/`
        printer.println("*/");
    }

    @Override
    public void visit(LambdaExpr n, Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);

        final NodeList<Parameter> parameters = n.getParameters();
        final boolean printPar = n.isEnclosingParameters();

        if (printPar) {
            printer.print("(");
        }
        for (Iterator<Parameter> i = parameters.iterator(); i.hasNext(); ) {
            Parameter p = i.next();
            p.accept(this, arg);
            if (i.hasNext()) {
                printer.print(", ");
            }
        }
        if (printPar) {
            printer.print(")");
        }

        printer.print(" -> ");
        final Statement body = n.getBody();
        if (body instanceof ExpressionStmt) {
            // Print the expression directly
            ((ExpressionStmt) body).getExpression().accept(this, arg);
        } else {
            body.accept(this, arg);
        }
    }

    @Override
    public void visit(MethodReferenceExpr n, Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        Expression scope = n.getScope();
        String identifier = n.getIdentifier();
        if (scope != null) {
            n.getScope().accept(this, arg);
        }

        printer.print("::");
        printTypeArgs(n, arg);
        if (identifier != null) {
            printer.print(identifier);
        }
    }

    @Override
    public void visit(TypeExpr n, Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        if (n.getType() != null) {
            n.getType().accept(this, arg);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(NodeList n, Void arg) {
        if (getOption(ConfigOption.ORDER_IMPORTS).isPresent() && !n.isEmpty() && n.get(0) instanceof ImportDeclaration) {
            //noinspection unchecked
            NodeList<ImportDeclaration> modifiableList = new NodeList<>(n);
            modifiableList.sort(
                    comparingInt((ImportDeclaration i) -> i.isStatic() ? 0 : 1)
                            .thenComparing(NodeWithName::getNameAsString));
            for (var node : modifiableList) {
                ((Node) node).accept(this, arg);
            }
        } else {
            for (Object node : n) {
                ((Node) node).accept(this, arg);
            }
        }
    }

    @Override
    public void visit(final ImportDeclaration n, final Void arg) {
        printOrphanCommentsBeforeThisChildNode(n);
        printComment(n.getComment(), arg);
        printer.print("import ");
        if (n.isStatic()) {
            printer.print("static ");
        }
        n.getName().accept(this, arg);
        if (n.isAsterisk()) {
            printer.print(".*");
        }
        printer.println(";");

        printOrphanCommentsEnding(n);
    }


    @Override
    public void visit(ModuleDeclaration n, Void arg) {
        printMemberAnnotations(n.getAnnotations(), arg);
        if (n.isOpen()) {
            printer.print("open ");
        }
        printer.print("module ");
        n.getName().accept(this, arg);
        printer.println(" {").indent();
        n.getDirectives().accept(this, arg);
        printer.unindent().println("}");
    }

    @Override
    public void visit(ModuleRequiresDirective n, Void arg) {
        printer.print("requires ");
        printModifiers(n.getModifiers());
        n.getName().accept(this, arg);
        printer.println(";");
    }

    @Override
    public void visit(ModuleExportsDirective n, Void arg) {
        printer.print("exports ");
        n.getName().accept(this, arg);
        printPrePostFixOptionalList(n.getModuleNames(), arg, " to ", ", ", "");
        printer.println(";");
    }

    @Override
    public void visit(ModuleProvidesDirective n, Void arg) {
        printer.print("provides ");
        n.getName().accept(this, arg);
        printPrePostFixRequiredList(n.getWith(), arg, " with ", ", ", "");
        printer.println(";");
    }

    @Override
    public void visit(ModuleUsesDirective n, Void arg) {
        printer.print("uses ");
        n.getName().accept(this, arg);
        printer.println(";");
    }

    @Override
    public void visit(ModuleOpensDirective n, Void arg) {
        printer.print("opens ");
        n.getName().accept(this, arg);
        printPrePostFixOptionalList(n.getModuleNames(), arg, " to ", ", ", "");
        printer.println(";");
    }

    @Override
    public void visit(UnparsableStmt n, Void arg) {
        printer.print("???;");
    }

    private void printOrphanCommentsBeforeThisChildNode(final Node node) {
    }

    private void printOrphanCommentsAfterThisChildNode(final Node node) {
        if (getOption(ConfigOption.PRINT_COMMENTS).isEmpty()) return;
        printComment(node.getOrphanComments().stream().filter(LineComment.class::isInstance).filter(c -> "endregion".equals(c.getContent())).findFirst(), null);
    }

    private void printOrphanCommentsEnding(final Node node) {
    }

    private void indentIf(boolean expr) {
        if (expr)
            printer.indent();
    }

    private void unindentIf(boolean expr) {
        if (expr)
            printer.unindent();
    }

    private Optional<ConfigurationOption> getOption(ConfigOption cOption) {
        return configuration.get(new DefaultConfigurationOption(cOption));
    }
}

