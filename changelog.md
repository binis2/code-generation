# Change Log
**18-03-2023** ver. 1.0.4
* Core: Extended tools section.
* Generator: Removed dependencies to Spring packages.
* IDEA Plugin: Added find usages functionality.

**12-03-2023** ver. 1.0.3
* IDEA Plugin: Initial version.
* Generation: Changed generation information to contain full class name of the prototype.
* Projections: Engine now respects projection's annotations. 

**26-02-2023** ver. 1.0.2
* Core: Added generic mapping handling for interfaces.
* Code: Added Serializable handling via Mapper.
* Core: Added lazy initialization for CodeFactory objects.
* Core: Added mapping with parameters
* Annotation Processor: Annotation Processor behaves correctly when invoked inside tests. 
* Validation: Added support for typed collection validations.
* Validation: Added nicer way to describe validation messages.
* Validation: Added @ValidateNotEmpty validator.
* Validation: Added @Valid validator.
* Generation: Fixed imports for mixed in and nested objects.
* Generation: Added enricher dependencies.
* Generation: Renamed some PrototypeDescription's methods to more convenient names.
* Generation: More reliable type discovery. 
* Generation: Added discovery for generated interfaces.
* Jackson: Added String to Object mapping.
* Jackson: Better mapping for ValidationFormException.
* Redis: New experimental Redis module.

**28-01-2023** ver. 1.0.1
* Core: Added inheritance of custom prototypes.
* Core: Added class based mappings.
* Core: Added enum conversions.
* Generator: Added new code generation strategies: IMPLEMENTATION, NONE
* Generator: Added ability to create custom generated files.
* Jackson: Added fast routine to set up ObjectMapper to be aware of CodeGen objects.
* Spring: Fixed broken Async routines
* Annotation Processor: Fixed binis.properties serialization when multiple custom services are presented.  
* Annotation Processor: Fixed @CodeConfiguration service discovery in some cases.

**12-01-2023** ver. 1.0.0
* All: Migrated to Java 17
* Core: Added @CodeConfiguration (Class discovery)
* Core: CodeFactory improvements.
* Core: Added objects mapping and conversion support.
* Spring: Migrated to Spring 6 and Spring Boot 3
* Generator: Added support for custom prototypes.
* Hibernate: Migrated to Hibernate 6.1
* Jackson: Fixed deserialization of prototypes nested in collections.

**10-12-2022** ver. 0.4.3
* Core: CodeFactory can instantiate objects with parameters.
* Core: Added if/else routine to BaseModifier.
* Core: Added functional loop routines.
* Core: Added projections usability helper.
* Generator: Code generation for enums with constructors is less likely to fail because of duplicated parameter names.
* Generator: Added enrichers handling for single class expressions.
* Generator: Added constants handling for validation annotations.
* Generator: Fixed JPA annotations discovery when fragments are used. 

**20-10-2022** ver. 0.4.2
* Generator: Added @DefaultString annotation to ease the definition of the default string values.
* Generator: Fixed imports for compiled prototypes when used in combination with MixIn.

**29-09-2022** ver. 0.4.1
* Generator: Added ordinal offset for mixed in enum prototypes.

**19-09-2022** ver. 0.4.0
* Generator: Reimagined enumerations handling.
* Hibernate: New module to enable handling of generated enums by Hibernate.

**24-08-2022** ver. 0.3.10
* Generator: Added support for circular prototype dependencies.
* Spring: Added Map<> support for QueryEnricher.

**14-08-2022** ver. 0.3.9
* Core: Added .if() and ._self() routines to BaseModifier that gives access to currently modified object.
* Spring: Added .save() with projection for BaseEntityModifier
* Projections: Added projection of List and Set support.
* Generation: Fixed prototype detection when compiled prototypes are involved.

**08-08-2022** ver. 0.3.8
* Core: Added projection provider support.
* Generator: AsEnricher now supports projections.
* Spring: Handles projections via. registered projection provider.
* Projections: **(New module)** CodeGen own projections support.
* Spring-Configuration: Registering Spring's Spel projection as system projection provider if one is not present. 

**04-08-2022** ver. 0.3.7
* Spring: Added .notExists() query executor routine.
* Spring: Added .equal() for @OneToOne relations that accepts nested query.
* Spring: Fixed exception on .exists() and .count() in some cases.

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
  
