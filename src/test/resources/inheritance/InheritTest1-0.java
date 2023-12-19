/*Generated code by Binis' code generator.*/
package net.binis.codegen.proto.base;

import javax.annotation.processing.Generated;
import java.util.List;

@Generated(value = "net.binis.codegen.proto.base.InheirTestPrototype", comments = "InheirTest")
public class InheirTestImpl implements InheirTest {

    protected int data;

    protected List<String> list;

    protected String title;

    public InheirTestImpl() {
    }

    public int getData() {
        return data;
    }

    public List<String> getList() {
        return list;
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

    public void setTitle(String title) {
        this.title = title;
    }
}
