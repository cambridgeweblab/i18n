package ucles.weblab.common.i18n.countries.domain;

import java.util.concurrent.Future;

/**
 * DDD service interface, for methods which do not naturally form part of the {@link CountriesRawRepository} or {@link CountryRepository}.
 *
 * @since 18/05/15
 */
public interface CountriesService {
    /**
     * Fetch the most recent countries data from <a href='http://restcountries.eu'>REST Countries</a> and update
     * the repository to contain it.
     *
     * @return a future to obtain the refreshed data or any exception which occurred during the refresh
     */
    Future<String> refreshRepository();
}
