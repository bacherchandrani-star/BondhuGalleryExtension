package com.bondhubazar.gallery.bondhugallery;

// Android Context এবং UI
import android.content.Context;
import android.content.DialogInterface;
import android.app.Dialog;
import android.view.Window;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
// Android Layout এবং Widget
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

// Android Graphics
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

// Background Thread এবং Main Thread
import android.os.Handler;
import android.os.Looper;

// Internet
import java.net.URL;
import java.net.HttpURLConnection;

// List
import java.util.ArrayList;
import java.util.List;

// App Inventor
import com.google.appinventor.components.annotations.Extension;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailList;

@Extension(
    description = "Bondhu Bazar Product Image Gallery"
)
public class BondhuGallery extends AndroidNonvisibleComponent {

    // Android Context
    private final Context context;

    // Main Thread Handler
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    // Gallery Dialog এবং UI Views
    private Dialog dialog;
    private LinearLayout mainLayout;
    private ImageView imageView;
    private TextView titleView;
    private TextView counterView;

    // ছবির URL List
    private final List<String> imageUrls =
            new ArrayList<String>();

    // বর্তমানে যে ছবিটি দেখানো হচ্ছে তার Index
    private int currentIndex = 0;

    // Gallery Title
    private String galleryTitle = "Product Gallery";

    // Gallery Background Color
    private int backgroundColor = Color.WHITE;

    // Swipe Gesture-এর অবস্থান
    private float startX = 0;
    private float startY = 0;
    private int swipeThreshold = 80;

    // পুরনো Image Download Request বাতিল/উপেক্ষা করার Counter
    private int imageRequestId = 0;

    // Constructor
    public BondhuGallery(ComponentContainer container) {
        super(container.$form());
        context = container.$context();
    }

    // =========================================
    // ১. Gallery-এর ছবির URL সেট করা
    // =========================================
    @SimpleFunction(description = "Gallery-এর Image URL List সেট করুন")
    public void SetImages(YailList urls) {
        try {
            imageUrls.clear();

            if (urls == null) {
                ErrorOccurred("SetImages: Image List খালি");
                return;
            }

            Object[] items = urls.toArray();

            for (Object item : items) {
                if (item == null) {
                    continue;
                }

                String url = String.valueOf(item).trim();

                if (!url.isEmpty()) {
                    imageUrls.add(url);
                }
            }

            currentIndex = 0;

        } catch (Exception e) {
            ErrorOccurred("SetImages: " + getErrorMessage(e));
        }
    }

    // =========================================
    // ২. Gallery Title সেট করা
    // =========================================
    @SimpleFunction(description = "Gallery-এর Title সেট করুন")
    public void SetGalleryTitle(String title) {
        galleryTitle = (title == null) ? "" : title;

        if (titleView != null) {
            titleView.setText(galleryTitle);
        }
    }

    // =========================================
    // ৩. Background Color সেট করা
    // =========================================
    @SimpleFunction(description = "Gallery Background Color সেট করুন, যেমন #FFFFFF")
    public void SetBackgroundColor(String colorCode) {
        try {
            backgroundColor = Color.parseColor(colorCode);

            if (mainLayout != null) {
                mainLayout.setBackgroundColor(backgroundColor);
            }

            if (imageView != null) {
                imageView.setBackgroundColor(backgroundColor);
            }

        } catch (Exception e) {
            ErrorOccurred("SetBackgroundColor: " + getErrorMessage(e));
        }
    }

    // =========================================
    // ৪. Swipe-এর Minimum Distance সেট করা
    // =========================================
    @SimpleFunction(description = "Swipe করার Minimum Distance DP-তে সেট করুন")
    public void SetSwipeThreshold(int distance) {
        if (distance > 0) {
            swipeThreshold = distance;
        }
    }

    // =========================================
    // ৫. Gallery খুলুন
    // =========================================
    @SimpleFunction(description = "Product Image Gallery খুলুন")
    public void ShowGallery() {
        try {
            if (imageUrls.isEmpty()) {
                ErrorOccurred("Gallery-তে কোনো Image URL নেই");
                return;
            }

            // আগের Gallery খোলা থাকলে বন্ধ করুন
            CloseGallery();

            currentIndex = 0;

            dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            // Main Layout
            mainLayout = new LinearLayout(context);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setPadding(dp(12), dp(12), dp(12), dp(12));
            mainLayout.setBackgroundColor(backgroundColor);

            // -------------------------------------
            // Header
            // -------------------------------------
            LinearLayout header = new LinearLayout(context);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);

            titleView = new TextView(context);
            titleView.setText(galleryTitle);
            titleView.setTextColor(Color.parseColor("#222222"));
            titleView.setTextSize(18);
            titleView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );

            LinearLayout.LayoutParams titleParams =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(48),
                            1
                    );
            titleView.setLayoutParams(titleParams);

            Button closeButton = new Button(context);
            closeButton.setText("✕");
            closeButton.setTextSize(18);
            closeButton.setTextColor(Color.WHITE);
            closeButton.setBackgroundColor(Color.parseColor("#2E7D32"));

            LinearLayout.LayoutParams closeParams =
                    new LinearLayout.LayoutParams(dp(48), dp(48));
            closeButton.setLayoutParams(closeParams);

            header.addView(titleView);
            header.addView(closeButton);
            mainLayout.addView(header);

            // -------------------------------------
            // Image Area
            // -------------------------------------
            FrameLayout imageFrame = new FrameLayout(context);

            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setBackgroundColor(backgroundColor);

            FrameLayout.LayoutParams imageParams =
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dp(360),
                            Gravity.CENTER
                    );
            imageView.setLayoutParams(imageParams);

            imageFrame.addView(imageView);

            LinearLayout.LayoutParams frameParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(360)
                    );
            frameParams.topMargin = dp(10);
            imageFrame.setLayoutParams(frameParams);

            mainLayout.addView(imageFrame);

            // -------------------------------------
            // Image Counter
            // -------------------------------------
            counterView = new TextView(context);
            counterView.setTextColor(Color.parseColor("#555555"));
            counterView.setTextSize(14);
            counterView.setGravity(Gravity.CENTER);
            counterView.setPadding(0, dp(12), 0, dp(8));
            mainLayout.addView(counterView);

            // -------------------------------------
            // Previous এবং Next Button
            // -------------------------------------
            LinearLayout navigation = new LinearLayout(context);
            navigation.setOrientation(LinearLayout.HORIZONTAL);
            navigation.setGravity(Gravity.CENTER);

            Button previousButton = new Button(context);
            previousButton.setText("◀");

            Button nextButton = new Button(context);
            nextButton.setText("▶");

            previousButton.setTextColor(Color.WHITE);
            nextButton.setTextColor(Color.WHITE);

            previousButton.setBackgroundColor(Color.parseColor("#2E7D32"));
            nextButton.setBackgroundColor(Color.parseColor("#2E7D32"));

            LinearLayout.LayoutParams buttonParams =
                    new LinearLayout.LayoutParams(dp(100), dp(48));
            buttonParams.setMargins(dp(8), dp(4), dp(8), dp(4));

            previousButton.setLayoutParams(buttonParams);
            nextButton.setLayoutParams(buttonParams);

            navigation.addView(previousButton);
            navigation.addView(nextButton);
            mainLayout.addView(navigation);

            // Dialog-এ Layout বসানো
            dialog.setContentView(mainLayout);

            // -------------------------------------
            // Button Click Listener
            // -------------------------------------
            previousButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PreviousImage();
                }
            });

            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NextImage();
                }
            });

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CloseGallery();
                }
            });

            // -------------------------------------
            // Swipe Listener
            // -------------------------------------
            View.OnTouchListener swipeListener =
                    new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            startY = event.getY();
                            return true;

                        case MotionEvent.ACTION_UP:
                            float endX = event.getX();
                            float endY = event.getY();

                            float diffX = endX - startX;
                            float diffY = endY - startY;

                            if (Math.abs(diffX) > swipeThreshold
                                    && Math.abs(diffX) > Math.abs(diffY)) {
                                if (diffX < 0) {
                                    NextImage();
                                } else {
                                    PreviousImage();
                                }
                            }
                            return true;
                    }
                    return true;
                }
            };

            imageFrame.setOnTouchListener(swipeListener);
            imageView.setOnTouchListener(swipeListener);

            // -------------------------------------
            // Dialog বন্ধ হওয়ার Event
            // -------------------------------------
            dialog.setOnDismissListener(
                    new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    imageRequestId++;
                    imageView = null;
                    titleView = null;
                    counterView = null;
                    mainLayout = null;
                    dialog = null;
                    GalleryClosed();
                }
            });

            // Dialog Show
            dialog.show();

            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(
                        android.R.color.transparent
                );
              window.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
                );
            }

            // প্রথম ছবি Load
            LoadCurrentImage();

        } catch (Exception e) {
            ErrorOccurred("ShowGallery: " + getErrorMessage(e));
        }
    }

    // =========================================
    // ৬. পরের ছবি দেখুন
    // =========================================
    @SimpleFunction(description = "Gallery-এর পরের Image দেখুন")
    public void NextImage() {
        if (imageUrls.isEmpty()) {
            return;
        }

        currentIndex++;

        if (currentIndex >= imageUrls.size()) {
            currentIndex = 0;
        }

        LoadCurrentImage();
    }

    // =========================================
    // ৭. আগের ছবি দেখুন
    // =========================================
    @SimpleFunction(description = "Gallery-এর আগের Image দেখুন")
    public void PreviousImage() {
        if (imageUrls.isEmpty()) {
            return;
        }

        currentIndex--;

        if (currentIndex < 0) {
            currentIndex = imageUrls.size() - 1;
        }

        LoadCurrentImage();
    }

    // =========================================
    // ৮. বর্তমান ছবিটি Download এবং Show করা
    // =========================================
    private void LoadCurrentImage() {
        if (imageUrls.isEmpty() || imageView == null) {
            return;
        }

        final String imageUrl = imageUrls.get(currentIndex);
        final int requestedIndex = currentIndex;
        final int requestId = ++imageRequestId;
        final ImageView targetImageView = imageView;

        // Counter Update
        if (counterView != null) {
            counterView.setText(
                    (requestedIndex + 1) + " / " + imageUrls.size()
            );
        }

        // নতুন ছবি আসা পর্যন্ত আগের ছবি সরানো
        targetImageView.setImageDrawable(null);

        // Background Thread-এ Internet থেকে ছবি Download
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                Bitmap bitmap = null;
                String errorMessage = null;

                try {
                    URL url = new URL(imageUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.connect();

                    int responseCode = connection.getResponseCode();

                    if (responseCode >= 200 && responseCode < 300) {
                        bitmap = BitmapFactory.decodeStream(
                                connection.getInputStream()
                        );
                    } else {
                        errorMessage = "HTTP Error: " + responseCode;
                    }

                    if (bitmap == null && errorMessage == null) {
                        errorMessage = "ছবিটি Decode করা যায়নি";
                    }

                } catch (Exception e) {
                    errorMessage = getErrorMessage(e);

                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

                final Bitmap finalBitmap = bitmap;
                final String finalError = errorMessage;

                // Main/UI Thread-এ Result দেখানো
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // এর মধ্যে নতুন Request এলে পুরনো Result বাদ
                        if (requestId != imageRequestId
                                || imageView != targetImageView
                                || dialog == null
                                || !dialog.isShowing()) {
                            return;
                        }

                        if (finalBitmap != null) {
                            targetImageView.setImageBitmap(finalBitmap);

                            ImageChanged(
                                    requestedIndex + 1,
                                    imageUrls.size()
                            );
                        } else {
                            ErrorOccurred(
                                    "LoadCurrentImage: " + finalError
                            );
                        }
                    }
                });
            }
        }).start();
    }

    // =========================================
    // ৯. Gallery বন্ধ করা
    // =========================================
    @SimpleFunction(description = "Gallery Dialog বন্ধ করুন")
    public void CloseGallery() {
        // চলমান Download-এর পুরনো Result আর দেখানো হবে না
        imageRequestId++;

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else {
            dialog = null;
            imageView = null;
            titleView = null;
            counterView = null;
            mainLayout = null;
        }
    }

    // =========================================
    // ১০. মোট ছবির সংখ্যা জানুন
    // =========================================
    @SimpleFunction(description = "Gallery-তে মোট কতগুলি ছবি আছে তা জানুন")
    public int GetImageCount() {
        return imageUrls.size();
    }

    // =========================================
    // ১১. বর্তমান ছবির নম্বর জানুন
    // =========================================
    @SimpleFunction(description = "বর্তমানে কোন নম্বর ছবি দেখা হচ্ছে তা জানুন")
    public int GetCurrentImageNumber() {
        return currentIndex + 1;
    }

    // =========================================
    // ১২. Image Changed Event
    // =========================================
    @SimpleEvent(description = "Image পরিবর্তন হলে Event চালু হবে")
    public void ImageChanged(int imageNum, int totalImages) {
        EventDispatcher.dispatchEvent(
                this,
                "ImageChanged",
                imageNum,
                totalImages
        );
    }

    // =========================================
    // ১৩. Gallery Closed Event
    // =========================================
    @SimpleEvent(description = "Gallery বন্ধ হলে Event চালু হবে")
    public void GalleryClosed() {
        EventDispatcher.dispatchEvent(
                this,
                "GalleryClosed"
        );
    }

    // =========================================
    // ১৪. Error Event
    // =========================================
    @SimpleEvent(description = "কোনো Error হলে Event চালু হবে")
    public void ErrorOccurred(String message) {
        EventDispatcher.dispatchEvent(
                this,
                "ErrorOccurred",
                message
        );
    }

    // =========================================
    // ১৫. DP-কে Pixel-এ Convert করা
    // =========================================
    private int dp(float value) {
        return (int) (
                value * context.getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    // =========================================
    // ১৬. Exception-এর Message নেওয়া
    // =========================================
    private String getErrorMessage(Exception e) {
        if (e == null) {
            return "Unknown error";
        }

        String message = e.getMessage();

        if (message == null || message.trim().isEmpty()) {
            return e.getClass().getSimpleName();
        }

        return message;
    }
}
