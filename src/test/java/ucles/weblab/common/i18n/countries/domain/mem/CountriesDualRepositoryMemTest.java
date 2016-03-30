package ucles.weblab.common.i18n.countries.domain.mem;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import ucles.weblab.common.i18n.countries.domain.CountriesBuilders;
import ucles.weblab.common.i18n.countries.domain.CountryEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;

/**
 * @since 18/05/15
 */
public class CountriesDualRepositoryMemTest {
    private static final String COUNTRIES_SUBSET_JSON = "countries-subset.json";
    private CountriesDualRepositoryMem countriesDualRepository = new CountriesDualRepositoryMem(new CountriesBuilders().countryEntityBuilder());

    @Test
    public void testRepository() {
        assertFalse("Expect no data", countriesDualRepository.findAllRaw().isPresent());
        final String rawData = "{ \"stuff\": \"nonsense\"; \"mess\": \"fuss\"; }";
        countriesDualRepository.updateAll(rawData);
        final Optional<String> result = countriesDualRepository.findAllRaw();
        assertTrue("Expect some data", countriesDualRepository.findAllRaw().isPresent());
        assertEquals("Expect same data we saved", rawData, result.get());
    }

    @Test
    public void testFindOneByName() {
        loadCountries();
        final Optional<? extends CountryEntity> result = countriesDualRepository.findOneByName("New Zealand");
        assertTrue("Expect a result", result.isPresent());
        assertEquals("New Zealand", result.get().getName());
        assertEquals("NZ", result.get().getIso3166Alpha2Code());
        assertEquals((Long) 4547900L, result.get().getPopulation().get());
        assertEquals(Arrays.asList("64"), result.get().getCallingCodes());
    }

    @Test
    public void testFindAll() {
        loadCountries();
        final List<? extends CountryEntity> all = countriesDualRepository.findAll();
        assertThat("Expect all countries", all, contains(countryWithName("Australia"), countryWithName("Christmas Island"),
                countryWithName("Cocos (Keeling) Islands"), countryWithName("New Zealand"), countryWithName("Norfolk Island")));
    }
    
    @Test
    public void findOneBy2LetterCode() throws Exception {
        loadCountries();
        Optional<? extends CountryEntity> result = countriesDualRepository.findOneByAlpha2Code("AU");
        assertTrue("Expect a result", result.isPresent());
        assertEquals("Australia", result.get().getName());
        assertEquals("AU", result.get().getIso3166Alpha2Code());        
    }

    private Matcher<? super CountryEntity> countryWithName(final String name) {
        final Matcher<String> nameMatcher = equalTo(name);
        return new BaseMatcher<CountryEntity>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof CountryEntity && nameMatcher.matches(((CountryEntity) item).getName());
            }

            public void describeTo(Description description) {
                description.appendText("a country with name ").appendDescriptionOf(nameMatcher);
            }
        };
    }

    private void loadCountries() {
        final InputStream resource = getClass().getResourceAsStream(COUNTRIES_SUBSET_JSON);
        try (final InputStreamReader readable = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            final String initialCountries = readAll(readable);
            countriesDualRepository.updateAll(initialCountries);
        } catch (IOException e) {
            assumeNoException("Error loading countries data", e);
        }
    }

    private String readAll(Readable readable) {
        return new Scanner(readable).useDelimiter("\\A").next();
    }
}
