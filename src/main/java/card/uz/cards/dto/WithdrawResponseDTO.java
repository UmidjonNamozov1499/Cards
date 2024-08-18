package card.uz.cards.dto;

import card.uz.cards.entity.CardState.Currency;
import card.uz.cards.entity.CardState.Purpose;
import lombok.*;
import org.hibernate.event.spi.PreInsertEvent;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithdrawResponseDTO {
    private String transaction_id;
    private String external_id;
    private String card_id;
    private Long amount;
    private Long after_balance;
    private Currency currency;
    private Purpose purpose;
    private Long exchange_rate;
}
