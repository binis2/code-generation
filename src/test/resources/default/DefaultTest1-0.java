/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.map.Mapper;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;
import java.util.Map;

@Generated(value = "net.binis.codegen.EdgeMessagePrototype", comments = "EdgeMessage")
public class EdgeMessageImpl implements EdgeMessage {

    protected Map<String, Object> payload;

    public EdgeMessageImpl() {
    }

    public EdgeMessage fromJson(String message) {
        var instance = CodeFactory.create(EdgeMessage.class);
        return Mapper.convert(message, instance);
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    static EdgeMessage jsonForClass(String message) {
        var instance = CodeFactory.create(EdgeMessage.class);
        return Mapper.convert(message, instance);
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
