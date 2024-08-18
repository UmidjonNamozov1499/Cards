package card.uz.cards.entity;

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
    private Long amount;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Card card;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;
    @Column(unique = true)
    private String idempotencyKey;
}
