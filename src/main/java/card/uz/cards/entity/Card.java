package card.uz.cards.entity;

import card.uz.cards.entity.CardState.CardStatus;
import card.uz.cards.entity.CardState.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "card")
@Builder
public class Card {
    @Id
    @GeneratedValue
    private String id;
    private String year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.ACTIVE;
    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.UZS;


    public void init() {
        this.id = String.valueOf(generateCustomUUID());
    }

    private UUID generateCustomUUID() {
        Random random = new Random();
        StringBuilder uuidBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int segment = 1000 + random.nextInt(9000);
            uuidBuilder.append(segment);
            if (i < 3) {
                uuidBuilder.append(" ");
            }
        }
        String customUUID = uuidBuilder.toString();
        return UUID.nameUUIDFromBytes(customUUID.getBytes());
    }

}
