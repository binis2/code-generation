/*Generated code by Binis' code generator.*/
package net.binis.test.card;

import net.binis.test.cards.payload.AccountOverviewCardPayload;
import net.binis.codegen.objects.impl.CompiledGenericImpl;
import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "AccountOverviewCardPrototype", comments = "AccountOverviewCard")
public class AccountOverviewCardImpl extends CompiledGenericImpl<AccountOverviewCardPayload> implements AccountOverviewCard, Modifiable<AccountOverviewCard.Modify> {

    // region constructor & initializer
    {
        CodeFactory.registerType(AccountOverviewCard.class, AccountOverviewCardImpl::new, null);
    }

    public AccountOverviewCardImpl() {
        super();
        this.type = null;
    }
    // endregion

    // region getters
    public AccountOverviewCard.Modify with() {
        return new AccountOverviewCardModifyImpl(this);
    }
    // endregion

    // region inner classes
    protected class AccountOverviewCardModifyImpl extends BaseModifierImpl<AccountOverviewCard.Modify, AccountOverviewCard> implements AccountOverviewCard.Modify {

        protected AccountOverviewCardModifyImpl(AccountOverviewCard parent) {
            super(parent);
        }

        public AccountOverviewCard done() {
            return AccountOverviewCardImpl.this;
        }

        public AccountOverviewCard.Modify payload(AccountOverviewCardPayload payload) {
            AccountOverviewCardImpl.this.payload = payload;
            return this;
        }

        public AccountOverviewCard.Modify schema(String schema) {
            AccountOverviewCardImpl.this.schema = schema;
            return this;
        }

        public AccountOverviewCard.Modify subType(String subType) {
            AccountOverviewCardImpl.this.subType = subType;
            return this;
        }

        public AccountOverviewCard.Modify timestamp(Long timestamp) {
            AccountOverviewCardImpl.this.timestamp = timestamp;
            return this;
        }

        public AccountOverviewCard.Modify type(String type) {
            AccountOverviewCardImpl.this.type = type;
            return this;
        }
    }
    // endregion
}
