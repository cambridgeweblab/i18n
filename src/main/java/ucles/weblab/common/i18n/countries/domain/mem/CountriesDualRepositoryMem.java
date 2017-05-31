package ucles.weblab.common.i18n.countries.domain.mem;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucles.weblab.common.i18n.countries.domain.CountriesRawRepository;
import ucles.weblab.common.i18n.countries.domain.CountryEntity;
import ucles.weblab.common.i18n.countries.domain.CountryRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.jayway.jsonpath.Configuration.defaultConfiguration;
import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * Implementation of the repository interface which stores countries raw data in application memory.
 *
 * @since 18/05/15
 */
public class CountriesDualRepositoryMem implements CountriesRawRepository, CountryRepository {
    private final Logger log = LoggerFactory.getLogger(CountriesDualRepositoryMem.class);
    private final Function<Object, CountryEntity> jsonObjectToCountryEntity;

    public CountriesDualRepositoryMem(Supplier<CountryEntity.Builder> countryEntityBuilder) {
        this.jsonObjectToCountryEntity =
                    ((Function<Object, Map>) (o -> (Map) o)).andThen(
                            map -> countryEntityBuilder.get()
                                    .name((String) map.get("name"))
                                    .iso3166Alpha2Code((String) map.get("alpha2Code"))
                                    .population(Optional.ofNullable((Number) map.get("population")).map(Number::longValue).filter(n -> n > 0L))
                                    .callingCodes(((Collection<String>) map.get("callingCodes")).stream().filter(s -> !s.isEmpty()).collect(toList()))
                                    .get());
    }

    private Optional<String> countriesRawData = Optional.empty();
    private Optional<ReadContext> jsonPathReadContext = Optional.empty();

    @Override
    public Optional<String> findAllRaw() {
        return countriesRawData;
    }

    @Override
    public void updateAll(String rawData) {
        countriesRawData = Optional.ofNullable(rawData);
    }

    @Override
    public Optional<? extends CountryEntity> findOneByName(String name) {
        return getReadContext().map(readContext -> (List<Object>) readContext.read("$[?]", filter(where("name").eq(name))))
                .orElse(emptyList()).stream()
                .findFirst()
                .map(jsonObjectToCountryEntity);
    }

    @Override
    public List<? extends CountryEntity> findAll() {
        return getReadContext().map(readContext -> (List<Object>) readContext.json())
                .orElse(emptyList()).stream()
                .map(jsonObjectToCountryEntity)
                .collect(toList());
    }

    private Optional<ReadContext> getReadContext() {
        if (!jsonPathReadContext.isPresent()) {
            jsonPathReadContext = countriesRawData.map(rawData -> JsonPath.using(defaultConfiguration()).parse(rawData));
        }
        return jsonPathReadContext;
    }

    @Override
    public Optional<? extends CountryEntity> findOneByAlpha2Code(String countryCode) {

        return getReadContext().map(readContext -> (List<Object>) readContext.read("$[?]", filter(where("alpha2Code").eq(countryCode))))
                .orElse(emptyList()).stream()
                .findFirst()
                .map(jsonObjectToCountryEntity);
    }

    @Override
    public List<? extends CountryEntity> findByNameContaining(String countrySearchString, String languageCode) {
        boolean englishOnly = ( languageCode == null || languageCode.equals("en") );

        Pattern searchStringContains = Pattern.compile(".*" + regexSafeQuote(countrySearchString) + ".*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        if(englishOnly) {
            return getReadContext().map(readContext -> (List<Object>) readContext.read("$[?]", filter(where("name").regex(searchStringContains))))
                    .orElse(emptyList()).stream()
                    .map(jsonObjectToCountryEntity)
                    .collect(toList());
        } else {

            return getReadContext().map(readContext -> (List<Object>) readContext.read("$[?]", filter(
                        where("name").regex(searchStringContains))
                        .or(where("translations." + languageCode).regex(searchStringContains))))
                    .orElse(emptyList()).stream()
                    .map(jsonObjectToCountryEntity)
                    .collect(toList());
        }
    }

    private String regexSafeQuote(String s) {
        s = s.trim();
        if(s.contains("\\E")) {
            log.warn("Encountered an end quote special character in a field using regex \"" + s + "\", continuing and treating it as an 'E'");
            s = s.replace("\\E", "E");
        }
        return "\\Q" + s + "\\E";
    }
}
