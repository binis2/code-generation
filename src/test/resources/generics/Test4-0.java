/*Generated code by Binis' code generator.*/
package net.binis.test.card;

import net.binis.test.cards.payload.AccountOverviewCardPayload;
import net.binis.codegen.objects.impl.CompiledGenericImpl;
import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;
import java.util.function.Consumer;

@Generated(value = "net.binis.test.prototype.card.AccountOverviewCardPrototype", comments = "AccountOverviewCard")
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
    @Generated("ModifierEnricher")
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

        public AccountOverviewCardPayload.EmbeddedSoloModify<AccountOverviewCard.Modify> payload() {
            if (AccountOverviewCardImpl.this.payload == null) {
                AccountOverviewCardImpl.this.payload = CodeFactory.create(AccountOverviewCardPayload.class);
            }
            return CodeFactory.modify(this, AccountOverviewCardImpl.this.payload, AccountOverviewCardPayload.class);
        }

        public AccountOverviewCard.Modify payload$(Consumer<AccountOverviewCardPayload.Modify> init) {
            if (AccountOverviewCardImpl.this.payload == null) {
                AccountOverviewCardImpl.this.payload = CodeFactory.create(AccountOverviewCardPayload.class);
            }
            init.accept(AccountOverviewCardImpl.this.payload.with());
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
