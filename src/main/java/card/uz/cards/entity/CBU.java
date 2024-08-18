package card.uz.cards.entity;

import card.uz.cards.entity.CardState.Currency;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "cbu")
@Builder
public class CBU {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "currency_exchange", length = 3, nullable = false)
    private String currencyExchange;

    @Column(name = "amount", nullable = false)
    private Long amount;
}
