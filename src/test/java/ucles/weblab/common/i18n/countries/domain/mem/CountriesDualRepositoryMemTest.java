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
import static org.junit.Assert.fail;
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

    @Test
    public void testFindNamesByNameContaining() throws Exception {
        loadCountries();

        confirmContainsCountries("AUS", null, "Australia"); //Test ignores case
        confirmContainsCountries("isl", "en", "Christmas Island", "Cocos (Keeling) Islands", "Norfolk Island"); //Test multiple results
        confirmContainsCountries("NUEva", "es", "New Zealand"); //Test other languages
        confirmContainsCountries("ーランド", "ja", "New Zealand"); //Test other characters
        confirmContainsCountries("Zeal", "ja", "New Zealand"); //Still returns with the English when a non English language code is provided as current users are used to using English
        confirmContainsCountries("Zeal", "biscuit", "New Zealand"); //Still returns with the English for unsupported languages

        assertTrue(countriesDualRepository.findByNameContaining("NUEva", "ja").isEmpty()); //Search is Spanish but language code is Japanese
        assertTrue(countriesDualRepository.findByNameContaining("AUSB", null).isEmpty()); //No matches
        assertTrue(countriesDualRepository.findByNameContaining(".*", null).isEmpty()); //Regex should not work as this would be a security risk
        assertTrue(countriesDualRepository.findByNameContaining("\\E.*", null).isEmpty()); //Including end quotes followed by other regex

        confirmContainsCountries("z\\E", null, "New Zealand"); //Edge case
    }

    /*
        Provides asserts to testFindNamesByNameContaining - pulls the countries out of the repo by searchString and languageCode,
        then matches them against the provided array of country names
    */
    private void confirmContainsCountries(String searchString, String languageCode, String... expectedResultNames) {
        List<? extends CountryEntity> countries = countriesDualRepository.findByNameContaining(searchString, languageCode);
        if(countries.size() != expectedResultNames.length) {
            fail("Unexpected number of countries returned - expecting " + expectedResultNames.length + " but found " + countries.size());
        }
        List<String> countryNames = Arrays.asList(expectedResultNames);
        for(CountryEntity country : countries) {
            assertTrue(country.getName() + " was not in the expected country names", countryNames.contains(country.getName()));
        }
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
