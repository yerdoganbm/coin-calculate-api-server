package tr.com.sgveteris.coin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"tr.com.sgveteris.*"})
public class CoinCalculatorApiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoinCalculatorApiServerApplication.class, args);
    }

}
