/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.objects.Pair;
import net.binis.codegen.modifier.BaseModifier;
import net.binis.codegen.creator.EntityCreatorModifier;
import net.binis.codegen.collection.CodeList;
import net.binis.codegen.annotation.Default;
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

    List<Pair<String, List<String>>> getTest();

    Test.Modify with();

    // region inner classes
    interface Modify extends BaseModifier<Test.Modify, Test> {
        CodeList<Pair, Test.Modify> test();
        Modify test(List<Pair<String, List<String>>> test);
    }
    // endregion
}
