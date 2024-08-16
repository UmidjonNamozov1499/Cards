package card.uz.cards.service;

import card.uz.cards.dto.CardCreateDTO;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface CardService {
HttpEntity<?> createCard(UUID idempotencyKey, CardCreateDTO dto);

}
