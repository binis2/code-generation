package net.binis.codegen.compiler;

import net.binis.codegen.tools.Reflection;
import net.binis.codegen.compiler.base.JavaCompilerObject;

import static net.binis.codegen.tools.Reflection.loadClass;

public class CGTag extends JavaCompilerObject {

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.tree.JCTree$Tag");
    }

    /**
     * For methods that return an invalid tag if a given condition is not met
     */
    public static CGTag NO_TAG = initFlag("NO_TAG");

    /**
     * Toplevel nodes, of type TopLevel, representing entire source files.
     */
    public static CGTag TOPLEVEL = initFlag("TOPLEVEL");

    /**
     * Package level definitions.
     */
    public static CGTag PACKAGEDEF = initFlag("PACKAGEDEF");

    /**
     * Import clauses, of type Import.
     */
    public static CGTag IMPORT = initFlag("IMPORT");

    /**
     * Class definitions, of type ClassDef.
     */
    public static CGTag CLASSDEF = initFlag("CLASSDEF");

    /**
     * Method definitions, of type MethodDef.
     */
    public static CGTag METHODDEF = initFlag("METHODDEF");

    /**
     * Variable definitions, of type VarDef.
     */
    public static CGTag VARDEF = initFlag("VARDEF");

    /**
     * The no-op statement ";", of type Skip
     */
    public static CGTag SKIP = initFlag("SKIP");

    /**
     * Blocks, of type Block.
     */
    public static CGTag BLOCK = initFlag("BLOCK");

    /**
     * Do-while loops, of type DoLoop.
     */
    public static CGTag DOLOOP = initFlag("DOLOOP");

    /**
     * While-loops, of type WhileLoop.
     */
    public static CGTag WHILELOOP = initFlag("WHILELOOP");

    /**
     * For-loops, of type ForLoop.
     */
    public static CGTag FORLOOP = initFlag("FORLOOP");

    /**
     * Foreach-loops, of type ForeachLoop.
     */
    public static CGTag FOREACHLOOP = initFlag("FOREACHLOOP");

    /**
     * Labelled statements, of type Labelled.
     */
    public static CGTag LABELLED = initFlag("LABELLED");

    /**
     * Switch statements, of type Switch.
     */
    public static CGTag SWITCH = initFlag("SWITCH");

    /**
     * Case parts in switch statements/expressions, of type Case.
     */
    public static CGTag CASE = initFlag("CASE");

    /**
     * Switch expression statements, of type Switch.
     */
    public static CGTag SWITCH_EXPRESSION = initFlag("SWITCH_EXPRESSION");

    /**
     * Synchronized statements, of type Synchronized.
     */
    public static CGTag SYNCHRONIZED = initFlag("SYNCHRONIZED");

    /**
     * Try statements, of type Try.
     */
    public static CGTag TRY = initFlag("TRY");

    /**
     * Catch clauses in try statements, of type Catch.
     */
    public static CGTag CATCH = initFlag("CATCH");

    /**
     * Conditional expressions, of type Conditional.
     */
    public static CGTag CONDEXPR = initFlag("CONDEXPR");

    /**
     * Conditional statements, of type If.
     */
    public static CGTag IF = initFlag("IF");

    /**
     * Expression statements, of type Exec.
     */
    public static CGTag EXEC = initFlag("EXEC");

    /**
     * Break statements, of type Break.
     */
    public static CGTag BREAK = initFlag("BREAK");

    /**
     * Yield statements, of type Yield.
     */
    public static CGTag YIELD = initFlag("YIELD");

    /**
     * Continue statements, of type Continue.
     */
    public static CGTag CONTINUE = initFlag("CONTINUE");

    /**
     * Return statements, of type Return.
     */
    public static CGTag RETURN = initFlag("RETURN");

    /**
     * Throw statements, of type Throw.
     */
    public static CGTag THROW = initFlag("THROW");

    /**
     * Assert statements, of type Assert.
     */
    public static CGTag ASSERT = initFlag("ASSERT");

    /**
     * Method invocation expressions, of type Apply.
     */
    public static CGTag APPLY = initFlag("APPLY");

    /**
     * Class instance creation expressions, of type NewClass.
     */
    public static CGTag NEWCLASS = initFlag("NEWCLASS");

    /**
     * Array creation expressions, of type NewArray.
     */
    public static CGTag NEWARRAY = initFlag("NEWARRAY");

    /**
     * Lambda expression, of type Lambda.
     */
    public static CGTag LAMBDA = initFlag("LAMBDA");

    /**
     * Parenthesized subexpressions, of type Parens.
     */
    public static CGTag PARENS = initFlag("PARENS");

    /**
     * Assignment expressions, of type Assign.
     */
    public static CGTag ASSIGN = initFlag("ASSIGN");

    /**
     * Type cast expressions, of type TypeCast.
     */
    public static CGTag TYPECAST = initFlag("TYPECAST");

    /**
     * Type test expressions, of type TypeTest.
     */
    public static CGTag TYPETEST = initFlag("TYPETEST");

    /**
     * Patterns.
     */
    public static CGTag ANYPATTERN = initFlag("ANYPATTERN");
    public static CGTag BINDINGPATTERN = initFlag("BINDINGPATTERN");
    public static CGTag RECORDPATTERN = initFlag("RECORDPATTERN");

    /* Case labels.
     */
    public static CGTag DEFAULTCASELABEL = initFlag("DEFAULTCASELABEL");
    public static CGTag CONSTANTCASELABEL = initFlag("CONSTANTCASELABEL");
    public static CGTag PATTERNCASELABEL = initFlag("PATTERNCASELABEL");

    /**
     * Indexed array expressions, of type Indexed.
     */
    public static CGTag INDEXED = initFlag("INDEXED");

    /**
     * Selections, of type Select.
     */
    public static CGTag SELECT = initFlag("SELECT");

    /**
     * Member references, of type Reference.
     */
    public static CGTag REFERENCE = initFlag("REFERENCE");

    /**
     * Simple identifiers, of type Ident.
     */
    public static CGTag IDENT = initFlag("IDENT");

    /**
     * Literals, of type Literal.
     */
    public static CGTag LITERAL = initFlag("LITERAL");

    /**
     * String template expression.
     */
    public static CGTag STRING_TEMPLATE = initFlag("STRING_TEMPLATE");

    /**
     * Basic type identifiers, of type TypeIdent.
     */
    public static CGTag TYPEIDENT = initFlag("TYPEIDENT");

    /**
     * Array types, of type TypeArray.
     */
    public static CGTag TYPEARRAY = initFlag("TYPEARRAY");

    /**
     * Parameterized types, of type TypeApply.
     */
    public static CGTag TYPEAPPLY = initFlag("TYPEAPPLY");

    /**
     * Union types, of type TypeUnion.
     */
    public static CGTag TYPEUNION = initFlag("TYPEUNION");

    /**
     * Intersection types, of type TypeIntersection.
     */
    public static CGTag TYPEINTERSECTION = initFlag("TYPEINTERSECTION");

    /**
     * Formal type parameters, of type TypeParameter.
     */
    public static CGTag TYPEPARAMETER = initFlag("TYPEPARAMETER");

    /**
     * Type argument.
     */
    public static CGTag WILDCARD = initFlag("WILDCARD");

    /**
     * Bound kind: extends, super, exact, or unbound
     */
    public static CGTag TYPEBOUNDKIND = initFlag("TYPEBOUNDKIND");

    /**
     * metadata: Annotation.
     */
    public static CGTag ANNOTATION = initFlag("ANNOTATION");

    /**
     * metadata: Type annotation.
     */
    public static CGTag TYPE_ANNOTATION = initFlag("TYPE_ANNOTATION");

    /**
     * metadata: Modifiers
     */
    public static CGTag MODIFIERS = initFlag("MODIFIERS");

    /**
     * An annotated type tree.
     */
    public static CGTag ANNOTATED_TYPE = initFlag("ANNOTATED_TYPE");

    /**
     * Error trees, of type Erroneous.
     */
    public static CGTag ERRONEOUS = initFlag("ERRONEOUS");

    /**
     * Unary operators, of type Unary.
     */
    public static CGTag POS = initFlag("POS");                             // +
    public static CGTag NEG = initFlag("NEG");                             // -
    public static CGTag NOT = initFlag("NOT");                             // !
    public static CGTag COMPL = initFlag("COMPL");                           // ~
    public static CGTag PREINC = initFlag("PREINC");                          // ++ _
    public static CGTag PREDEC = initFlag("PREDEC");                          // -- _
    public static CGTag POSTINC = initFlag("POSTINC");                         // _ ++
    public static CGTag POSTDEC = initFlag("POSTDEC");                         // _ --

    /**
     * unary operator for null reference checks, only used internally.
     */
    public static CGTag NULLCHK = initFlag("NULLCHK");

    /**
     * Binary operators, of type Binary.
     */
    public static CGTag OR = initFlag("OR");                              // ||
    public static CGTag AND = initFlag("AND");                             // &&
    public static CGTag BITOR = initFlag("BITOR");                           // |
    public static CGTag BITXOR = initFlag("BITXOR");                          // ^
    public static CGTag BITAND = initFlag("BITAND");                          // &
    public static CGTag EQ = initFlag("EQ");                              // ==
    public static CGTag NE = initFlag("NE");                              // !=
    public static CGTag LT = initFlag("LT");                              // <
    public static CGTag GT = initFlag("GT");                              // >
    public static CGTag LE = initFlag("LE");                              // <=
    public static CGTag GE = initFlag("GE");                              // >=
    public static CGTag SL = initFlag("SL");                              // <<
    public static CGTag SR = initFlag("SR");                              // >>
    public static CGTag USR = initFlag("USR");                             // >>>
    public static CGTag PLUS = initFlag("PLUS");                            // +
    public static CGTag MINUS = initFlag("MINUS");                           // -
    public static CGTag MUL = initFlag("MUL");                             // *
    public static CGTag DIV = initFlag("DIV");                             // /
    public static CGTag MOD = initFlag("MOD");                             // %

    /**
     * Assignment operators, of type Assignop.
     */
    public static CGTag BITOR_ASG = initFlag("BITOR_ASG");                // |=

    public static CGTag BITXOR_ASG = initFlag("BITXOR_ASG");              // ^=

    public static CGTag BITAND_ASG = initFlag("BITAND_ASG");              // &=

    public static CGTag SL_ASG = initFlag("SL_ASG");                      // <<=

    public static CGTag SR_ASG = initFlag("SR_ASG");                      // >>=

    public static CGTag USR_ASG = initFlag("USR_ASG");                    // >>>=

    public static CGTag PLUS_ASG = initFlag("PLUS_ASG");                  // +=

    public static CGTag MINUS_ASG = initFlag("MINUS_ASG");                // -=

    public static CGTag MUL_ASG = initFlag("MUL_ASG");                    // *=

    public static CGTag DIV_ASG = initFlag("DIV_ASG");                    // /=

    public static CGTag MOD_ASG = initFlag("MOD_ASG");                    // %=

    public static CGTag MODULEDEF = initFlag("MODULEDEF");
    public static CGTag EXPORTS = initFlag("EXPORTS");
    public static CGTag OPENS = initFlag("OPENS");
    public static CGTag PROVIDES = initFlag("PROVIDES");
    public static CGTag REQUIRES = initFlag("REQUIRES");
    public static CGTag USES = initFlag("USES");

    /**
     * A synthetic let expression, of type LetExpr.
     */
    public static CGTag LETEXPR = initFlag("LETEXPR");                         // ala scheme

    public static CGTag initFlag(String tag) {
        return new CGTag(tag);
    }

    public CGTag(String tag) {
        super();
        instance = Reflection.invokeStatic("valueOf", cls, tag);
    }

    @Override
    protected void init() {
        cls = CGTag.theClass();
    }
}
