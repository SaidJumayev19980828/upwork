package com.nasnav.service;

import com.nasnav.ErrorCodes;
import com.nasnav.persistence.UserEntity;
import com.nasnav.dao.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static com.nasnav.ErrorCodes.*;

public class UserService {

    private final static String _initPassword = "!needs_reset!";

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
}
