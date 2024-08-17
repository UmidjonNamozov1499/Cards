package card.uz.cards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "idempotency")
@Builder
public class Idempotency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String idempotencyKey;
    @Column(nullable = false)
    private String enCode;

    @OneToMany
    private List<Card> card;

    @OneToMany
    private List<Transaction> transactions;
    public void encodeCard(Object object) {
        try {
            // Serialize the Card object
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
//            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            objectOutputStream.close();

            // Convert serialized object to Base64 encoded string
            byte[] serializedCard = byteArrayOutputStream.toByteArray();
            this.enCode = Base64.getEncoder().encodeToString(serializedCard);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode card", e);
        }
    }
    public Object decodeCard() {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(this.enCode);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

            return objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode card", e);
        }
    }
}
