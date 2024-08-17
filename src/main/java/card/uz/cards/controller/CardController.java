package card.uz.cards.controller;

import card.uz.cards.dto.CardCreateDTO;
import card.uz.cards.payload.Payload;
import card.uz.cards.payload.ResponseMessage;
import card.uz.cards.service.CardService;
import card.uz.cards.service.serviceImpl.CardServiceImpl;
import lombok.RequiredArgsConstructor;
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

    @Autowired
    CardServiceImpl cardServiceImpl;
    @PostMapping()
    public HttpEntity<?> createCard(@RequestHeader String idempotencyKey,
                                    @RequestBody CardCreateDTO dto) {
        return cardService.createCard(idempotencyKey, dto);
    }
    @GetMapping("/{cardId}")
    public HttpEntity<?> getCard(@PathVariable String cardId,
                                 @RequestHeader String eTag){
        return cardService.getCard(eTag, cardId);

    }
    @PostMapping("/block/{cardId}")
    public HttpEntity<?> blockCard(@PathVariable String cardId,
                                   @RequestParam UUID eTag){

        return ResponseEntity.ok().body()

        try {
            System.out.println("cardId : "+cardId);
            System.out.println("eTag : "+eTag);
            ResponseMessage responseMessage = cardServiceImpl.blockCard(cardId,eTag);
            return ResponseEntity.ok().headers(responseMessage.getHeader()).body(responseMessage.getObject());
        }catch (Exception re){
            re.printStackTrace();
            System.out.println("Error : "+re);
            return Payload.conflict().response();
        }
    }
    @PutMapping("/{cardId}/unblock")
    public HttpEntity<?> unBlock(@PathVariable String cardId,
                                 @RequestHeader String ifMatch){
        return cardService.unBlock(cardId, ifMatch);
    }
}
