package card.uz.cards.controller;

import card.uz.cards.dto.CardCreateDTO;
import card.uz.cards.dto.WithdrawRequestDTO;
import card.uz.cards.payload.Payload;
import card.uz.cards.payload.ResponseMessage;
import card.uz.cards.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/card")
@CrossOrigin
public class CardController {
    @Autowired
    CardService cardService;

    @PostMapping()
    public HttpEntity<?> createCard(@RequestHeader String idempotencyKey,
                                    @RequestBody CardCreateDTO dto) {
        return cardService.createCard(idempotencyKey, dto);
    }

    @GetMapping("/{cardId}")
    public HttpEntity<?> getCard(@PathVariable String cardId) {
        return cardService.getCard(cardId);
    }

    @PostMapping("/{cardId}/block")
    public HttpEntity<?> blockCard(@PathVariable String cardId,
                                   @RequestHeader String ifMatch) {
        return cardService.blockCard(cardId, ifMatch);
    }

    @PostMapping("/{cardId}/unblock")
    public HttpEntity<?> unBlock(@PathVariable String cardId,
                                 @RequestHeader String ifMatch) {
        return cardService.unBlock(cardId, ifMatch);
    }

    @PostMapping("/{cardId}/debit")
    public HttpEntity<?> withdraw(@PathVariable String cardId,
                                  @RequestHeader String idempotencyKey,
                                  @RequestBody WithdrawRequestDTO dto) {
        return cardService.withdraw(cardId, idempotencyKey, dto);
    }
}
