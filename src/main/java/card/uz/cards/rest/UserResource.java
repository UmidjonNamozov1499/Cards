package card.uz.cards.rest;

import card.uz.cards.entity.User;
import card.uz.cards.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserResource {
    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity create(@RequestBody User user) {
        User result = userService.save(user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users")
    public ResponseEntity getAll(){
        return ResponseEntity.ok(userService.findAll());
    }
}
