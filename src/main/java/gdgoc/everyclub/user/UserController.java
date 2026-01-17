package gdgoc.everyclub.user;

import gdgoc.everyclub.auth.EmailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final EmailService emailService;

    public UserController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/check-email")
    public boolean checkEmail(@RequestBody String email) {
        return emailService.isSchoolEmail(email);
    }
}
