package com.nasnav.payments.misr;

import com.nasnav.payments.Account;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Properties;

@Component
@Getter
public class MisrAccount extends Account {

    @PostConstruct
    public void setup() {
        Properties properties = new Properties();
        try (final InputStream stream =
                     this.getClass().getResourceAsStream("/provider.banquemisr.properties")) {
            properties.load(stream);
            super.init(properties, "misr");
        } catch (Exception ex) {
            System.err.println("Unable to load resource: provider.banquemisr.properties");
        }

    }
}
