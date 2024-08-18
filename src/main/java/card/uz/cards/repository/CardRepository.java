package card.uz.cards.repository;

import card.uz.cards.entity.Card;
import card.uz.cards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card,Long> {
    List<Card> findAllByUser(User user);
//    @Query(nativeQuery = true,
//            "SELECT COUNT(c) FROM Card c WHERE c.user.id = :userId")
//    long countByUserId(@Param("userId") Long userId);
}
