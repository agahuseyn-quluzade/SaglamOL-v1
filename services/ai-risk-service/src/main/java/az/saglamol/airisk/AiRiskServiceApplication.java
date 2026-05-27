package az.saglamol.airisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EntityScan(basePackages = {"az.saglamol.airisk.entity", "az.saglamol.common.kafka.consumer"})
public class AiRiskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiRiskServiceApplication.class, args);
    }
}
