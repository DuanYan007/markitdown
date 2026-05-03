package com.markitdown.ocr;

import com.markitdown.config.ConversionOptions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Central factory for selecting OCR engines by provider name.
 */
public final class OcrEngineFactory {

    private static final Map<String, OcrProvider> PROVIDERS = new LinkedHashMap<>();

    static {
        register(new Tess4jOcrProvider());
        register(new TesseractCliOcrProvider());
        register(new HttpOcrProvider());
        register(new MockOcrProvider());
    }

    private OcrEngineFactory() {
    }

    public static void register(OcrProvider provider) {
        Objects.requireNonNull(provider, "provider cannot be null");
        PROVIDERS.put(provider.getName().toLowerCase(), provider);
    }

    public static OcrEngine create(ConversionOptions options) {
        if (options == null || !options.isUseOcr()) {
            return new UnavailableOcrEngine("OCR is disabled");
        }

        String providerName = options.getOcrEngine();
        if (providerName == null || providerName.isBlank()) {
            providerName = "tess4j";
        }

        OcrProvider provider = PROVIDERS.get(providerName.toLowerCase());
        if (provider == null) {
            return new UnavailableOcrEngine("Unsupported OCR engine: " + providerName);
        }

        if (!provider.isAvailable()) {
            return new UnavailableOcrEngine(
                    "OCR engine '" + providerName + "' is not available in the current build or environment");
        }

        OcrEngine engine = provider.create(options);
        if (engine == null || !engine.isAvailable()) {
            return new UnavailableOcrEngine(
                    "OCR engine '" + providerName + "' is not available for the current configuration");
        }
        return engine;
    }
}
