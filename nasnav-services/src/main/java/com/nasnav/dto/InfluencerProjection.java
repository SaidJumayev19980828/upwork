package com.nasnav.dto;

import org.springframework.beans.factory.annotation.Value;




    public interface InfluencerProjection {
        @Value("#{target.employeeUser != null ? target.employeeUser.name : target.user != null ? target.user.name : null}")
        String getName();

        @Value("#{target.employeeUser != null ? target.employeeUser.image : target.user != null ? target.user.image : null}")
        String getImage();

}