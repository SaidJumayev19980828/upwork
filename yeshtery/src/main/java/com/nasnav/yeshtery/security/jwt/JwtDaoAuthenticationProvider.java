package com.nasnav.yeshtery.security.jwt;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JwtDaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    /**
     * The plaintext password used to perform
     * {@link PasswordEncoder#matches(CharSequence, String)} on when the user is not found
     * to avoid SEC-2056.
     */
    private static final String USER_NOT_FOUND_PASSWORD = "userNotFoundPassword";

    private final JwtUserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public JwtDaoAuthenticationProvider(JwtUserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        if (authentication.getCredentials() == null) {
            this.logger.warn("Failed to authenticate since no credentials provided");
            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        String presentedPassword = authentication.getCredentials().toString();
        if (!this.passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            this.logger.warn("Failed to authenticate since password does not match stored value");
            throw new BadCredentialsException(this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        YeshteryAuthenticationToken token = (YeshteryAuthenticationToken) authentication;
        UserDetails loadedUser = null;
        try {
            loadedUser = userDetailsService.loadUser(token.getLoginData());
        } catch (UsernameNotFoundException notFound) {
            if (authentication.getCredentials() != null) {
                String presentedPassword = authentication.getCredentials().toString();
                passwordEncoder.matches(presentedPassword, this.passwordEncoder.encode(USER_NOT_FOUND_PASSWORD));
            }
            throw notFound;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }

        return loadedUser;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (YeshteryAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
