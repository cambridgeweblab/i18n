package ucles.weblab.common.i18n.countries.domain.mem;

import com.jayway.jsonpath.Filter;
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
    private static final String NAME = "name";
    private static final String ALPHA_2_CODE = "alpha2Code";
    private static final String POPULATION = "population";
    private static final String CALLING_CODES = "callingCodes";
    private static final String ARRAY_MEMBER = "$[?]";

    private final Logger log = LoggerFactory.getLogger(CountriesDualRepositoryMem.class);
    private final Supplier<CountryEntity.Builder> countryEntityBuilder;
    private final Function<Object, CountryEntity> jsonToCountryEnglish; //jsonToTranslation(countryCode) method is available

    private Optional<String> countriesRawData = Optional.empty();
    private Optional<ReadContext> jsonPathReadContext = Optional.empty();

    public CountriesDualRepositoryMem(Supplier<CountryEntity.Builder> countryEntityBuilder) {
        this.countryEntityBuilder = countryEntityBuilder;
        this.jsonToCountryEnglish =
                    ((Function<Object, Map>) (o -> (Map) o)).andThen(
                            map -> countryEntityBuilder.get()
                                    .name((String) map.get(NAME))
                                    .iso3166Alpha2Code((String) map.get(ALPHA_2_CODE))
                                    .population(Optional.ofNullable((Number) map.get(POPULATION)).map(Number::longValue).filter(n -> n > 0L))
                                    .callingCodes(((Collection<String>) map.get(CALLING_CODES)).stream().filter(s -> !s.isEmpty()).collect(toList()))
                                    .get());
    }

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
        return getReadContext().map(readContext -> (List<Object>) readContext.read(ARRAY_MEMBER, filter(where(NAME).eq(name))))
                .orElse(emptyList()).stream()
                .findFirst()
                .map(jsonToCountryEnglish);
    }

    @Override
    public List<? extends CountryEntity> findAll() {
        return getReadContext().map(readContext -> (List<Object>) readContext.json())
                .orElse(emptyList()).stream()
                .map(jsonToCountryEnglish)
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
        return findOneByAlpha2Code(countryCode, null);
    }

    @Override
    public Optional<? extends CountryEntity> findOneByAlpha2Code(String countryCode, String languageCode) {
        Function<Object, CountryEntity> mapper = isEnglish(languageCode) ? jsonToCountryEnglish : jsonToCountryTranslation(languageCode);

        return getReadContext().map(readContext -> (List<Object>) readContext.read(ARRAY_MEMBER, filter(where(ALPHA_2_CODE).eq(countryCode))))
                .orElse(emptyList()).stream()
                .findFirst()
                .map(mapper);
    }

    @Override
    public List<? extends CountryEntity> findByNameContaining(String countrySearchString, String languageCode) {
        Pattern searchStringContains = Pattern.compile(".*" + regexSafeQuote(countrySearchString) + ".*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        if(isEnglish(languageCode)) {
            return getReadContext().map(readContext -> (List<Object>) readContext.read(ARRAY_MEMBER, filter(where(NAME).regex(searchStringContains))))
                    .orElse(emptyList()).stream()
                    .map(jsonToCountryEnglish)
                    .collect(toList());
        } else {
            return getReadContext().map(readContext -> (List<Object>) readContext.read(ARRAY_MEMBER, filter(
                        where(NAME).regex(searchStringContains))
                        .or(where("translations." + languageCode).regex(searchStringContains))))
                    .orElse(emptyList()).stream()
                    .map(jsonToCountryTranslation(languageCode))
                    .collect(toList());
        }
    }

    @Override
    public Optional<String> getCodeByNameAndLocale(String countryName, String languageCode) {
        /*  There is currently no english translation, so the name attribute needs to be searched if the
            language code is english, otherwise the appropriate translations are searched */
        Filter filter = isEnglish(languageCode) ? filter(where(NAME).eq(countryName)) : filter(where("translations." + languageCode).eq(countryName));

        return getReadContext().map(readContext -> (List<Object>) readContext.read(ARRAY_MEMBER, filter))
                .orElse(emptyList()).stream()
                .findFirst()
                .map( o -> ((Map<String, String>) o).get(ALPHA_2_CODE).toString() );
    }

    private static boolean isEnglish(String languageCode) {
        return languageCode == null || languageCode.equals("en");
    }

    private String regexSafeQuote(final String s) {
        String safe = s.trim();
        if(safe.contains("\\E")) {
            log.warn("Encountered an end quote special character in a field using regex \"" + safe + "\", continuing and treating it as an 'E'");
            safe = safe.replace("\\E", "E");
        }
        return "\\Q" + safe + "\\E";
    }

    private Function<Object, CountryEntity> jsonToCountryTranslation(String languageCode) {
        if(isEnglish(languageCode)) {
            return jsonToCountryEnglish;
        }
        return ((Function<Object, Map>) (o -> (Map) o)).andThen(
                        map -> countryEntityBuilder.get()
                                .name((String) ((Map) map.get("translations")).get(languageCode))
                                .iso3166Alpha2Code((String) map.get(ALPHA_2_CODE))
                                .population(Optional.ofNullable((Number) map.get(POPULATION)).map(Number::longValue).filter(n -> n > 0L))
                                .callingCodes(((Collection<String>) map.get(CALLING_CODES)).stream().filter(s -> !s.isEmpty()).collect(toList()))
                                .get());
    }
}
