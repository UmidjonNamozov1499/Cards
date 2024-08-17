package card.uz.cards.payload;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.header.Header;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMessage {

    private boolean status;

    private String code;

    private String message;

    private HttpHeaders header;

    private Object object;

    public ResponseMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseMessage(String code, String message, Object object) {
        this.code = code;
        this.message = message;
        this.object = object;
    }

    public ResponseMessage(boolean status, String code, Object object) {
        this.status = status;
        this.code = code;
        this.object = object;
    }

    public ResponseMessage(HttpHeaders header, Object object) {
        this.header = header;
        this.object = object;
    }
}
