package com.nasnav.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.ErrorCodes;
import com.nasnav.dao.UserRepository;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.ApiResponse;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.exception.EntityValidationException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import static com.nasnav.ErrorCodes.*;

public class UserService {

    private final static String _initPassword = "!needs_reset!";

    UserRepository userRepository;

    public UserService() {
    }


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static String getParam(Map<String, String[]> params, String name) {
        String[] result = params.get(name);
        if (result != null && result.length > 0) {
            return result[0];
        }
        return null;
    }

    private int patternMatcher(String in, String pattern) {
//System.out.println("match: " + in + " against: " + pattern);
        if (in == null || in.equals("")) {
            return ErrorCodes.FIELD_EMPTY;
        }
        if (in.matches(pattern)) {
            return 0;
        }
        return ErrorCodes.FIELD_DOESNT_MATCH_REQUIRED_PATTERN;
    }

    private int validateName(String name) {
        return patternMatcher(name, "^[a-zA-z \']+$");
    }

    private int validateEmail(String email) {
        return patternMatcher(email, "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,6}$");
    }

    public String register(UserRepository userRepository, Map<String, String[]> parameters) {
        String name = getParam(parameters, "name");
        String email = getParam(parameters, "email");
        String address = getParam(parameters, "address");
        String country = getParam(parameters, "country");
        String city = getParam(parameters, "city");
        String phone = getParam(parameters, "phone");

        ArrayList<String> statusList = new ArrayList<>();
        JSONObject response = new JSONObject();

        if ((validateName(name)) != 0) {
            statusList.add(textStatus.get(INVALID_NAME));
        }
        if ((validateEmail(email)) != 0) {
            statusList.add(textStatus.get(INVALID_EMAIL));
        }

        if (statusList.size() == 0) {
            if (userRepository.findByEmail(email).size() > 0) {
                statusList.add("EMAIL_EXISTS");
            }
        }

        JSONArray statuses = new JSONArray();

        if (statusList.size() > 0) {
            response.put("success", false);
            for (String err: statusList) {
                statuses.put(err);
            }
        } else {
            UserEntity ue = new UserEntity();
            ue.address = address;
            ue.addressCity = city;
            ue.addressCountry = country;
            ue.email = email;
            ue.encPassword = _initPassword;
            ue.name = name;
            ue.phoneNumber = phone;
            ue.createdAt = new Date();
            ue.updatedAt = new Date();
            userRepository.saveAndFlush(ue);
            if (ue.getId() <= 0) {
                response.put("success", false);
            } else {
                response.put("success", true);
                response.put("id", ue.getId());
                statuses.put(textStatus.get(ACCOUNT_NEEDS_ACTIVATION));
            }
        }
        response.put("status", statuses);
        return response.toString();
    }

    public ApiResponse registerUser(String userJson) {
        UserEntity userEntity = this.doRegisterUser(userJson);
        return new ApiResponse(userEntity.getId(), Arrays.asList(ResponseStatus.NEED_ACTIVATION, ResponseStatus.ACTIVATION_SENT));
    }

    public UserEntity doRegisterUser(String userJson) {
        // parse Json to User entity.
        UserEntity user = this.createUserFromJson(userJson);
        // validate user entity
        this.validate(user);
        // save to DB
        return userRepository.save(user);
    }

    /**
     * map Json Request to User Entity
     *
     * @param userJson Json String holding user data
     * @return mapped User entity from Json request
     */
    private UserEntity createUserFromJson(String userJson) {
        // convert JSON request to user object
        // avoid creating new DTO as it is not needed now,
        try {
            UserEntity userEntity = new ObjectMapper().readValue(userJson, UserEntity.class);
            // init required fields to avoid not-null constraint
            userEntity.encPassword = _initPassword;
            userEntity.createdAt = new Date();
            userEntity.updatedAt = new Date();
            return userEntity;
        } catch (IOException e) {
            throw new EntityValidationException("Invalid Json Request",
                    Arrays.asList(ResponseStatus.INVALID_NAME, ResponseStatus.INVALID_EMAIL));
        }

    }

    /**
     * validate passed user entity against business rules
     *
     * @param user User entity to be validated
     */
    private void validate(UserEntity user) {
        this.validateNameAndEmail(user);
        this.checkEmailExistence(user);
    }


    /**
     * called pre save user to
     * validate fields of user entity
     *
     * @param user User entity to be processed before registering
     */
    private void validateNameAndEmail(UserEntity user) {
        List<ResponseStatus> responseStatusList = new ArrayList<>();
        if ((validateName(user.name)) != 0) {
            responseStatusList.add(ResponseStatus.INVALID_NAME);
        }
        if ((validateEmail(user.email)) != 0) {
            responseStatusList.add(ResponseStatus.INVALID_EMAIL);
        }
        if (!responseStatusList.isEmpty()) {
            throw new EntityValidationException("Invalid User Entity,responseStatusList", responseStatusList);
        }
    }

    /**
     * Ensure that the new email is not registered to another user
     *
     * @param user User entity containing email to be checked
     */
    private void checkEmailExistence(UserEntity user) {
        boolean emailAlreadyExists = userRepository.existsByEmail(user.email);
        if (emailAlreadyExists) {
            throw new EntityValidationException(ResponseStatus.EMAIL_EXISTS.getValue(),
                    Collections.singletonList(ResponseStatus.EMAIL_EXISTS));
        }
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
