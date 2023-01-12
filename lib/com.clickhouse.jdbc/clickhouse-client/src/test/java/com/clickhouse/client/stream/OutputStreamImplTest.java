package com.clickhouse.client.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.clickhouse.client.ClickHouseByteBuffer;
import com.clickhouse.client.ClickHouseOutputStream;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class OutputStreamImplTest {
    protected byte[] toByteArray(int... bytes) {
        byte[] b = ClickHouseByteBuffer.EMPTY_BYTES;
        if (bytes != null) {
            b = new byte[bytes.length];
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) (0xFF & bytes[i]);
            }
        }
        return b;
    }

    @DataProvider(name = "bufferSizeProvider")
    private Object[][] getOutputStreamBufferSize() {
        return new Object[][] {
                new Object[] { 2 }, new Object[] { 3 }, new Object[] { 4 }, new Object[] { 5 }
        };
    }

    @DataProvider(name = "streamWithData")
    private Object[][] getOutputStreamWithData() {
        return new Object[][] {
                new Object[] { new WrappedInputStream(null,
                        new ByteArrayInputStream(new byte[] { -1, 1, 2, 3, 4, 5, 6 }, 1, 5), 1,
                        null) },
                new Object[] {
                        new IterableByteArrayInputStream(
                                Arrays.asList(new byte[] { 1 }, new byte[] { 2, 3, 4 },
                                        new byte[] { 5 }),
                                null)
                },
                new Object[] {
                        new IterableByteArrayInputStream(
                                Arrays.asList(null, new byte[0], new byte[] { 1, 2, 3 },
                                        new byte[0], null,
                                        new byte[] { 4, 5 }, null,
                                        new byte[0], null),
                                null)
                },
                new Object[] { new IterableByteBufferInputStream(
                        Arrays.asList(null, ByteBuffer.allocateDirect(0),
                                ByteBuffer.wrap(new byte[] { -1, 1, 2, 3, -4 }, 1, 3),
                                ByteBuffer.allocate(0), null,
                                ByteBuffer.wrap(new byte[] { 4, 5 }), null,
                                ByteBuffer.allocate(0), null),
                        null) }
        };
    }

    @Test(dataProvider = "bufferSizeProvider", groups = { "unit" })
    public void testEmptyOrClosedOutput(int bufferSize) throws IOException {
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream();
                WrappedOutputStream out = new WrappedOutputStream(null, bas, bufferSize, null)) {
            Assert.assertFalse(out.isClosed());
            out.write(12);
            out.flush();
            Assert.assertEquals(bas.toByteArray(), new byte[] { 12 });
        }
    }

    @Test(dataProvider = "bufferSizeProvider", groups = { "unit" })
    public void testWriteCustom(int bufferSize) throws IOException {
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream();
                ClickHouseOutputStream out = new WrappedOutputStream(null, bas, bufferSize, null)) {
            out.writeCustom((bytes, position, limit) -> 0);
            Assert.assertEquals(bas.toByteArray(), new byte[0]);
            out.writeCustom((bytes, position, limit) -> {
                if (bytes[position] != Byte.MAX_VALUE) {
                    bytes[position] = Byte.MAX_VALUE;
                    return -1;
                }

                return 1;
            });
            Assert.assertEquals(bas.toByteArray(), new byte[0]);
            out.flush();
            Assert.assertEquals(bas.toByteArray(), new byte[] { Byte.MAX_VALUE });
        }
    }
}
