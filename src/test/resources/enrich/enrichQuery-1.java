/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.spring.query.QueryExecute;

public interface Test {

    static QueryStart find() {
        return QueryBase.create(Test.class);
    }

    String getTitle();

    void setTitle(String title);

    interface QueryOrder<QR> extends QueryExecute<QR> {

        QueryOrderOperation<QR, QueryOrder<QR>> title(String title);
    }

    interface QuerySelect<QR> extends QueryExecute<QR> {

        QueryOrder<QR> order();

        QuerySelectOperation<QR, QuerySelect<QR>> title(String title);
    }

    interface QueryStart {

        QuerySelect<List<Test>> all();

        QuerySelect<Test> by();

        QuerySelect<Long> count();
    }
}
