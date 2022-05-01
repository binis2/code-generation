/*Generated code by Binis' code generator.*/
package net.binis.test.cards.payload;

import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "AccountOverviewCardPayloadPrototype", comments = "AccountOverviewCardPayload")
public class AccountOverviewCardPayloadImpl implements AccountOverviewCardPayload, Modifiable<AccountOverviewCardPayload.Modify> {

    protected int donated;

    protected int matching;

    protected int raised;

    // region constructor & initializer
    {
        CodeFactory.registerType(AccountOverviewCardPayload.class, AccountOverviewCardPayloadImpl::new, null);
    }

    public AccountOverviewCardPayloadImpl() {
    }
    // endregion

    // region getters
    public int getDonated() {
        return donated;
    }

    public int getMatching() {
        return matching;
    }

    public int getRaised() {
        return raised;
    }

    public AccountOverviewCardPayload.Modify with() {
        return new AccountOverviewCardPayloadModifyImpl(this);
    }
    // endregion

    // region inner classes
    protected class AccountOverviewCardPayloadModifyImpl extends BaseModifierImpl<AccountOverviewCardPayload.Modify, AccountOverviewCardPayload> implements AccountOverviewCardPayload.Modify {

        protected AccountOverviewCardPayloadModifyImpl(AccountOverviewCardPayload parent) {
            super(parent);
        }

        public AccountOverviewCardPayload.Modify donated(int donated) {
            AccountOverviewCardPayloadImpl.this.donated = donated;
            return this;
        }

        public AccountOverviewCardPayload done() {
            return AccountOverviewCardPayloadImpl.this;
        }

        public AccountOverviewCardPayload.Modify matching(int matching) {
            AccountOverviewCardPayloadImpl.this.matching = matching;
            return this;
        }

        public AccountOverviewCardPayload.Modify raised(int raised) {
            AccountOverviewCardPayloadImpl.this.raised = raised;
            return this;
        }
    }
    // endregion
}
