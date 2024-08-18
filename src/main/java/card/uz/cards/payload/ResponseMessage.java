package card.uz.cards.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMessage {

    private int code;

    private String message;

    private HttpHeaders header;

    private Object object;
    private int page;
    private int size;

    public ResponseMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseMessage(int code, String message, Object object) {
        this.code = code;
        this.message = message;
        this.object = object;
    }

    public ResponseMessage(HttpHeaders header, Object object) {
        this.header = header;
        this.object = object;
    }
}
