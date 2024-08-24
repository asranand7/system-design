package org.system.design.cdn.controller;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@RestController
class ImageController {

    private static final String CACHE_DIR = "image_cache/";
    private static final String ORIGIN_BASE_URL = "https://images.unsplash.com"; // Origin server base URL

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    @GetMapping("/cdn/images/**")
    @ResponseBody
    public void getImage(HttpServletRequest request, HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        // Extract the image path from the request URI
        String imagePath = request.getRequestURI().replace("/cdn/images/", "");

        // Generate a SHA-256 hash key from the image path
        String cacheKey = generateSHA256Hash(imagePath);
        File cachedImage = new File(CACHE_DIR + cacheKey);

        byte[] imageBytes;

        // Check if the image is cached locally
        if (cachedImage.exists()) {
            imageBytes = Files.readAllBytes(cachedImage.toPath());
        } else {
            // Construct the full URL to fetch the image from the origin server
            String imageUrl = ORIGIN_BASE_URL + "/" + imagePath;

            // Fetch the image from the origin server
            Request fetchRequest = new Request.Builder()
                    .url(imageUrl)
                    .build();

            try (Response originResponse = client.newCall(fetchRequest).execute()) {
                if (!originResponse.isSuccessful()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                imageBytes = originResponse.body().bytes();

                // Cache the image locally using the SHA-256 hash key
                Files.createDirectories(Paths.get(CACHE_DIR));
                Files.write(Paths.get(CACHE_DIR + cacheKey), imageBytes);
            }
        }

        // Determine the image type (e.g., jpeg, png) and set the content type
        String contentType = Files.probeContentType(Paths.get(imagePath));
        contentType = "image/jpeg"; // Default to binary if unknown
        response.setContentType(contentType);

        // Write the image bytes to the response output stream
        response.getOutputStream().write(imageBytes);
        response.getOutputStream().flush();
    }

    private String generateSHA256Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

