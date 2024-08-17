package card.uz.cards.repository;

import card.uz.cards.entity.Idempotency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<Idempotency,Long> {
    Optional<Idempotency> findByIdempotencyKey(String idempotencyKey);

    Optional<Idempotency> findFirstByIdempotencyKeyOrderByIdDesc(String idempotencyKey);
}
