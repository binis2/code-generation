/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.test.TestExecutor;
import net.binis.test.card.AccountOverviewCard;

public class Execute extends TestExecutor {

    @Override
    public boolean execute() {

        AccountOverviewCard.create().payload(AccountOverviewCard.AccountOverviewCardPayload.create().done()).done();

        return true;
    }
}
