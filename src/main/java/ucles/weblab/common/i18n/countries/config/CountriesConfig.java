package ucles.weblab.common.i18n.countries.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ucles.weblab.common.i18n.countries.domain.AutoRefreshCountriesServiceImpl;
import ucles.weblab.common.i18n.countries.domain.CountriesBuilders;
import ucles.weblab.common.i18n.countries.domain.CountriesRawRepository;
import ucles.weblab.common.i18n.countries.domain.CountriesService;
import ucles.weblab.common.i18n.countries.domain.CountryEntity;
import ucles.weblab.common.i18n.countries.domain.mem.CountriesDualRepositoryMem;
import ucles.weblab.common.i18n.countries.webapi.CountriesController;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

/**
 * Configuration for the countries domain.
 *
 * @since 18/05/15
 */
@Configuration
@EnableScheduling
@EnableAsync
@ConditionalOnClass({ObjectMapper.class})
@ConditionalOnProperty(name = "i18n.countries.enabled", havingValue = "true", matchIfMissing = true)
@Import(CountriesBuilders.class)
public class CountriesConfig {

    private static final String INITIAL_RESTCOUNTRIES_JSON = "restcountries.json";

    @Bean
    public CountriesRawRepository countriesRawRepository(Supplier<CountryEntity.Builder> countryEntityBuilder) {
        return new CountriesDualRepositoryMem(countryEntityBuilder);
    }

    @Bean
    public CountriesService countriesService(@Value("${restcountries.uri:http://restcountries.eu/rest/v1/all}") URI countriesUri,
                                             CountriesRawRepository countriesRawRepository) {
        return new AutoRefreshCountriesServiceImpl(countriesUri, countriesRawRepository, new RestTemplate());
    }

    @Configuration
    @AutoConfigureAfter({DispatcherServletAutoConfiguration.class, WebMvcAutoConfiguration.class})
    @ConditionalOnWebApplication
    @ConditionalOnClass({RestController.class})
    @ComponentScan(basePackageClasses = CountriesController.class)
    static class CountriesConfigWeb { }

    @Configuration
    static class CountriesInitialisation {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Bean
        CommandLineRunner initCountriesData(CountriesRawRepository repository, CountriesService service) {
            return args -> {
                final InputStream resource = CountriesConfig.class.getResourceAsStream(INITIAL_RESTCOUNTRIES_JSON);
                try (final InputStreamReader readable = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
                    final String initialCountries = readAll(readable);
                    // Ensure it parses OK, but we don't need to keep the result.
                    final List<Object> list = JsonParserFactory.getJsonParser().parseList(initialCountries);
                    logger.info("Loaded initial countries data - " + (list != null ? list.size() : 0) + " countries available.");
                    repository.updateAll(initialCountries);
                }
                service.refreshRepository();
            };
        }

        /**
         * From https://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner.html
         */
        String readAll(Readable readable) {
            return new Scanner(readable).useDelimiter("\\A").next();
        }
    }

}
