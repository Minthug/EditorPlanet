package setting.SettingServer.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;

@Configuration
public class GcpStorageConfig {

    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String credentialPath;

    @Bean
    public Storage storage() throws IOException {
        Resource resource = new ClassPathResource(credentialPath.replace("classpath:", ""));

        if (!resource.exists()) {
            throw new FileNotFoundException("Credential file not found: " + credentialPath );
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}


