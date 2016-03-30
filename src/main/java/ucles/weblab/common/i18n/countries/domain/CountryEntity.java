package ucles.weblab.common.i18n.countries.domain;

import ucles.weblab.common.domain.Buildable;

import java.util.List;
import java.util.Optional;

/**
 * Persistence-technology-neutral interface representing a retrieved country, which has natural entity status.
 *
 * @since 23/07/15
 */
public interface CountryEntity extends Country, Buildable<CountryEntity> {
    String getName();
    String getIso3166Alpha2Code();

    interface Builder extends Buildable.Builder<CountryEntity> {
        Builder name(String name);
        Builder population(Optional<Long> population);
        Builder iso3166Alpha2Code(String alpha2Code);
        Builder callingCodes(List<String> callingCodes);
    }
}
