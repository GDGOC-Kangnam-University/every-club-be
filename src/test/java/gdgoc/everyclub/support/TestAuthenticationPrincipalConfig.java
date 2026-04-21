package gdgoc.everyclub.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.bind.support.WebDataBinderFactory;

import java.util.List;

@TestConfiguration
public class TestAuthenticationPrincipalConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new SessionBackedAuthenticationPrincipalResolver());
    }

    private static class SessionBackedAuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
                if (request != null) {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        SecurityContext context = (SecurityContext) session.getAttribute(
                                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                        if (context == null) {
                            context = new SecurityContextImpl();
                        }
                        authentication = context.getAuthentication();
                    }
                }
            }

            if (authentication == null) {
                return null;
            }

            Object principal = authentication.getPrincipal();
            if (parameter.getParameterType().isInstance(principal)) {
                return principal;
            }
            return null;
        }
    }
}
