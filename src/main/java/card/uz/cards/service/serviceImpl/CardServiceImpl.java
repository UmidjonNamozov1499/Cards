package card.uz.cards.service.serviceImpl;

import card.uz.cards.dto.CardCreateDTO;
import card.uz.cards.dto.CardResponseDTO;
import card.uz.cards.entity.Card;
import card.uz.cards.entity.CardState.CardStatus;
import card.uz.cards.entity.Idempotency;
import card.uz.cards.entity.User;
import card.uz.cards.payload.Payload;
import card.uz.cards.payload.ResponseMessage;
import card.uz.cards.repository.CardRepository;
import card.uz.cards.repository.IdempotencyRepository;
import card.uz.cards.repository.UserRepository;
import card.uz.cards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.header.Header;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class CardServiceImpl implements CardService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final IdempotencyRepository idempotencyRepository;
    private static final Logger _logger = LoggerFactory.getLogger(CardServiceImpl.class);

    @Override
    public HttpEntity<?> createCard(String idempotencyKey, CardCreateDTO dto) {
        try {
            if (idempotencyKey == null || dto == null) {
                return Payload.badRequest("Request error").response();
            }
            if (!isAuthenticated()) {
                return Payload.unauthorized().response();
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (dto.getBalance().compareTo(new BigDecimal("10000")) > 0) {
                return Payload.badRequest("Balance entered high").response();
            }

            Card card = new Card();
            card.setUser(user);
            card.setStatus(dto.getStatus());
            card.setBalance(dto.getBalance());
            card.setCurrency(dto.getCurrency());


            LocalDate now = LocalDate.now();
            int year = now.getYear() + 4;
            int month = now.getMonthValue();
            card.setYear(String.format("%02d/%02d", month, year));
            Card save = cardRepository.save(card);

            CardResponseDTO cardResponseDTO = new CardResponseDTO(save.getId(),
                    save.getUser().getId(),
                    save.getStatus(),
                    save.getBalance(),
                    save.getCurrency());
            Idempotency idempotency = new Idempotency();
            idempotency.setIdempotencyKey(idempotencyKey);
            idempotency.encodeCard(card);
            idempotencyRepository.save(idempotency);
            return Payload.ok(cardResponseDTO).response();
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return Payload.internalServerError().response();
        }
    }

    @Override
    public HttpEntity<?> getCard(String eTag, String id) {
        try {
            if (id == null) {
                return Payload.badRequest("Id null").response();
            }
            if (!isAuthenticated()) {
                return Payload.unauthorized().response();
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!user.isActivated()) {
                return Payload.unauthorized("User not active").response();
            }
            List<Card> allByUsers_id = cardRepository.findAllByUser(user);
            Card card = new Card();
            for (Card cards : allByUsers_id) {
                if (cards.getId().equals(id)) {
                    card.setId(cards.getId());
                    card.setBalance(cards.getBalance());
                    card.setCurrency(cards.getCurrency());
                    card.setUser(cards.getUser());
                    card.setStatus(cards.getStatus());
                    card.setYear(cards.getYear());
                }
            }
            if (card == null) {
                return Payload.conflict("Card not found").response();
            }
            Idempotency idempotency = new Idempotency();
            idempotency.setIdempotencyKey(eTag);
            idempotency.encodeCard(card);
            idempotencyRepository.save(idempotency);
            return Payload.ok(card).response();
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return Payload.internalServerError().response();
        }
    }
//    @Override
    public ResponseMessage blockCard(String cardId, UUID eTag) {
        try {
            System.out.println(cardId);
            if (!isAuthenticated()) {
                return Payload.unauthorized().response();
            }
            if (eTag == null) {
                return Payload.badRequest("Etag null").response();
            }
            Optional<Idempotency> byIdempotencyKey = idempotencyRepository.findFirstByIdempotencyKeyOrderByIdDesc(String.valueOf(eTag));
            if (byIdempotencyKey.isEmpty()) {
                return Payload.notFound("eTag not found").response();
            }
            Card card = (Card) byIdempotencyKey.get().decodeCard();
            if (card == null) {
                return Payload.conflict("Card not found").response();
            }
            if (!card.getId().equals(cardId)){
                return Payload.conflict("Cart id error").response();
            }
            HttpHeaders header = new HttpHeaders();
            header.set("eTag",String.valueOf(eTag));
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!card.getUser().equals(user)) {
                return Payload.conflict("User not Authenticate").response();
            }
            if (card.getStatus().equals(CardStatus.BLOCKED) || card.getStatus().equals(CardStatus.CLOSED)) {
                return Payload.conflict("CardStatus blocked or Closed").response();
            }
            card.setStatus(CardStatus.BLOCKED);
            cardRepository.save(card);




            return new ResponseMessage(header,card);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return Payload.internalServerError().response();
        }
    }

    @Override
    public HttpEntity<?> unBlock(String cardId, String ifMatch) {
        try {
            if (!isAuthenticated()) {
                return Payload.unauthorized().response();
            }
            Idempotency idempotency = idempotencyRepository.findByIdempotencyKey(ifMatch).orElse(null);
            if (idempotency == null) {
                return Payload.badRequest("If-Match null or not found").response();
            }
            if (cardId == null) {
                return Payload.badRequest("Card id null").response();
            }
            Card card = (Card) idempotency.decodeCard();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!card.getUser().equals(user)) {
                return Payload.conflict("User not Authenticate").response();
            }
            if (card.getId().equals(cardId)){
                return Payload.conflict("Card id error").response();
            }
            if (card.getStatus().equals(CardStatus.CLOSED) || card.getStatus().equals(CardStatus.BLOCKED)) {
                return Payload.badRequest("Card closed pr blocked").response();
            }
            card.setStatus(CardStatus.BLOCKED);
            cardRepository.save(card);
            return Payload.ok().response();
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return Payload.internalServerError().response();
        }
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.isEnabled();
    }

}
