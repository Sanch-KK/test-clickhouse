package com.clickhouse.client;

import java.util.Locale;

/**
 * Supported compression algoritms.
 */
public enum ClickHouseCompression {
    NONE("", "", ""),
    BROTLI("application/x-brotli", "br", "br"),
    BZ2("application/x-bzip2", "bz2", "bz2"),
    DEFLATE("application/deflate", "deflate", "zz"),
    GZIP("application/gzip", "gzip", "gz"),
    LZMA("application/x-lzma", "lzma", "xz"),
    LZ4("application/x-lz4", "lz4", "lz4"),
    ZIP("application/zip", "zip", "zip"),
    ZSTD("application/zstd", "zstd", "zst");

    private String mimeType;
    private String encoding;
    private String fileExt;

    // and maybe magic bytes?
    ClickHouseCompression(String mimeType, String encoding, String fileExt) {
        this.mimeType = mimeType;
        this.encoding = encoding;
        this.fileExt = fileExt;
    }

    public String mimeType() {
        return mimeType;
    }

    public String encoding() {
        return encoding;
    }

    public String fileExtension() {
        return fileExt;
    }

    /**
     * Get compression algorithm based on given MIME type.
     *
     * @param mimeType MIME type
     * @return compression algorithm
     */
    public static ClickHouseCompression fromMimeType(String mimeType) {
        ClickHouseCompression compression = NONE;

        if (mimeType != null) {
            for (ClickHouseCompression c : values()) {
                if (c.mimeType.equals(mimeType)) {
                    compression = c;
                    break;
                }
            }
        }

        return compression;
    }

    /**
     * Get compression algorithm based on given encoding.
     *
     * @param encoding content encoding
     * @return compression algorithm
     */
    public static ClickHouseCompression fromEncoding(String encoding) {
        ClickHouseCompression compression = NONE;

        if (encoding != null) {
            for (ClickHouseCompression c : values()) {
                if (c.encoding.equals(encoding)) {
                    compression = c;
                    break;
                }
            }
        }

        return compression;
    }

    /**
     * Get compression algorithm based on given file name.
     *
     * @param file file name
     * @return compression algorithm
     */
    public static ClickHouseCompression fromFileName(String file) {
        ClickHouseCompression compression = NONE;

        int index = 0;
        if (file != null && (index = file.lastIndexOf('.')) > 0) {
            String ext = file.substring(index + 1).toLowerCase(Locale.ROOT);
            for (ClickHouseCompression c : values()) {
                if (c.fileExt.equals(ext)) {
                    compression = c;
                    break;
                }
            }
        }

        return compression;
    }
}
