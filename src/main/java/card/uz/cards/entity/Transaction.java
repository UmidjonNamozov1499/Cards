package card.uz.cards.entity;

import card.uz.cards.entity.CardState.Currency;
import card.uz.cards.entity.CardState.Purpose;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "transaction")
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String transactionId;
    @Column(nullable = false)
    private Long amount;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Card card;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;
    @Column(unique = true)
    private String idempotencyKey;
    private Long exchangeRate;
    private Purpose purpose;
    private Currency currency;

    public Transaction(String transactionId, Long amount, Card card, User user, String idempotencyKey, Long exchangeRate, Purpose purpose, Currency currency) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.card = card;
        this.user = user;
        this.idempotencyKey = idempotencyKey;
        this.exchangeRate = exchangeRate;
        this.purpose = purpose;
        this.currency = currency;
    }

    public Transaction(String transactionId, Long amount, Card card, User user, String idempotencyKey, Long exchangeRate, Currency currency) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.card = card;
        this.user = user;
        this.idempotencyKey = idempotencyKey;
        this.exchangeRate = exchangeRate;
        this.currency = currency;
    }
}
