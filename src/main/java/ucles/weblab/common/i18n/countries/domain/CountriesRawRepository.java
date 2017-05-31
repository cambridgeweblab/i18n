package ucles.weblab.common.i18n.countries.domain;

import java.util.List;
import java.util.Optional;

/**
 * DDD repository interface - persistence-technology-neutral interface providing repository (i.e. CRUD) methods for
 * manipulating countries data.
 * <p>
 * Note that the countries data is persisted as a raw String holding an array of aggregate JSON data in the
 * <a href='http://restcountries.eu'>REST Countries</a> format.
 * </p>
 *
 * @since 18/05/15
 */
public interface CountriesRawRepository {
    /**
     * Find all the data.
     *
     * @return
     */
    Optional<String> findAllRaw();

    void updateAll(String rawData);
    
    /**
     * Get the country by iso 2 letter country code. 
     * @param countryCode
     * @return 
     */
    Optional<? extends CountryEntity> findOneByAlpha2Code(String countryCode);

    /**
     * Fetches a list of countries by searching for the country name - search is case insensitive and unicode enabled,
     * and supports up to one language in addition to English.
     *
     * @param languageCode  defaults to English, will search within the country translations for the provided language code in addition to the English name
     * @return The returned object's name will be in the given language (defaulting to English)
     */
    List<? extends CountryEntity> findByNameContaining(String countrySearchString, String languageCode);

    /**
     * Fetches the alpha2Code for a country by the country name and the language the country name is in.
     * @param languageCode  defaults to English, defines which language the country name is in (e.g. Australien is Australia in german (language code DE))
     */
    Optional<String> getCodeByNameAndLocale(String countryName, String languageCode);

}
