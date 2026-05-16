package com.markitdown;

import com.markitdown.cli.MarkItDownCommand;
import com.markitdown.core.MarkItDownEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application entry point for the MarkItDown Java package.
 *
 * <p>The current application surface is the Picocli-based CLI backed by the
 * shared conversion engine. This class keeps the startup boundary small and
 * exposes a helper for building the default engine registry.</p>
 *
 * @author duan yan
 * @version 2.1.0
 * @since 2.0.0
 */
public class MarkItDownApplication {

    private static final Logger logger = LoggerFactory.getLogger(MarkItDownApplication.class);

    /**
     * Starts the CLI entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        MarkItDownCommand.main(args);
    }

    /**
     * Builds an engine instance with the default converter registry.
     *
     * @return configured {@link MarkItDownEngine} instance
     */
    public static MarkItDownEngine createEngine() {
        return new MarkItDownEngine(MarkItDownEngine.createDefaultRegistry());
    }
}
