/*Generated code by Binis' code generator.*/
package net.binis.codegen.tst;

import net.binis.codegen.proto.base.InheirTest;
import javax.annotation.processing.Generated;
import java.util.List;

@Generated(value = "net.binis.codegen.tst.Inheir2TestPrototype", comments = "Inheir2Test")
public class Inheir2TestImpl implements Inheir2Test {

    protected int data;

    protected List<String> list;

    @SuppressWarnings("unused")
    protected InheirTest parent;

    protected String title;

    public Inheir2TestImpl() {
    }

    public int getData() {
        return data;
    }

    public List<String> getList() {
        return list;
    }

    public InheirTest getParent() {
        return parent;
    }

    public String getTitle() {
        return title;
    }

    public void setData(int data) {
        this.data = data;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public void setParent(InheirTest parent) {
        this.parent = parent;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
