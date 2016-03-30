package ucles.weblab.common.i18n.countries.domain;

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

}
