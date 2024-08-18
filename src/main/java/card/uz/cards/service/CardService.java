package card.uz.cards.service;

import card.uz.cards.dto.*;
import card.uz.cards.entity.CBU;
import card.uz.cards.entity.Card;
import card.uz.cards.entity.CardState.CardStatus;
import card.uz.cards.entity.CardState.Currency;
import card.uz.cards.entity.Transaction;
import card.uz.cards.entity.User;
import card.uz.cards.payload.ResponseMessage;
import card.uz.cards.repository.CBURepository;
import card.uz.cards.repository.CardRepository;
import card.uz.cards.repository.TransactionRepository;
import card.uz.cards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class CardService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CBURepository cbuRepository;
    private final TransactionRepository transactionRepository;
    private static final Logger _logger = LoggerFactory.getLogger(CardService.class);

    public HttpEntity<?> createCard(String idempotencyKey, CardCreateDTO dto) {
        try {
            ResponseMessage responseMessage = new ResponseMessage();

            if (idempotencyKey == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Missing field");
                return ResponseEntity.badRequest().body(responseMessage);
            }
            if (dto == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Missing field");
                return ResponseEntity.badRequest().body(responseMessage);
            }
            if (!isAuthenticated()) {
                responseMessage.setCode(401);
                responseMessage.setMessage("Unauthorized");
                return ResponseEntity.status(401).body(responseMessage);
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<User> userOptional = userRepository.findByUsername(userDetails.getUsername());
            if (userOptional.isEmpty()) {
                responseMessage.setCode(404);
                responseMessage.setMessage("User not found");
                return ResponseEntity.status(404).body(responseMessage);
            }
            List<Card> cardList = cardRepository.findAllByUser(userOptional.get());
            if (cardList.size() >= 3) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Limit exceeded");
                return ResponseEntity.status(400).body(responseMessage);
            }
            Card card = new Card();
            card.setId(generateUUID());
            card.setUser(userOptional.get());
            card.setCurrency(dto.getCurrency());
            card.setStatus(dto.getStatus());
            card.setBalance(dto.getBalance());
            card.setIdempotencyKey(idempotencyKey);
            Card saveUser = cardRepository.save(card);
            CardResponseDTO responseDTO = new CardResponseDTO(
                    saveUser.getId(),
                    saveUser.getUser().getId(),
                    saveUser.getStatus(),
                    saveUser.getBalance(),
                    saveUser.getCurrency()
            );
            responseMessage.setCode(200);
            responseMessage.setMessage("Ok");
            responseMessage.setObject(responseDTO);
            return ResponseEntity.status(responseMessage.getCode()).headers(responseMessage.getHeader()).body(responseMessage.getObject());
        } catch (Exception e) {
            _logger.error(e.getMessage());
            ResponseMessage responseMessage = new ResponseMessage(500, "Internal Server Error");
            return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
        }
    }

    public HttpEntity<?> getCard(String id) {
        try {
            ResponseMessage responseMessage = new ResponseMessage();
            if (id == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Missing field");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            if (!isAuthenticated()) {
                responseMessage.setCode(401);
                responseMessage.setMessage("Unauthorized");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();
            if (!user.isActivated()) {
                responseMessage.setCode(404);
                responseMessage.setMessage("User not found");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            List<Card> allByUsers_id = cardRepository.findAllByUser(user);
            Card card = null;
            for (Card cards : allByUsers_id) {
                if (cards.getId().equals(id)) {
                    card = new Card(
                            cards.getId(),
                            cards.getUser(),
                            cards.getBalance(),
                            cards.getStatus(),
                            cards.getCurrency(),
                            cards.getIdempotencyKey()
                    );
                }
            }
            if (card == null) {
                responseMessage.setCode(404);
                responseMessage.setMessage("Not Found Card");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            CardResponseDTO responseDTO = new CardResponseDTO(card.getId(),
                    card.getUser().getId(),
                    card.getStatus(),
                    card.getBalance(),
                    card.getCurrency());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("eTag", card.getIdempotencyKey());
            responseMessage.setCode(200);
            responseMessage.setHeader(httpHeaders);
            responseMessage.setObject(responseDTO);
            return ResponseEntity.status(responseMessage.getCode()).headers(httpHeaders).body(responseMessage.getObject());
        } catch (Exception e) {
            _logger.error(e.getMessage());
            ResponseMessage responseMessage = new ResponseMessage(500, "Internal Server Error");
            return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
        }
    }

    public HttpEntity<?> blockCard(String cardId, String ifMatch) {
        try {
            ResponseMessage responseMessage = new ResponseMessage();
            if (!isAuthenticated()) {
                responseMessage.setCode(401);
                responseMessage.setMessage("Unauthorized");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            if (cardId == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Missing field");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            if (ifMatch == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Missing field");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();
            if (!user.isActivated()) {
                responseMessage.setCode(404);
                responseMessage.setMessage("User not found");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            List<Card> cardList = cardRepository.findAllByUser(user);
            Card card = null;
            for (Card cards : cardList) {
                if (cards.getId().equals(cardId)) {
                    card = cards;
                }
            }
            if (card == null) {
                responseMessage.setCode(404);
                responseMessage.setMessage("Card not found");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            if (!card.getIdempotencyKey().equals(ifMatch)) {
                responseMessage.setCode(404);
                responseMessage.setMessage("Card eTag and If-Match not equals");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            if (card.getStatus().equals(CardStatus.BLOCKED) || card.getStatus().equals(CardStatus.CLOSED)) {
                responseMessage.setCode(404);
                responseMessage.setMessage("Card Status blocked or Closed");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            card.setStatus(CardStatus.BLOCKED);
            cardRepository.save(card);
            responseMessage.setMessage("No Content");
            responseMessage.setCode(204);
            return ResponseEntity.ok().body(responseMessage);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            ResponseMessage responseMessage = new ResponseMessage(500, "Internal Server Error");
            return ResponseEntity.status(responseMessage.getCode()).body(new ResponseMessage(responseMessage.getCode(), responseMessage.getMessage()));
        }
    }

    public HttpEntity<?> unBlock(String cardId, String ifMatch) {
        try {
            ResponseMessage responseMessage = new ResponseMessage();
            if (!isAuthenticated()) {
                responseMessage.setCode(401);
                responseMessage.setMessage("Unauthorized");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            if (cardId == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Missing field");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            if (ifMatch == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Missing field");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();
            if (!user.isActivated()) {
                responseMessage.setCode(404);
                responseMessage.setMessage("User not found");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            List<Card> cardList = cardRepository.findAllByUser(user);
            Card card = null;
            for (Card cards : cardList) {
                if (cards.getId().equals(cardId)) {
                    card = cards;
                }
            }
            if (card == null) {
                responseMessage.setCode(404);
                responseMessage.setMessage("Card not found");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            if (!card.getIdempotencyKey().equals(ifMatch)) {
                responseMessage.setCode(404);
                responseMessage.setMessage("Card eTag and If-Match not equals");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            if (card.getStatus().equals(CardStatus.ACTIVE) || card.getStatus().equals(CardStatus.CLOSED)) {
                responseMessage.setCode(404);
                responseMessage.setMessage("Card Status Active or Closed");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            card.setStatus(CardStatus.ACTIVE);
            cardRepository.save(card);
            responseMessage.setMessage("No Content");
            responseMessage.setCode(204);
            return ResponseEntity.ok().body(responseMessage);
        } catch (Exception e) {
            _logger.error(e.getMessage());
            ResponseMessage responseMessage = new ResponseMessage(500, "Internal Server Error");
            return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
        }
    }

    public HttpEntity<?> withdraw(String idempotencyKey, String cardId, WithdrawRequestDTO requestDTO) {
        try {
            ResponseMessage responseMessage = new ResponseMessage();
            if (idempotencyKey == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Idempotency-Key null");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            if (requestDTO == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: DTO null");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            if (cardId == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Card ID null");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            if (!isAuthenticated()) {
                responseMessage.setCode(401);
                responseMessage.setMessage("Unauthorized");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();
            if (!user.isActivated()) {
                responseMessage.setCode(404);
                responseMessage.setMessage("User not found");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            List<Card> allByUser = cardRepository.findAllByUser(user);
            Card card = null;
            for (Card cards : allByUser) {
                if (cards.getId().equals(cardId)) {
                    card = cards;
                }
            }
            if (card == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: invalid data");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            if (card.getStatus().equals(CardStatus.BLOCKED)) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Card Blocked");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            if (card.getStatus().equals(CardStatus.CLOSED)) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Card Closed");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            WithdrawResponseDTO responseDTO = new WithdrawResponseDTO();
            Currency requestCurrency = requestDTO.getCurrency();
            Currency cardCurrency = card.getCurrency();
            Long requestDTOAmount = requestDTO.getAmount();
            Long cardBalance = card.getBalance();
            Long cardBalanceAfter = null;
//            CBU cbu = cbuRepository.findFirstByCurrencyAndCurrencyExchangeOrderByIdDesc(
//                    String.valueOf(Currency.USD),
//                    String.valueOf(Currency.UZS)).get();
            CBU cbu = cbuRepository.findFirstByCurrencyOrderByIdDesc(String.valueOf(Currency.USD)).get();

            if (!requestCurrency.equals(cardCurrency)) {
                //Valyuta kartadan sum yechmoqchi bo`lganda
                if (requestCurrency.equals(Currency.UZS)
                        && cardCurrency.equals(Currency.USD)) {
                    long result = requestDTOAmount / cbu.getAmount();
                    //Kiritilgan summa karta balancian ko`p bo`lsa
                    if (result > cardBalance) {
                        responseMessage.setCode(400);
                        responseMessage.setMessage("Bad request: Insufficient Funds");
                        return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
                    }
                    cardBalanceAfter = cardBalance - result;

                } else {
                    //Sum kartaan valyuta olmoqchi bo`lganda
                    long result = requestDTOAmount * cbu.getAmount();
                    if (result > cardBalance) {
                        responseMessage.setCode(400);
                        responseMessage.setMessage("Bad request: Insufficient Funds");
                        return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
                    }
                    cardBalanceAfter = cardBalance - result;
                }

                responseDTO.setAmount(requestDTO.getAmount());
                responseDTO.setAfter_balance(cardBalanceAfter);
                responseDTO.setTransaction_id(generateUUID());
                responseDTO.setCard_id(cardId);
                responseDTO.setExternal_id(idempotencyKey);
                responseDTO.setCurrency(card.getCurrency());
                responseDTO.setPurpose(requestDTO.getPurpose());
                responseDTO.setExchange_rate(cbu.getAmount());

                card.setBalance(cardBalance);

                cardRepository.save(card);
                responseMessage.setCode(200);
                responseMessage.setObject(responseDTO);
                responseMessage.setMessage("Ok");

                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }

            if (cardBalance < requestDTOAmount) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Insufficient Funds");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            cardBalanceAfter = cardBalance - requestDTOAmount;
            responseDTO.setAmount(requestDTO.getAmount());
            responseDTO.setAfter_balance(cardBalanceAfter);
            responseDTO.setTransaction_id(generateUUID());
            responseDTO.setCard_id(cardId);
            responseDTO.setExternal_id(idempotencyKey);
            responseDTO.setCurrency(card.getCurrency());
            responseDTO.setPurpose(requestDTO.getPurpose());
            responseDTO.setExchange_rate(cbu.getAmount());
            card.setBalance(cardBalanceAfter);
            cardRepository.save(card);
            Transaction transaction = new Transaction(responseDTO.getTransaction_id(),
                    requestDTO.getAmount(),
                    card,
                    user,
                    idempotencyKey,
                    responseDTO.getExchange_rate(),
                    requestDTO.getPurpose(),
                    cardCurrency);
            transactionRepository.save(transaction);
            responseMessage.setCode(200);
            responseMessage.setObject(responseDTO);
            responseMessage.setMessage("Ok");

            return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);

        } catch (Exception e) {
            _logger.error(e.getMessage());
            ResponseMessage responseMessage = new ResponseMessage(500, "Internal Server Error");
            return ResponseEntity.status(responseMessage.getCode()).body(new ResponseMessage(responseMessage.getCode(), responseMessage.getMessage()));
        }
    }

    public HttpEntity<?> topUpFunds(String idempotencyKey, String cardId, TopUpFundsRequestDTO requestDTO) {
        try {
            ResponseMessage responseMessage = new ResponseMessage();
            if (idempotencyKey == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Idempotency-Key null");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            if (requestDTO == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: DTO null");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            if (cardId == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Card ID null");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            if (!isAuthenticated()) {
                responseMessage.setCode(401);
                responseMessage.setMessage("Unauthorized");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();
            if (!user.isActivated()) {
                responseMessage.setCode(404);
                responseMessage.setMessage("User not found");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            List<Card> allByUser = cardRepository.findAllByUser(user);
            Card card = null;
            for (Card cards : allByUser) {
                if (cards.getId().equals(cardId)) {
                    card = cards;
                }
            }
            if (card == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: invalid data");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            if (card.getStatus().equals(CardStatus.BLOCKED)) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Card Blocked");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            if (card.getStatus().equals(CardStatus.CLOSED)) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Card Closed");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            TopUpFundsResponseDTO responseDTO = new TopUpFundsResponseDTO();
            Currency requestCurrency = requestDTO.getCurrency();
            Currency cardCurrency = card.getCurrency();
            Long requestDTOAmount = requestDTO.getAmount();
            Long cardBalance = card.getBalance();
            Long cardBalanceAfter = null;
//            CBU cbu = cbuRepository.findFirstByCurrencyAndCurrencyExchangeOrderByIdDesc(
//                    String.valueOf(Currency.USD),
//                    String.valueOf(Currency.UZS)).get();

            if (!requestCurrency.equals(cardCurrency)) {
                CBU cbu = cbuRepository.findFirstByCurrencyOrderByIdDesc(String.valueOf(Currency.USD)).get();
                //Valyuta kartadan sum yechmoqchi bo`lganda
                if (requestCurrency.equals(Currency.UZS)
                        && cardCurrency.equals(Currency.USD)) {
                    long result = requestDTOAmount / cbu.getAmount();
                    //Kiritilgan summa karta balancian ko`p bo`lsa
                    if (result > cardBalance) {
                        responseMessage.setCode(400);
                        responseMessage.setMessage("Bad request: Insufficient Funds");
                        return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
                    }
                    cardBalanceAfter = cardBalance + result;

                } else {
                    //Sum kartaan valyuta olmoqchi bo`lganda
                    long result = requestDTOAmount * cbu.getAmount();
                    if (result > cardBalance) {
                        responseMessage.setCode(400);
                        responseMessage.setMessage("Bad request: Insufficient Funds");
                        return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
                    }
                    cardBalanceAfter = cardBalance + result;
                }

                responseDTO.setAmount(requestDTO.getAmount());
                responseDTO.setAfter_balance(cardBalanceAfter);
                responseDTO.setTransaction_id(generateUUID());
                responseDTO.setCard_id(cardId);
                responseDTO.setExternal_id(idempotencyKey);
                responseDTO.setCurrency(card.getCurrency());
                responseDTO.setExchange_rate(cbu.getAmount());

                card.setBalance(cardBalance);

                cardRepository.save(card);
                responseMessage.setCode(200);
                responseMessage.setObject(responseDTO);
                responseMessage.setMessage("Ok");

                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }

            if (cardBalance < requestDTOAmount) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Insufficient Funds");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
            }
            cardBalanceAfter = cardBalance + requestDTOAmount;
            responseDTO.setAmount(requestDTO.getAmount());
            responseDTO.setAfter_balance(cardBalanceAfter);
            responseDTO.setTransaction_id(generateUUID());
            responseDTO.setCard_id(cardId);
            responseDTO.setExternal_id(idempotencyKey);
            responseDTO.setCurrency(card.getCurrency());

            card.setBalance(cardBalanceAfter);
            cardRepository.save(card);
            Transaction transaction = new Transaction(responseDTO.getTransaction_id(),
                    requestDTO.getAmount(),
                    card,
                    user,
                    idempotencyKey,
                    responseDTO.getExchange_rate(),
                    cardCurrency);
            transactionRepository.save(transaction);
            responseMessage.setCode(200);
            responseMessage.setObject(responseDTO);
            responseMessage.setMessage("Ok");

            return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);

        } catch (Exception e) {
            _logger.error(e.getMessage());
            ResponseMessage responseMessage = new ResponseMessage(500, "Internal Server Error");
            return ResponseEntity.status(responseMessage.getCode()).body(new ResponseMessage(responseMessage.getCode(), responseMessage.getMessage()));
        }
    }

    public HttpEntity<?> transaction(String cardId, int page, int size) {
        try {
            ResponseMessage responseMessage = new ResponseMessage();
            if (!isAuthenticated()) {
                responseMessage.setCode(401);
                responseMessage.setMessage("Unauthorized");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            if (cardId == null) {
                responseMessage.setCode(400);
                responseMessage.setMessage("Bad request: Missing field");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).get();
            if (!user.isActivated()) {
                responseMessage.setCode(404);
                responseMessage.setMessage("User not found");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            List<Card> cardList = cardRepository.findAllByUser(user);
            Card card = null;
            for (Card cards : cardList) {
                if (cards.getId().equals(cardId)) {
                    card = cards;
                }
            }
            Page<Transaction> transactions = transactionRepository.findByCardId(card.getId(), PageRequest.of(page, size));

            if (transactions.isEmpty()){
                responseMessage.setCode(404);
                responseMessage.setMessage("Card transaction not found");
                return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getMessage());
            }
            responseMessage.setCode(200);
            responseMessage.setObject(transactions);
            responseMessage.setMessage("Ok");
            responseMessage.setPage(page);
            responseMessage.setSize(size);
            return ResponseEntity.status(responseMessage.getCode()).body(responseMessage.getObject());
        } catch (Exception e) {
            _logger.error(e.getMessage());
            ResponseMessage responseMessage = new ResponseMessage(500, "Internal Server Error");
            return ResponseEntity.status(responseMessage.getCode()).body(responseMessage);
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

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
