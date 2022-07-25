# Change Log
**25-07-2022** ver. 0.3.6
* Core: Moved async routines to core module.
* Generation: Cleaned up some unchecked warnings for generated files.
* Spring Configuration: Added actuator for monitoring async flows. 
* Spring: Redesigned exists() implementation.
* Spring: Added option to set alias for aggregation.
* Spring: Fixed aggregations for @OneToOne relations.

**07-07-2022** ver. 0.3.5
* Spring: Added logging of query processing time.
* Spring: Fixed mockCountQuery() when order part is set.
* Spring: Fixed collections size() querying.

**04-07-2022** ver. 0.3.4
* Test: Fixed mockExistsQuery() for identifiable entities.
* Jackson: Fixed validation handling after attempt parsing of invalid json.

**03-07-2022** ver. 0.3.3
* OpenApi: Added OpenApi enricher.
* Jackson: Added Jackson enricher.
* Jackson: Added handling of nested Validatable complex structures.
* Test: Added mockPageQuery() helper routines.
* Validation: Added form validation for collections.
* Spring Configuration: Added properties to enable printing of generated queries.
* Generation: @ForInterface works for field and type declarations as well.
* Generation: Fixed handling of nested prototypes is some cases.
* Generation: Fixed generation of prototype collections if the prototype is not embeddable.
* Spring: Fixed count() when order() is used.
* Spring: Fixed count() when page() is set.

**28-06-2022** ver. 0.3.2
* Validation: Fixed @AsCode validation/sanitization generation in some cases.
* Validation: Fixed custom validators/sanitizers that inherits @AsCode validators/sanitizers.

**26-06-2022** ver. 0.3.1
* Validation: Forms validation extends over the sub objects as well.
* Validation: Added options to expose validate() method to the generated interface.
* Spring: Added script() with params.
* Spring: Added in(T...).
* Spring: Fixed script() usage in some corner cases.
* Spring: Optimized exists() performance.

**12-06-2022** ver. 0.3.0
* Validation: **(New Feature)** Validation forms.
* Jackson: **(New Module)** Added Jackson support.
* Spring Configuration: **(New Module)** Spring Boot configuration to enable deserialization of generated objects via Jackson.
* Generator: **(Breaking change)** Added '$' suffix to sub-field initializers to remove ambiguity when using null param. 
* Core: New @CodeRequest object preset.
* Generator: Added enricher options functionality.
* Generator: Added "Hide create() option" to CreatorEnricher.
* Validation: Fixed possible wrong messages and params ordering for multi-message and multi-param validators.

**01-06-2022** ver. 0.2.5
* Spring: Fixed _self() return object when the selected object is embeddable as well.

**29-05-2022** ver. 0.2.4
* Generator: **(Breaking change)** User fields that collides with reserved words has _ suffix instead of _ prefix.   
* Generator: Fixed return type of certain embedded collections flows.
* Generator: Fixed BaseModifierImpl import for mixed in prototypes.
* Generator: Fixed query classes generation for mixed in prototypes.
* Validation: Fixed @AsCode handling for '{entity}' syntax.
* Spring: Renamed internal routines to reduce the possibility of clashes with user declared fields.

**25-05-2022** ver. 0.2.3
* Generator: Fixed modifier code generation for MixIn prototypes.

**24-05-2022** ver. 0.2.2
* Core: Improved enveloping factory routines.
* Spring: Added ability to generate update queries.
* Spring: Added total results counting when single page is requested.
* Test: Added mockDeleteQuery() routines.
* Test: Added persistence operation calls counting and intercepting based on class or operation.
* Spring: Fixed count() execution for prepared queries. 

**17-05-2022** ver. 0.2.1
* Spring: Added selecting, aggregating and ordering by bub property.
* Spring: Made QueryName queryable. 
* Spring: Made QueryAggregator scriptable.
* Generation: Proper handling of cascaded compiled prototypes.

**08-05-2022** ver. 0.2.0
* Generator: Fully rewritten modifier enrichment logic for better support of embedded object modifiers.
* Generator: **(Breaking change)** renamed most of the system routines with prefix "_" for consistency and better IDE filtering 
* Generator: Added @Ignore(forQuery).
* Spring: Added .isNull() and .isNotNull() routines for @OneToOne relations.
* Validation: Added basic support for custom validation annotations declared in same module as the prototype it is used on.
* Validation: Fixed .validateWithMessages() generation.

**26-04-2022** ver. 0.1.21
* Generator: Ensured proper ordering when enriching entities.

**25-04-2022** ver. 0.1.20
* Generator: Fixed instantiation of nested objects.

**24-04-2022** ver. 0.1.19
* Generator: Added support for nested prototypes.
* Generator: Added support for generic prototypes.
* Generator: Fixed @CodeClassAnnotations behavior.

**09-04-2022** ver. 0.1.18
* Generator: Added @Initializer annotation to initialize fields inside default constructor.
* Generator: Added ability to override fields in base implementation.
* Generator: Better handling of default annotation params for compiled prototypes.
* Generator: Added _if() for modifiers.
* Spring: Added leftJoin() for single relations.

**05-04-2022** ver. 0.1.17
* Spring: Added join() for single relations.
* Spring: Added group() to aggregations.
* Spring: **(Breaking change)** distinct() no longer groups by that column.
* Validation: Added option to create validations with multiple error messages.

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
  
