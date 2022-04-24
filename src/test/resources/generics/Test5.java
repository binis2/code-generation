package net.binis.test.prototype.card;

import net.binis.codegen.annotation.Initialize;
import net.binis.codegen.annotation.builder.CodeBuilder;
import net.binis.codegen.objects.Payload;
import net.binis.codegen.objects.prototype.CompiledGenericPrototype;

@CodeBuilder
@Initialize(field = "type", expression = "null")
public interface AccountOverviewCardPrototype extends CompiledGenericPrototype<AccountOverviewCardPrototype.AccountOverviewCardPayloadPrototype> {

    @CodeBuilder
    public interface AccountOverviewCardPayloadPrototype extends Payload {
        int raised();
        int donated();
        int matching();
    }

}
