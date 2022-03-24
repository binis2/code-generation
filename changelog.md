# Change Log

**24-03-2022** ver. 0.1.16
* Generator: Fixed field generics handling for compiled prototypes.

**20-03-2022** ver. 0.1.15
* Generator: Added ability to define method body with string literal.
* Spring: Fixed projection complex field discovery when inheritance is involved.
* Generator: Fixed @Default handling when normal annotation expression is used.
* Generator: Fixed redundant field creation in some cases.

**13-03-2022** ver. 0.1.14
* Generator: New experimental feature - Query fragments.
* Generator: Added ability to @Keep default method implementations in the generated interface.
* Validation: Fixed generation of consecutive sanitization annotations.

**09-03-2022** ver. 0.1.13
* Generator: Improved generics handling for external types.
* Generator: Fixed inherited enrichers for compiled types.

**28-02-2022** ver. 0.1.12
* Generator: Proper handling of annotation values imports for compiled prototypes.
* Generator: Added resolving of prototypes with their full names.
* Spring: Better handling of single column selects or aggregations.
* Spring: Added toString() implementation for TupleBackedProjection.

**15-02-2022** ver. 0.1.11
* Generator: Fixed issue that can prevent code generation when compiled type is used.

**13-02-2022** ver. 0.1.10
* Generator: Added support for compiled prototypes used as types in another prototypes.
* Code: Added @Embeddable annotation to enable explicit collection embedding.
* Spring: Added @Joinable annotation to enable explicit collection joining.

**06-02-2022** ver. 0.1.9
* Core: Added feature to specify specific destination path for interfaces and implementations.
* Annotation: Files generated from compiled prototypes won't be saved by the annotation processor.
* Spring: Fixed initialization of AsyncDispatcher.

**01-02-2022** ver. 0.1.8
* Spring: Renamed async consumer methods to avoid compilation issues.
* Annotation: Fixed code generation from source code.

**31-01-2022** ver. 0.1.7
* Spring: Added CompletableFeature support.
* Spring: Added in() support for OneToOne relations.
* Generation: Improved support for compiled prototypes.

**17-01-2022** ver. 0.1.6
* Spring: Added support for different async flows.
* Spring: Added query logging.
* Spring: Fixed 'in' expressions for empty or null collections.

**08-01-2022** ver. 0.1.5
* Spring: Added support for references.
* Spring: Better 'distinct' support. 
* Spring: Fixed aggregations type mapping for single column queries.

**28-12-2021** ver. 0.1.4
* Spring: Static projections.
* Spring: Added join fetch for OneToOne relations.
* Generation: Fixed field generation from external setter

**19-12-2021** ver. 0.1.3
* Spring: Added left join support.
* Spring: Fixed .count() for join queries.
* Generation: Added RegionEnricher.
* Test: Better error handling for using mocks and matchers without CodeGenExtension.
* Annotation: Added support for non prototype interfaces declared in the same package and the prototypes. 

**28-11-2021** ver. 0.1.2
* Core: Improved capabilities for mocking object creation.
* Generation: More streamlined way to indicate default implementation.
* Validation: All routines print the name of the field in question by default.
* Test: Query execution count errors won't show if the test failed beforehand.
* Test: Seamless query mocking for join queries.
* Test: Added feature to execute code on persistence operation.

**23-11-2021** ver. 0.1.1
* Test: Added JUnit extension.
* Test: Added feature to count execution of mocked queries.
* Test: Fixed any() mocking for operations.

**21-11-2021** ver. 0.1.0
* Initial release.
  
