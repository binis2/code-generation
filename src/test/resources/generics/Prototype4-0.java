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
        CodeFactory.registerType(AccountOverviewCardPayload.class, AccountOverviewCardPayloadImpl::new, (p, v, r) -> ((AccountOverviewCardPayloadImpl) v).new AccountOverviewCardPayloadImplSoloModifyImpl(p));
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
    protected class AccountOverviewCardPayloadImplEmbeddedModifyImpl<T, R> extends BaseModifierImpl<T, R> implements AccountOverviewCardPayload.EmbeddedModify<T, R> {

        protected AccountOverviewCardPayloadImplEmbeddedModifyImpl(R parent) {
            super(parent);
        }

        public T donated(int donated) {
            AccountOverviewCardPayloadImpl.this.donated = donated;
            return (T) this;
        }

        public T matching(int matching) {
            AccountOverviewCardPayloadImpl.this.matching = matching;
            return (T) this;
        }

        public T raised(int raised) {
            AccountOverviewCardPayloadImpl.this.raised = raised;
            return (T) this;
        }
    }

    protected class AccountOverviewCardPayloadImplSoloModifyImpl extends AccountOverviewCardPayloadImplEmbeddedModifyImpl implements AccountOverviewCardPayload.EmbeddedSoloModify {

        protected AccountOverviewCardPayloadImplSoloModifyImpl(Object parent) {
            super(parent);
        }
    }

    protected class AccountOverviewCardPayloadModifyImpl extends AccountOverviewCardPayloadImplEmbeddedModifyImpl<AccountOverviewCardPayload.Modify, AccountOverviewCardPayload> implements AccountOverviewCardPayload.Modify {

        protected AccountOverviewCardPayloadModifyImpl(AccountOverviewCardPayload parent) {
            super(parent);
        }
    }
    // endregion
}
