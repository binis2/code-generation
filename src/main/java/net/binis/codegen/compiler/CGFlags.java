package net.binis.codegen.compiler;

/**
 * Copy of com.sun.tools.javac.code.Flags
 *
 * Access flags and other modifiers for Java classes and members.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class CGFlags {

    private CGFlags() {} // uninstantiable

    /* Standard Java flags.
     */
    public static final int PUBLIC       = 1;
    public static final int PRIVATE      = 1<<1;
    public static final int PROTECTED    = 1<<2;
    public static final int STATIC       = 1<<3;
    public static final int FINAL        = 1<<4;
    public static final int SYNCHRONIZED = 1<<5;
    public static final int VOLATILE     = 1<<6;
    public static final int TRANSIENT    = 1<<7;
    public static final int NATIVE       = 1<<8;
    public static final int INTERFACE    = 1<<9;
    public static final int ABSTRACT     = 1<<10;
    public static final int STRICTFP     = 1<<11;

    /* Flag that marks a symbol synthetic, added in classfile v49.0. */
    public static final int SYNTHETIC    = 1<<12;

    /** Flag that marks attribute interfaces, added in classfile v49.0. */
    public static final int ANNOTATION   = 1<<13;

    /** An enumeration type or an enumeration constant, added in
     *  classfile v49.0. */
    public static final int ENUM         = 1<<14;

    /** Added in SE8, represents constructs implicitly declared in source. */
    public static final int MANDATED     = 1<<15;

    public static final int StandardFlags = 0x0fff;

    // Because the following access flags are overloaded with other
    // bit positions, we translate them when reading and writing class
    // files into unique bits positions: ACC_SYNTHETIC <-> SYNTHETIC,
    // for example.
    public static final int ACC_SUPER    = 0x0020;
    public static final int ACC_BRIDGE   = 0x0040;
    public static final int ACC_VARARGS  = 0x0080;
    public static final int ACC_MODULE   = 0x8000;

    /*****************************************
     * Internal compiler flags (no bits in the lower 16).
     *****************************************/

    /** Flag is set if symbol is deprecated.  See also DEPRECATED_REMOVAL.
     */
    public static final int DEPRECATED   = 1<<17;

    /** Flag is set for a variable symbol if the variable's definition
     *  has an initializer part.
     */
    public static final int HASINIT          = 1<<18;

    /** Flag is set for compiler-generated anonymous method symbols
     *  that `own' an initializer block.
     */
    public static final int BLOCK            = 1<<20;

    /** Flag bit 21 is available. (used earlier to tag compiler-generated abstract methods that implement
     *  an interface method (Miranda methods)).
     */

    /** Flag is set for nested classes that do not access instance members
     *  or `this' of an outer class and therefore don't need to be passed
     *  a this$n reference.  This value is currently set only for anonymous
     *  classes in superclass constructor calls.
     *  todo: use this value for optimizing away this$n parameters in
     *  other cases.
     */
    public static final int NOOUTERTHIS  = 1<<22;

    /** Flag is set for package symbols if a package has a member or
     *  directory and therefore exists.
     */
    public static final int EXISTS           = 1<<23;

    /** Flag is set for compiler-generated compound classes
     *  representing multiple variable bounds
     */
    public static final int COMPOUND     = 1<<24;

    /** Flag is set for class symbols if a class file was found for this class.
     */
    public static final int CLASS_SEEN   = 1<<25;

    /** Flag is set for class symbols if a source file was found for this
     *  class.
     */
    public static final int SOURCE_SEEN  = 1<<26;

    /* State flags (are reset during compilation).
     */

    /** Flag for class symbols is set and later re-set as a lock in
     *  Enter to detect cycles in the superclass/superinterface
     *  relations.  Similarly for constructor call cycle detection in
     *  Attr.
     */
    public static final int LOCKED           = 1<<27;

    /** Flag for class symbols is set and later re-set to indicate that a class
     *  has been entered but has not yet been attributed.
     */
    public static final int UNATTRIBUTED = 1<<28;

    /** Flag for synthesized default constructors of anonymous classes.
     */
    public static final int ANONCONSTR   = 1<<29; //non-class members

    /**
     * Flag to indicate the super classes of this ClassSymbol has been attributed.
     */
    public static final int SUPER_OWNER_ATTRIBUTED = 1<<29; //ClassSymbols

    /** Flag for class symbols to indicate it has been checked and found
     *  acyclic.
     */
    public static final int ACYCLIC          = 1<<30;

    /** Flag that marks bridge methods.
     */
    public static final long BRIDGE          = 1L<<31;

    /** Flag that marks formal parameters.
     */
    public static final long PARAMETER   = 1L<<33;

    /** Flag that marks varargs methods.
     */
    public static final long VARARGS   = 1L<<34;

    /** Flag for annotation type symbols to indicate it has been
     *  checked and found acyclic.
     */
    public static final long ACYCLIC_ANN      = 1L<<35;

    /** Flag that marks a generated default constructor.
     */
    public static final long GENERATEDCONSTR   = 1L<<36;

    /** Flag that marks a hypothetical method that need not really be
     *  generated in the binary, but is present in the symbol table to
     *  simplify checking for erasure clashes - also used for 292 poly sig methods.
     */
    public static final long HYPOTHETICAL   = 1L<<37;

    /**
     * Flag that marks an internal proprietary class.
     */
    public static final long PROPRIETARY = 1L<<38;

    /**
     * Flag that marks a multi-catch parameter.
     */
    public static final long UNION = 1L<<39;

    /**
     * Flags an erroneous TypeSymbol as viable for recovery.
     * TypeSymbols only.
     */
    public static final long RECOVERABLE = 1L<<40;

    /**
     * Flag that marks an 'effectively final' local variable.
     */
    public static final long EFFECTIVELY_FINAL = 1L<<41;

    /**
     * Flag that marks non-override equivalent methods with the same signature,
     * or a conflicting match binding (BindingSymbol).
     */
    public static final long CLASH = 1L<<42;

    /**
     * Flag that marks either a default method or an interface containing default methods.
     */
    public static final long DEFAULT = 1L<<43;

    /**
     * Flag that marks class as auxiliary, ie a non-public class following
     * the public class in a source file, that could block implicit compilation.
     */
    public static final long AUXILIARY = 1L<<44;

    /**
     * Flag that marks that a symbol is not available in the current profile
     */
    public static final long NOT_IN_PROFILE = 1L<<45;

    /**
     * Flag that indicates that an override error has been detected by Check.
     */
    public static final long BAD_OVERRIDE = 1L<<45;

    /**
     * Flag that indicates a signature polymorphic method (292).
     */
    public static final long SIGNATURE_POLYMORPHIC = 1L<<46;

    /**
     * Flag that indicates that an inference variable is used in a 'throws' clause.
     */
    public static final long THROWS = 1L<<47;

    /**
     * Flag that marks potentially ambiguous overloads
     */
    public static final long POTENTIALLY_AMBIGUOUS = 1L<<48;

    /**
     * Flag that marks a synthetic method body for a lambda expression
     */
    public static final long LAMBDA_METHOD = 1L<<49;

    /**
     * Flag to control recursion in TransTypes
     */
    public static final long TYPE_TRANSLATED = 1L<<50;

    /**
     * Flag to indicate class symbol is for module-info
     */
    public static final long MODULE = 1L<<51;

    /**
     * Flag to indicate the given ModuleSymbol is an automatic module.
     */
    public static final long AUTOMATIC_MODULE = 1L<<52; //ModuleSymbols only

    /**
     * Flag to indicate the given PackageSymbol contains any non-.java and non-.class resources.
     */
    public static final long HAS_RESOURCE = 1L<<52; //PackageSymbols only

    /**
     * Flag to indicate the given ParamSymbol has a user-friendly name filled.
     */
    public static final long NAME_FILLED = 1L<<52; //ParamSymbols only

    /**
     * Flag to indicate the given ModuleSymbol is a system module.
     */
    public static final long SYSTEM_MODULE = 1L<<53; //ModuleSymbols only

    /**
     * Flag to indicate the given ClassSymbol is a value based.
     */
    public static final long VALUE_BASED = 1L<<53; //ClassSymbols only

    /**
     * Flag to indicate the given symbol has a @Deprecated annotation.
     */
    public static final long DEPRECATED_ANNOTATION = 1L<<54;

    /**
     * Flag to indicate the given symbol has been deprecated and marked for removal.
     */
    public static final long DEPRECATED_REMOVAL = 1L<<55;

    /**
     * Flag to indicate the API element in question is for a preview API.
     */
    public static final long PREVIEW_API = 1L<<56; //any Symbol kind

    /**
     * Flag for synthesized default constructors of anonymous classes that have an enclosing expression.
     */
    public static final long ANONCONSTR_BASED = 1L<<57;

    /**
     * Flag that marks finalize block as body-only, should not be copied into catch clauses.
     * Used to implement try-with-resources.
     */
    public static final long BODY_ONLY_FINALIZE = 1L<<17; //blocks only

    /**
     * Flag to indicate the API element in question is for a preview API.
     */
    public static final long PREVIEW_REFLECTIVE = 1L<<58; //any Symbol kind

    /**
     * Flag to indicate the given variable is a match binding variable.
     */
    public static final long MATCH_BINDING = 1L<<59;

    /**
     * A flag to indicate a match binding variable whose scope extends after the current statement.
     */
    public static final long MATCH_BINDING_TO_OUTER = 1L<<60;

    /**
     * Flag to indicate that a class is a record. The flag is also used to mark fields that are
     * part of the state vector of a record and to mark the canonical constructor
     */
    public static final long RECORD = 1L<<61; // ClassSymbols, MethodSymbols and VarSymbols

    /**
     * Flag to mark a record constructor as a compact one
     */
    public static final long COMPACT_RECORD_CONSTRUCTOR = 1L<<51; // MethodSymbols only

    /**
     * Flag to mark a record field that was not initialized in the compact constructor
     */
    public static final long UNINITIALIZED_FIELD= 1L<<51; // VarSymbols only

    /** Flag is set for compiler-generated record members, it could be applied to
     *  accessors and fields
     */
    public static final int GENERATED_MEMBER = 1<<24; // MethodSymbols and VarSymbols

    /**
     * Flag to indicate sealed class/interface declaration.
     */
    public static final long SEALED = 1L<<62; // ClassSymbols

    /**
     * Flag to indicate that the class/interface was declared with the non-sealed modifier.
     */
    public static final long NON_SEALED = 1L<<63; // ClassSymbols

}
