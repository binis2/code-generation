/*Generated code by Binis' code generator.*/
package net.binis.test.card;

import net.binis.codegen.objects.Payload;
import net.binis.codegen.objects.CompiledGeneric;
import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;
import java.util.function.Consumer;

@Generated(value = "AccountOverviewCardPrototype", comments = "AccountOverviewCardImpl")
@Default("net.binis.test.card.AccountOverviewCardImpl")
public interface AccountOverviewCard extends CompiledGeneric<AccountOverviewCard.AccountOverviewCardPayload> {

    // region starters
    @SuppressWarnings(value = "unchecked")
    static AccountOverviewCard.Modify create() {
        return (AccountOverviewCard.Modify) EntityCreatorModifier.create(AccountOverviewCard.class).with();
    }
    // endregion

    AccountOverviewCard.Modify with();

    // region inner classes
    @Default("net.binis.test.card.AccountOverviewCardImpl$AccountOverviewCardPayloadImpl")
    public interface AccountOverviewCardPayload extends Payload {

        // region starters
        @SuppressWarnings(value = "unchecked")
        static AccountOverviewCardPayload.Modify create() {
            return (AccountOverviewCardPayload.Modify) EntityCreatorModifier.create(AccountOverviewCardPayload.class).with();
        }
        // endregion

        int getDonated();
        int getMatching();
        int getRaised();

        AccountOverviewCardPayload.Modify with();

        // region inner classes
        interface EmbeddedModify<T, R> extends BaseModifier<T, R>, AccountOverviewCardPayload.Fields<T> {
        }

        interface EmbeddedSoloModify<R> extends AccountOverviewCardPayload.EmbeddedModify<AccountOverviewCardPayload.EmbeddedSoloModify<R>, R> {
        }

        interface Fields<T> {
            T donated(int donated);
            T matching(int matching);
            T raised(int raised);
        }

        interface Modify extends EmbeddedModify<AccountOverviewCard.AccountOverviewCardPayload.Modify, AccountOverviewCard.AccountOverviewCardPayload> {
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

    interface Modify extends AccountOverviewCard.Fields<AccountOverviewCard.Modify>, BaseModifier<AccountOverviewCard.Modify, AccountOverviewCard> {
        AccountOverviewCard.AccountOverviewCardPayload.EmbeddedSoloModify<Modify> payload();
        Modify payload$(Consumer<AccountOverviewCard.AccountOverviewCardPayload.Modify> init);
    }
    // endregion
}
