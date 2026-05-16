package com.markitdown.api;

import com.markitdown.config.ConversionOptions;
import com.markitdown.exceptions.ConversionException;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Core contract for converters that turn source documents into Markdown.
 *
 * <p>Implementations usually declare support for one or more MIME types and
 * expose a primary path-based conversion entry point. Converters may also opt
 * into stream-based conversion when they can operate without direct file system
 * access.</p>
 */
public interface DocumentConverter {

    /**
     * Converts a file on disk to Markdown.
     *
     * @param filePath source file path
     * @param options conversion options
     * @return conversion result containing Markdown, metadata, and warnings
     * @throws ConversionException when conversion fails
     */
    ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException;

    /**
     * Converts a document stream to Markdown.
     *
     * <p>The default implementation rejects streaming so converters only need to
     * override this method when they explicitly support it.</p>
     *
     * @param inputStream source document stream
     * @param mimeType detected or declared MIME type
     * @param options conversion options
     * @return conversion result containing Markdown, metadata, and warnings
     * @throws ConversionException when streaming is unsupported or conversion fails
     * @since 2.1.0
     */
    default ConversionResult convert(InputStream inputStream, String mimeType, ConversionOptions options)
            throws ConversionException {
        throw new ConversionException(
                "Stream-based conversion is not supported by " + getName() + ". Use Path-based convert() method instead.",
                "stream",
                getName()
        );
    }

    /**
     * Indicates whether this converter supports stream-based conversion.
     *
     * @return {@code true} when {@link #convert(InputStream, String, ConversionOptions)}
     *         is implemented, otherwise {@code false}
     * @since 2.1.0
     */
    default boolean supportsStreaming() {
        return false;
    }

    /**
     * Checks whether the converter supports a MIME type.
     *
     * @param mimeType MIME type to evaluate
     * @return {@code true} when the converter can handle the MIME type
     */
    boolean supports(String mimeType);

    /**
     * Returns the converter priority used during registry selection.
     *
     * <p>Higher values win when multiple converters support the same MIME type.</p>
     *
     * @return converter priority
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Returns the human-readable converter name.
     *
     * @return converter name
     */
    String getName();
}
