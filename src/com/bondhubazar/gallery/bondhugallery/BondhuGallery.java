
package com.bondhubazar.gallery.bondhugallery;

// Android Context
import android.content.Context;

// Dialog
import android.app.Dialog;
import android.view.Window;

// Android View
import android.view.View;
import android.view.Gravity;

// Layout
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

// Button
import android.widget.Button;

// Image
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

// Background
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

// Touch Gesture
import android.view.MotionEvent;

// Internet
import java.net.URL;
import java.net.HttpURLConnection;

// Thread
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
    description = "Bondhu Bazar Product Image Gallery",
    version = 1
)

public class BondhuGallery
        extends AndroidNonvisibleComponent {

    // Android Context
    private Context context;

    // Gallery Dialog
    private Dialog dialog;

    // Gallery Image
    private ImageView imageView;

    // Title
    private TextView titleView;

    // Image Counter
    private TextView counterView;

    // Image URL List
    private List<String> imageUrls = new ArrayList<>();

    // বর্তমান ছবির Index
    private int currentIndex = 0;

    // Gallery Title
    private String galleryTitle = "Product Gallery";

    // Background Color
    private int backgroundColor = Color.WHITE;

    // Swipe-এর প্রথম X Position
    private float startX = 0;

    // Swipe-এর প্রথম Y Position
    private float startY = 0;

    // Swipe Distance
    private int swipeThreshold = 80;

    // Constructor
    public BondhuGallery(ComponentContainer container) {

        super(container.$form());

        // Context নেওয়া
        context = container.$context();
    }

    // =========================================
    // ১. Gallery-এর ছবিগুলি সেট করা
    // =========================================

    @SimpleFunction(
        description = "Gallery-এর Image URL List সেট করুন"
    )
    public void SetImages(YailList urls) {

        try {

            // পুরনো Image List মুছে ফেলা
            imageUrls.clear();

            // App Inventor List থেকে URL নেওয়া
            for (Object item : urls.toArray()) {

                String url = String.valueOf(item);

                // খালি URL বাদ দেওয়া
                if (!url.trim().isEmpty()) {

                    imageUrls.add(url);
                }
            }

        } catch (Exception e) {

            ErrorOccurred(
                "SetImages: " + e.getMessage()
            );
        }
    }

    // =========================================
    // ২. Gallery Title সেট করা
    // =========================================

    @SimpleFunction(
        description = "Gallery-এর Title সেট করুন"
    )
    public void SetGalleryTitle(String title) {

        galleryTitle = title;

        // Dialog খোলা থাকলে Title Update
        if (titleView != null) {

            titleView.setText(title);
        }
    }

    // =========================================
    // ৩. Background Color সেট করা
    // =========================================

    @SimpleFunction(
        description = "Gallery Background Color সেট করুন"
    )
    public void SetBackgroundColor(String colorCode) {

        try {

            backgroundColor = Color.parseColor(colorCode);

            if (dialog != null && dialog.isShowing()) {

                dialog.getWindow()
                        .getDecorView()
                        .setBackgroundColor(backgroundColor);
            }

        } catch (Exception e) {

            ErrorOccurred(
                "SetBackgroundColor: " + e.getMessage()
            );
        }
    }

    // =========================================
    // ৪. Gallery Show করা
    // =========================================

    @SimpleFunction(
        description = "Product Image Gallery খুলুন"
    )
    public void ShowGallery() {

        try {

            // Image না থাকলে
            if (imageUrls.isEmpty()) {

                ErrorOccurred("Gallery-তে কোনো Image নেই");

                return;
            }

            // পুরনো Dialog বন্ধ
            CloseGallery();

            // Index শূন্য থেকে শুরু
            currentIndex = 0;

            // Dialog তৈরি
            dialog = new Dialog(context);

            // Dialog Title Bar বাদ
            dialog.requestWindowFeature(
                    Window.FEATURE_NO_TITLE
            );

            // Main Layout
            LinearLayout mainLayout =
                    new LinearLayout(context);

            // Vertical Layout
            mainLayout.setOrientation(
                    LinearLayout.VERTICAL
            );

            // Background Color
            mainLayout.setBackgroundColor(
                    backgroundColor
            );

            // Padding
            mainLayout.setPadding(
                    dp(12),
                    dp(12),
                    dp(12),
                    dp(12)
            );

            // =================================
            // Header Layout
            // =================================

            LinearLayout header =
                    new LinearLayout(context);

            header.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            header.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            // Title
            titleView = new TextView(context);

            titleView.setText(galleryTitle);

            titleView.setTextColor(
                    Color.parseColor("#222222")
            );

            titleView.setTextSize(18);

            titleView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );

            // Title Layout
            LinearLayout.LayoutParams titleParams =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(48),
                            1
                    );

            titleView.setLayoutParams(titleParams);

            // Close Button
            Button closeButton =
                    new Button(context);

            closeButton.setText("✕");

            closeButton.setTextSize(18);

            closeButton.setTextColor(Color.WHITE);

            closeButton.setBackgroundColor(
                    Color.parseColor("#2E7D32")
            );

            // Close Button Layout
            LinearLayout.LayoutParams closeParams =
                    new LinearLayout.LayoutParams(
                            dp(48),
                            dp(48)
                    );

            closeButton.setLayoutParams(
                    closeParams
            );

            // Header-এ Title যোগ
            header.addView(titleView);

            // Header-এ Close Button যোগ
            header.addView(closeButton);

            // Main Layout-এ Header যোগ
            mainLayout.addView(header);

            // =================================
            // Image Area
            // =================================

            FrameLayout imageFrame =
                    new FrameLayout(context);

            // ImageView
            imageView = new ImageView(context);

            // Image Fit
            imageView.setScaleType(
                    ImageView.ScaleType.FIT_CENTER
            );

            // Image Background
            imageView.setBackgroundColor(
                    backgroundColor
            );

            // Image Layout
            FrameLayout.LayoutParams imageParams =
                    new FrameLayout.LayoutParams(
                            ViewGroupSize.MATCH,
                            dp(360),
                            Gravity.CENTER
                    );

            imageView.setLayoutParams(
                    imageParams
            );

            // Frame-এ Image যোগ
            imageFrame.addView(imageView);

            // Image Area Layout
            LinearLayout.LayoutParams frameParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(360)
                    );

            frameParams.topMargin = dp(10);

            imageFrame.setLayoutParams(
                    frameParams
            );

            // Main Layout-এ Image Area যোগ
            mainLayout.addView(imageFrame);

            // =================================
            // Image Counter
            // =================================

            counterView = new TextView(context);

            counterView.setTextColor(
                    Color.parseColor("#555555")
            );

            counterView.setTextSize(14);

            counterView.setGravity(
                    Gravity.CENTER
            );

            counterView.setPadding(
                    0,
                    dp(12),
                    0,
                    dp(8)
            );

            mainLayout.addView(counterView);

            // =================================
            // Navigation Buttons
            // =================================

            LinearLayout navigation =
                    new LinearLayout(context);

            navigation.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            navigation.setGravity(
                    Gravity.CENTER
            );

            // Previous Button
            Button previousButton =
                    new Button(context);

            previousButton.setText("◀");

            // Next Button
            Button nextButton =
                    new Button(context);

            nextButton.setText("▶");

            // Button Color
            previousButton.setTextColor(Color.WHITE);

            nextButton.setTextColor(Color.WHITE);

            previousButton.setBackgroundColor(
                    Color.parseColor("#2E7D32")
            );

            nextButton.setBackgroundColor(
                    Color.parseColor("#2E7D32")
            );

            // Button Layout
            LinearLayout.LayoutParams buttonParams =
                    new LinearLayout.LayoutParams(
                            dp(100),
                            dp(48)
                    );

            buttonParams.setMargins(
                    dp(8),
                    dp(4),
                    dp(8),
                    dp(4)
            );

            previousButton.setLayoutParams(
                    buttonParams
            );

            nextButton.setLayoutParams(
                    buttonParams
            );

            // Navigation-এ Button যোগ
            navigation.addView(previousButton);

            navigation.addView(nextButton);

            // Main Layout-এ Navigation যোগ
            mainLayout.addView(navigation);

            // =================================
            // Dialog সেট করা
            // =================================

            dialog.setContentView(mainLayout);

            // Dialog Window
            Window window = dialog.getWindow();

            if (window != null) {

                // Background
                window.setBackgroundDrawableResource(
                        android.R.color.transparent
                );

                // Screen Width
                window.setLayout(
                        Window.LayoutParams.MATCH_PARENT,
                        Window.LayoutParams.WRAP_CONTENT
                );
            }

            // =================================
            // Button Click
            // =================================

            previousButton.setOnClickListener(
                    new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    PreviousImage();
                }
            });

            nextButton.setOnClickListener(
                    new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    NextImage();
                }
            });

            closeButton.setOnClickListener(
                    new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    CloseGallery();
                }
            });

            // =================================
            // Swipe Gesture
            // =================================

            imageFrame.setOnTouchListener(
                    new View.OnTouchListener() {

                @Override
                public boolean onTouch(
                        View view,
                        MotionEvent event
                ) {

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

                            // Horizontal Swipe
                            if (Math.abs(diffX)
                                    > swipeThreshold
                                    && Math.abs(diffX)
                                    > Math.abs(diffY)) {

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
            });

            // Dialog বন্ধ হলে Event
            dialog.setOnDismissListener(
                    new android.content.DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(
                        android.content.DialogInterface dialogInterface
                ) {

                    GalleryClosed();
                }
            });

            // Dialog Show
            dialog.show();

            // Dialog Size
            if (dialog.getWindow() != null) {

                dialog.getWindow().setLayout(
                        Window.LayoutParams.MATCH_PARENT,
                        Window.LayoutParams.WRAP_CONTENT
                );
            }

            // প্রথম Image Load
            LoadCurrentImage();

        } catch (Exception e) {

            ErrorOccurred(
                "ShowGallery: " + e.getMessage()
            );
        }
    }

    // =========================================
    // ৫. Next Image
    // =========================================

    @SimpleFunction(
        description = "Gallery-এর পরের Image দেখুন"
    )
    public void NextImage() {

        if (imageUrls.isEmpty()) {
            return;
        }

        // শেষ Image হলে আবার প্রথম Image
        currentIndex++;

        if (currentIndex >= imageUrls.size()) {

            currentIndex = 0;
        }

        // Image Load
        LoadCurrentImage();
    }

    // =========================================
    // ৬. Previous Image
    // =========================================

    @SimpleFunction(
        description = "Gallery-এর আগের Image দেখুন"
    )
    public void PreviousImage() {

        if (imageUrls.isEmpty()) {
            return;
        }

        // প্রথম Image হলে শেষ Image
        currentIndex--;

        if (currentIndex < 0) {

            currentIndex = imageUrls.size() - 1;
        }

        // Image Load
        LoadCurrentImage();
    }

    // =========================================
    // ৭. বর্তমান Image Load
    // =========================================

    private void LoadCurrentImage() {

        if (imageUrls.isEmpty()) {
            return;
        }

        // Current URL
        final String imageUrl =
                imageUrls.get(currentIndex);

        // Counter Update
        if (counterView != null) {

            counterView.setText(
                    (currentIndex + 1)
                    + " / "
                    + imageUrls.size()
            );
        }

        // Image Download Thread
        new Thread(
                new Runnable() {

            @Override
            public void run() {

                HttpURLConnection connection = null;

                try {

                    // URL
                    URL url = new URL(imageUrl);

                    // Connection
                    connection =
                            (HttpURLConnection)
                                    url.openConnection();

                    // Timeout
                    connection.setConnectTimeout(15000);

                    connection.setReadTimeout(15000);

                    connection.setRequestMethod("GET");

                    connection.connect();

                    // Bitmap Decode
                    final Bitmap bitmap =
                            BitmapFactory.decodeStream(
                                    connection.getInputStream()
                            );

                    // UI Update
                    if (imageView != null
                            && bitmap != null) {

                        imageView.post(
                                new Runnable() {

                            @Override
                            public void run() {

                                if (imageView != null) {

                                    imageView.setImageBitmap(
                                            bitmap
                                    );

                                    ImageChanged(
                                            currentIndex + 1,
                                            imageUrls.size()
                                    );
                                }
                            }
                        });
                    }

                } catch (Exception e) {

                    ErrorOccurred(
                            "LoadCurrentImage: "
                            + e.getMessage()
                    );

                } finally {

                    if (connection != null) {

                        connection.disconnect();
                    }
                }
            }

        }).start();
    }

    // =========================================
    // ৮. Gallery বন্ধ করা
    // =========================================

    @SimpleFunction(
        description = "Gallery Dialog বন্ধ করুন"
    )
    public void CloseGallery() {

        if (dialog != null
                && dialog.isShowing()) {

            dialog.dismiss();
        }

        dialog = null;

        imageView = null;

        titleView = null;

        counterView = null;
    }

    // =========================================
    // ৯. Image Changed Event
    // =========================================

    @SimpleEvent(
        description = "Image পরিবর্তন হলে Event চালু হবে"
    )
    public void ImageChanged(
            int imageNum
