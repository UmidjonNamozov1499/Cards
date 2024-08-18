package card.uz.cards.dto;

import card.uz.cards.entity.CardState.Currency;
import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopUpFundsResponseDTO {
    private String transaction_id;
    private String external_id;
    private String card_id;
    private Long after_balance;
    private Long amount;
    private Currency currency;
    private Long exchange_rate;
}
