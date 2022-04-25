/*Generated code by Binis' code generator.*/
package net.binis.test.card;

import net.binis.codegen.objects.impl.CompiledGenericImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

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
        return new AccountOverviewCardModifyImpl();
    }
    // endregion

    // region inner classes
    protected class AccountOverviewCardModifyImpl implements AccountOverviewCard.Modify {

        public AccountOverviewCard done() {
            return AccountOverviewCardImpl.this;
        }

        public AccountOverviewCard.Modify payload(AccountOverviewCard.AccountOverviewCardPayload payload) {
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

    public static class AccountOverviewCardPayloadImpl implements AccountOverviewCardPayload, Modifiable<AccountOverviewCardPayload.Modify> {

        protected int donated;

        protected int matching;

        protected int raised;

        // region constructor & initializer
        {
            CodeFactory.registerType(AccountOverviewCard.AccountOverviewCardPayload.class, AccountOverviewCardPayloadImpl::new, null);
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
            return new AccountOverviewCardPayloadModifyImpl();
        }
        // endregion

        // region inner classes
        protected class AccountOverviewCardPayloadModifyImpl implements AccountOverviewCardPayload.Modify {

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
    // endregion
}
