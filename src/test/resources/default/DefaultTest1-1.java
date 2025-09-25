/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.map.Mapper;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;
import java.util.Map;

@Generated(value = "net.binis.codegen.EdgeMessagePrototype", comments = "EdgeMessageImpl")
public interface EdgeMessage {
    EdgeMessage fromJson(String message);

    Map<String, Object> getPayload();

    static EdgeMessage json(String message) {
        var instance = CodeFactory.create(EdgeMessage.class);
        return Mapper.convert(message, instance);
    }

    void setPayload(Map<String, Object> payload);
}
