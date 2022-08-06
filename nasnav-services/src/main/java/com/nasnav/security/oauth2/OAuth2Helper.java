package com.nasnav.security.oauth2;

import com.nasnav.AppConfig;
import com.nasnav.dao.OAuth2ProviderRepository;
import com.nasnav.dao.OAuth2UserRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dao.yeshtery.YeshteryUserRepository;
import com.nasnav.dto.UserDTOs;
import com.nasnav.dto.UserDTOs.UserRegistrationObject;
import com.nasnav.persistence.OAuth2ProviderEntity;
import com.nasnav.persistence.OAuth2UserEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.yeshtery.YeshteryUserEntity;
import com.nasnav.security.oauth2.exceptions.RegistrationEmailExistsException;
import com.nasnav.service.yeshtery.YeshteryUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.nasnav.enumerations.UserStatus.ACTIVATED;
import static java.lang.String.format;

@Component
public class OAuth2Helper {
	

	private static final String PROVIDER_NOT_FOUND = "No provider with name [%s] was found in the Database!";


	private static final String EMAIL_EXISTS = "A user with email [%s] already exists!"
			+ ", if you are the owner of this account, please login and link your %s account to it!";

	
	
	@Autowired
	private OAuth2ProviderRepository providerRepo;
	
	@Autowired
	private OAuth2UserRepository oAuthUserRepo;
	
	@Autowired
	private UserRepository userRepo;

	@Autowired
	private YeshteryUserService yeshteryUserService;

	@Autowired
	private AppConfig appConfig;
	
	@Transactional
    public OAuth2UserEntity registerNewOAuth2User(UserPrincipal user, String token, Long orgId) {
    	String email = user.getEmail();
    	String provider = user.getProvider();
    	
    	Boolean emailExists = userRepo.existsByEmailIgnoreCaseAndOrganizationId(email, orgId);
    	if(emailExists) {
    		throw new RegistrationEmailExistsException( format(EMAIL_EXISTS, email, provider));
    	}
    	
    	UserEntity nasnavUser = registerNasnavUser(user, orgId);

		int yeshteryOrgId = appConfig.yeshteryOrgId;

		if (yeshteryOrgId > 0) {
			yeshteryUserService.createYeshteryEntity(user.getUsername(), user.getEmail(), nasnavUser, yeshteryOrgId, orgId);
		}

    	OAuth2UserEntity oAuthUser = saveNewOAuthUserToDB(user, token, orgId, nasnavUser);
    	
		return oAuthUser;
	}
	
	
	
	private OAuth2UserEntity saveNewOAuthUserToDB(UserPrincipal user, String token, Long orgId, UserEntity nasnavUser) {
		OAuth2ProviderEntity providerEntity = providerRepo.findByProviderNameIgnoreCase(user.getProvider())
    												.orElseThrow(() -> getNoProviderFoundException(user));
    	
    	OAuth2UserEntity oAuthUser = new OAuth2UserEntity();
    	oAuthUser.setUser(nasnavUser);
    	oAuthUser.setEmail(user.getEmail());
    	oAuthUser.setLoginToken(token);
    	oAuthUser.setOrganizationId(orgId);
    	oAuthUser.setProvider(providerEntity);
    	oAuthUser.setOAuth2Id(user.getId());
    	
    	oAuthUser = oAuthUserRepo.save(oAuthUser);
		return oAuthUser;
	}

	
	

	private UserEntity registerNasnavUser(UserPrincipal user, Long orgId) {
		UserDTOs.UserRegistrationObject reg = new UserRegistrationObject();
    	reg.email = user.getEmail();
    	reg.name = user.getUsername();
    	reg.orgId = orgId;
    	
    	UserEntity nasnavUser = UserEntity.registerUser(reg);
    	nasnavUser.setUserStatus(ACTIVATED.getValue());

    	nasnavUser  = userRepo.save(nasnavUser);
		return nasnavUser;
	}

	
	  
    private RuntimeException getNoProviderFoundException(UserPrincipal user) {
		return new IllegalStateException(format(PROVIDER_NOT_FOUND, user.getProvider()));
	}



}
