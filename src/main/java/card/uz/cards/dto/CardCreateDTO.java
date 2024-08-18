package card.uz.cards.dto;

import card.uz.cards.entity.CardState.CardStatus;
import card.uz.cards.entity.CardState.Currency;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardCreateDTO {
    private Long user_id;
    private CardStatus status;
    private Long balance;
    private Currency currency;
}
