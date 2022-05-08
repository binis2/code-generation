/*Generated code by Binis' code generator.*/
package net.binis.test.cards.payload;

import net.binis.codegen.objects.Payload;
import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "AccountOverviewCardPayloadPrototype", comments = "AccountOverviewCardPayloadImpl")
@Default("net.binis.test.cards.payload.AccountOverviewCardPayloadImpl")
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
    interface EmbeddedModify<T, R> extends BaseModifier<T, R>, AccountOverviewCardPayload.Fields<T> {
    }

    interface EmbeddedSoloModify<R> extends AccountOverviewCardPayload.EmbeddedModify<AccountOverviewCardPayload.EmbeddedSoloModify<R>, R> {
    }

    interface Fields<T> {
        T donated(int donated);
        T matching(int matching);
        T raised(int raised);
    }

    interface Modify extends EmbeddedModify<AccountOverviewCardPayload.Modify, AccountOverviewCardPayload> {
    }
    // endregion
}
