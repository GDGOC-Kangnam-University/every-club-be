package gdgoc.everyclub.auth;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public boolean isSchoolEmail(String email) {
        return email.endsWith("@knu.ac.kr");
    }
}
