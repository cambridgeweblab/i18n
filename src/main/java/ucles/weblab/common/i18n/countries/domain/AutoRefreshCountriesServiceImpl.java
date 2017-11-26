package ucles.weblab.common.i18n.countries.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Implementation of the service which as well as providing an on-demand refresh also schedules an automatic refresh
 * of country data every month.
 *
 * @since 18/05/15
 */
public class AutoRefreshCountriesServiceImpl implements CountriesService, ScheduledRefresh {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final URI countriesUri;
    private final CountriesRawRepository countriesRawRepository;
    private RestTemplate restTemplate;

    public AutoRefreshCountriesServiceImpl(URI countriesUri, CountriesRawRepository countriesRawRepository, RestTemplate restTemplate) {
        this.countriesUri = countriesUri;
        this.countriesRawRepository = countriesRawRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Scheduled job to refresh the repository once a month.
     */
    @Scheduled(cron = "0 0 5 1 * *")
    public void scheduledRefresh() {
        refreshRepository();
    }

    /**
     * {@inheritDoc}
     * If any error occurs during the refresh the exception will be returned in the {@code Future}.
     *
     * @return the refreshed data
     */
    @Async
    @Override
    public Future<String> refreshRepository() {
        logger.info("Refreshing countries data from REST Countries…");
        final ResponseEntity<String> response = restTemplate.getForEntity(countriesUri, String.class);
        final String body = response.getBody();
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("Failed to invoke REST Countries at " + countriesUri + " - " + response.getStatusCode().toString() + "\n" + body);
        }
        // Ensure it parses OK, but we don't need to keep the result.
        final List<Object> list = JsonParserFactory.getJsonParser().parseList(body);
        logger.debug("Loaded countries - " + (list == null ? 0 : list.size()) + " countries returned");

        countriesRawRepository.updateAll(body);
        logger.info("Finished refreshing countries data.");
        return new AsyncResult<>(body);
    }
}
