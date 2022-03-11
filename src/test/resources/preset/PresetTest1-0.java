/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.spring.query.executor.QueryOrderer;
import net.binis.codegen.spring.query.executor.QueryExecutor;
import net.binis.codegen.spring.query.base.BaseQueryNameImpl;
import net.binis.codegen.spring.query.*;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.creator.EntityCreator;
import javax.annotation.processing.Generated;
import java.util.function.Function;
import java.util.Optional;
import java.util.List;

@Generated(value = "PresetTestPrototype", comments = "PresetTest")
public class PresetTestImpl implements PresetTest {

    protected int data;

    protected String title;

    // region constructor & initializer
    {
        CodeFactory.registerType(PresetTest.QuerySelect.class, PresetTestQueryExecutorImpl::new, null);
        CodeFactory.registerType(PresetTest.class, PresetTestImpl::new, null);
        CodeFactory.registerType(PresetTest.QueryName.class, PresetTestQueryNameImpl::new, null);
    }

    public PresetTestImpl() {
    }
    // endregion

    // region getters
    public int getData() {
        return data;
    }

    public String getTitle() {
        return title;
    }
    // endregion

    // region setters
    public void setData(int data) {
        this.data = data;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    // endregion

    // region inner classes
    protected static class PresetTestQueryExecutorImpl extends QueryExecutor implements PresetTest.QuerySelect, PresetTest.QueryFieldsStart {

        protected PresetTestQueryExecutorImpl() {
            super(PresetTest.class, () -> new PresetTestQueryNameImpl());
        }

        public PresetTestQueryExecutorImpl __queryTitle(String title, int data) {
            ((PresetTest.QuerySelect<Object>) this).title().contains(title).and().data(data);
            return this;
        }

        public PresetTestQueryExecutorImpl __queryTitleString() {
            ((PresetTest.QuerySelect<Object>) this).title("title");
            return this;
        }

        public QueryAggregateOperation aggregate() {
            return (QueryAggregateOperation) aggregateStart(new PresetTestQueryOrderImpl(this, PresetTestQueryExecutorImpl.this::aggregateIdentifier));
        }

        public QuerySelectOperation data(int data) {
            return identifier("data", data);
        }

        public QueryFunctions data() {
            return identifier("data");
        }

        public PresetTest.QueryOrder order() {
            return (PresetTest.QueryOrder) orderStart(new PresetTestQueryOrderImpl(this, PresetTestQueryExecutorImpl.this::orderIdentifier));
        }

        public QuerySelectOperation title(String title) {
            return identifier("title", title);
        }

        public QueryFunctions title() {
            return identifier("title");
        }

        protected class PresetTestQueryOrderImpl extends QueryOrderer implements PresetTest.QueryOrder, PresetTest.QueryAggregate {

            protected PresetTestQueryOrderImpl(PresetTestQueryExecutorImpl executor, Function<String, Object> func) {
                super(executor, func);
            }

            public QueryOrderOperation data() {
                return (QueryOrderOperation) func.apply("data");
            }

            public QueryOrderOperation title() {
                return (QueryOrderOperation) func.apply("title");
            }
        }
    }

    protected static class PresetTestQueryNameImpl extends BaseQueryNameImpl implements PresetTest.QueryName, QueryEmbed {

        public QueryFunctions data() {
            return executor.identifier("data");
        }

        public QuerySelectOperation data(int data) {
            return executor.identifier("data", data);
        }

        public QueryFunctions title() {
            return executor.identifier("title");
        }

        public QuerySelectOperation title(String title) {
            return executor.identifier("title", title);
        }
    }
    // endregion
}
