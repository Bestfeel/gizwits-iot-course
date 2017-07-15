
package com.gizwits.tail;


public class TailerListenerAdapter implements TailerListener {

    /**
     * The tailer will call this method during construction,
     * giving the listener a method of stopping the tailer.
     *
     * @param tailer the tailer.
     */
    public void init(final Tailer tailer) {
    }

    /**
     * This method is called if the tailed file is not found.
     */
    public void fileNotFound() {
    }

    /**
     * Called if a file rotation is detected.
     * <p>
     * This method is called before the file is reopened, and fileNotFound may
     * be called if the new file has not yet been created.
     */
    public void fileRotated() {
    }

    /**
     * Handles a line from a Tailer.
     *
     * @param line the line.
     */
    public void handle(final String line) {
    }

    /**
     * Handles an Exception .
     *
     * @param ex the exception.
     */
    public void handle(final Exception ex) {
    }

    /**
     * Called each time the Tailer reaches the end of the file.
     * <p>
     * <b>Note:</b> this is called from the tailer thread.
     * <p>
     * Note: a future version of commons-io will pull this method up to the TailerListener interface,
     * for now clients must subclass this class to use this feature.
     *
     * @since 2.5
     */
    public void endOfFileReached() {
    }
}
