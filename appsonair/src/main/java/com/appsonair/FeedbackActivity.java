package com.appsonair;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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

import com.appsonair.shakeBug.OnItemClickListener;
import com.appsonair.shakeBug.ShakeBugAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;
import java.util.List;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";
    private static final int PICK_IMAGE = 100;
    private final List<Uri> imageList = new ArrayList<>();
    private ShakeBugAdapter shakeBugAdapter;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
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