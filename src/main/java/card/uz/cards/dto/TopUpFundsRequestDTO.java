package card.uz.cards.dto;

import card.uz.cards.entity.CardState.Currency;
import card.uz.cards.entity.CardState.Purpose;
import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopUpFundsRequestDTO {
    @Column(nullable = false)
    private String external_id;
    private Long amount;
    private Currency currency;
}
