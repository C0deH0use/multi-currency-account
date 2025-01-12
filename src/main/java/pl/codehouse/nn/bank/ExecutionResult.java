package pl.codehouse.nn.bank;

import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the result of a command execution.
 *
 * @param <T> The type of the value contained in the result.
 */
public record ExecutionResult<T>(Optional<T> value, RuntimeException exception) {
    private static final Logger log = LoggerFactory.getLogger(ExecutionResult.class);
    /**
     * Creates a successful execution result with the given value.
     *
     * @param value The value of the successful execution.
     * @param <T> The type of the value.
     * @return An ExecutionResult instance representing a successful execution.
     */
    public static <T> ExecutionResult<T> success(T value) {
        return new ExecutionResult<>(Optional.of(value), null);
    }

    /**
     * Creates a failed execution result with the given exception.
     *
     * @param exception The exception that caused the failure.
     * @param <T> The type of the value (which will be absent in case of failure).
     * @return An ExecutionResult instance representing a failed execution.
     */
    public static <T> ExecutionResult<T> failure(RuntimeException exception) {
        return new ExecutionResult<>(Optional.empty(), exception);
    }

    /**
     * Checks if the execution was successful.
     *
     * @return true if the execution was successful, false otherwise.
     */
    public boolean isSuccess() {
        return value.isPresent();
    }

    /**
     * Checks if the execution failed.
     *
     * @return true if the execution failed, false otherwise.
     */
    public boolean isFailure() {
        return !isSuccess();
    }

    public T handle() {
        log.info("Resolving the Execution result... Result is {}, going to {}.",
                 isSuccess(), BooleanUtils.toString(isSuccess(), "return the value object", "going to throw passed exception"));
        return value.orElseThrow(() -> exception);
    }
}
