/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.spring.query.*;
import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.creator.EntityCreator;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;
import java.util.Optional;
import java.util.List;

@Generated(value = "net.binis.codegen.TestDataPrototype", comments = "TestDataImpl")
@Default("net.binis.codegen.TestDataImpl")
@SuppressWarnings("unchecked")
public interface TestData {

    static QueryStarter<TestData, TestData.QuerySelect<TestData>, QueryAggregateOperation<QueryOperationFields<TestData.QueryAggregate<Number, TestData.QuerySelect<Number>>>>, QueryFieldsStart<TestData, TestData.QuerySelect<TestData>>, QueryUpdate<TestData, TestData.QuerySelect<TestData>>> find() {
        return (QueryStarter) EntityCreator.create(TestData.QuerySelect.class);
    }

    List<Test> getTests();

    void setTests(List<Test> tests);

    TestData.Modify with();

    interface Modify extends BaseModifier<TestData.Modify, TestData> {
        Modify tests(List<Test> tests);
    }

    interface QueryAggregate<QR, QA> extends QueryExecute<QR>, QueryAggregator<QA, QueryAggregateOperation<QueryOperationFields<TestData.QueryAggregate<TestData, TestData.QuerySelect<Number>>>>, TestData.QueryAggregate<TestData, TestData.QuerySelect<Number>>> {
    }

    interface QueryFields<QR> extends QueryScript<QR> {
    }

    interface QueryFieldsStart<QR, QS> extends QueryExecute<QR>, QueryWhere<QS>, QueryOperationFields<QueryFieldsStart<QR, QS>> {
    }

    interface QueryFuncs<QR> {
    }

    interface QueryName<QS, QO, QR, QF> extends TestData.QueryFields<QuerySelectOperation<QS, QO, QR>>, TestData.QueryFuncs<QuerySelectOperation<QS, QO, QR>>, QueryFetch<QuerySelectOperation<QS, QO, QR>, QF> {
    }

    interface QueryOperationFields<QR> extends QueryScript<QR>, QuerySelf<QR> {
    }

    interface QueryOrder<QR> extends QueryOperationFields<QueryOrderOperation<TestData.QueryOrder<QR>, QR>>, QueryExecute<QR> {
    }

    interface QuerySelect<QR> extends QueryExecute<QR>, QueryModifiers<TestData.QueryName<TestData.QuerySelect<QR>, TestData.QueryOrder<QR>, QR, TestData>>, TestData.QueryFields<QuerySelectOperation<TestData.QuerySelect<QR>, TestData.QueryOrder<QR>, QR>>, TestData.QueryFuncs<QuerySelectOperation<TestData.QuerySelect<QR>, TestData.QueryOrder<QR>, QR>>, QueryOrderStart<QueryOperationFields<QueryOrderOperation<TestData.QueryOrder<QR>, QR>>>, QueryBracket<QuerySelect<QR>> {
        QueryCollectionFunctions<Test, QuerySelectOperation<TestData.QuerySelect<QR>, QueryOperationFields<QueryOrderOperation<TestData.QueryOrder<QR>, QR>>, QR>> tests();
    }

    interface QueryUpdate<QR, QS> extends QueryFields<QueryUpdate<QR, QS>>, QueryWhere<QS>, QueryScript<QueryUpdate<QR, QS>>, UpdatableQuery {
    }
}
