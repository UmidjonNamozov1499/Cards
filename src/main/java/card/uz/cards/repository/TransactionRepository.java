package card.uz.cards.repository;

import card.uz.cards.entity.Transaction;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TransactionRepository extends JpaRepository<Transaction,Long> {

   Page<Transaction> findByCardId(String cardId,Pageable pageable);
}
