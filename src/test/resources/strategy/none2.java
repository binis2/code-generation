package net.binis.codegen;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.Default;
import net.binis.codegen.annotation.type.GenerationStrategy;
import net.binis.codegen.objects.enrichers.TestNoneStrategyEnricher;

@CodePrototype(strategy = GenerationStrategy.NONE, enrichers = TestNoneStrategyEnricher.class)
public class TestNoneStrategy {

}