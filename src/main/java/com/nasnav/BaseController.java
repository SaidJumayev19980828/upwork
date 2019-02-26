package com.nasnav;

import com.nasnav.persistence.OrganizationRepository;
import com.nasnav.dao.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class BaseController
{
    @Autowired
    OrganizationRepository organizationRepository;

    @GetMapping("/org-test")
    @ResponseBody
    public String sayHello(
            @RequestParam(name="name", defaultValue="nasnav") String name
    ) {
        return new Organization(organizationRepository, name).getJSON();
    }
}

