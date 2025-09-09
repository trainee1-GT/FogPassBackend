package train.local.fogpass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FogPassEoelProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(FogPassEoelProjectApplication.class, args);
    }

}
