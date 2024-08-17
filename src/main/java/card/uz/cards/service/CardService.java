package card.uz.cards.service;

import card.uz.cards.dto.CardCreateDTO;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface CardService {
    HttpEntity<?> createCard(String idempotencyKey, CardCreateDTO dto);

    HttpEntity<?> getCard(String eTag,String  id);

//    HttpEntity<?> blockCard(String cardId,UUID eTag);
    HttpEntity<?>unBlock(String cardId,String ifMatch);
}
