package com.nasnav.dao;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.OrganizationRepository;
import org.json.JSONObject;

import java.util.List;

public class Organization {

    private OrganizationRepository organizationRepository;
    private OrganizationEntity organizationEntity;

    public Organization(OrganizationRepository repository, String searchName) {
        organizationRepository = repository;
        List<OrganizationEntity> org = organizationRepository.findByName(searchName);
        if (org.size() > 0) {
            organizationEntity = org.get(0);
        }
    }

    public String getJSON() {
        JSONObject json = new JSONObject();
        if (organizationEntity == null) {
            json.put("id", 0);
            return json.toString();
        }
        json.put("id", organizationEntity.getId());
        json.put("name", organizationEntity.getName());
        json.put("pname", organizationEntity.getPName());
        json.put("description", organizationEntity.getDescription());
        json.put("type", organizationEntity.getType().name());
        return json.toString();
    }


}
