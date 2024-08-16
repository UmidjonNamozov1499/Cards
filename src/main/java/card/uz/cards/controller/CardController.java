package card.uz.cards.controller;

import card.uz.cards.dto.CardCreateDTO;
import card.uz.cards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
@CrossOrigin
public class CardController {
    private final CardService cardService;

    @PostMapping(value = "/card")
    public HttpEntity<?> createCard(@RequestHeader UUID idempotencyKey,
                                    @RequestBody CardCreateDTO dto) {
        return cardService.createCard(idempotencyKey, dto);
    }
}
