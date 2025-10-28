/*Generated code by Binis' code generator.*/
package net.binis.codegen.other;

import net.binis.codegen.Test;
import javax.annotation.processing.Generated;
import java.util.Optional;
import java.util.List;
import static java.util.Comparator.comparing;

@Generated(value = "net.binis.codegen.other.ReferencePrototype", comments = "Reference")
public class ReferenceImpl implements Reference {

    protected String title;

    public ReferenceImpl() {
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var other = (Reference) o;
        return getTitle().equals(other.getTitle());
    }

    public Optional<Test> getOptional() {
        return Optional.empty();
    }

    public List<Test> getSorted(List<Test> list) {
        return list.stream().sorted(comparing(Test::getTitle)).toList();
    }

    public String getStr(Test proto) {
        return proto.getTitle();
    }

    public String getTitle() {
        return title;
    }

    public int hashCode() {
        return getTitle().hashCode();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toString() {
        return getTitle();
    }
}
