package ucles.weblab.common.i18n.countries.domain;

/**
 * Interface to allow JDK proxying of {@link AutoRefreshCountriesServiceImpl}.
 *
 * @since 30/05/2017
 */
public interface ScheduledRefresh {
    void scheduledRefresh();
}
