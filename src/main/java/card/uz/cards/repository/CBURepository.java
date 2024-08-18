package card.uz.cards.repository;

import card.uz.cards.entity.CBU;
import card.uz.cards.entity.CardState.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CBURepository extends JpaRepository<CBU, Long> {
    Optional<CBU> findFirstByCurrencyOrderByIdDesc(String currency);

    Optional<CBU> findFirstByCurrencyAndCurrencyExchangeOrderByIdDesc(String currency,String currencyExchange);
}
