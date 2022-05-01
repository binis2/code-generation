/*Generated code by Binis' code generator.*/
package net.binis.test.card;

import net.binis.test.cards.payload.AccountOverviewCardPayload;
import net.binis.codegen.objects.CompiledGeneric;
import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "AccountOverviewCardPrototype", comments = "AccountOverviewCardImpl")
@Default("net.binis.test.card.AccountOverviewCardImpl")
public interface AccountOverviewCard extends CompiledGeneric<AccountOverviewCardPayload> {

    // region starters
    static AccountOverviewCard.Modify create() {
        return (AccountOverviewCard.Modify) EntityCreatorModifier.create(AccountOverviewCard.class).with();
    }
    // endregion

    AccountOverviewCard.Modify with();

    // region inner classes
    interface Fields<T> {
        T payload(AccountOverviewCardPayload payload);
        T schema(String schema);
        T subType(String subType);
        T timestamp(Long timestamp);
        T type(String type);
    }

    interface Modify extends AccountOverviewCard.Fields<AccountOverviewCard.Modify>, BaseModifier<AccountOverviewCard.Modify, AccountOverviewCard> {
    }
    // endregion
}
