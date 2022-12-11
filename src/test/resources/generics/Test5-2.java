/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.test.TestExecutor;
import net.binis.test.card.AccountOverviewCard;

import static org.junit.jupiter.api.Assertions.*;

public class Execute extends TestExecutor {

    @Override
    public boolean execute() {

        AccountOverviewCard.create().payload(AccountOverviewCard.AccountOverviewCardPayload.create().done()).done();
        assertNotNull(AccountOverviewCard.create().payload().done().done().getPayload());

        return true;
    }
}
