package com.nasnav.yeshtery.security.jwt;

import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.CommonUserRepository;
import com.nasnav.enumerations.UserStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.service.RoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.nasnav.enumerations.UserStatus.NOT_ACTIVATED;
import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.LOCKED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class JwtUserDetailsServiceImpl implements JwtUserDetailsService {

    private final CommonUserRepository userRepo;
    private final RoleService roleService;

    @Autowired
    public JwtUserDetailsServiceImpl(CommonUserRepository userRepo,
                                     RoleService roleService) {
        this.userRepo = userRepo;
        this.roleService = roleService;
    }

    @Override
    public UserDetails loadUser(JwtLoginData loginData) throws UsernameNotFoundException {

        valideLoginData(loginData);

        BaseUserEntity userEntity = userRepo.getByEmailIgnoreCaseAndOrganizationId(loginData.email(),
                loginData.orgId(), loginData.isEmployee());

        validateLoginUser(userEntity);
        List<SimpleGrantedAuthority> authorities = getUserAuthorities(userEntity);
        return new JwtUserDetailsImpl(userEntity, authorities);
    }

    List<SimpleGrantedAuthority> getUserAuthorities(BaseUserEntity userEntity) {
        List<String> roles = roleService.getUserRoles(userEntity);

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return new ArrayList<>(authorities);
    }

    static void valideLoginData(JwtLoginData loginData) {
        if (loginData == null || StringUtils.isBlank(loginData.email())) {
            throw new RuntimeBusinessException(UNAUTHORIZED, U$LOG$0002);
        }
    }

    static void validateLoginUser(BaseUserEntity userEntity) {
        if(userEntity == null) {
            throw new RuntimeBusinessException(UNAUTHORIZED, U$LOG$0002);
        }

        if (isAccountLocked(userEntity)) {
            throw new RuntimeBusinessException(LOCKED,  U$LOG$0004);
        }

        if (isUserDeactivated(userEntity)) {
            throw new RuntimeBusinessException(LOCKED,  U$LOG$0003);
        }
        if (isEmployeeUserNeedActivation(userEntity)) {
            throw new RuntimeBusinessException(LOCKED,  U$LOG$0003);
        }
    }

    static boolean isAccountLocked(BaseUserEntity userEntity) {
        return userEntity.getUserStatus().equals(UserStatus.ACCOUNT_SUSPENDED.getValue());
    }

    static boolean isUserDeactivated(BaseUserEntity user) {
        return user.getUserStatus().equals(NOT_ACTIVATED.getValue());
    }

    static boolean isEmployeeUserNeedActivation(BaseUserEntity userEntity) {
        String encPassword = userEntity.getEncryptedPassword();
        return StringUtils.isBlank(encPassword) || EntityConstants.INITIAL_PASSWORD.equals(encPassword);
    }
}
