/*Generated code by Binis' code generator.*/
package net.binis.test.card;

import net.binis.codegen.objects.impl.CompiledGenericImpl;
import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;
import java.util.function.Consumer;

@Generated(value = "AccountOverviewCardPrototype", comments = "AccountOverviewCard")
public class AccountOverviewCardImpl extends CompiledGenericImpl<AccountOverviewCard.AccountOverviewCardPayload> implements AccountOverviewCard, Modifiable<AccountOverviewCard.Modify> {

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

        public AccountOverviewCard.Modify payload(AccountOverviewCard.AccountOverviewCardPayload payload) {
            AccountOverviewCardImpl.this.payload = payload;
            return this;
        }

        public AccountOverviewCard.AccountOverviewCardPayload.EmbeddedSoloModify<AccountOverviewCard.Modify> payload() {
            if (AccountOverviewCardImpl.this.payload == null) {
                AccountOverviewCardImpl.this.payload = CodeFactory.create(AccountOverviewCard.AccountOverviewCardPayload.class);
            }
            return CodeFactory.modify(this, AccountOverviewCardImpl.this.payload, AccountOverviewCard.AccountOverviewCardPayload.class);
        }

        public AccountOverviewCard.Modify payload$(Consumer<AccountOverviewCard.AccountOverviewCardPayload.Modify> init) {
            if (AccountOverviewCardImpl.this.payload == null) {
                AccountOverviewCardImpl.this.payload = CodeFactory.create(AccountOverviewCard.AccountOverviewCardPayload.class);
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

    public static class AccountOverviewCardPayloadImpl implements AccountOverviewCardPayload, Modifiable<AccountOverviewCardPayload.Modify> {

        protected int donated;

        protected int matching;

        protected int raised;

        // region constructor & initializer
        {
            CodeFactory.registerType(AccountOverviewCard.AccountOverviewCardPayload.class, AccountOverviewCardPayloadImpl::new, (p, v) -> ((AccountOverviewCardPayloadImpl) v).new AccountOverviewCardPayloadImplSoloModifyImpl(p));
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

        protected class AccountOverviewCardPayloadModifyImpl extends AccountOverviewCardPayloadImplEmbeddedModifyImpl<AccountOverviewCard.AccountOverviewCardPayload.Modify, AccountOverviewCard.AccountOverviewCardPayload> implements AccountOverviewCardPayload.Modify {

            protected AccountOverviewCardPayloadModifyImpl(AccountOverviewCardPayload parent) {
                super(parent);
            }
        }
        // endregion
    }
    // endregion
}
