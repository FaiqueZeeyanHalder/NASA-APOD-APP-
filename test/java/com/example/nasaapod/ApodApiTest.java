package com.example.nasaapod;

import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;

public class ApodApiTest {

    @Test
    public void testApodResponseDeserialization() {
        String json = "{\n" +
                "  \"date\": \"2025-01-01\",\n" +
                "  \"explanation\": \"A beautiful nebula in deep space.\",\n" +
                "  \"hdurl\": \"https://apod.nasa.gov/apod/image/2501/nebula_hd.jpg\",\n" +
                "  \"media_type\": \"image\",\n" +
                "  \"service_version\": \"v1\",\n" +
                "  \"title\": \"Deep Space Nebula\",\n" +
                "  \"url\": \"https://apod.nasa.gov/apod/image/2501/nebula.jpg\",\n" +
                "  \"thumbnail_url\": \"https://apod.nasa.gov/apod/image/2501/nebula_thumb.jpg\",\n" +
                "  \"copyright\": \"NASA / Hubble\"\n" +
                "}";

        Gson gson = new Gson();
        ApodResponse response = gson.fromJson(json, ApodResponse.class);

        assertNotNull(response);
        assertEquals("2025-01-01", response.getDate());
        assertEquals("A beautiful nebula in deep space.", response.getExplanation());
        assertEquals("image", response.getMediaType());
        assertEquals("image", response.getMedia_type());
        assertEquals("Deep Space Nebula", response.getTitle());
        assertEquals("https://apod.nasa.gov/apod/image/2501/nebula.jpg", response.getUrl());
        assertEquals("https://apod.nasa.gov/apod/image/2501/nebula_thumb.jpg", response.getThumbnailUrl());
        assertEquals("https://apod.nasa.gov/apod/image/2501/nebula_thumb.jpg", response.getThumbnail_url());
        assertEquals("NASA / Hubble", response.getCopyright());
    }
}
