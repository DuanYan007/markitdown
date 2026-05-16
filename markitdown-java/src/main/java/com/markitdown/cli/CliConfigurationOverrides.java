package com.markitdown.cli;

import com.markitdown.config.ConfigurationManager;

/**
 * Applies CLI-derived overrides onto a configuration manager.
 */
final class CliConfigurationOverrides {

    private CliConfigurationOverrides() {
    }

    static void apply(ConfigurationManager configManager, Inputs inputs) {
        applyBooleanOverride(configManager::overrideContentIncludeImages,
                inputs.includeImages(), inputs.noImages());
        applyBooleanOverride(configManager::overrideContentIncludeTables,
                inputs.includeTables(), inputs.noTables());
        applyBooleanOverride(configManager::overrideContentIncludeMetadata,
                inputs.includeMetadata(), inputs.noMetadata());

        if (inputs.useOcr()) {
            configManager.overrideOcrEnabled(true, ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.languageSpecified()) {
            configManager.overrideOcrLanguage(inputs.language(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.ocrEngineSpecified()) {
            configManager.overrideOcrEngine(inputs.ocrEngine(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.ocrEndpointSpecified()) {
            configManager.overrideOcrEndpoint(inputs.ocrEndpoint(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.ocrApiKeySpecified()) {
            configManager.overrideOcrApiKey(inputs.ocrApiKey(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.ocrModelSpecified()) {
            configManager.overrideOcrModel(inputs.ocrModel(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.ocrTimeoutSpecified()) {
            configManager.overrideOcrTimeout(inputs.ocrTimeout(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.ocrPollIntervalSpecified()) {
            configManager.overrideOcrPollInterval(inputs.ocrPollInterval(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.tableFormatSpecified()) {
            configManager.overrideTableFormat(inputs.tableFormat(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.imageFormatSpecified()) {
            configManager.overrideImageFormat(inputs.imageFormat(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.imageOutputDirSpecified()) {
            configManager.overrideOutputImageDir(inputs.imageOutputDir(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.tempDirSpecified()) {
            configManager.overrideOutputTempDir(inputs.tempDir(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.maxFileSizeSpecified()) {
            configManager.overridePerformanceMaxFileSize(inputs.maxFileSize(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.largeFile()) {
            configManager.overrideFilesLargeFile(true, ConfigurationManager.ConfigSource.CLI);
            configManager.overridePerformanceMaxFileSize(0L, ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.parallel()) {
            configManager.overridePerformanceParallel(true, ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.threadsSpecified()) {
            configManager.overridePerformanceThreads(inputs.threads(), ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.showProgress()) {
            configManager.overrideUiProgress(true, ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.showStats()) {
            configManager.overrideUiStats(true, ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.optimizeMemory()) {
            configManager.overridePerformanceOptimizeMemory(true, ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.verbose()) {
            configManager.overrideUiVerbose(true, ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.quiet()) {
            configManager.overrideUiQuiet(true, ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.recursive()) {
            configManager.overrideFilesRecursive(true, ConfigurationManager.ConfigSource.CLI);
        }
        if (inputs.batch()) {
            configManager.overrideFilesBatch(true, ConfigurationManager.ConfigSource.CLI);
        }
    }

    private static void applyBooleanOverride(
            BooleanOverride override,
            Boolean includeFlag,
            boolean excludeFlag
    ) {
        if (includeFlag != null) {
            override.apply(includeFlag, ConfigurationManager.ConfigSource.CLI);
            return;
        }
        if (excludeFlag) {
            override.apply(false, ConfigurationManager.ConfigSource.CLI);
        }
    }

    @FunctionalInterface
    interface BooleanOverride {
        void apply(boolean value, ConfigurationManager.ConfigSource source);
    }

    static final class Inputs {
        private final Boolean includeImages;
        private final boolean noImages;
        private final Boolean includeTables;
        private final boolean noTables;
        private final Boolean includeMetadata;
        private final boolean noMetadata;
        private final boolean useOcr;
        private final boolean languageSpecified;
        private final String language;
        private final boolean ocrEngineSpecified;
        private final String ocrEngine;
        private final boolean ocrEndpointSpecified;
        private final String ocrEndpoint;
        private final boolean ocrApiKeySpecified;
        private final String ocrApiKey;
        private final boolean ocrModelSpecified;
        private final String ocrModel;
        private final boolean ocrTimeoutSpecified;
        private final int ocrTimeout;
        private final boolean ocrPollIntervalSpecified;
        private final int ocrPollInterval;
        private final boolean tableFormatSpecified;
        private final String tableFormat;
        private final boolean imageFormatSpecified;
        private final String imageFormat;
        private final boolean imageOutputDirSpecified;
        private final String imageOutputDir;
        private final boolean tempDirSpecified;
        private final String tempDir;
        private final boolean maxFileSizeSpecified;
        private final long maxFileSize;
        private final boolean largeFile;
        private final boolean parallel;
        private final boolean threadsSpecified;
        private final int threads;
        private final boolean showProgress;
        private final boolean showStats;
        private final boolean optimizeMemory;
        private final boolean verbose;
        private final boolean quiet;
        private final boolean recursive;
        private final boolean batch;

        private Inputs(Builder builder) {
            this.includeImages = builder.includeImages;
            this.noImages = builder.noImages;
            this.includeTables = builder.includeTables;
            this.noTables = builder.noTables;
            this.includeMetadata = builder.includeMetadata;
            this.noMetadata = builder.noMetadata;
            this.useOcr = builder.useOcr;
            this.languageSpecified = builder.languageSpecified;
            this.language = builder.language;
            this.ocrEngineSpecified = builder.ocrEngineSpecified;
            this.ocrEngine = builder.ocrEngine;
            this.ocrEndpointSpecified = builder.ocrEndpointSpecified;
            this.ocrEndpoint = builder.ocrEndpoint;
            this.ocrApiKeySpecified = builder.ocrApiKeySpecified;
            this.ocrApiKey = builder.ocrApiKey;
            this.ocrModelSpecified = builder.ocrModelSpecified;
            this.ocrModel = builder.ocrModel;
            this.ocrTimeoutSpecified = builder.ocrTimeoutSpecified;
            this.ocrTimeout = builder.ocrTimeout;
            this.ocrPollIntervalSpecified = builder.ocrPollIntervalSpecified;
            this.ocrPollInterval = builder.ocrPollInterval;
            this.tableFormatSpecified = builder.tableFormatSpecified;
            this.tableFormat = builder.tableFormat;
            this.imageFormatSpecified = builder.imageFormatSpecified;
            this.imageFormat = builder.imageFormat;
            this.imageOutputDirSpecified = builder.imageOutputDirSpecified;
            this.imageOutputDir = builder.imageOutputDir;
            this.tempDirSpecified = builder.tempDirSpecified;
            this.tempDir = builder.tempDir;
            this.maxFileSizeSpecified = builder.maxFileSizeSpecified;
            this.maxFileSize = builder.maxFileSize;
            this.largeFile = builder.largeFile;
            this.parallel = builder.parallel;
            this.threadsSpecified = builder.threadsSpecified;
            this.threads = builder.threads;
            this.showProgress = builder.showProgress;
            this.showStats = builder.showStats;
            this.optimizeMemory = builder.optimizeMemory;
            this.verbose = builder.verbose;
            this.quiet = builder.quiet;
            this.recursive = builder.recursive;
            this.batch = builder.batch;
        }

        static Builder builder() {
            return new Builder();
        }

        Boolean includeImages() { return includeImages; }
        boolean noImages() { return noImages; }
        Boolean includeTables() { return includeTables; }
        boolean noTables() { return noTables; }
        Boolean includeMetadata() { return includeMetadata; }
        boolean noMetadata() { return noMetadata; }
        boolean useOcr() { return useOcr; }
        boolean languageSpecified() { return languageSpecified; }
        String language() { return language; }
        boolean ocrEngineSpecified() { return ocrEngineSpecified; }
        String ocrEngine() { return ocrEngine; }
        boolean ocrEndpointSpecified() { return ocrEndpointSpecified; }
        String ocrEndpoint() { return ocrEndpoint; }
        boolean ocrApiKeySpecified() { return ocrApiKeySpecified; }
        String ocrApiKey() { return ocrApiKey; }
        boolean ocrModelSpecified() { return ocrModelSpecified; }
        String ocrModel() { return ocrModel; }
        boolean ocrTimeoutSpecified() { return ocrTimeoutSpecified; }
        int ocrTimeout() { return ocrTimeout; }
        boolean ocrPollIntervalSpecified() { return ocrPollIntervalSpecified; }
        int ocrPollInterval() { return ocrPollInterval; }
        boolean tableFormatSpecified() { return tableFormatSpecified; }
        String tableFormat() { return tableFormat; }
        boolean imageFormatSpecified() { return imageFormatSpecified; }
        String imageFormat() { return imageFormat; }
        boolean imageOutputDirSpecified() { return imageOutputDirSpecified; }
        String imageOutputDir() { return imageOutputDir; }
        boolean tempDirSpecified() { return tempDirSpecified; }
        String tempDir() { return tempDir; }
        boolean maxFileSizeSpecified() { return maxFileSizeSpecified; }
        long maxFileSize() { return maxFileSize; }
        boolean largeFile() { return largeFile; }
        boolean parallel() { return parallel; }
        boolean threadsSpecified() { return threadsSpecified; }
        int threads() { return threads; }
        boolean showProgress() { return showProgress; }
        boolean showStats() { return showStats; }
        boolean optimizeMemory() { return optimizeMemory; }
        boolean verbose() { return verbose; }
        boolean quiet() { return quiet; }
        boolean recursive() { return recursive; }
        boolean batch() { return batch; }

        static final class Builder {
            private Boolean includeImages;
            private boolean noImages;
            private Boolean includeTables;
            private boolean noTables;
            private Boolean includeMetadata;
            private boolean noMetadata;
            private boolean useOcr;
            private boolean languageSpecified;
            private String language;
            private boolean ocrEngineSpecified;
            private String ocrEngine;
            private boolean ocrEndpointSpecified;
            private String ocrEndpoint;
            private boolean ocrApiKeySpecified;
            private String ocrApiKey;
            private boolean ocrModelSpecified;
            private String ocrModel;
            private boolean ocrTimeoutSpecified;
            private int ocrTimeout;
            private boolean ocrPollIntervalSpecified;
            private int ocrPollInterval;
            private boolean tableFormatSpecified;
            private String tableFormat;
            private boolean imageFormatSpecified;
            private String imageFormat;
            private boolean imageOutputDirSpecified;
            private String imageOutputDir;
            private boolean tempDirSpecified;
            private String tempDir;
            private boolean maxFileSizeSpecified;
            private long maxFileSize;
            private boolean largeFile;
            private boolean parallel;
            private boolean threadsSpecified;
            private int threads;
            private boolean showProgress;
            private boolean showStats;
            private boolean optimizeMemory;
            private boolean verbose;
            private boolean quiet;
            private boolean recursive;
            private boolean batch;

            Builder includeImages(Boolean includeImages) { this.includeImages = includeImages; return this; }
            Builder noImages(boolean noImages) { this.noImages = noImages; return this; }
            Builder includeTables(Boolean includeTables) { this.includeTables = includeTables; return this; }
            Builder noTables(boolean noTables) { this.noTables = noTables; return this; }
            Builder includeMetadata(Boolean includeMetadata) { this.includeMetadata = includeMetadata; return this; }
            Builder noMetadata(boolean noMetadata) { this.noMetadata = noMetadata; return this; }
            Builder useOcr(boolean useOcr) { this.useOcr = useOcr; return this; }
            Builder languageSpecified(boolean languageSpecified) { this.languageSpecified = languageSpecified; return this; }
            Builder language(String language) { this.language = language; return this; }
            Builder ocrEngineSpecified(boolean ocrEngineSpecified) { this.ocrEngineSpecified = ocrEngineSpecified; return this; }
            Builder ocrEngine(String ocrEngine) { this.ocrEngine = ocrEngine; return this; }
            Builder ocrEndpointSpecified(boolean ocrEndpointSpecified) { this.ocrEndpointSpecified = ocrEndpointSpecified; return this; }
            Builder ocrEndpoint(String ocrEndpoint) { this.ocrEndpoint = ocrEndpoint; return this; }
            Builder ocrApiKeySpecified(boolean ocrApiKeySpecified) { this.ocrApiKeySpecified = ocrApiKeySpecified; return this; }
            Builder ocrApiKey(String ocrApiKey) { this.ocrApiKey = ocrApiKey; return this; }
            Builder ocrModelSpecified(boolean ocrModelSpecified) { this.ocrModelSpecified = ocrModelSpecified; return this; }
            Builder ocrModel(String ocrModel) { this.ocrModel = ocrModel; return this; }
            Builder ocrTimeoutSpecified(boolean ocrTimeoutSpecified) { this.ocrTimeoutSpecified = ocrTimeoutSpecified; return this; }
            Builder ocrTimeout(int ocrTimeout) { this.ocrTimeout = ocrTimeout; return this; }
            Builder ocrPollIntervalSpecified(boolean ocrPollIntervalSpecified) { this.ocrPollIntervalSpecified = ocrPollIntervalSpecified; return this; }
            Builder ocrPollInterval(int ocrPollInterval) { this.ocrPollInterval = ocrPollInterval; return this; }
            Builder tableFormatSpecified(boolean tableFormatSpecified) { this.tableFormatSpecified = tableFormatSpecified; return this; }
            Builder tableFormat(String tableFormat) { this.tableFormat = tableFormat; return this; }
            Builder imageFormatSpecified(boolean imageFormatSpecified) { this.imageFormatSpecified = imageFormatSpecified; return this; }
            Builder imageFormat(String imageFormat) { this.imageFormat = imageFormat; return this; }
            Builder imageOutputDirSpecified(boolean imageOutputDirSpecified) { this.imageOutputDirSpecified = imageOutputDirSpecified; return this; }
            Builder imageOutputDir(String imageOutputDir) { this.imageOutputDir = imageOutputDir; return this; }
            Builder tempDirSpecified(boolean tempDirSpecified) { this.tempDirSpecified = tempDirSpecified; return this; }
            Builder tempDir(String tempDir) { this.tempDir = tempDir; return this; }
            Builder maxFileSizeSpecified(boolean maxFileSizeSpecified) { this.maxFileSizeSpecified = maxFileSizeSpecified; return this; }
            Builder maxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; return this; }
            Builder largeFile(boolean largeFile) { this.largeFile = largeFile; return this; }
            Builder parallel(boolean parallel) { this.parallel = parallel; return this; }
            Builder threadsSpecified(boolean threadsSpecified) { this.threadsSpecified = threadsSpecified; return this; }
            Builder threads(int threads) { this.threads = threads; return this; }
            Builder showProgress(boolean showProgress) { this.showProgress = showProgress; return this; }
            Builder showStats(boolean showStats) { this.showStats = showStats; return this; }
            Builder optimizeMemory(boolean optimizeMemory) { this.optimizeMemory = optimizeMemory; return this; }
            Builder verbose(boolean verbose) { this.verbose = verbose; return this; }
            Builder quiet(boolean quiet) { this.quiet = quiet; return this; }
            Builder recursive(boolean recursive) { this.recursive = recursive; return this; }
            Builder batch(boolean batch) { this.batch = batch; return this; }

            Inputs build() {
                return new Inputs(this);
            }
        }
    }
}
