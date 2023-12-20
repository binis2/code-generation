/*Generated code by Binis' code generator.*/
package net.binis.codegen.proto.base;

import javax.annotation.processing.Generated;
import java.util.List;

@Generated(value = "net.binis.codegen.proto.base.InheirTestPrototype", comments = "InheirTest")
public class InheirTestImpl implements InheirTest {

    protected List<String> rules;

    public InheirTestImpl() {
    }

    public List<String> getRules() {
        return rules;
    }

    public List getRules(Class cls) {
        return getRules().stream().map(r -> cls.getCanonicalName() + r).toList();
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }
}
