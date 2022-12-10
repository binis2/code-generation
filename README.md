# code-generation

Code generation module for Binis CodeGen Library.

### Introduction

This is a code generation library inspired by [lombok](https://projectlombok.org/) with the addition of generating/handling interfaces and reduced manual typing even more.
There are some extensions that heavily support functional programming.
### Basics

CodeGen library is based on object prototypes. Here is simple example.

```java
@CodePrototype
public interface TestPrototype {
    String title();
}
```

The generated code for this prototype will look like this:

*Interface*:
```java
public interface Test {
    String getTitle();
    void setTitle(String title);
}
```

*Implementation*:
```java
public class TestImpl implements Test {
    protected String title;

    public TestImpl() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
```

### Enrichers

The CodeGen library introduces code enrichers to help you quickly spice up your code.

#### ModifierEnricher

```java
@CodePrototype(enrichers = {ModifierEnricher.class})
public interface TestPrototype {
    String title();
    double amount;
}
```

this will enable you the ability to modify existing instances of your objects in a functional way like this

```java
    test.with().title("title").amount(10.0).done();
```

#### AsEnricher

```java
@CodePrototype(enrichers = {AsEnricher.class})
public interface TestPrototype extends OtherInterface {
    String title();
    double amount;
}
```

enables functional casting for your objects

```java
    test.as(OtherInterface.class)...
```

#### CreatorEnricher and CreatorModifierEnricher

```java
@CodePrototype(enrichers = {CreatorModifierEnricher.class, ModifierEnricher.class})
public interface TestPrototype {
    String title();

    double amount;
}
```

enables decoupled instantiation of your objects 

```java
    Test.create().title("title").amount(10.0).done();
```

The difference between CreatorModifierEnricher and CreatorEnricher is that CreatorEnricher give you the created object itself instead of it's modifier.
CreatorModifierEnricher is to be used with combination with ModifierEnricher. If ModifierEnricher is not added to the prototype CreatorModifierEnricher will act as CreatorEnricher.

#### QueryEnricher

```java
@CodePrototype(enrichers = {QueryEnricher.class})
public interface TestPrototype {
    String title();
    double amount;
}
```

enables query building for your jpa entities 

```java
    Test.find().by().title("title").get();
    Test.find().by().amount().greater(10.0).and().not().title("title").get();
```

*Note that QueryEnricher requires code-generation-spring module dependency to your project*

#### ValidationEnricher

```java
import net.binis.codegen.annotation.validation.Sanitize;

@CodePrototype(enrichers = {ValidationEnricher.class})
public interface TestPrototype {
    @ValidateNull
    @SanitizeTrim
    String title();

    //or use the longer syntax 
    @Sanitize(ReplaceSanitizer.class, "\\s+", "_")
    @Validate(NullValidator.class, "Subtitle can't be null!")
    String subtitle;
}
```

enables validation and sanitization for your entities. It handles both setters and modifiers.  

```java
    assertThrows(ValidationException.class, () -> entity.setTitle(null));
    assertEquals("title", entity.with().title("  title  ").done().getTitle());
```

##### Custom validators/sanitizers

The validation system has a neat way to inherit existing validation/sanitization annotations and introduce your own custom annotation validations/sanitizations.

```java
@Validate(LambdaValidator.class)
public @interface ValidateNotBlank {
    @AsCode
    @AliasFor("params")
    String value() default "org.apache.commons.lang3.StringUtils::isNotBlank";
    String message() default "Value can't be blank!";
}
```
```java
@Validate(value = RegExValidator.class, params = ValidateEmail.REGEX)
public @interface ValidateEmail {
    String REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    String message() default "Invalid Email!";
}
```
and use it like this

```java
@SanitizeTrim
@ValidateNotBlank
String field();

@ValidateEmail
String email();

@ValidateRange(min = 0, max = 100)
int value();

@SanitizeLambda("String::trim")
String lambda();
```



*Note: [Validation module](https://github.com/binis2/code-generation-validation) needs to be included into the project!* 
      
*Note that ValidationEnricher requires code-generation-validation module dependency to your project*

Multiple enrichers can be combined for spicing up your objects. See below.

### Collections support

All enrichers have extensive collection support

```java
@CodePrototype(enrichers = {QueryEnricher.class, ModifierEnricher.class})
public interface TestPrototype {
    String title();
    List<Double> amounts(); 
}
```

For either creation
```java
    Test.create()
            .title("title")
            .amounts()
                .add(5)
                .add(6)
            .and()
        .done();
```
or querying 
```java
    Test.find().by().title("title").get().ifPresent(test ->
        test.with()
            .amounts().add(10.0)
        .save());
    Test.find().by().amounts().contains(10.0).list();
```

### Base and Sub Prototypes

The library supports prototypes interaction

```java
@CodePrototype
public interface UserPrototype extends BasePrototype {
    String username();
    List<AccountPrototype> accounts(); 
}
```

### Real life examples

Initial design of the library was to ease the creation and maintenance of JPA entities. So here is a full scale example.
*Note that it copes well with lombok.*

So let's declare a base entity first:
```java
@CodePrototype(
        base = true,
        interfaceName = "BaseInterface",
        classGetters = false,
        classSetters = false,
        interfaceSetters = false,
        implementationPackage = "my.project.db.entity",
        enrichers = {AsEnricher.class, ModifierEnricher.class},
        inheritedEnrichers = {CreatorModifierEnricher.class, ModifierEnricher.class, QueryEnricher.class})
@MappedSuperclass
public interface BaseEntityPrototype extends Serializable, Identifiable {

    @CodeConstant(isPublic = false)
    long serialVersionUID = 1862576989031617048L;

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonSerialize(using = ToStringSerializer.class)
    @ToString.Include
    Long id();

    @Ignore(forModifier = true)
    @CreatedDate
    @Column(nullable = false, updatable = false)
    @ColumnDefault("current_timestamp")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    OffsetDateTime created();

    @LastModifiedDate
    @Column(nullable = false)
    @ColumnDefault("current_timestamp")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    OffsetDateTime modified();

    @Ignore(forInterface = true, forModifier = true)
    @CreatedBy
    @Column(updatable = false)
    String createdBy();

    @Ignore(forInterface = true, forModifier = true)
    @LastModifiedBy
    String modifiedBy();

    @Data
    @ToString(onlyExplicitlyIncluded = true)
    public static class BaseClassAnnotations {

    }
}
```

Now an actual entity:

```java

@CodePrototype(
        classGetters = false,
        classSetters = false,
        interfaceSetters = false,
        implementationPackage = "my.project.db.entity",
        baseModifierClass = BaseEntityModifier.class)
@Entity(name = UserEntityPrototype.TABLE_NAME)
public interface UserEntityPrototype extends BaseEntityPrototype, Addressable, Statusable<CustomerStatus> {

    @CodeConstant(isPublic = false)
    long serialVersionUID = 2344606065855771677L;

    String TABLE_NAME = "users";

    @ToString.Include
    @Column(unique = true)
    String username();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password();

    String firstName();
    String lastName();
    String email();

    @OneToOne(targetEntity = AddressEntityPrototype.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    AddressEntityPrototype address();

    @ColumnDefault("0")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    CustomerStatus status();

    @Ignore(forInterface = true)
    @Transient
    default String getPreview() {
        return username() + " (" + firstName() + " " + lastName() + ")";
    }

    @ForImplementation
    @Transient
    default boolean isActive() {
        return !CustomerStatus.INACTIVE.equals(status());
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(onlyExplicitlyIncluded = true)
    class ClassAnnotations extends BaseClassAnnotations implements Previewable {

    }
}
```
Now you can simply write things like this anywhere in your code without need of anything else except importing your new interface
```java
User.create()
        .username("username")
        .password("password")
        .email("email@email.com")
        .address(Address.create()
            .street("nowhere str.")
            .number(5)
            .done())
        .status(CustomerStatus.ACTIVE)
        .save();
```
or
```java
User.find().by().email("email@email.com").get().ifPresent(user -> 
        user.with().status(CustomerStatus.ACTIVE).save());
```



*For more use cases check the unit tests [here](https://github.com/binis2/code-generation/tree/master/src/test/resources) and [here](https://github.com/binis2/code-generation-test/tree/master/src/test/resources) *

### How to make it work?

Depends on your preferences you can keep your prototypes outside your actual project into lets call it prototypes project. Just add this build step to your prototypes project's pom.xml.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <executions>
                <execution>
                    <id>generateCode</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>java</goal>
                    </goals>
                    <configuration>
                        <mainClass>net.binis.codegen.CodeGen</mainClass>
                        <arguments>
                            <argument>-s</argument>
                            <argument>${project.basedir}/..</argument>
                            <argument>-d</argument>
                            <argument>${project.basedir}/../modules/core/src/main/java</argument>
                            <argument>-id</argument>
                            <argument>${project.basedir}/../modules/db/src/main/java</argument>
                            <argument>-f</argument>
                            <argument>**Prototype.java</argument>
                        </arguments>
                        <classpathScope>compile</classpathScope>
                        <sourceRoot>${project.build.directory}/generated-sources/main/java</sourceRoot>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
or you can use the annotation processor
```xml
<dependency>
    <groupId>dev.binis</groupId>
    <artifactId>code-generator-annotation</artifactId>
    <version>0.4.3</version>
    <scope>compile</scope>
</dependency>
```

### Maven Dependency
```xml
    <dependency>
        <groupId>dev.binis</groupId>
        <artifactId>code-generator</artifactId>
        <version>0.4.3</version>
    </dependency>
```

### Other modules of the suite

Core - [https://github.com/binis2/code-generation-core]   
Spring Extension - [https://github.com/binis2/code-generation-spring]   
Tests mocking suite - [https://github.com/binis2/code-generation-test]   
Annotation processor - [https://github.com/binis2/code-generation-annotation]   
Validation and Sanitization extension - [https://github.com/binis2/code-generation-validation]   
Jackson support - [https://github.com/binis2/code-generation-jackson]   
Spring Boot configuration - [https://github.com/binis2/code-generation-spring-configuration]   
Projections support - [https://github.com/binis2/code-generation-projection]      
Hibernate support - [https://github.com/binis2/code-generation-hibernate]   