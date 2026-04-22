package com.Tu.Tu.configuration;

import com.Tu.Tu.entity.User;
import com.Tu.Tu.service.AuthenticationService;
import com.Tu.Tu.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            String email = oAuth2User.getAttribute("email");
            String fullName = oAuth2User.getAttribute("name");

            if (email == null || email.isBlank()) {
                log.error("Google OAuth2 callback missing email attribute");
                getRedirectStrategy().sendRedirect(request, response,
                        UriComponentsBuilder.fromUriString("http://localhost:5173/login")
                                .queryParam("error", "google_email_missing")
                                .build()
                                .toUriString());
                return;
            }

            User user = userService.findOrCreateGoogleUser(email, fullName);
            String token = authenticationService.generateToken(user);

            String redirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", token)
                    .build(true)
                    .toUriString();
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } catch (Exception exception) {
            log.error("Google OAuth2 success handler failed", exception);
            getRedirectStrategy().sendRedirect(request, response,
                    UriComponentsBuilder.fromUriString("http://localhost:5173/login")
                            .queryParam("error", "google_login_failed")
                            .build()
                            .toUriString());
        }
    }
}