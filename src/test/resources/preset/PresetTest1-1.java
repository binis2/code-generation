/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.spring.query.*;
import net.binis.codegen.creator.EntityCreator;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;
import java.util.Optional;
import java.util.List;

@Generated(value = "PresetTestPrototype", comments = "PresetTestImpl")
@Default("net.binis.codegen.PresetTestImpl")
public interface PresetTest {

    // region starters
    static QueryStarter<PresetTest, PresetTest.QuerySelect<PresetTest>, QueryAggregateOperation<QueryOperationFields<PresetTest.QueryAggregate<Number, PresetTest.QuerySelect<Number>>>>, QueryFieldsStart<PresetTest, PresetTest.QuerySelect<PresetTest>>> find() {
        return (QueryStarter) EntityCreator.create(PresetTest.QuerySelect.class);
    }
    // endregion

    int getData();
    List<String> getList();
    PresetTest getParent();
    String getTitle();

    void setData(int data);
    void setList(List<String> list);
    void setParent(PresetTest parent);
    void setTitle(String title);

    // region inner classes
    interface QueryAggregate<QR, QA> extends QueryExecute<QR>, QueryAggregator<QA, QueryAggregateOperation<QueryOperationFields<PresetTest.QueryAggregate<PresetTest, PresetTest.QuerySelect<Number>>>>> {
    }

    interface QueryFields<QR> extends QueryScript<QR> {
        QR data(int data);
        QR parent(PresetTest parent);
        QR title(String title);
    }

    interface QueryFieldsStart<QR, QS> extends QueryExecute<QR>, QueryWhere<QS>, QueryOperationFields<QueryFieldsStart<QR, QS>> {
    }

    interface QueryFuncs<QR> {
        QueryFunctions<Integer, QR> data();
        QueryFunctions<String, QR> title();
    }

    interface QueryName<QS, QO, QR, QF> extends PresetTest.QueryFields<QuerySelectOperation<QS, QO, QR>>, PresetTest.QueryFuncs<QuerySelectOperation<QS, QO, QR>>, QueryFetch<QuerySelectOperation<QS, QO, QR>, QF> {
        PresetTest.QueryName<QS, QO, QR, PresetTest> parent();
    }

    interface QueryOperationFields<QR> extends QueryScript<QR>, QuerySelf<QR> {
        QR data();
        PresetTest.QueryOperationFields<QR> parent();
        QR title();
    }

    interface QueryOrder<QR> extends QueryOperationFields<QueryOrderOperation<PresetTest.QueryOrder<QR>, QR>>, QueryExecute<QR> {
    }

    interface QuerySelect<QR> extends QueryExecute<QR>, QueryModifiers<PresetTest.QueryName<PresetTest.QuerySelect<QR>, PresetTest.QueryOrder<QR>, QR, PresetTest>>, PresetTest.QueryFields<QuerySelectOperation<PresetTest.QuerySelect<QR>, PresetTest.QueryOrder<QR>, QR>>, PresetTest.QueryFuncs<QuerySelectOperation<PresetTest.QuerySelect<QR>, PresetTest.QueryOrder<QR>, QR>>, QueryOrderStart<QueryOperationFields<QueryOrderOperation<PresetTest.QueryOrder<QR>, QR>>>, QueryBracket<QuerySelect<QR>> {
        QuerySelectOperation<PresetTest.QuerySelect<QR>, PresetTest.QueryOrder<QR>, QR> __queryPrototype(PresetTest parent);
        QuerySelectOperation<PresetTest.QuerySelect<QR>, PresetTest.QueryOrder<QR>, QR> __queryString(String title, PresetTest parent, int data);
        QuerySelectOperation<PresetTest.QuerySelect<QR>, PresetTest.QueryOrder<QR>, QR> __queryTitle(String title, int data);
        QuerySelectOperation<PresetTest.QuerySelect<QR>, PresetTest.QueryOrder<QR>, QR> __queryTitleString();
        QueryCollectionFunctions<String, QuerySelectOperation<PresetTest.QuerySelect<QR>, QueryOperationFields<QueryOrderOperation<PresetTest.QueryOrder<QR>, QR>>, QR>> _list();
        PresetTest.QueryName<PresetTest.QuerySelect<QR>, PresetTest.QueryOrder<QR>, QR, PresetTest> parent();
    }
    // endregion
}
