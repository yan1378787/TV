package com.fongmi.android.tv.ui.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.ApiConfig;
import com.fongmi.android.tv.api.LiveConfig;
import com.fongmi.android.tv.api.WallConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.databinding.DialogConfigBinding;
import com.fongmi.android.tv.impl.ConfigCallback;
import com.fongmi.android.tv.ui.custom.CustomTextListener;
import com.fongmi.android.tv.utils.FileChooser;
import com.fongmi.android.tv.utils.UrlUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ConfigDialog {

    private final DialogConfigBinding binding;
    private final ConfigCallback callback;
    private final Fragment fragment;
    private AlertDialog dialog;
    private boolean append;
    private boolean edit;
    private String url;
    private int type;

    public static ConfigDialog create(Fragment fragment) {
        return new ConfigDialog(fragment);
    }

    public ConfigDialog type(int type) {
        this.type = type;
        return this;
    }

    public ConfigDialog edit() {
        this.edit = true;
        return this;
    }

    public ConfigDialog(Fragment fragment) {
        this.fragment = fragment;
        this.callback = (ConfigCallback) fragment;
        this.binding = DialogConfigBinding.inflate(LayoutInflater.from(fragment.getContext()));
        this.append = true;
    }

    public void show() {
        initDialog();
        initView();
        initEvent();
    }

    private void initDialog() {
        dialog = new MaterialAlertDialogBuilder(binding.getRoot().getContext()).setTitle(type == 0 ? R.string.setting_vod : type == 1 ? R.string.setting_live : R.string.setting_wall).setView(binding.getRoot()).setPositiveButton(edit ? R.string.dialog_edit : R.string.dialog_positive, this::onPositive).setNegativeButton(R.string.dialog_negative, this::onNegative).create();
        dialog.getWindow().setDimAmount(0);
        dialog.show();
    }

    private void initView() {
        binding.text.setText(url = getUrl());
        binding.input.setEndIconOnClickListener(this::onChoose);
        binding.text.setSelection(TextUtils.isEmpty(url) ? 0 : url.length());
    }

    private void initEvent() {
        binding.text.addTextChangedListener(new CustomTextListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                detect(s.toString());
            }
        });
        binding.text.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            return true;
        });
    }

    private String getUrl() {
        switch (type) {
            case 0:
                return ApiConfig.getUrl();
            case 1:
                return LiveConfig.getUrl();
            case 2:
                return WallConfig.getUrl();
            default:
                return "";
        }
    }

    private void onChoose(View view) {
        FileChooser.from(fragment).show();
        dialog.dismiss();
    }

    private void detect(String s) {
        if (append && s.equalsIgnoreCase("h")) {
            append = false;
            binding.text.append("ttp://");
        } else if (append && s.equalsIgnoreCase("f")) {
            append = false;
            binding.text.append("ile://");
        } else if (append && s.equalsIgnoreCase("a")) {
            append = false;
            binding.text.append("ssets://");
        } else if (s.length() > 1) {
            append = false;
        } else if (s.length() == 0) {
            append = true;
        }
    }

    private void onPositive(DialogInterface dialog, int which) {
        String text = UrlUtil.fixUrl(binding.text.getText().toString().trim());
        if (edit) Config.find(url, type).url(text).update();
        if (text.isEmpty()) Config.delete(url, type);
        callback.setConfig(Config.find(text, type));
        dialog.dismiss();
    }

    private void onNegative(DialogInterface dialog, int which) {
        dialog.dismiss();
    }
}
