package com.nasnav.payments.qnb;

import com.nasnav.payments.Account;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Properties;

@Component
//@PropertySource(value = "classpath:provider.qnb.properties")
//@ConfigurationProperties(prefix = "upg")
@Getter
public class QnbAccount extends Account {

    @PostConstruct
    public void setup() {
        Properties properties = new Properties();
        try (final InputStream stream =
                     this.getClass().getResourceAsStream("/provider.qnb.properties")) {
            properties.load(stream);
            super.init(properties, "qnb");
        } catch (Exception ex) {
            System.err.println("Unable to load resource: provider.qnb.properties");
            ex.printStackTrace();
        }
    }
}
