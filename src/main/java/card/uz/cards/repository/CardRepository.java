package card.uz.cards.repository;

import card.uz.cards.entity.Card;
import card.uz.cards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card,Long> {
    List<Card> findAllByUser(User user);
}
