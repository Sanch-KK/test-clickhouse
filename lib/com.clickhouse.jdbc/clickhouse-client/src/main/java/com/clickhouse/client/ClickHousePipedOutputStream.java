package com.clickhouse.client;

/**
 * SPSC(Single-producer single-consumer) channel for streaming.
 */
public abstract class ClickHousePipedOutputStream extends ClickHouseOutputStream {
    protected ClickHousePipedOutputStream(Runnable postCloseAction) {
        super(null, postCloseAction);
    }

    /**
     * Gets input stream to reada data being written into the output stream.
     *
     * @return non-null input stream
     */
    public final ClickHouseInputStream getInputStream() {
        return getInputStream(null);
    }

    /**
     * Gets input stream to reada data being written into the output stream.
     *
     * @param postCloseAction custom action will be performed right after closing
     *                        the input stream
     * @return non-null input stream
     */
    public abstract ClickHouseInputStream getInputStream(Runnable postCloseAction);
}
