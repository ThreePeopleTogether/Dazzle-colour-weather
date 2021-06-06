package com.threepeople.dazzlecolourweather.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.threepeople.dazzlecolourweather.R;
import com.threepeople.dazzlecolourweather.theme.BlackUITheme;
import com.threepeople.dazzlecolourweather.theme.ColorfulDayUITheme;
import com.threepeople.dazzlecolourweather.theme.ColorfulNightUITheme;
import com.threepeople.dazzlecolourweather.theme.CustomDayUITheme;
import com.threepeople.dazzlecolourweather.theme.CustomNightUITheme;
import com.threepeople.dazzlecolourweather.theme.UITheme;
import com.threepeople.dazzlecolourweather.theme.WhiteUITheme;
import com.threepeople.dazzlecolourweather.utils.Tools;
import com.threepeople.dazzlecolourweather.view.ColorChangeableIconView;
import com.threepeople.dazzlecolourweather.view.ProgressView;
import com.threepeople.dazzlecolourweather.view.RoundAngleLinearLayout;

import top.defaults.colorpicker.ColorPickerView;

public class SettingThemeActivity extends AppCompatActivity {
    private final AppCompatActivity activity;
    private final int toSystemPicAlbumCode;
    private final int toCutPictureCode;
    private boolean hasEditCustom;
    private boolean customMode = false;
    private UITheme oldTheme = null;
    private UITheme nowTheme;
    private UITheme hideTheme;

    private AlertDialog colorSelectDialog = null;
    private View colorSelectView = null;
    private AlertDialog backgroundSourceDialog = null;

    private ViewGroup settingThemeLayout;
    private ColorChangeableIconView returnHome;
    private TextView settingThemeTitle;
    private RadioGroup selectTheme;
    private TextView tipText;
    private ColorChangeableIconView previewLast;
    private ColorChangeableIconView previewNext;
    private TextView dayText;
    private TextView nightText;
    private LinearLayout addPreview;
    private View preview = null;

    {
        activity = this;
        toSystemPicAlbumCode = Tools.getRequestCode();
        toCutPictureCode = Tools.getRequestCode();
        hasEditCustom = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_theme);
        if (Tools.theme.useBlackOnStatusBar()) {
            Tools.setBlackWordOnStatus(activity);
        }

        Tools.readCustomTheme(activity);
        if (Tools.theme instanceof CustomDayUITheme) {
            oldTheme = new CustomDayUITheme(Tools.theme);
        } else if (Tools.theme instanceof CustomNightUITheme) {
            oldTheme = new CustomNightUITheme(Tools.theme);
        } else {
            oldTheme = Tools.theme;
        }
        initView();
        settingTheme();
        myListener();
        if (Tools.theme instanceof ColorfulDayUITheme || Tools.theme instanceof ColorfulNightUITheme) {
            selectTheme.check(R.id.colorfulTheme);
        } else if (Tools.theme instanceof WhiteUITheme) {
            selectTheme.check(R.id.whiteTheme);
        } else if (Tools.theme instanceof BlackUITheme) {
            selectTheme.check(R.id.blackTheme);
        } else {
            selectTheme.check(R.id.customTheme);
        }
    }

    private void initView() {
        settingThemeLayout = findViewById(R.id.settingThemeLayout);
        returnHome         = findViewById(R.id.returnHome);
        settingThemeTitle  = findViewById(R.id.settingThemeTitle);
        selectTheme        = findViewById(R.id.selectTheme);
        tipText            = findViewById(R.id.tipText);
        previewLast        = findViewById(R.id.previewLast);
        previewNext        = findViewById(R.id.previewNext);
        dayText            = findViewById(R.id.dayText);
        nightText          = findViewById(R.id.nightText);
        addPreview         = findViewById(R.id.addPreview);
    }

    private void selectColor(int initColor, OnColorSelect onColorSelect) {
        ViewHolderColorSelect viewHolder;
        if (colorSelectView == null) {
            colorSelectView = View.inflate(activity, R.layout.dialog_color_picker, null);
            colorSelectDialog = new AlertDialog.Builder(activity, R.style.CircleCornerAlertDialog)
                    .setView(colorSelectView)
                    .create();
            viewHolder = new ViewHolderColorSelect();
            viewHolder.colorPicker = colorSelectView.findViewById(R.id.colorPicker);
            viewHolder.dialogOk = colorSelectView.findViewById(R.id.dialogOk);
            colorSelectView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderColorSelect) colorSelectView.getTag();
        }
        colorSelectDialog.show();
        viewHolder.colorPicker.setInitialColor(initColor);
        viewHolder.dialogOk.setOnClickListener(v -> {
            colorSelectDialog.dismiss();
            onColorSelect.onResult(viewHolder.colorPicker.getColor());
        });
    }

    private void settingBackground() {
        if (backgroundSourceDialog == null) {
            View dialogView = View.inflate(activity, R.layout.dialog_setting_background, null);
            backgroundSourceDialog = new AlertDialog.Builder(activity, R.style.CircleCornerAlertDialog)
                    .setView(dialogView)
                    .create();
            dialogView.findViewById(R.id.dialogLayout).setBackgroundColor(oldTheme.getThemeColor());
            dialogView.findViewById(R.id.dialogLine).setBackgroundColor(oldTheme.getIgnoreColor());
            TextView useColor = dialogView.findViewById(R.id.useColor);
            TextView selectPhoto = dialogView.findViewById(R.id.selectPhoto);
            if (!(oldTheme instanceof BlackUITheme)) {
                useColor.setTextColor(oldTheme.getTextColor());
                selectPhoto.setTextColor(oldTheme.getTextColor());
            }
            useColor.setOnClickListener(v -> {
                backgroundSourceDialog.dismiss();
                selectColor(Color.WHITE, color -> {
                    hasEditCustom = true;
                    if (nowTheme instanceof CustomDayUITheme) {
                        ((CustomDayUITheme) nowTheme).setBackgroundColor(color);
                    } else if (nowTheme instanceof CustomNightUITheme) {
                        ((CustomNightUITheme) nowTheme).setBackgroundColor(color);
                    }
                    addPreview(nowTheme);
                });
            });
            selectPhoto.setOnClickListener(v -> {
                backgroundSourceDialog.dismiss();
                Intent toSystemPicAlbum = new Intent(Intent.ACTION_PICK);
                toSystemPicAlbum.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(toSystemPicAlbum, toSystemPicAlbumCode);
            });
        }
        backgroundSourceDialog.show();
    }

    private void settingTheme() {
        settingThemeLayout.setBackground(Tools.theme.getBackground());
        returnHome.setGoalColor(Tools.theme.getIconColor());
        settingThemeTitle.setTextColor(Tools.theme.getTextColor());
        tipText.setTextColor(Tools.theme.getTextColor());
        previewLast.setGoalColor(Tools.theme.getIconColor());
        previewNext.setGoalColor(Tools.theme.getIconColor());
        dayText.setTextColor(Tools.theme.getTextColor());
        nightText.setTextColor(Tools.theme.getTextColor());
    }

    private void myListener() {
        returnHome.setOnClickListener(v -> onBackPressed());

        selectTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.whiteTheme) {
                customMode = false;
                Tools.theme = oldTheme instanceof WhiteUITheme ? oldTheme : new WhiteUITheme(activity);
                addPreview(Tools.theme);
                tipText.setVisibility(View.INVISIBLE);
                dayText.setVisibility(View.INVISIBLE);
                nightText.setVisibility(View.GONE);
                previewLast.setVisibility(View.INVISIBLE);
                previewNext.setVisibility(View.INVISIBLE);
            } else if (checkedId == R.id.blackTheme) {
                customMode = false;
                Tools.theme = oldTheme instanceof BlackUITheme ? oldTheme : new BlackUITheme(activity);
                addPreview(Tools.theme);
                tipText.setVisibility(View.INVISIBLE);
                dayText.setVisibility(View.INVISIBLE);
                nightText.setVisibility(View.GONE);
                previewLast.setVisibility(View.INVISIBLE);
                previewNext.setVisibility(View.INVISIBLE);
            } else if (checkedId == R.id.colorfulTheme) {
                customMode = false;
                boolean oldIsDayTheme = oldTheme instanceof ColorfulDayUITheme;
                boolean oldIsNightTheme = oldTheme instanceof ColorfulNightUITheme;
                boolean isNightTime = Tools.isNightTime();
                if (oldIsDayTheme || oldIsNightTheme) {
                    if (oldIsDayTheme && isNightTime) {
                        Tools.theme = new ColorfulNightUITheme(activity);
                    } else if (oldIsNightTheme && !isNightTime) {
                        Tools.theme = new ColorfulDayUITheme(activity);
                    } else {
                        Tools.theme = oldTheme;
                    }
                } else {
                    Tools.theme = isNightTime ? new ColorfulNightUITheme(activity) : new ColorfulDayUITheme(activity);
                }
                nowTheme = new ColorfulDayUITheme(activity);
                hideTheme = new ColorfulNightUITheme(activity);
                addPreview(nowTheme);
                tipText.setVisibility(View.INVISIBLE);
                dayText.setVisibility(View.VISIBLE);
                nightText.setVisibility(View.GONE);
                previewLast.setVisibility(View.INVISIBLE);
                previewNext.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.customTheme) {
                customMode = true;
                Tools.theme = Tools.isNightTime() ? Tools.customNightUITheme : Tools.customDayUITheme;
                nowTheme = Tools.customDayUITheme;
                hideTheme = Tools.customNightUITheme;
                addPreview(nowTheme);
                tipText.setVisibility(View.VISIBLE);
                dayText.setVisibility(View.VISIBLE);
                nightText.setVisibility(View.GONE);
                previewLast.setVisibility(View.INVISIBLE);
                previewNext.setVisibility(View.VISIBLE);
            }
        });

        previewLast.setOnClickListener(v -> {
            dayText.setVisibility(View.VISIBLE);
            nightText.setVisibility(View.GONE);
            previewLast.setVisibility(View.INVISIBLE);
            previewNext.setVisibility(View.VISIBLE);
            UITheme temp = nowTheme;
            nowTheme = hideTheme;
            hideTheme = temp;
            addPreview(nowTheme);
        });

        previewNext.setOnClickListener(v -> {
            dayText.setVisibility(View.GONE);
            nightText.setVisibility(View.VISIBLE);
            previewLast.setVisibility(View.VISIBLE);
            previewNext.setVisibility(View.INVISIBLE);
            UITheme temp = nowTheme;
            nowTheme = hideTheme;
            hideTheme = temp;
            addPreview(nowTheme);
        });
    }

    private void addPreview(UITheme theme) {
        ViewHolder viewHolder;
        if (preview == null) {
            preview = View.inflate(activity, R.layout.layout_preview, null);
            viewHolder = new ViewHolder();
            viewHolder.preBackground     = preview.findViewById(R.id.preBackground);
            viewHolder.preStatus1        = preview.findViewById(R.id.preStatus1);
            viewHolder.preStatus2        = preview.findViewById(R.id.preStatus2);
            viewHolder.preStatus3        = preview.findViewById(R.id.preStatus3);
            viewHolder.preStatus4        = preview.findViewById(R.id.preStatus4);
            viewHolder.preIconColor      = preview.findViewById(R.id.preIconColor);
            viewHolder.preTextColor      = preview.findViewById(R.id.preTextColor);
            viewHolder.preProgressView   = preview.findViewById(R.id.preProgressView);
            viewHolder.preStressColor    = preview.findViewById(R.id.preStressColor);
            viewHolder.preIgnoreColor    = preview.findViewById(R.id.preIgnoreColor);
            viewHolder.preThemeLayout    = preview.findViewById(R.id.preThemeLayout);
            viewHolder.preThemeColor1    = preview.findViewById(R.id.preThemeColor1);
            viewHolder.preTheme1         = preview.findViewById(R.id.preTheme1);
            viewHolder.preTheme2         = preview.findViewById(R.id.preTheme2);
            viewHolder.preThemeColor2    = preview.findViewById(R.id.preThemeColor2);
            viewHolder.preTheme3         = preview.findViewById(R.id.preTheme3);
            viewHolder.preTheme4         = preview.findViewById(R.id.preTheme4);
            viewHolder.clearAll          = preview.findViewById(R.id.clearAll);

            viewHolder.changeStatusColor = preview.findViewById(R.id.changeStatusColor);
            viewHolder.settingBackground = preview.findViewById(R.id.settingBackground);
            // 主题设置
            viewHolder.preBackground.setLineColor(oldTheme.getIconColor());
            viewHolder.changeStatusColor.setTextColor(oldTheme.getTextColor());
            viewHolder.settingBackground.setTextColor(oldTheme.getTextColor());
            // 监听设置
            viewHolder.preIconColor.setOnClickListener(v -> {
                if (customMode) {
                    selectColor(nowTheme.getIconColor(), color -> {
                        hasEditCustom = true;
                        if (nowTheme instanceof CustomDayUITheme) {
                            ((CustomDayUITheme) nowTheme).setIconColor(color);
                        } else if (nowTheme instanceof CustomNightUITheme) {
                            ((CustomNightUITheme) nowTheme).setIconColor(color);
                        }
                        addPreview(nowTheme);
                    });
                }
            });
            viewHolder.preTextColor.setOnClickListener(v -> {
                if (customMode) {
                    selectColor(nowTheme.getTextColor(), color -> {
                        hasEditCustom = true;
                        if (nowTheme instanceof CustomDayUITheme) {
                            ((CustomDayUITheme) nowTheme).setTextColor(color);
                        } else if (nowTheme instanceof CustomNightUITheme) {
                            ((CustomNightUITheme) nowTheme).setTextColor(color);
                        }
                        addPreview(nowTheme);
                    });
                }
            });
            viewHolder.preStressColor.setOnClickListener(v -> {
                if (customMode) {
                    selectColor(nowTheme.getStressColor(), color -> {
                        hasEditCustom = true;
                        if (nowTheme instanceof CustomDayUITheme) {
                            ((CustomDayUITheme) nowTheme).setStressColor(color);
                        } else if (nowTheme instanceof CustomNightUITheme) {
                            ((CustomNightUITheme) nowTheme).setStressColor(color);
                        }
                        addPreview(nowTheme);
                    });
                }
            });
            viewHolder.preIgnoreColor.setOnClickListener(v -> {
                if (customMode) {
                    selectColor(nowTheme.getIgnoreColor(), color -> {
                        hasEditCustom = true;
                        if (nowTheme instanceof CustomDayUITheme) {
                            ((CustomDayUITheme) nowTheme).setIgnoreColor(color);
                        } else if (nowTheme instanceof CustomNightUITheme) {
                            ((CustomNightUITheme) nowTheme).setIgnoreColor(color);
                        }
                        addPreview(nowTheme);
                    });
                }
            });
            View.OnClickListener themeColorSelectListener = v -> {
                if (customMode) {
                    selectColor(nowTheme.getThemeColor(), color -> {
                        hasEditCustom = true;
                        if (nowTheme instanceof CustomDayUITheme) {
                            ((CustomDayUITheme) nowTheme).setThemeColor(color);
                        } else if (nowTheme instanceof CustomNightUITheme) {
                            ((CustomNightUITheme) nowTheme).setThemeColor(color);
                        }
                        addPreview(nowTheme);
                    });
                }
            };
            viewHolder.preThemeColor1.setOnClickListener(themeColorSelectListener);
            viewHolder.preThemeColor2.setOnClickListener(themeColorSelectListener);
            viewHolder.changeStatusColor.setOnClickListener(v -> {
                hasEditCustom = true;
                boolean newValue = !nowTheme.useBlackOnStatusBar();
                if (nowTheme instanceof CustomDayUITheme) {
                    ((CustomDayUITheme) nowTheme).setUseBlackOnStatusBar(newValue);
                } else if (nowTheme instanceof CustomNightUITheme) {
                    ((CustomNightUITheme) nowTheme).setUseBlackOnStatusBar(newValue);
                }
                addPreview(nowTheme);
            });
            viewHolder.settingBackground.setOnClickListener(v -> settingBackground());
            viewHolder.clearAll.setOnClickListener(v -> {
                hasEditCustom = true;
                if (nowTheme instanceof CustomDayUITheme) {
                    Tools.customDayUITheme = new CustomDayUITheme(activity);
                    nowTheme = Tools.customDayUITheme;
                } else if (nowTheme instanceof CustomNightUITheme) {
                    Tools.customNightUITheme = new CustomNightUITheme(activity);
                    nowTheme = Tools.customNightUITheme;
                }
                Tools.theme = Tools.isNightTime() ? Tools.customNightUITheme : Tools.customDayUITheme;
                addPreview(nowTheme);
            });
            preview.setTag(viewHolder);
            addPreview.addView(preview);
        } else {
            viewHolder = (ViewHolder) preview.getTag();
        }
        if (theme instanceof ColorfulDayUITheme) {
            viewHolder.preBackground.setBackground(((ColorfulDayUITheme) theme).getHomeBackground().getConstantState().newDrawable());
        } else {
            viewHolder.preBackground.setBackground(theme.getBackground().getConstantState().newDrawable());
        }
        int statusColor = theme.useBlackOnStatusBar() ? Color.BLACK : Color.WHITE;
        viewHolder.preStatus1.setTextColor(statusColor);
        viewHolder.preStatus2.setGoalColor(statusColor);
        viewHolder.preStatus3.setGoalColor(statusColor);
        viewHolder.preStatus4.setTextColor(statusColor);
        viewHolder.preIconColor.setGoalColor(theme.getIconColor());
        viewHolder.preTextColor.setTextColor(theme.getTextColor());
        viewHolder.preProgressView.setNewColor(theme.getIgnoreColor(), theme.getStressColor(), theme.getIgnoreColor());
        viewHolder.preStressColor.setBackgroundColor(theme.getStressColor());
        viewHolder.preIgnoreColor.setBackgroundColor(theme.getIgnoreColor());
        viewHolder.preThemeLayout.setBackground(theme.getLifeBackground());
        viewHolder.preTheme1.setGoalColor(theme.getThemeColor());
        viewHolder.preTheme2.setTextColor(theme.getThemeColor());
        viewHolder.preTheme3.setGoalColor(theme.getThemeColor());
        viewHolder.preTheme4.setTextColor(theme.getThemeColor());
        viewHolder.clearAll.setTextColor(theme.getTextColor());
        if (theme instanceof CustomDayUITheme || theme instanceof CustomNightUITheme) {
            viewHolder.clearAll.setVisibility(View.VISIBLE);
            viewHolder.changeStatusColor.setVisibility(View.VISIBLE);
            viewHolder.settingBackground.setVisibility(View.VISIBLE);
        } else {
            viewHolder.clearAll.setVisibility(View.INVISIBLE);
            viewHolder.changeStatusColor.setVisibility(View.INVISIBLE);
            viewHolder.settingBackground.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        Intent back = new Intent();
        back.putExtra(Tools.hasChangeThemeKey, !oldTheme.equals(Tools.theme));
        back.putExtra(Tools.hasEditCustomKey, hasEditCustom);
        setResult(RESULT_OK, back);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == toSystemPicAlbumCode) {
            Uri uri = data.getData();
            Intent toCut = new Intent(activity, CutPictureActivity.class);
            toCut.putExtra(Tools.picUriKey, uri);
            startActivityForResult(toCut, toCutPictureCode);
        }
        if (requestCode == toCutPictureCode) {
            if (resultCode == RESULT_OK && data != null) {
                Tools.BitmapBinder bitmapBinder = (Tools.BitmapBinder) data.getExtras().getBinder(Tools.cutResultKey);
                if (bitmapBinder != null) {
                    BitmapDrawable drawable = new BitmapDrawable(activity.getResources(), bitmapBinder.getBitmap());
                    hasEditCustom = true;
                    if (nowTheme instanceof CustomDayUITheme) {
                        ((CustomDayUITheme) nowTheme).setBackground(drawable);
                    } else if (nowTheme instanceof CustomNightUITheme) {
                        ((CustomNightUITheme) nowTheme).setBackground(drawable);
                    }
                    addPreview(nowTheme);
                }
            }
        }
    }

    private interface OnColorSelect {
        void onResult(int color);
    }

    private static class ViewHolder {
        private RoundAngleLinearLayout preBackground;
        private TextView preStatus1;
        private ColorChangeableIconView preStatus2;
        private ColorChangeableIconView preStatus3;
        private TextView preStatus4;
        private ColorChangeableIconView preIconColor;
        private TextView preTextColor;
        private ProgressView preProgressView;
        private View preStressColor;
        private View preIgnoreColor;
        private ViewGroup preThemeLayout;
        private View preThemeColor1;// 仅响应点击
        private ColorChangeableIconView preTheme1;
        private TextView preTheme2;
        private View preThemeColor2;// 仅响应点击
        private ColorChangeableIconView preTheme3;
        private TextView preTheme4;
        private TextView clearAll;

        private TextView changeStatusColor;
        private TextView settingBackground;
    }

    private static class ViewHolderColorSelect {
        private ColorPickerView colorPicker;
        private Button dialogOk;
    }
}