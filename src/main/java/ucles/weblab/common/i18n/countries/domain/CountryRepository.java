package ucles.weblab.common.i18n.countries.domain;

import java.util.List;
import java.util.Optional;

/**
 * DDD repository interface - persistence-technology-neutral interface providing repository (i.e. CRUD) methods for
 * manipulating countries data.
 * <p>
 * Note that this manipulates countries as individual entries. Bulk operations on the whole repository are provided by
 * {@link CountriesRawRepository}.
 * </p>
 * <p>
 * Although this is technology neutral, it uses Spring Data naming conventions for methods. This allows the
 * interface to be extended with a Spring Data Repository interface for which an implementation is proxied in
 * at runtime.
 * </p>
 *
 * @since 23/07/15
 */
public interface CountryRepository {
    Optional<? extends CountryEntity> findOneByName(String name);
    List<? extends CountryEntity> findAll();
}
