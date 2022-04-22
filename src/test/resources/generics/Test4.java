package net.binis.test.prototype.card;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.Initialize;
import net.binis.codegen.objects.prototype.CompiledGenericPrototype;
import net.binis.test.cards.prototype.payload.AccountOverviewCardPayloadPrototype;

@CodePrototype(interfaceSetters = false, classSetters = false)
@Initialize(field = "type", expression = "null")
public interface AccountOverviewCardPrototype extends CompiledGenericPrototype<AccountOverviewCardPayloadPrototype> {

}
