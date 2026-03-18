package gdgoc.everyclub.auth;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final String SCHOOL_EMAIL_DOMAIN = "@kangnam.ac.kr";

    public boolean isSchoolEmail(String email) {
        return email.endsWith(SCHOOL_EMAIL_DOMAIN);
    }
}
