package com.example.nasaapod;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // UI elements
    private ImageView apodImage;
    private TextView titleText;
    private TextView dateText;
    private TextView explanationText;
    private TextView copyrightText;
    private ProgressBar progressBar;
    private Button dateButton;
    private Button shareButton;

    // Current APOD information
    private String currentImageUrl = "";
    private String currentTitle = "";

    // NASA API key
    // Replace DEMO_KEY with your own NASA API key from https://api.nasa.gov if rate limited.
    private static final String API_KEY = "Wle6E0jIcJ2ztdGGaNwB8pHVNNbbsVuC94Ud0n4n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect this activity with activity_main.xml
        setContentView(R.layout.activity_main);

        // Find UI elements
        apodImage = findViewById(R.id.apodImage);
        titleText = findViewById(R.id.titleText);
        dateText = findViewById(R.id.dateText);
        explanationText = findViewById(R.id.explanationText);
        copyrightText = findViewById(R.id.copyrightText);
        progressBar = findViewById(R.id.progressBar);
        dateButton = findViewById(R.id.dateButton);
        shareButton = findViewById(R.id.shareButton);

        // Load latest available APOD when app starts (passing null fetches the most recent APOD)
        loadApod(null);

        // Date button
        dateButton.setOnClickListener(v -> openDatePicker());

        // Share button
        shareButton.setOnClickListener(v -> shareApod());
    }

    /**
     * Opens the Android DatePicker.
     */
    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                MainActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Convert selected date into NASA's format: YYYY-MM-DD
                    String selectedDate = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                    );

                    // Load APOD for selected date
                    loadApod(selectedDate);
                },
                year,
                month,
                day
        );

        // Don't allow the user to select a future date
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /**
     * Loads APOD data from NASA API.
     * @param date Date in YYYY-MM-DD format, or null to load the latest APOD.
     */
    private void loadApod(String date) {
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);

        // Disable buttons while loading
        dateButton.setEnabled(false);
        shareButton.setEnabled(false);

        // Create API service
        ApiService apiService =
                RetrofitClient.getRetrofit().create(ApiService.class);

        // Make API request
        Call<ApodResponse> call = apiService.getApod(API_KEY, date, true);

        call.enqueue(new Callback<ApodResponse>() {
            @Override
            public void onResponse(
                    Call<ApodResponse> call,
                    Response<ApodResponse> response) {

                if (isDestroyed() || isFinishing()) {
                    return;
                }

                // Hide loading indicator
                progressBar.setVisibility(View.GONE);

                // Enable buttons
                dateButton.setEnabled(true);
                shareButton.setEnabled(true);

                // Check if request succeeded
                if (response.isSuccessful() && response.body() != null) {
                    ApodResponse apod = response.body();

                    // Save current information for sharing
                    currentTitle = apod.getTitle() != null ? apod.getTitle() : "";

                    // Set title
                    titleText.setText(currentTitle);

                    // Set date
                    dateText.setText(formatDate(apod.getDate()));

                    // Set explanation
                    explanationText.setText(apod.getExplanation() != null ? apod.getExplanation() : "");

                    // Set copyright
                    if (apod.getCopyright() != null && !apod.getCopyright().trim().isEmpty()) {
                        copyrightText.setVisibility(View.VISIBLE);
                        copyrightText.setText("© " + apod.getCopyright().trim());
                    } else {
                        copyrightText.setVisibility(View.GONE);
                    }

                    String imageUrl;
                    if ("image".equalsIgnoreCase(apod.getMediaType())) {
                        imageUrl = apod.getUrl();
                    } else {
                        imageUrl = apod.getThumbnailUrl();
                        if (imageUrl == null || imageUrl.isEmpty()) {
                            imageUrl = apod.getUrl();
                        }
                    }

                    currentImageUrl = imageUrl != null ? imageUrl : "";

                    // Load image using Glide
                    if (!currentImageUrl.isEmpty()) {
                        Glide.with(MainActivity.this)
                                .load(currentImageUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_dialog_alert)
                                .into(apodImage);
                    } else {
                        apodImage.setImageResource(android.R.drawable.ic_dialog_alert);
                    }

                } else {
                    // API returned an error - extract message from NASA response
                    String errorMessage = "NASA could not load this APOD.";
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            if (errorJson.contains("\"msg\"")) {
                                JSONObject jsonObject = new JSONObject(errorJson);
                                if (jsonObject.has("msg")) {
                                    errorMessage = jsonObject.getString("msg");
                                }
                            } else if (errorJson.contains("\"error\"")) {
                                JSONObject jsonObject = new JSONObject(errorJson);
                                if (jsonObject.has("error")) {
                                    JSONObject errorObj = jsonObject.getJSONObject("error");
                                    if (errorObj.has("message")) {
                                        errorMessage = errorObj.getString("message");
                                    }
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }

                    if (response.code() == 429) {
                        errorMessage = "Rate limit exceeded for DEMO_KEY. Please try again later or use your own API key.";
                    }

                    Toast.makeText(
                            MainActivity.this,
                            errorMessage,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<ApodResponse> call,
                    Throwable t) {

                if (isDestroyed() || isFinishing()) {
                    return;
                }

                // Hide loading indicator
                progressBar.setVisibility(View.GONE);

                // Enable buttons
                dateButton.setEnabled(true);
                shareButton.setEnabled(true);

                // Show error
                Toast.makeText(
                        MainActivity.this,
                        "Network error. Please check your internet connection.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    /**
     * Converts:
     * 2026-09-27
     * into:
     * SEPTEMBER 27, 2026
     */
    private String formatDate(String date) {
        if (date == null || date.length() != 10) {
            return date != null ? date : "";
        }

        try {
            String[] parts = date.split("-");

            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, day);

            String monthName = calendar.getDisplayName(
                    Calendar.MONTH,
                    Calendar.LONG,
                    Locale.US
            );

            return (monthName != null ? monthName.toUpperCase(Locale.US) : "")
                    + " "
                    + day
                    + ", "
                    + year;

        } catch (Exception e) {
            return date;
        }
    }

    /**
     * Shares the current APOD.
     */
    private void shareApod() {
        // Make sure APOD has loaded
        if (currentImageUrl == null || currentImageUrl.isEmpty()) {
            Toast.makeText(
                    MainActivity.this,
                    "Please wait for the APOD to load.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Create sharing text
        String shareText =
                "NASA Astronomy Picture of the Day\n\n"
                        + currentTitle
                        + "\n\n"
                        + currentImageUrl;

        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        // Open Android share menu
        Intent chooser = Intent.createChooser(shareIntent, "Share NASA APOD");
        startActivity(chooser);
    }
}
