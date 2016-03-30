package ucles.weblab.common.i18n.countries.domain;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ucles.weblab.common.i18n.countries.config.CountriesConfig;

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
    public static class Config {}

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
            if (isoCountry.equals("AN") || isoCountry.equals("AQ") || isoCountry.equals("VA") || isoCountry.equals("VI")) {
                // AN: Old code for Netherlands Antilles, not present in data set. See http://en.wikipedia.org/wiki/ISO_3166-2:AN
                // AQ: Antarctica, noone lives there (honest).
                // VA: Holy See. This is a bug, to my mind.
                // VI: US Virign Islands. This is a bug, to my mind.
                continue;
            }
            readResult = readContext.read("$[?(@.alpha2Code == '" + isoCountry + "')]");
            assertEquals("Expect to parse the results as JSON and find the country: " + isoCountry, 1,
                    readResult.size());
        }
        readResult = readContext.read("$[?(@.alpha2Code == 'VA')]");
        assertEquals("Hooray, REST Countries has been fixed to return the Holy See (VA) - update this test!", 0,
                readResult.size());
        readResult = readContext.read("$[?(@.alpha2Code == 'VI')]");
        assertEquals("Hooray, REST Countries has been fixed to return the US Virgin Islands (VI) - update this test!", 0,
                readResult.size());
    }
}
