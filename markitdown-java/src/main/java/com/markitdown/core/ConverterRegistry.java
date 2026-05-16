package com.markitdown.core;

import com.markitdown.api.DocumentConverter;
import com.markitdown.utils.FileTypeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe registry for document converters.
 *
 * <p>The registry supports lookup by converter name and by MIME type. MIME
 * lookups are cached after first resolution and ordered by converter
 * priority.</p>
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class ConverterRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ConverterRegistry.class);

    private final Map<String, List<DocumentConverter>> mimeTypeToConverters;
    private final Map<String, DocumentConverter> nameToConverter;

    /**
     * Creates an empty converter registry.
     */
    public ConverterRegistry() {
        this.mimeTypeToConverters = new ConcurrentHashMap<>();
        this.nameToConverter = new ConcurrentHashMap<>();
    }

    // ==================== Registration ====================

    /**
     * Registers a converter instance.
     *
     * @param converter converter to register
     */
    public synchronized void registerConverter(DocumentConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");

        String name = converter.getName();
        if (nameToConverter.containsKey(name)) {
            throw new IllegalArgumentException("Converter with name '" + name + "' is already registered");
        }

        nameToConverter.put(name, converter);
        logger.info("Registered converter: {}", name);

        // MIME type cache entries are created lazily during lookup.
    }

    /**
     * Unregisters a converter by name.
     *
     * @param converterName converter name
     * @return {@code true} when a converter was removed
     */
    public synchronized boolean unregisterConverter(String converterName) {
        DocumentConverter removed = nameToConverter.remove(converterName);
        if (removed != null) {
            logger.info("Unregistered converter: {}", converterName);
            mimeTypeToConverters.clear();
            return true;
        }
        return false;
    }

    // ==================== Lookup ====================

    /**
     * Returns the highest-priority converter for a MIME type.
     *
     * @param mimeType MIME type to resolve
     * @return matching converter when available
     */
    public Optional<DocumentConverter> getConverter(String mimeType) {
        Objects.requireNonNull(mimeType, "MIME type cannot be null");

        List<DocumentConverter> converters = mimeTypeToConverters.get(mimeType);
        if (converters != null && !converters.isEmpty()) {
            return Optional.of(converters.get(0));
        }

        List<DocumentConverter> matchingConverters = nameToConverter.values().stream()
                .filter(converter -> converter.supports(mimeType))
                .sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority()))
                .collect(Collectors.toList());

        if (matchingConverters.isEmpty()) {
            logger.debug("No converter found for MIME type: {}", mimeType);
            return Optional.empty();
        }

        mimeTypeToConverters.put(mimeType, matchingConverters);
        logger.debug("Found {} converter(s) for MIME type: {}", matchingConverters.size(), mimeType);

        return Optional.of(matchingConverters.get(0));
    }

    /**
     * Returns all converters that support a MIME type ordered by priority.
     *
     * @param mimeType MIME type to resolve
     * @return immutable converter list
     */
    public List<DocumentConverter> getAllConverters(String mimeType) {
        Objects.requireNonNull(mimeType, "MIME type cannot be null");

        List<DocumentConverter> converters = nameToConverter.values().stream()
                .filter(converter -> converter.supports(mimeType))
                .sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority()))
                .collect(Collectors.toList());

        return Collections.unmodifiableList(converters);
    }

    /**
     * Returns a converter by its unique name.
     *
     * @param name converter name
     * @return matching converter when available
     */
    public Optional<DocumentConverter> getConverterByName(String name) {
        Objects.requireNonNull(name, "Converter name cannot be null");
        return Optional.ofNullable(nameToConverter.get(name));
    }

    /**
     * Returns all registered converters.
     *
     * @return immutable converter collection
     */
    public Collection<DocumentConverter> getAllConverters() {
        return Collections.unmodifiableCollection(nameToConverter.values());
    }

    // ==================== MIME support ====================

    /**
     * Returns the set of supported MIME types inferred from registered converters.
     *
     * @return immutable MIME type set
     */
    public Set<String> getSupportedMimeTypes() {
        Set<String> mimeTypes = new HashSet<>();

        for (DocumentConverter converter : nameToConverter.values()) {
            mimeTypes.addAll(getCommonMimeTypesForConverter(converter));
        }

        return Collections.unmodifiableSet(mimeTypes);
    }

    /**
     * Returns whether a MIME type is supported by any registered converter.
     *
     * @param mimeType MIME type to check
     * @return {@code true} when a converter supports the MIME type
     */
    public boolean isSupported(String mimeType) {
        return getConverter(mimeType).isPresent();
    }

    // ==================== Management ====================

    /**
     * Removes all registered converters and clears MIME caches.
     */
    public synchronized void clear() {
        logger.info("Clearing all converters from registry");
        nameToConverter.clear();
        mimeTypeToConverters.clear();
    }

    /**
     * Returns the number of registered converters.
     *
     * @return converter count
     */
    public int getConverterCount() {
        return nameToConverter.size();
    }

    /**
     * Returns debug-friendly information for all registered converters.
     *
     * @return immutable map keyed by converter name
     */
    public Map<String, String> getConverterInfo() {
        Map<String, String> info = new LinkedHashMap<>();

        for (Map.Entry<String, DocumentConverter> entry : nameToConverter.entrySet()) {
            DocumentConverter converter = entry.getValue();
            info.put(entry.getKey(),
                    String.format("Priority: %d, Class: %s",
                            converter.getPriority(),
                            converter.getClass().getSimpleName()));
        }

        return Collections.unmodifiableMap(info);
    }

    // ==================== Helpers ====================

    /**
     * Infers common MIME types from a converter class name.
     *
     * <p>This remains a heuristic until converters expose supported MIME types
     * directly.</p>
     *
     * @param converter converter to inspect
     * @return inferred MIME type set
     */
    private Set<String> getCommonMimeTypesForConverter(DocumentConverter converter) {
        Set<String> mimeTypes = new HashSet<>();
        for (String mimeType : FileTypeDetector.getKnownMimeTypes()) {
            if (converter.supports(mimeType)) {
                mimeTypes.add(mimeType);
            }
        }

        return mimeTypes;
    }

    @Override
    public String toString() {
        return "ConverterRegistry{" +
                "converterCount=" + nameToConverter.size() +
                ", supportedMimeTypes=" + getSupportedMimeTypes().size() +
                '}';
    }
}
