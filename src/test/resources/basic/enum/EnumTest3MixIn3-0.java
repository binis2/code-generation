/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.spring.query.executor.QueryOrderer;
import net.binis.codegen.spring.query.executor.QueryExecutor;
import net.binis.codegen.spring.query.base.BaseQueryNameImpl;
import net.binis.codegen.spring.query.*;
import net.binis.codegen.modifier.impl.BaseModifierImpl;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.creator.EntityCreator;
import javax.annotation.processing.Generated;
import java.util.function.Function;
import java.util.Optional;
import java.util.List;

@Generated(value = "net.binis.codegen.UseEnumPrototype", comments = "UseEnum")
@SuppressWarnings("unchecked")
public class UseEnumImpl implements UseEnum, Modifiable<UseEnum.Modify> {

    protected Test mixIn;

    protected Test mixIn2;

    protected Test test;

    static {
        CodeFactory.registerType(UseEnum.class, UseEnumImpl::new, null);
        CodeFactory.registerType(UseEnum.QueryName.class, UseEnumQueryNameImpl::new, null);
        CodeFactory.registerType(UseEnum.QuerySelect.class, UseEnumQueryExecutorImpl::new, null);
        CodeFactory.registerType(UseEnum.QueryOperationFields.class, UseEnumQueryExecutorImpl::new, null);
    }

    public UseEnumImpl() {
    }

    public Test getMixIn() {
        return mixIn;
    }

    public Test getMixIn2() {
        return mixIn2;
    }

    public Test getTest() {
        return test;
    }

    public void setMixIn(Test mixIn) {
        this.mixIn = mixIn;
    }

    public void setMixIn2(Test mixIn2) {
        this.mixIn2 = mixIn2;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public UseEnum.Modify with() {
        return new UseEnumModifyImpl(this);
    }

    @Generated("ModifierEnricher")
    protected class UseEnumModifyImpl extends BaseModifierImpl<UseEnum.Modify, UseEnum> implements UseEnum.Modify {

        protected UseEnumModifyImpl(UseEnum parent) {
            super(parent);
        }

        public UseEnum done() {
            return UseEnumImpl.this;
        }

        public UseEnum.Modify mixIn(Test mixIn) {
            UseEnumImpl.this.mixIn = mixIn;
            return this;
        }

        public UseEnum.Modify mixIn2(Test mixIn2) {
            UseEnumImpl.this.mixIn2 = mixIn2;
            return this;
        }

        public UseEnum.Modify test(Test test) {
            UseEnumImpl.this.test = test;
            return this;
        }
    }

    @Generated("QueryEnricher")
    protected static class UseEnumQueryExecutorImpl extends QueryExecutor implements UseEnum.QueryUpdate, UseEnum.QuerySelect, UseEnum.QueryFieldsStart {

        protected UseEnumQueryExecutorImpl() {
            super(UseEnum.class, () -> new UseEnumQueryNameImpl(), parent -> parent);
        }

        public QueryAggregateOperation aggregate() {
            return (QueryAggregateOperation) _aggregateStart(new UseEnumQueryOrderImpl(this, UseEnumQueryExecutorImpl.this::_aggregateIdentifier));
        }

        public QueryFunctions mixIn() {
            return $identifier("mixIn");
        }

        public QuerySelectOperation mixIn(Test mixIn) {
            return $identifier("mixIn", mixIn);
        }

        public QueryFunctions mixIn2() {
            return $identifier("mixIn2");
        }

        public QuerySelectOperation mixIn2(Test mixIn2) {
            return $identifier("mixIn2", mixIn2);
        }

        public UseEnum.QueryOrder order() {
            return (UseEnum.QueryOrder) _orderStart(new UseEnumQueryOrderImpl(this, UseEnumQueryExecutorImpl.this::_orderIdentifier));
        }

        public QueryFunctions test() {
            return $identifier("test");
        }

        public QuerySelectOperation test(Test test) {
            return $identifier("test", test);
        }

        @Generated("QueryEnricher")
        protected class UseEnumQueryOrderImpl extends QueryOrderer implements UseEnum.QueryOrder, UseEnum.QueryAggregate {

            protected UseEnumQueryOrderImpl(UseEnumQueryExecutorImpl executor, Function<String, Object> func) {
                super(executor, func);
            }

            public QueryOrderOperation mixIn() {
                return (QueryOrderOperation) func.apply("mixIn");
            }

            public QueryOrderOperation mixIn2() {
                return (QueryOrderOperation) func.apply("mixIn2");
            }

            public QueryOrderOperation test() {
                return (QueryOrderOperation) func.apply("test");
            }
        }
    }

    @Generated("QueryEnricher")
    protected static class UseEnumQueryNameImpl extends BaseQueryNameImpl implements UseEnum.QueryName, QueryEmbed {

        public QueryFunctions mixIn() {
            return executor.$identifier("mixIn");
        }

        public QuerySelectOperation mixIn(Test mixIn) {
            return executor.$identifier("mixIn", mixIn);
        }

        public QueryFunctions mixIn2() {
            return executor.$identifier("mixIn2");
        }

        public QuerySelectOperation mixIn2(Test mixIn2) {
            return executor.$identifier("mixIn2", mixIn2);
        }

        public QueryFunctions test() {
            return executor.$identifier("test");
        }

        public QuerySelectOperation test(Test test) {
            return executor.$identifier("test", test);
        }
    }
}
