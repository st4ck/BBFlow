package bbflow;

/**
 * Global settings for the bbflow package
 */
public class bb_settings {
    /**
     * global settings to decide if the queue should be BLOCKING or NON BLOCKING
     */
    public static boolean BLOCKING = false;

    /**
     * global settings to decide if the queue should be BOUNDED or UNBOUNDED
     */
    public static boolean BOUNDED = false;

    /**
     * defualt buffer size for BOUNDED queues, used if user left blank
     */
    public static int defaultBufferSize = Integer.MAX_VALUE;

    /**
     * back off time used in case push or pop from a queue fail (non-blocking queues)
     * Unit is nanoseconds
     */
    public static int backOff = 1000;

    /**
     * listening port used as a base for INPUT network queues. Every INPUT channel will listen on
     * port = serverPort + channelId
     */
    public static int serverPort = 44444;

    /**
     * buffering in TCP channels. True by default. Unbuffered behavior not advised.
     */
    public static boolean bufferedTCP = true;
}
