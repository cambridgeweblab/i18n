package ucles.weblab.common.i18n.countries.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ucles.weblab.common.i18n.countries.config.CountriesConfig;
import ucles.weblab.common.schema.webapi.EnumSchemaCreator;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.security.SecurityChecker;
import ucles.weblab.common.xc.service.CrossContextConversionService;
import ucles.weblab.common.xc.service.CrossContextConversionServiceImpl;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @since 18/05/15
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AutoRefreshCountriesServiceImpl_IT {
    @Configuration
    @Import({CountriesConfig.class, PropertyPlaceholderAutoConfiguration.class})
    public static class Config {
        @Bean
        @ConditionalOnMissingBean(MethodSecurityExpressionHandler.class)
        MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
            return new DefaultMethodSecurityExpressionHandler();
        }

        @Bean
        SecurityChecker securityChecker(MethodSecurityExpressionHandler handler) {
            return new SecurityChecker(handler);
        }

        @Bean
        CrossContextConversionService crossContextConversionService() {
            return new CrossContextConversionServiceImpl();
        }

        @Bean
        EnumSchemaCreator enumSchemaCreator(final JsonSchemaFactory schemaFactory) {
            return new EnumSchemaCreator();
        }

        @Bean
        JsonSchemaFactory jsonSchemaFactory() {
            return new JsonSchemaFactory();
        }

        @Bean
        public ResourceSchemaCreator resourceSchemaCreator(SecurityChecker securityChecker,
                                                           CrossContextConversionService crossContextConversionService,
                                                           EnumSchemaCreator enumSchemaCreator,
                                                           JsonSchemaFactory jsonSchemaFactory,
                                                           MessageSource messageSource) {

            return new ResourceSchemaCreator(securityChecker,
                    new ObjectMapper(),
                    crossContextConversionService,
                    enumSchemaCreator,
                    jsonSchemaFactory,
                    messageSource);
        }


    }

    @Autowired
    private CountriesService countriesService;

    @Test
    public void testRefresh() throws InterruptedException, ExecutionException, TimeoutException {
        final Future<String> promise = countriesService.refreshRepository();
        final String result;
        assertNotNull("Expect a result to be returned within 10s", result = promise.get(10, TimeUnit.SECONDS));
        final ReadContext readContext = JsonPath.parse(result);
        String[] isoCountries = Locale.getISOCountries();
        JSONArray readResult;
        for (String isoCountry : isoCountries) {
            if (isoCountry.equals("AN") || isoCountry.equals("AQ")) {
                // AN: Old code for Netherlands Antilles, not present in data set. See http://en.wikipedia.org/wiki/ISO_3166-2:AN
                // AQ: Antarctica, noone lives there (honest).
                continue;
            }
            readResult = readContext.read("$[?(@.alpha2Code == '" + isoCountry + "')]");
            assertEquals("Expect to parse the results as JSON and find the country: " + isoCountry, 1,
                    readResult.size());
        }
    }
}
