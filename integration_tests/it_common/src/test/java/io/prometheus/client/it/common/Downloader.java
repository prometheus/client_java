package io.prometheus.client.it.common;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Downloader {

    /**
     * Download a file from an HTTP URL and store it in the target/ directory.
     */
    public static void downloadToTarget(String url, String fileName) throws IOException, URISyntaxException {
        Path destination = Paths.get(Downloader.class.getResource("/").toURI()).getParent().resolve(fileName);
        if (Files.exists(destination)) {
            return; // only download once
        }
        System.out.println("Downloading " + url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        InputStream responseBody = response.body().byteStream();
        OutputStream output = new FileOutputStream(destination.toFile());
        byte[] data = new byte[1024];
        int count;
        while ((count = responseBody.read(data)) != -1) {
            output.write(data, 0, count);
        }
        output.close();
        responseBody.close();
    }
}
