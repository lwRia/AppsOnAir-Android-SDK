package com.appsonair;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.StorageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsonair.model.DeviceInfo;
import com.appsonair.shakeBug.OnItemClickListener;
import com.appsonair.shakeBug.ShakeBugAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";
    private static final int PICK_IMAGE = 100;
    private final List<Uri> imageList = new ArrayList<>();
    private ShakeBugAdapter shakeBugAdapter;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private float batteryLevel;
    private final BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryLevel = level * 100 / (float) scale;
            Log.d(TAG, "getDeviceInfo: Battery level : " + batteryLevel);
        }
    };
    private String screenOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        ShakeBugService.Companion companion1 = ShakeBugService.Companion;
        Log.d(TAG, "testInitialMap :::::::" + companion1.getExtraPayload());
        //init views
        LinearLayout linearLayout = findViewById(R.id.ll_main);
        LinearLayout llAppbar = findViewById(R.id.ll_appbar);

        TextView tvAppbarTitle = findViewById(R.id.tv_appbar_title);
        TextView tvTicketType = findViewById(R.id.tv_ticket_type);

        TextView tvDescription = findViewById(R.id.tv_description);
        TextInputEditText etDescription = findViewById(R.id.et_description);
        TextInputLayout tilDescription = findViewById(R.id.til_description);

        PowerSpinnerView spinner = findViewById(R.id.sp_ticket_type);
        Button btnSubmit = findViewById(R.id.btn_submit);
        ImageView imgClose = findViewById(R.id.img_close);
        ImageView imgAdd = findViewById(R.id.img_add);

        RecyclerView recyclerView = findViewById(R.id.rv_image);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        shakeBugAdapter = new ShakeBugAdapter(imageList, new OnItemClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemClick(int position) {
                imageList.remove(position);
                if (imageList.size() < 2) {
                    imgAdd.setVisibility(View.VISIBLE);
                }
                shakeBugAdapter.notifyDataSetChanged();
            }
        });
        recyclerView.setAdapter(shakeBugAdapter);

        //set view properties
        ShakeBugService.Companion companion = ShakeBugService.Companion;
        linearLayout.setBackgroundColor(parseColorToInteger(companion.getPageBackgroundColor()));

        llAppbar.setBackgroundColor(parseColorToInteger(companion.getAppbarBackgroundColor()));
        tvAppbarTitle.setText(companion.getAppbarTitleText());
        tvAppbarTitle.setTextColor(parseColor(companion.getAppbarTitleColor()));

        tvTicketType.setText(companion.getTicketTypeLabelText());
        tvTicketType.setTextColor(parseColor(companion.getLabelColor()));

        tvDescription.setText(companion.getDescriptionLabelText());
        tvDescription.setTextColor(parseColor(companion.getLabelColor()));
        etDescription.setTextColor(parseColor(companion.getInputTextColor()));
        tilDescription.setCounterMaxLength(companion.getDescriptionMaxLength());
        tilDescription.setCounterTextColor(parseColor(companion.getLabelColor()));
        tilDescription.setPlaceholderText(companion.getDescriptionHintText());
        tilDescription.setPlaceholderTextColor(parseColor(companion.getHintColor()));

        btnSubmit.setText(companion.getButtonText());
        btnSubmit.setTextColor(parseColor(companion.getButtonTextColor()));
        btnSubmit.setBackgroundTintList(parseColor(companion.getButtonBackgroundColor()));

        // Retrieve image path from Intent extras
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("IMAGE_PATH")) {
            Uri imagePath = intent.getParcelableExtra("IMAGE_PATH");
            if (imagePath != null) {
                imageList.add(imagePath);
                shakeBugAdapter.notifyItemInserted(imageList.size() - 1);
            }
        }

        imgClose.setOnClickListener(view -> onBackPressed());

        imgAdd.setOnClickListener(view -> openGallery());

        spinner.selectItemByIndex(0);
        spinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {

            }
        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedImage = result.getData().getData();
                imageList.add(selectedImage);
                if (imageList.size() > 1) {
                    imgAdd.setVisibility(View.GONE);
                }
                shakeBugAdapter.notifyItemInserted(imageList.size() - 1);
            }
        });

        btnSubmit.setOnClickListener(view -> {
            String description = etDescription.getText().toString().trim();
            if (description.isEmpty()) {
                etDescription.setError(getResources().getString(R.string.description_required));
            } else {
                hideKeyboard();
                etDescription.setError(null);
            }
        });

        getDeviceInfo();
    }

    private void getDeviceInfo() {
        try {
            PackageManager packageManager = getPackageManager();
            String packageName = getPackageName();

            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            String appName = (String) packageManager.getApplicationLabel(applicationInfo);

            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;

            Log.d(TAG, "getDeviceInfo: Version name : " + versionName);
            Log.d(TAG, "getDeviceInfo: Version code : " + versionCode);
            Log.d(TAG, "getDeviceInfo: App name : " + appName);
            Log.d(TAG, "getDeviceInfo: Package name : " + packageName);

            Log.d(TAG, "getDeviceInfo: Model : " + Build.DEVICE);
            Log.d(TAG, "getDeviceInfo: Manufacture : " + Build.MANUFACTURER);
            Log.d(TAG, "getDeviceInfo: Brand : " + Build.BRAND);
            Log.d(TAG, "getDeviceInfo: Release : " + Build.VERSION.RELEASE);
            Log.d(TAG, "getDeviceInfo: SDK version : " + Build.VERSION.SDK_INT);

            Locale locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = getResources().getConfiguration().locale;
            }

            Log.d(TAG, "getDeviceInfo: Country name : " + locale.getDisplayCountry());
            Log.d(TAG, "getDeviceInfo: Country code : " + locale.getCountry());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date now = new Date();

            Log.d(TAG, "getDeviceInfo: Current timestamp : " + sdf.format(now));

            this.registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            int screenWidth, screenHeight;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
                WindowInsets insets = windowMetrics.getWindowInsets();

                int insetsLeft = insets.getInsets(WindowInsets.Type.systemBars()).left;
                int insetsRight = insets.getInsets(WindowInsets.Type.systemBars()).right;
                int insetsTop = insets.getInsets(WindowInsets.Type.systemBars()).top;
                int insetsBottom = insets.getInsets(WindowInsets.Type.systemBars()).bottom;

                screenWidth = windowMetrics.getBounds().width() - insetsLeft - insetsRight;
                screenHeight = windowMetrics.getBounds().height() - insetsTop - insetsBottom;

                Log.d(TAG, "getDeviceInfo: Width : " + screenWidth + " pixels");
                Log.d(TAG, "getDeviceInfo: Height : " + screenHeight + " pixels");
            } else {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                screenWidth = displayMetrics.widthPixels;
                screenHeight = displayMetrics.heightPixels;

                Log.d(TAG, "getDeviceInfo: Width : " + screenWidth + " pixels");
                Log.d(TAG, "getDeviceInfo: Height : " + screenHeight + " pixels");
            }

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                screenOrientation = "Portrait";
                Log.d(TAG, "getDeviceInfo: Orientation : " + screenOrientation);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                screenOrientation = "Landscape";
                Log.d(TAG, "getDeviceInfo: Orientation : " + screenOrientation);
            }

            String usedStorage = getReadableStorageSize(getTotalStorageSize(this, false));
            Log.d(TAG, "getDeviceInfo: Used storage : " + usedStorage);
            String totalStorage = getReadableStorageSize(getTotalStorageSize(this, true));
            Log.d(TAG, "getDeviceInfo: Total storage : " + totalStorage);
            String totalMemory = getReadableStorageSize(getAvailableMemory().totalMem);
            Log.d(TAG, "getDeviceInfo: Total memory : " + totalMemory);
            String availableMemory = getReadableStorageSize(getAvailableMemory().availMem);
            Log.d(TAG, "getDeviceInfo: Available memory : " + availableMemory);

            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
            String appMemoryUsage = "";
            for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcesses) {
                if (processInfo.processName.equals(packageName)) {
                    android.os.Debug.MemoryInfo[] memoryInfoArray =
                            activityManager.getProcessMemoryInfo(new int[]{processInfo.pid});
                    android.os.Debug.MemoryInfo memoryInfo = memoryInfoArray[0];
                    int totalPss = memoryInfo.getTotalPss();
                    appMemoryUsage = getReadableStorageSize((long) totalPss * 1024);
                    Log.d(TAG, "getDeviceInfo: AppMemoryUsage : " + appMemoryUsage);
                    break;
                }
            }

            String libVersionName = com.appsonair.BuildConfig.VERSION_NAME;
            String libVersionCode = com.appsonair.BuildConfig.VERSION_CODE;
            Log.d(TAG, "getDeviceInfo: AppsOnAirVersion : " + libVersionName);
            Log.d(TAG, "getDeviceInfo: AppsOnAirVersion : " + libVersionCode);

            DeviceInfo deviceInfo = new DeviceInfo.Builder()
                    .setDeviceModel(Build.BRAND + " " + Build.MODEL)
                    .setDeviceOsVersion(Build.VERSION.RELEASE)
                    .setDeviceBatteryLevel(String.valueOf(batteryLevel))
                    .setDeviceScreenSize(screenWidth + "X" + screenHeight + " px")
                    .setDeviceOrientation(screenOrientation)
                    .setEnvironment("Development")
                    .setDeviceRegionCode(locale.getCountry())
                    .setDeviceRegionName(locale.getDisplayCountry())
                    .setTimestamp(sdf.format(now))
                    .setBuildVersionNumber(String.valueOf(versionCode))
                    .setReleaseVersionNumber(versionName)
                    .setBundleIdentifier(packageName)
                    .setAppName(appName)
                    .setDeviceUsedStorage(usedStorage)
                    .setDeviceTotalStorage(totalStorage)
                    .setDeviceMemory(totalMemory)
                    .setAppMemoryUsage(appMemoryUsage)
                    .setAppsOnAirSDKVersion(libVersionName)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ActivityManager.MemoryInfo getAvailableMemory() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    public String getReadableStorageSize(long size) {
        if (size <= 0) return "0";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public long getTotalStorageSize(Context context, boolean getTotalStorage) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
                UUID uuid = storageManager.getUuidForPath(Environment.getDataDirectory());
                long totalBytes = storageStatsManager.getTotalBytes(uuid);
                long freeBytes = storageStatsManager.getFreeBytes(uuid);
                long usedBytes = totalBytes - freeBytes;
                return getTotalStorage ? totalBytes : usedBytes;
            } catch (IOException e) {
                return 0L;
            }
        } else {
            return 0L;
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Permission granted!");
            }
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private ColorStateList parseColor(String color) {
        return ColorStateList.valueOf(Color.parseColor(color));
    }

    private Integer parseColorToInteger(String color) {
        return Color.parseColor(color);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}