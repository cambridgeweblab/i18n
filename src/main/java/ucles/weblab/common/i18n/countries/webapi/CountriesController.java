package ucles.weblab.common.i18n.countries.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ucles.weblab.common.i18n.countries.domain.CountriesRawRepository;
import ucles.weblab.common.i18n.countries.webapi.resource.CountryResource;
import ucles.weblab.common.i18n.countries.webapi.resource.CountryResource.CurrencyResource;
import ucles.weblab.common.webapi.exception.ResourceNotFoundException;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.schema.webapi.SchemaMediaTypes;
import ucles.weblab.common.xc.service.CrossContextMapping;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Controller for retrieving countries data.
 * This controller is registered outside the /api/ path so that it can be excluding from Spring Security
 * and specify cacheing headers.
 * Provides the following API:
 * <dl>
 * <dt>/data/countries/</dt>
 * <dd>GET to retrieve country names, internal dialling codes and ISO3166-1 alpha-2 country codes</dd>
 * </dl>
 *
 * @since 18/05/15
 */
@RestController
@RequestMapping(value = "/data/countries")
public class CountriesController {
    private final CountriesRawRepository countriesRawRepository;
    private final ResourceSchemaCreator schemaCreator;

    @Autowired
    public CountriesController(CountriesRawRepository countriesRawRepository, ResourceSchemaCreator schemaCreator) {
        this.countriesRawRepository = countriesRawRepository;
        this.schemaCreator = schemaCreator;
    }

    @GetMapping(value = "/", produces = APPLICATION_JSON_UTF8_VALUE)
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = 60 * 60 * 24 * 7)
    public List<CountryResource> getCountries() {
        final String rawData = countriesRawRepository.findAllRaw().orElseThrow(() -> new ResourceNotFoundException(-1));
        final List<Object> list = JsonParserFactory.getJsonParser().parseList(rawData);
        return list.stream()
                .map((o) -> {
                    Assert.isInstanceOf(Map.class, o);
                    Map map = (Map) o;
                    // Use this to filter out any empty codes
                    @SuppressWarnings("unchecked")
                    final Collection<String> callingCodes = (Collection<String>) map.get("callingCodes");
                    @SuppressWarnings("unchecked")
                    final Collection<String> currencies = (Collection<String>) map.get("currencies");
                    @SuppressWarnings("unchecked")
                    final Collection<String> languages = (Collection<String>) map.get("languages");
                    final Number population = (Number) map.get("population");
                    final String isoCode = (String) map.get("alpha2Code");
                    final Locale locale = new Locale(languages.stream().findFirst().orElse(""), isoCode);
                    final Function<String, CurrencyResource> resourceForCurrencyCode = s -> {
                        try {
                            final Currency currency = Currency.getInstance(s);
                            return new CurrencyResource(currency.getSymbol(locale));
                        } catch (IllegalArgumentException e) {
                            return new CurrencyResource(s);
                        }
                    };
                    return new CountryResource((String) map.get("name"),
                            isoCode,
                            languages,
                            callingCodes.stream().filter(s -> !s.isEmpty()).collect(toList()),
                            population == null || population.longValue() == 0L ? null : population.longValue(),
                            currencies.stream().filter(s -> !s.isEmpty()).collect(toMap(Function.identity(),
                                    resourceForCurrencyCode, (a, b) -> a, LinkedHashMap::new)));
                })
                .collect(toList());
    }

    @CrossContextMapping(value = "urn:xc:i18n:countries:$isoCodes")
    @GetMapping(value = "/$isoCodes", produces = SchemaMediaTypes.APPLICATION_SCHEMA_JSON_UTF8_VALUE)
    public ResponseEntity<JsonSchema> enumerate() {
        final JsonSchema enumSchema = schemaCreator.createEnum(getCountries(), methodOn(CountriesController.class).enumerate(),
                CountryResource::getIso, Optional.of(CountryResource::getName));
        return ResponseEntity.ok(enumSchema);
    }

    @CrossContextMapping(value = "urn:xc:i18n:countries:$iddPrefixes")
    @RequestMapping(value = "/$iddPrefixes", method = GET, produces = SchemaMediaTypes.APPLICATION_SCHEMA_JSON_UTF8_VALUE)
    public ResponseEntity<JsonSchema> countryCallingCodes() {
        Stream<CountryResource> countriesWithCodes = getCountries().stream()
                .filter(r -> !r.getCode().isEmpty())
                .sorted(Comparator.comparing(r -> r.getCode().iterator().next()));
        final Function<CountryResource, String> valueFn = r -> r.getCode().iterator().next();
        final Optional<Function<CountryResource, String>> nameFn = Optional.of(r -> "+" + r.getCode().iterator().next());
        final Optional<Function<CountryResource, String>> descFn = Optional.of(CountryResource::getIso);
        final JsonSchema enumSchema = schemaCreator.createEnum(countriesWithCodes, methodOn(CountriesController.class).countryCallingCodes(),
                valueFn, nameFn, descFn);
        return ResponseEntity.ok(enumSchema);
    }
}
