package com.nasnav.security.oauth2;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.nasnav.constatnts.EntityConstants.INITIAL_PASSWORD;

@ToString
@Getter
@Setter
public class UserPrincipal implements OidcUser,OAuth2User, UserDetails {
    /**
	 * 
	 */
	private static final long serialVersionUID = 51841222L;
	private String id;
	private String username;
    private String password;
    private String provider;
    private String email;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public UserPrincipal(String id, String username, String email, String password, String provider, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
    	this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.provider = provider;
    }

    
    
    
    public static UserPrincipal create(OAuth2UserInfo user, String provider) {
        List<GrantedAuthority> authorities = Collections.
                singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new UserPrincipal(
        		user.getId(),
        		user.getName(),
                user.getEmail(),
                INITIAL_PASSWORD,
                provider,
                authorities
        );
    }

    
    

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return username;
    }

	@Override
	public Map<String, Object> getClaims() {
		return this.attributes;
	}

	@Override
	public OidcUserInfo getUserInfo() {
		return new OidcUserInfo(attributes);
	}

	@Override
	public OidcIdToken getIdToken() {
		return new OidcIdToken(this.id, Instant.now(), Instant.now().plus(1, ChronoUnit.YEARS), attributes);
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
	@Override
	public String getEmail() {
		return this.email;
	}
	
}

