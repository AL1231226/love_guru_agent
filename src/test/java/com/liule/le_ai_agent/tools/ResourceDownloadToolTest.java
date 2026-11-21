package com.liule.le_ai_agent.tools;

import cn.hutool.core.io.FileUtil;
import com.liule.le_ai_agent.constant.FileConstant;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceDownloadToolTest {

    private static final byte[] PNG_BYTES = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n', 0, 0, 0, 0};

    private static final byte[] WEBP_BYTES = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

    private static HttpServer server;

    private static String baseUrl;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/image.png", exchange -> respond(exchange, "image/png", PNG_BYTES));
        server.createContext("/image.webp", exchange -> respond(exchange, "image/webp", WEBP_BYTES));
        server.createContext("/page.html", exchange -> respond(exchange, "text/html; charset=utf-8",
                "<!DOCTYPE html><html><body>page</body></html>".getBytes(StandardCharsets.UTF_8)));
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static void respond(HttpExchange exchange, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(body);
        }
    }

    @Test
    public void testDownloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String result = tool.downloadResource(baseUrl + "/image.png", "test-image.png");
        assertTrue(result.startsWith("Resource downloaded successfully"), result);

        File file = new File(FileConstant.FILE_SAVE_DIR + "/download/test-image.png");
        assertTrue(file.exists());
        byte[] bytes = FileUtil.readBytes(file);
        assertArrayEquals(PNG_BYTES, Arrays.copyOf(bytes, PNG_BYTES.length));
    }

    @Test
    public void testExtensionAlignedWithContentType() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String result = tool.downloadResource(baseUrl + "/image.webp", "test-logo.png");
        assertTrue(result.endsWith("test-logo.webp"), result);

        File file = new File(FileConstant.FILE_SAVE_DIR + "/download/test-logo.webp");
        assertTrue(file.exists());
        byte[] bytes = FileUtil.readBytes(file);
        assertArrayEquals(WEBP_BYTES, Arrays.copyOf(bytes, WEBP_BYTES.length));
    }

    @Test
    public void testDownloadHtmlPageRejected() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String result = tool.downloadResource(baseUrl + "/page.html", "page.png");
        assertTrue(result.contains("HTML page"), result);
        assertFalse(new File(FileConstant.FILE_SAVE_DIR + "/download/page.png").exists());
    }
}
