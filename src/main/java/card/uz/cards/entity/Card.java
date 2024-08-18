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
    @Column(unique = true, nullable = false)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
    @Column(nullable = false)
    private Long balance;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CardStatus status = CardStatus.ACTIVE;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.UZS;
    @Column(unique = true)
    private String idempotencyKey;

    public Card( User user, Long balance, CardStatus status, Currency currency, String idempotencyKey) {
        this.user = user;
        this.balance = balance;
        this.status = status;
        this.currency = currency;
        this.idempotencyKey = idempotencyKey;
    }
    public Card( User user, Long balance, CardStatus status, Currency currency) {
        this.user = user;
        this.balance = balance;
        this.status = status;
        this.currency = currency;
    }

//    public void init() {
//        this.id = String.valueOf(generateCustomUUID());
//    }
//
//    private UUID generateCustomUUID() {
//        Random random = new Random();
//        StringBuilder uuidBuilder = new StringBuilder();
//        for (int i = 0; i < 4; i++) {
//            int segment = 1000 + random.nextInt(9000);
//            uuidBuilder.append(segment);
//            if (i < 3) {
//                uuidBuilder.append(" ");
//            }
//        }
//        String customUUID = uuidBuilder.toString();
//        return UUID.nameUUIDFromBytes(customUUID.getBytes());
//    }

}
