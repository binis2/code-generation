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


Multiple enrichers can be combined for spicing up your objects.

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


*More Examples coming soon.*

### Other modules of the suite

Core - [https://github.com/binis2/code-generation-core]   
Spring Extension - [https://github.com/binis2/code-generation-spring]   
Tests mocking suite - [https://github.com/binis2/code-generation-test]   
Annotation processor - [https://github.com/binis2/code-generation-annotation] 
