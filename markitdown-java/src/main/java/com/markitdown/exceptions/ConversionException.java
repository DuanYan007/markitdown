package com.markitdown.exceptions;

/**
 * Exception raised when a document conversion operation fails.
 *
 * <p>The exception can optionally capture the source file name and the
 * converter name to make CLI and library error reporting more actionable.</p>
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class ConversionException extends Exception {

    private final String fileName;
    private final String converterName;

    /**
     * Creates a new exception with a detail message.
     *
     * @param message detail message
     */
    public ConversionException(String message) {
        super(message);
        this.fileName = null;
        this.converterName = null;
    }

    /**
     * Creates a new exception with a detail message and cause.
     *
     * @param message detail message
     * @param cause underlying cause
     */
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
        this.fileName = null;
        this.converterName = null;
    }

    /**
     * Creates a new exception with file and converter context.
     *
     * @param message detail message
     * @param fileName source file name
     * @param converterName converter name
     */
    public ConversionException(String message, String fileName, String converterName) {
        super(message);
        this.fileName = fileName;
        this.converterName = converterName;
    }

    /**
     * Creates a new exception with a cause plus file and converter context.
     *
     * @param message detail message
     * @param cause underlying cause
     * @param fileName source file name
     * @param converterName converter name
     */
    public ConversionException(String message, Throwable cause, String fileName, String converterName) {
        super(message, cause);
        this.fileName = fileName;
        this.converterName = converterName;
    }

    /**
     * Returns the file name associated with the failure when available.
     *
     * @return failed file name, or {@code null}
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the converter name associated with the failure when available.
     *
     * @return converter name, or {@code null}
     */
    public String getConverterName() {
        return converterName;
    }

    @Override
    public String toString() {
        String className = getClass().getName();
        String message = getLocalizedMessage();
        StringBuilder sb = new StringBuilder(className);

        if (message != null) {
            sb.append(": ").append(message);
        }

        if (fileName != null || converterName != null) {
            sb.append(" [");
            if (fileName != null) {
                sb.append("file=").append(fileName);
            }
            if (fileName != null && converterName != null) {
                sb.append(", ");
            }
            if (converterName != null) {
                sb.append("converter=").append(converterName);
            }
            sb.append("]");
        }

        return sb.toString();
    }
}
