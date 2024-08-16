package card.uz.cards.dto;

import card.uz.cards.model.CardStatus;
import card.uz.cards.model.Currency;
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
    private BigDecimal balance;
    private Currency currency;
}
