/*Generated code by Binis' code generator.*/
package net.binis.test.card;

import net.binis.codegen.objects.Payload;
import net.binis.codegen.objects.CompiledGeneric;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "AccountOverviewCardPrototype", comments = "AccountOverviewCardImpl")
@Default("net.binis.test.card.AccountOverviewCardImpl")
public interface AccountOverviewCard extends CompiledGeneric<AccountOverviewCard.AccountOverviewCardPayload> {

    // region starters
    static AccountOverviewCard.Modify create() {
        return (AccountOverviewCard.Modify) EntityCreatorModifier.create(AccountOverviewCard.class).with();
    }
    // endregion

    AccountOverviewCard.Modify with();

    // region inner classes
    @Default("net.binis.test.card.AccountOverviewCardImpl.AccountOverviewCardPayloadImpl")
    public interface AccountOverviewCardPayload extends Payload {

        // region starters
        static AccountOverviewCardPayload.Modify create() {
            return (AccountOverviewCardPayload.Modify) EntityCreatorModifier.create(AccountOverviewCardPayload.class).with();
        }
        // endregion

        int getDonated();
        int getMatching();
        int getRaised();

        AccountOverviewCardPayload.Modify with();

        // region inner classes
        interface Fields<T> {
            T donated(int donated);
            T matching(int matching);
            T raised(int raised);
        }

        interface Modify extends AccountOverviewCardPayload.Fields<AccountOverviewCardPayload.Modify> {
            AccountOverviewCardPayload done();
        }
        // endregion
    }

    interface Fields<T> {
        T payload(AccountOverviewCard.AccountOverviewCardPayload payload);
        T schema(String schema);
        T subType(String subType);
        T timestamp(Long timestamp);
        T type(String type);
    }

    interface Modify extends AccountOverviewCard.Fields<AccountOverviewCard.Modify> {
        AccountOverviewCard done();
    }
    // endregion
}
