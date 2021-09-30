package bbflow;

public class bb_settings {
    /**
     * global settings to decide if the queue should be BLOCKING or NON BLOCKING
     */
    public static boolean BLOCKING = true;

    /**
     * global settings to decide if the queue should be BOUNDED or UNBOUNDED
     */
    public static boolean BOUNDED = true;

    /**
     * defualt buffer size for BOUNDED queues, used if user left blank
     */
    public static int defaultBufferSize = Integer.MAX_VALUE;

    /**
     * back off time used in case push or pop from a queue fail (non-blocking queues)
     * Milliseconds by default
     */
    public static int backOff = 5;
}
