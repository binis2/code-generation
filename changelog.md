# Change Log
**02-11-2025** ver. 1.2.31
* Core: Improved mapping discovery.
* Core: Improved mapping of inherited data objects.
* Generation: Added ability to define toString and clone methods for prototypes.
* Hibernate: Added Tuple to Object mapping.
* Jackson: Moved Map To Object mapping into Jackson module.
* Jackson: Fixed serialization of custom collections (ex. ImmutableObjects)
* Spring: Fixed query references.
* Spring: Improved tuples handling.

**15-10-2025** ver. 1.2.30
* Core: Better mapping handling for asymmetric structures
* Generation: Handling of class expressions for validation params.
* Spring Configuration: Added map to object convertor.
* Validation: Added enum validator.

**08-10-2025** ver. 1.2.29
* Core: Added register mapping function with key
* Core: Force initializing a code enum when calling enumValueOf
* Core: Added mapping for conversions between Collections and Arrays
* Generation: Added support for static functions in prototypes.
* Generation: Better support for nested types
* Generation: Better handling for overridden fields.
* Spring Configuration: Added mappers for String to Object with JSON and XML mapping keys.

**22-08-2025** ver. 1.2.28
* Core: Added support for virtual threads for **Async** routines.

**01-06-2025** ver. 1.2.27
* Core: Added lock capabilities to **Async** flow. 
* Core: Updated constructor enrichers' error descriptions to match latest IDEA's.
* Core: Added @Generated
* Generation: MixIn enums are marked as generated.
* Generation: Added @Default for MixIn enums.

**18-05-2025** ver. 1.2.26
* Core: Added ability to declare producer with custom mapping builder
* Spring Configuration: Registered explicit XML conversions Object -> String (if jackson xml module is included in the project)
* Generation: Fixed @Default creation for nested enums.
* Generation: Fixed generation of modifiers for lists of enum prototypes
* All: Updated dependencies.

**09-05-2025** ver. 1.2.25
* Core: Fixed invocation of custom matchers for abstract source -> destination combinations.
* Projections: Allowed path projections to consider all available methods, not just the getters.

**01-05-2025** ver. 1.2.24
* Validation: Added 'AllBut' validations. All validations that have AllBut in the name will be applied for all the fields of the prototype but on the declared field.
* Validation: Added more descriptive field name when dealing with collection validations.
* Validation: Fixed Executions and Sanitizations for collections.

**24-01-2025** ver. 1.2.23
* Generation: Added @CodeDocumentation annotation.
* IDEA Plugin: Documented elements with @CodeDocumentation are shown as tooltip in IDEA.
* All: Updated dependencies.

**20-10-2024** ver. 1.2.22
* Generation: Fixed loading of custom annotation parameters when prototype template is in the same compilation cycle as the targeted class.
* Generation: Added Element annotation utils to work with class name passed as string.
* All: Updated dependencies. 

**07-10-2024** ver. 1.2.21
* Generation: Extended element enrichment toolset.

**15-09-2024** ver. 1.2.20
* Core: Added @Ordinal annotation to allow setting specific ordinal value for enum entry. 

**24-08-2024** ver. 1.2.19
* Core: (**Breaking change**) @CodeMapping annotation removed. Use Ignores(forMapping = true)
* Core: Added @CodeSnippet prototype template (generateImplementation = false, interfaceSetters = false). 
* Generator: Made @CodeFieldAnnotations smarter. It will only include annotations that are discoverable by the compilation scope. 

**01-08-2024** ver. 1.2.18
* Core: Improved reflection routines to call inaccessible static methods.

**16-07-2024** ver. 1.2.17
* Generator: Added helper routines to add multiple imports with one function call.
* Generator: @Ignores(forSerialization = true) makes the field transient.

**27-06-2024** ver. 1.2.16
* Generator: Fixed handling of array types.
* Generator: Fixed usage of prototypes in default prototype methods.
* Generator: Added ability to define equals and hashCode functions via prototypes.
* All: Updated dependencies.

**08-05-2024** ver. 1.2.15
* Core: All CodeFactory internal collections are now thread-safe.

**18-04-2024** ver. 1.2.14
* Projections: Fixed interaction between generic and non-generic custom projections.
* All: Updated dependencies.

**27-03-2024** ver. 1.2.13
* Generator: Made generated initializers static.
* Generator: The order of constructors and generated methods is guaranteed.
* Test: CodeGenMock registers entity manager in CodeFactory.

**19-03-2024** ver. 1.2.12
* Generator: OpenAPIEnricher respects present @Schema annotations.

**07-03-2024** ver. 1.2.11
* Generator: The order of fields for compiled prototypes is now guaranteed.
* Generator: Custom annotation properties now are initialized with clone of the original expression.
* Projections: Added ability to create proxies with multiple interfaces.

**27-02-2024** ver. 1.2.10
* Generator: Fixed imports when generics and prototypes are involved.
* Generator: Fixed handling of extended compiled prototypes.

**25-02-2024** ver. 1.2.9
* Annotation Processor: Added option to control destination of generated files.
* Annotation Processor: Added support for annotation processor options.
* Generator: Extended Tools. (@Theo Gilonis)

**18-02-2024** ver. 1.2.8
* Generator: Added support for annotation processor options.
* Generator: Added BaseConditionalEnricher.
* Annotation Processor: Fixed possible NPE.

**01-02-2024** ver. 1.2.7
* Core: Improved CodeFactory.envelop() type routines (@Theo Gilonis)
* Generator: Improved field full type discovery (@Theo Gilonis)
* Generator: OpenApiEnricher now correctly adds type=string for enums (@Theo Gilonis)

**30-01-2024** ver. 1.2.6
* Generator: findElement works for enums.
* Generator: By default new param elements are with pos = 0
* Generator: surroundWithTryCatch routine now points to the start of the surrounded block.
* Spring Configuration: Registered explicit JSON conversions Object->String

**28-01-2024** ver. 1.2.5
* Core: Added default mapping for String->UUID.
* Core: Added more modifying routines to CodeList, CodeSet and CodeMap. 
* Generator: OpenApiEnricher adds @ArraySchema to collections (thx @Theo Gilonis).
* Projections: Added Mapper.convert() call to projections if the source and projected types don't match.
* IDEA Plugin: Fixed false positive validation targets error when target is assignable from the annotated type.

**22-01-2024** ver. 1.2.4
* Generator: @Include/@Ignore works on plain interfaces as expected.
* Generator: Fixed imports and type handling for annotated plain interfaces.
* Generator: More element wrappers and utils.
* Validation: @ValidateNotEmpty targets collections as well.
* ALL: Updated licenses to 2024.
* ALL: Updated dependencies.

**15-01-2024** ver. 1.2.3
* Core: Added @Include annotation.
* Core: Added augment types: GETTER, SETTER
* Generation: Added prototype ToStringEnricher.
* Generation: Added element GetterEnricher
* Generation: Added element SetterEnricher
* Annotation Processor: Moved utility functions to generation module.
* IDEA Plugin: Added support for augmenting getters and setters.

**11-01-2024** ver. 1.2.2
* Core: Fixed concurrency issue with CodeFactory.

**10-01-2024** ver. 1.2.1
* Core: Added CodeMapper annotation.
* Core: Added method to CodeFactory to get all registered enums.
* Core: Reduced log spam if field is not found with Reflection. 

**07-01-2024** ver. 1.2.0
* Core: Mapper can now register additional mappers/converters for same type combinations.
* Generator: Added OpenApiElementEnricher.
* Generator: (**Breaking change**) Some annotation element enrichment methods were striped off from redundant parameters.
* Generator: Extended annotation element enrichment utils.
* Generator: Fixed OpenApiEnricher required flag generation. 
* Projections: Fixed discovery and execution of interface methods.

**03-01-2024** ver. 1.1.29
* Core: Class casting is used if every other mapping strategy fails.
* Generation: Added support for nested enums.
* Generation: Extended OpenApiEnricher to support enums.
* Generation: OpenApiEnricher annotates non-declared fields as well.
* Generation: Improved field getter/setter generation utils.
* Spring Configuration: CodeEnum serialization enabled. 
* IDEA Plugin: Added checks for already existing elements before augmenting classes.

**02-01-2024** ver. 1.1.28
* Core: Added MappingStrategy - FIELDS
* Generation: Improved method element discovery if the parameter type is ErrorType.
* Generation: Nested prototypes located in class prototypes are now properly discovered. 
* Validation: Simplified generated lambda expressions for modifiers.
* Jackson: Added serialization for @CodeEnum.

**29-12-2023** ver. 1.1.27
* Core: CodeFactory default creation now uses external factories as well.
* Core: Extended Holder to support stream operations.
* Generator: Improved field type discovery for QueryEnricher.
* Generator: Fixed recursive type handling when mixIn is used.

**27-12-2023** ver. 1.1.26
* Core: New discoverer type - INIT.
* Core: Added support for custom discoverer types.
* Test: Added enablePreview() Java features for tests.

**23-12-2023** ver. 1.1.25
* Generation: Fixed discovery of generic types.
* Generation: Better handling of generics for CGVarSymbol.
* Generation: Improved default method body handling.
* Generation: Better methodExists() handling.

**19-12-2023** ver. 1.1.24
* Core: Made CodeEnum serializable.
* Generation: Fixed imports for inherited fields.
* Generation: Fixed missing annotations for inherited fields.

**17-12-2023** ver. 1.1.23
* Core: Extended reflection routines.
* Generation: Prototype field description types consistently points to the generated type.
* Generation: Fixed usage of generic interfaces with non-generic fields.
* Generation: Fixed generation of generic external interfaces + prototypes.
* Generation: Fixed generation of setters from external interfaces.
* Spring: Added ability to mock transactions event if there is Spring context present.

**11-12-2023** ver. 1.1.22
* Core: Added object proxy routines to CodeFactory.
* Code: Fixed mapping failure if there are duplicated method signatures.
* Generation: Default method implementations will prefer getters to direct field access.
* Generation: Fixed default method implementation when field is not processed yet.
* Generation: Fixed code generation failure when external interface is used in combination with prototype.
* Annotation Processor: Detected sources roots are exposed to enrichers.
* Jackson: Better collection detection handling.
* Projection: Added object proxy support.
* Spring Configuration: Improved bridge between Spring application context and CodeFactory.
* IDEA Plugin: Updated to support IDEA 2023.3

**03-12-2023** ver. 1.1.21
* Core: Added ability to propagate exception when using CodeFactory instantiation routines. 
* Generation: Improved collection type discovery.
* Generation: Added safeguard for unparsable external prototypes.
* Generation: @CodeImplementation works for normal prototypes as well.
* Generation: Fixed collection type flag for custom fields.
* Annotation Processor: Improved handling of partial compilation.

**26-11-2023** ver. 1.1.20
* ALL: Updated dependencies. (Spring 6.1, Spring Boot 3.2)
* Generation: Fields added by enrichers are marked as custom.
* Generation: Fixed generation of class setters for external interfaces. 
* Hibernate: Updated to support Hibernate 6.3
* Hibernate: Added support for collections of enums in Hibernate enricher.

**22-11-2023** ver. 1.1.19
* Generation: Fixed discovery of inherited prototype properties.
* Generation: Fixed element matching when generics are involved.

**19-11-2023** ver. 1.1.18
* Generation: Added discovery of custom compiled enum prototypes.
* Generation: Improved compiler element discovery.
* Generation: Improved element wrapper routines.
* Hibernate: Smarter Hibernate enricher field discovery.
* IDEA Plugin: Removed hard references to PSIClass.

**13-11-2023** ver. 1.1.17
* Core: Added Reflection.loadNestedClass routine.
* Generation: Added support for external prototype sources.
* Generation: Fixed adding field routine to support other types than just String.
* Generation: Fixed duplicated methods generation.
* Generation: Fixed generation from compiled classes.
* Annotation Processor: Support for IDEA partial compilation.
* IDEA Plugin: Greatly improved discovery of various generated elements.
* IDEA Plugin: Improved error handling.

**01-11-2023** ver. 1.1.16
* Generation: Added element enrichment for unparsable files.
* IDEA Plugin: More reliable cache refreshing on code changes.
* IDEA Plugin: Fixed logs spamming expression.
 
**29-10-2023** ver. 1.1.15
* Core: Some Reflection routines made friendlier to use.
* Core: Added description to @CodeAugment.
* Generation: Fixed QueryEnricher to not generate join handling for CodeEnum lists.
* Generation: Added handling for inherited plain interfaces declared in the same module mixed with compiled interfaces.
* Generation: Fixed compiler ambiguity.
* Generation: Added ability to process not parsable files (if new language features are used).
* Annotation Processor: Properly logging file name if parsing failed.
* IDEA Plugin: Description on @CodeAugment is displayed on mouse-over.

**24-10-2023** ver. 1.1.14
* Generation: Fixed usage of compiled Enum prototypes. 
* Generation: Fixed handling of prototypes as generic type arguments.
* IDEA Plugin: Fixed caching of processed prototypes.

**21-10-2023** ver. 1.1.13
* Generation: Added helper function to add custom field to prototype.
* Generation: Reworked discovery of custom prototypes in the same compiling cycle.
* Generation: Added check if there is attempt of using not yet compiled enrichers.
* Spring: Renamed QueryExecutor internal routines to be less likely to collide with prototype defined fields.
* IDEA Plugin: Added support for method augmentation.
* IDEA Plugin: More reliable library discovery.
* IDEA Plugin: Added internal cache eviction on file save.

**15-10-2023** ver. 1.1.12
* Core: CodeFactory is able to instantiate objects with different constructors.
* Generation: Added handling of multiple prototype annotations on single class.
* Generation: Fixed base modifier class discovery in some corner cases.
* Generation: Fixed parsing error when prototype name is not string literal.
* Generation: Fixed NPE when searching for prototype by interface name.
* Compiler Plugin: Fixed annotation extending to target only annotations.

**12-10-2023** ver. 1.1.11
* Generation: Modify Enricher adds @Generated annotation to all generated classes.
* Generation: Added SuppressSpotBugsWarningsOption for Modifier Enricher.
* Generation: Query Enricher adds @Generated annotation to all generated classes.
* IDEA Plugin: Fixed incomplete annotations errors.

**10-10-2023** ver. 1.1.10
* Core: Improved reflection class params matching.
* Generation: Fixed compiler crash when using constructor enrichers.
* Generation: Removed some false positive compiler notes.
* Generation: Fixed generation of nested prototypes inside classes annotated with CodeGen annotations.

**28-09-2023** ver. 1.1.9
* Core: Better handling of @Default creation.
* Generation: Added constructor enrichers to replicate Lombok functionality into enrichers.
* Generation: Added @NotInitializedArgsConstructorEnricher.
* Generation: Prototype inheritance adds inherited enrichers to the list as well.
* Generation: Fixed compiler crash adding fields routine when used multiple times.
* Test: Added testing utility function to test multiple element augmented classes at once.
* IDEA Plugin: Fixed error suppression for nested classes.

**24-09-2023** ver. 1.1.8
* Core: Extended mapper functionality to mark mappings as value producers.
* Core: Better mapper handling for null values.
* Core: Added default construction for Map, Set and List.
* Core: Added mapping for Object -> Map.
* Core: Added support for registering foreign factories.
* Generation: Fixed handling of compiled prototypes with custom annotations.
* Spring Configuration: Registered ApplicationContext as foreign factory.
* Hibernate: Updated to support Hibenrate 6.2
* ALL: Updated dependencies. 

**09-09-2023** ver. 1.1.7
* Generator: (**Breaking change**) Replace Tools.notNull() with Tools.with() to remove duplicated routines.
* Generator: Fixed handling of nested prototypes with IMPLEMENTATION strategy.
* Generator: Fixed false positive compilation error when using IMPLEMENTATION strategy.
* Test: Updated loader to load all compiled objects.
* IDEA Plugin: Added check if CodeGen is used in project before enabling the plugin functionalities.
* IDEA Plugin: Fixed false positive errors when using IMPLEMENTATION strategy.

**03-09-2023** ver. 1.1.6
* Core: Extended BaseStringInterpolator to hold information about the segments expression splits into.
* Generator: Extended enricher helper functions.
* ALL: Updated dependencies.

**27-08-2023** ver. 1.1.5
* Generation: Prototype annotations are now cloned before attaching to generated classes.
* Generation: Added enricher augmentation descriptors.
* Generation: Added LogEnricher. (similar to @Slf4j)
* Generation: Added InjectionEnricher. (creates constructor with all final fields that are not initialized)
* Generation: Extended enricher reporting abilities.
* IDEA Plugin: Fixed various issues.
* IDEA Plugin: Added support for enricher augmentation.

**02-08-2023** ver. 1.1.4
* Compiler Plugin: Initial version.
* Compiler Plugin: Annotation inheritance.
* Compiler Plugin: Bracketless method calls.
* Core: Improved reflection utils to discover hidden fields.
* Test: Added testability for unparsable classes. 
* Generation: Fixed target discovery for foreign annotations.
* IDEA Plugin: Added support for annotation inheritance.
* IDEA Plugin: Added support for bracketless method calls.
* IDEA Plugin: Various fixes.
* ALL Modules: Updated dependencies.

**25-06-2023** ver. 1.1.3
* Annotation: Moved module access hacks to generation module.
* Generation: Added support for adding methods via element manipulation.
* IDEA Plugin: Added generated enums support.
* IDEA Plugin: Added discoverability of non-exposed templates.
* IDEA Plugin: Added IndexNotReady handling for usage provider.

**04-06-2023** ver. 1.1.2
* Validation: Added targeting type checks for validations.
* Generation: Added errors in cases when annotating wrong elements and when prototype naming is wrong.
* Generation: Element utils improvements.
* IDEA Plugin: Removed automatic code re-generation till it works properly.
* IDEA Plugin: Added targeting type checks for validations.
* IDEA Plugin: Added errors in cases when annotating wrong elements and when prototype naming is wrong.

**28-05-2023** ver. 1.1.1
* Generation: (**Breaking change**) Renamed CLASSIC generation strategy to PROTOTYPE.
* Generation: (**Breaking change**) Removed METHOD generation strategy.
* Generation: Added automatic @CodeImplementation injection for prototypes with default methods.
* Generation: Added support for parameter annotations element manipulation.
* Generation: Added support for constructor element manipulation.
* Generation: Fixed class mapping of CGLiteral.
* IDEA Plugin: Automatic code re-generation on saving prototype changes.
* IDEA Plugin: More reliable cache eviction on prototype changes.
* IDEA Plugin: More reliable annotation parameters reading.

**21-05-2023** ver. 1.1.0
* Generation: (**Breaking change**) Redesign enrichers framework to support every possible element.
* Generation: Added field introduction support for element manipulation.
* Generation: Added method invocation support for element manipulation.
* Generation: Added parsing support Java 17 language features.
* Annotation Processor: Extended annotation processor to support internally registered enrichers.
* Test: Added support for compiler element enrichment tests.

**01-05-2023** ver. 1.0.8
* Generation: Added recursive check for custom prototypes.
* Generation: Made iterating over CGList more convenient.
* Generation: Added some utility methods to CGExpression.

**07-04-2023** ver. 1.0.7
* Annotation Processor: Fixed wrong element passed to processor.
* Generation: Extended support for direct element manipulations.
* Generation: equals() and hashCode() to generated enums to suppress spotBugs errors.

**04-04-2023** ver. 1.0.6
* Generation: Added experimental method annotations' manipulation.
* Generation: Added enrichers for enums.

**26-03-2023** ver. 1.0.5
* Generation: Added experimental class annotations' manipulation.
* Generation: Added option for expression prefix with string interpolation.
* Validation: Added method based validation/sanitization annotations generation.
* Annotation Processor: Added support for custom generated classes.

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
  
