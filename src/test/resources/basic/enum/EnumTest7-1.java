/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.base.enumeration.CodeEnum;
import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.factory.CodeFactory;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.collection.CodeList;
import net.binis.codegen.annotation.Default;
import net.binis.codegen.Test.TestEnum;
import javax.annotation.processing.Generated;
import java.util.List;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "TestImpl")
@Default("net.binis.codegen.TestImpl")
@SuppressWarnings("unchecked")
public interface Test {

    // region starters
    static Test.Modify create() {
        return (Test.Modify) EntityCreatorModifier.create(Test.class).with();
    }
    // endregion

    List<Test.TestEnum> getList();

    Test.Modify with();

    // region inner classes
    interface Modify extends BaseModifier<Test.Modify, Test> {
        CodeList<Test.TestEnum, Test.Modify> list();
        Modify list(List<Test.TestEnum> list);
    }

    @net.binis.codegen.annotation.Generated(by = "net.binis.codegen.TestPrototype.TestEnumPrototype")
    @Default("net.binis.codegen.TestImpl$TestEnumImpl")
    public interface TestEnum extends CodeEnum {

        static final TestEnum ONE = CodeFactory.initializeEnumValue(TestEnum.class, "ONE", 0);

        static final TestEnum TWO = CodeFactory.initializeEnumValue(TestEnum.class, "TWO", 1);

        static TestEnum valueOf(String name) {
            return CodeFactory.enumValueOf(TestEnum.class, name);
        }

        static TestEnum valueOf(int ordinal) {
            return CodeFactory.enumValueOf(TestEnum.class, ordinal);
        }

        static TestEnum[] values() {
            return CodeFactory.enumValues(TestEnum.class);
        }
    }

    @net.binis.codegen.annotation.Generated(by = "net.binis.codegen.TestPrototype.TestEnum2Prototype")
    @Default("net.binis.codegen.TestImpl$TestEnumImpl")
    public interface TestEnum2 extends CodeEnum {

        static final TestEnum FOUR = CodeFactory.initializeEnumValue(TestEnum.class, "FOUR", 3);

        static final TestEnum ONE = TestEnum.ONE;

        static final TestEnum THREE = CodeFactory.initializeEnumValue(TestEnum.class, "THREE", 2);

        static final TestEnum TWO = TestEnum.TWO;

        static TestEnum valueOf(String name) {
            return CodeFactory.enumValueOf(TestEnum.class, name);
        }

        static TestEnum valueOf(int ordinal) {
            return CodeFactory.enumValueOf(TestEnum.class, ordinal);
        }

        static TestEnum[] values() {
            return CodeFactory.enumValues(TestEnum.class);
        }
    }
    // endregion
}
