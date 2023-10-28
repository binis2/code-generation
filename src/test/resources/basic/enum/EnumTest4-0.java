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

@Generated(value = "net.binis.codegen.TestDataPrototype", comments = "TestData")
@SuppressWarnings("unchecked")
public class TestDataImpl implements TestData, Modifiable<TestData.Modify> {

    protected List<Test> getTests;

    {
        CodeFactory.registerType(TestData.class, TestDataImpl::new, null);
        CodeFactory.registerType(TestData.QueryName.class, TestDataQueryNameImpl::new, null);
        CodeFactory.registerType(TestData.QuerySelect.class, TestDataQueryExecutorImpl::new, null);
        CodeFactory.registerType(TestData.QueryOperationFields.class, TestDataQueryExecutorImpl::new, null);
    }

    public TestDataImpl() {
    }

    public List<Test> getGetTests() {
        return getTests;
    }

    public void setGetTests(List<Test> getTests) {
        this.getTests = getTests;
    }

    public TestData.Modify with() {
        return new TestDataModifyImpl(this);
    }

    @Generated("ModifierEnricher")
    protected class TestDataModifyImpl extends BaseModifierImpl<TestData.Modify, TestData> implements TestData.Modify {

        protected TestDataModifyImpl(TestData parent) {
            super(parent);
        }

        public TestData done() {
            return TestDataImpl.this;
        }

        public TestData.Modify getTests(List<Test> getTests) {
            TestDataImpl.this.getTests = getTests;
            return this;
        }
    }

    @Generated("QueryEnricher")
    protected static class TestDataQueryExecutorImpl extends QueryExecutor implements TestData.QueryUpdate, TestData.QuerySelect, TestData.QueryFieldsStart {

        protected TestDataQueryExecutorImpl() {
            super(TestData.class, () -> new TestDataQueryNameImpl(), parent -> parent);
        }

        public QueryAggregateOperation aggregate() {
            return (QueryAggregateOperation) _aggregateStart(new TestDataQueryOrderImpl(this, TestDataQueryExecutorImpl.this::_aggregateIdentifier));
        }

        public TestData.QueryOrder order() {
            return (TestData.QueryOrder) _orderStart(new TestDataQueryOrderImpl(this, TestDataQueryExecutorImpl.this::_orderIdentifier));
        }

        @Generated("QueryEnricher")
        protected class TestDataQueryOrderImpl extends QueryOrderer implements TestData.QueryOrder, TestData.QueryAggregate {

            protected TestDataQueryOrderImpl(TestDataQueryExecutorImpl executor, Function<String, Object> func) {
                super(executor, func);
            }
        }
    }

    @Generated("QueryEnricher")
    protected static class TestDataQueryNameImpl extends BaseQueryNameImpl implements TestData.QueryName, QueryEmbed {
    }
}
