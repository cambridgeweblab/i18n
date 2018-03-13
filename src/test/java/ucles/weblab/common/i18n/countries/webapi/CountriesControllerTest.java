package ucles.weblab.common.i18n.countries.webapi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ucles.weblab.common.i18n.countries.domain.CountriesRawRepository;
import ucles.weblab.common.i18n.countries.webapi.resource.CountryResource;
import ucles.weblab.common.i18n.countries.webapi.resource.CountryResource.CurrencyResource;
import ucles.weblab.common.test.webapi.WebTestSupport;
import ucles.weblab.common.webapi.exception.ResourceNotFoundException;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @since 18/05/15
 */
@RunWith(MockitoJUnitRunner.class)
public class CountriesControllerTest {
    @Mock
    private CountriesRawRepository countriesRawRepository;
    @Mock
    private ResourceSchemaCreator schemaCreator;

    private CountriesController countriesController;

    @Before
    public void setUp() {
        countriesController = new CountriesController(countriesRawRepository, schemaCreator);
        WebTestSupport.setUpRequestContext();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetCountriesWithNoCountryData() {
        when(countriesRawRepository.findAllRaw()).thenReturn(Optional.empty());
        countriesController.getCountries();
    }

    @Test
    public void testGetCountries() {
        when(countriesRawRepository.findAllRaw()).thenReturn(Optional.of("[{\n" +
                "    \"name\": \"Republic of Macedonia\",\n" +
                "    \"altSpellings\": [\n" +
                "      \"MK\",\n" +
                "      \"Republic of Macedonia\",\n" +
                "      \"Република Македонија\"\n" +
                "    ],\n" +
                "    \"region\": \"Europe\",\n" +
                "    \"subregion\": \"Southern Europe\",\n" +
                "    \"population\": 2058539,\n" +
                "    \"nativeName\": \"Македонија\",\n" +
                "    \"callingCodes\": [\"389\"],\n" +
                "    \"topLevelDomain\": [\".mk\"],\n" +
                "    \"alpha2Code\": \"MK\",\n" +
                "    \"currencies\": [\"MKD\"],\n" +
                "    \"languages\": [\"mk\"]\n" +
                "  }]"));
        final List<CountryResource> result = countriesController.getCountries();
        assertEquals("Expected 1 country", 1, result.size());
        assertEquals("Expected name", "Republic of Macedonia", result.get(0).getName());
        assertEquals("Expected ISO code", "MK", result.get(0).getIso());
        assertEquals("Expected population", 2058539L, (long) result.get(0).getPopulation());
        assertThat("Expected dialling code", result.get(0).getCode(), contains("389"));
        assertThat("Expected currency", result.get(0).getCurrency(), hasEntry("MKD", new CurrencyResource("Den")));
        assertThat("Expected languages", result.get(0).getLanguages(), contains("mk"));
    }

    @Test
    public void testGetCountriesWithZeroPopulation() {
        when(countriesRawRepository.findAllRaw()).thenReturn(Optional.of("[{\n" +
                "    \"name\": \"Heard Island and McDonald Islands\",\n" +
                "    \"altSpellings\": [\"HM\"],\n" +
                "    \"region\": \"\",\n" +
                "    \"subregion\": \"\",\n" +
                "    \"translations\": {\n" +
                "      \"de\": \"Heard und die McDonaldinseln\",\n" +
                "      \"es\": \"Islas Heard y McDonald\",\n" +
                "      \"fr\": \"Îles Heard-et-MacDonald\",\n" +
                "      \"ja\": \"ハード島とマクドナルド諸島\",\n" +
                "      \"it\": \"Isole Heard e McDonald\"\n" +
                "    },\n" +
                "    \"population\": 0,\n" +
                "    \"nativeName\": \"Heard Island and McDonald Islands\",\n" +
                "    \"callingCodes\": [\"\"],\n" +
                "    \"topLevelDomain\": [\n" +
                "      \".hm\",\n" +
                "      \".aq\"\n" +
                "    ],\n" +
                "    \"alpha2Code\": \"HM\",\n" +
                "    \"currencies\": [\"AUD\"],\n" +
                "    \"languages\": [\"en\"]\n" +
                "  }]"));
        final List<CountryResource> result = countriesController.getCountries();
        assertEquals("Expected 1 country", 1, result.size());
        assertEquals("Expected name", "Heard Island and McDonald Islands", result.get(0).getName());
        assertEquals("Expected ISO code", "HM", result.get(0).getIso());
        assertNull("Expected population", result.get(0).getPopulation());
        assertThat("Expected no dialling code", result.get(0).getCode(), emptyCollectionOf(String.class));
        assertThat("Expected currency", result.get(0).getCurrency(), hasKey("AUD"));
        assertThat("Expected languages", result.get(0).getLanguages(), contains("en"));
    }

    @Test
    public void testGetCountriesWithNoPopulationSpecified() {
        when(countriesRawRepository.findAllRaw()).thenReturn(Optional.of("[{\n" +
                "    \"name\": \"Bouvet Island\",\n" +
                "    \"altSpellings\": [\n" +
                "      \"BV\",\n" +
                "      \"Bouvetøya\",\n" +
                "      \"Bouvet-øya\"\n" +
                "    ],\n" +
                "    \"translations\": {\n" +
                "      \"de\": \"Bouvetinsel\",\n" +
                "      \"es\": \"Isla Bouvet\",\n" +
                "      \"fr\": \"Île Bouvet\",\n" +
                "      \"ja\": \"ブーベ島\",\n" +
                "      \"it\": \"Isola Bouvet\"\n" +
                "    },\n" +
                "    \"nativeName\": \"Bouvetøya\",\n" +
                "    \"callingCodes\": [\"\"],\n" +
                "    \"topLevelDomain\": [\".bv\"],\n" +
                "    \"alpha2Code\": \"BV\",\n" +
                "    \"currencies\": [\"NOK\"],\n" +
                "    \"languages\": []\n" +
                "  }]"));
        final List<CountryResource> result = countriesController.getCountries();
        assertEquals("Expected 1 country", 1, result.size());
        assertEquals("Expected name", "Bouvet Island", result.get(0).getName());
        assertEquals("Expected ISO code", "BV", result.get(0).getIso());
        assertNull("Expected population", result.get(0).getPopulation());
        assertThat("Expected no dialling code", result.get(0).getCode(), emptyCollectionOf(String.class));
        assertThat("Expected currency", result.get(0).getCurrency(), hasKey("NOK"));
        assertThat("Expected languages", result.get(0).getLanguages(), hasSize(0));
    }


}
