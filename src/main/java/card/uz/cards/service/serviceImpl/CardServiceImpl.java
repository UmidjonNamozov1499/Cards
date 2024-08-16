package card.uz.cards.service.serviceImpl;

import card.uz.cards.dto.CardCreateDTO;
import card.uz.cards.entity.Card;
import card.uz.cards.entity.User;
import card.uz.cards.payload.Payload;
import card.uz.cards.repository.CardRepository;
import card.uz.cards.repository.UserRepository;
import card.uz.cards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class CardServiceImpl implements CardService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private static final Logger _logger = LoggerFactory.getLogger(CardServiceImpl.class);

    @Override
    public HttpEntity<?> createCard(UUID idempotencyKey, CardCreateDTO dto) {
        try {
            if (idempotencyKey == null || dto == null) {
                return Payload.badRequest("Request error").response();
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!authentication.isAuthenticated()) {
                return Payload.unauthorized().response();
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            if (!userDetails.isEnabled()){
                return Payload.unauthorized("User no active").response();
            }
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Card card =new Card();
            card.setUsers_id(user);
            card.setStatus(dto.getStatus());
            if (dto.getBalance().compareTo(new BigDecimal("10000")) > 0)
                return Payload.badRequest("Balance entered high").response();
            card.setBalance(dto.getBalance());
            card.setCurrency(dto.getCurrency());
return Payload.ok().response();
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return Payload.internalServerError().response();
        }
    }
}
