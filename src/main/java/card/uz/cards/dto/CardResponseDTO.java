package card.uz.cards.dto;

import card.uz.cards.entity.CardState.CardStatus;
import card.uz.cards.entity.CardState.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardResponseDTO {
    private String card_id;
    private Long user_id;
    private CardStatus status;
    private BigDecimal balance;
    private Currency currency;
}
