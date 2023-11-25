/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.spring.query.*;
import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.creator.EntityCreator;
import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;
import java.util.Optional;
import java.util.List;

@Generated(value = "net.binis.codegen.UseEnumPrototype", comments = "UseEnumImpl")
@Default("net.binis.codegen.UseEnumImpl")
@SuppressWarnings("unchecked")
public interface UseEnum {

    static QueryStarter<UseEnum, UseEnum.QuerySelect<UseEnum>, QueryAggregateOperation<QueryOperationFields<UseEnum.QueryAggregate<Number, UseEnum.QuerySelect<Number>>>>, QueryFieldsStart<UseEnum, UseEnum.QuerySelect<UseEnum>>, QueryUpdate<UseEnum, UseEnum.QuerySelect<UseEnum>>> find() {
        return (QueryStarter) EntityCreator.create(UseEnum.QuerySelect.class);
    }

    Test getMixIn();
    Test getMixIn2();
    Test getTest();

    void setMixIn(Test mixIn);
    void setMixIn2(Test mixIn2);
    void setTest(Test test);

    UseEnum.Modify with();

    interface Fields<T> {
        T mixIn(Test mixIn);
        T mixIn2(Test mixIn2);
        T test(Test test);
    }

    interface Modify extends UseEnum.Fields<UseEnum.Modify>, BaseModifier<UseEnum.Modify, UseEnum> {
    }

    interface QueryAggregate<QR, QA> extends QueryExecute<QR>, QueryAggregator<QA, QueryAggregateOperation<QueryOperationFields<UseEnum.QueryAggregate<UseEnum, UseEnum.QuerySelect<Number>>>>, UseEnum.QueryAggregate<UseEnum, UseEnum.QuerySelect<Number>>> {
    }

    interface QueryFields<QR> extends QueryScript<QR>, UseEnum.Fields<QR> {
    }

    interface QueryFieldsStart<QR, QS> extends QueryExecute<QR>, QueryWhere<QS>, QueryOperationFields<QueryFieldsStart<QR, QS>> {
    }

    interface QueryFuncs<QR> {
        QueryFunctions<Test, QR> mixIn();
        QueryFunctions<Test, QR> mixIn2();
        QueryFunctions<Test, QR> test();
    }

    interface QueryName<QS, QO, QR, QF> extends UseEnum.QueryFields<QuerySelectOperation<QS, QO, QR>>, UseEnum.QueryFuncs<QuerySelectOperation<QS, QO, QR>>, QueryFetch<QuerySelectOperation<QS, QO, QR>, QF> {
    }

    interface QueryOperationFields<QR> extends QueryScript<QR>, QuerySelf<QR> {
        QR mixIn();
        QR mixIn2();
        QR test();
    }

    interface QueryOrder<QR> extends QueryOperationFields<QueryOrderOperation<UseEnum.QueryOrder<QR>, QR>>, QueryExecute<QR> {
    }

    interface QuerySelect<QR> extends QueryExecute<QR>, QueryModifiers<UseEnum.QueryName<UseEnum.QuerySelect<QR>, UseEnum.QueryOrder<QR>, QR, UseEnum>>, UseEnum.QueryFields<QuerySelectOperation<UseEnum.QuerySelect<QR>, UseEnum.QueryOrder<QR>, QR>>, UseEnum.QueryFuncs<QuerySelectOperation<UseEnum.QuerySelect<QR>, UseEnum.QueryOrder<QR>, QR>>, QueryOrderStart<QueryOperationFields<QueryOrderOperation<UseEnum.QueryOrder<QR>, QR>>>, QueryBracket<QuerySelect<QR>> {
    }

    interface QueryUpdate<QR, QS> extends QueryFields<QueryUpdate<QR, QS>>, QueryWhere<QS>, QueryScript<QueryUpdate<QR, QS>>, UpdatableQuery {
    }
}
