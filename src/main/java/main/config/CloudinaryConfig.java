package main.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dfw0lmear",
                "api_key", "126391897398859",
                "api_secret", "SlxAYz19Knejy-BNbXzdFz4uA8Q",
                "secure", true
        ));
    }
}
