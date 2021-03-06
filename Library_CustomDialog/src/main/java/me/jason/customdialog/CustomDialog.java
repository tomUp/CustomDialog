package me.jason.customdialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.jason.customdialog.utils.DialogUtil;
import me.jason.customdialog.utils.ScreenUtil;
import me.jason.customdialog.view.MaxHeightRecyclerView;

/**
 * Project Name:LiZhiWeiKe
 * Package Name:com.widget.dialog
 * Created by jason on 2018/7/25 15:30 .
 * <p>
 * Copyright (c) 2016—2017 https://www.lizhiweike.com all rights reserved.
 */
public class CustomDialog extends Dialog {
    /*-----------------------------------DIALOG TYPE BEGIN-----------------------------------*/
    /**
     * 输入弹窗
     */
    public static final int DIALOG_TYPE_INPUT = 0x1;
    /**
     * 图片弹窗
     */
    public static final int DIALOG_TYPE_IMAGE = 0x2;
    /**
     * 普通消息弹窗
     */
    public static final int DIALOG_TYPE_MESSAGE = 0x3;
    /**
     * 单选列表弹窗
     */
    public static final int DIALOG_TYPE_SINGLE_CHOICE = 0x4;
    /**
     * 多选列表弹窗
     */
    public static final int DIALOG_TYPE_MULTI_CHOICE = 0x5;
    /**
     * 基础列表弹窗
     */
    public static final int DIALOG_TYPE_LIST = 0x6;
    /**
     * 自定义弹窗
     */
    public static final int DIALOG_TYPE_CUSTOM = 0x7;
    /*-----------------------------------DIALOG TYPE END-----------------------------------*/

    /*-----------------------------------DIALOG ACTION BEGIN-----------------------------------*/
    public static final String DIALOG_ACTION_POSITIVE = "positive";
    public static final String DIALOG_ACTION_NEGATIVE = "negative";
    /*-----------------------------------DIALOG ACTION END-----------------------------------*/

    protected final Builder builder;
    EditText input;
    EditText secondInput;
    TextView inputWarning;
    List<Integer> selectedIndiceList;

    public CustomDialog(@NonNull Builder builder) {
        super(builder.context, R.style.CustomDialogStyle1);
        this.builder = builder;

        // Don't keep a Context reference in the Builder after this point
        builder.context = null;
    }

    @Nullable
    public final EditText getInputEditText() {
        return input;
    }

    @Nullable
    public final EditText getSecondInputEditText() {
        return secondInput;
    }

    @Nullable
    public final TextView getInputWarning() {
        return inputWarning;
    }

    @Nullable
    public final String getInputValue() {
        if (getInputEditText() != null) {
            return getInputEditText().getText().toString().trim();
        }
        return null;
    }

    @Nullable
    public final String getSecondInputValue() {
        if (getSecondInputEditText() != null) {
            return getSecondInputEditText().getText().toString().trim();
        }
        return null;
    }

    /**
     * 清空输入内容
     */
    public final void clearInputValue() {
        if (getInputEditText() != null) {
            getInputEditText().setText("");
        }
    }

    /**
     * 设置警告内容
     *
     * @param warning null就不显示
     */
    public final void setInputWarning(String warning) {
        TextView inputWarning = getInputWarning();
        if (inputWarning == null) return;
        if (TextUtils.isEmpty(warning)) {
            if (inputWarning.getVisibility() != View.GONE)
                inputWarning.setVisibility(View.GONE);
        } else {
            if (inputWarning.getVisibility() != View.VISIBLE)
                inputWarning.setVisibility(View.VISIBLE);
            inputWarning.setText(warning);
        }
    }

    /**
     * 是否有第二个输入框
     */
    public final boolean isHasSecondInput() {
        return builder.secondInputVisible;
    }

    /**
     * 获得选中的单选框内容
     *
     * @return null表示不符合
     */
    public final String getSingleChoiceText() {
        int length = getItemsLength();
        if (length == 0) return null;
        if (builder.selectedIndex > -1 && builder.selectedIndex < length) {
            if (builder.isNeedInput && builder.selectedIndex == length - 1) return getInputValue();
            else return builder.items[builder.selectedIndex];
        }
        return null;
    }

    /**
     * 获得选中的多选框内容
     *
     * @return null表示没有
     */
    public final String[] getMultiChoiceTexts() {
        int length = getItemsLength();
        if (length == 0) return null;
        if (builder.selectedIndices != null && builder.selectedIndices.length > 0) {
            String[] text = new String[builder.selectedIndices.length];
            for (int i = 0; i < builder.selectedIndices.length; i++) {
                if (builder.isNeedInput && builder.selectedIndices[i] == length - 1) {
                    text[i] = getInputValue();
                } else {
                    text[i] = builder.items[builder.selectedIndices[i]];
                }
            }
            return text;
        }
        return null;
    }

    /**
     * 获得选择的单选框索引
     *
     * @return -1表示没有设置
     */
    public final int getSingleChoiceIndex() {
        return builder.selectedIndex;
    }

    /**
     * 获得选择的多选框索引
     *
     * @return null表示没有设置
     */
    public final Integer[] getMultiChoiceIndexs() {
        return builder.selectedIndices;
    }

    /**
     * An alternate way to define a single callback.
     */
    public interface SingleButtonCallback {
        void onClick(CustomDialog dialog, String dialogAction);
    }

    /**
     * A callback used for multi choice (check box) list dialogs.
     */
    public interface ListCallbackSingleChoice {
        boolean onSelection(CustomDialog dialog, View itemView, int which, @Nullable CharSequence text);
    }

    /**
     * A callback used for multi choice (check box) list dialogs.
     */
    public interface ListCallbackMultiChoice {
        boolean onSelection(CustomDialog dialog, Integer[] which, CharSequence[] text);
    }

    /**
     * An alternate way to define a custom-view callback.
     */
    public interface CustomViewCallBack {
        void onInitCustomView(CustomDialog dialog, View customView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(builder.cancelable);
        setCanceledOnTouchOutside(builder.canceledOnTouchOutside);
        switch (builder.dialogType) {
            case DIALOG_TYPE_INPUT:
                initInputLayout();
                break;
            case DIALOG_TYPE_IMAGE:
                initImageLayout();
                break;
            case DIALOG_TYPE_SINGLE_CHOICE:
                initSingleChoiceLayout();
                break;
            case DIALOG_TYPE_MULTI_CHOICE:
                initMultiChoiceLayout();
                break;
            case DIALOG_TYPE_LIST:
                initListLayout();
                break;
            case DIALOG_TYPE_CUSTOM:
                initCustomLayout();
                break;
            case DIALOG_TYPE_MESSAGE:
            default:
                initMessageLayout();
                break;
        }
        // 通用控件
        TextView dialogTitle = findViewById(R.id.dialogTitle);
        TextView dialogContent = findViewById(R.id.dialogContent);
        View dialogBottomDivider = findViewById(R.id.dialogBottomDivider);
        View dialogBtnLayout = findViewById(R.id.dialogBtnLayout);
        Button dialogNegative = findViewById(R.id.dialogNegative);
        Button dialogPositive = findViewById(R.id.dialogPositive);
        View dialogBtnDivide = findViewById(R.id.dialogBtnDivide);

        if (builder.title != null) {
            dialogTitle.setVisibility(View.VISIBLE);
            dialogTitle.setText(builder.title);
        } else {
            dialogTitle.setVisibility(View.GONE);
        }

        if (builder.titleLines > 0) {
            if (builder.titleLines == 1) dialogTitle.setSingleLine(true);
            else dialogTitle.setLines(builder.titleLines);
        }

        if (builder.titleEllipsize != null) {
            dialogTitle.setEllipsize(builder.titleEllipsize);
        }

        if (builder.content != null) {
            dialogContent.setVisibility(View.VISIBLE);
            dialogContent.setText(builder.content);
        } else {
            dialogContent.setVisibility(View.GONE);
        }

        if (builder.contentLines > 0) {
            if (builder.contentLines == 1) dialogContent.setSingleLine(true);
            else dialogContent.setLines(builder.contentLines);
        }

        if (builder.contentEllipsize != null) {
            dialogContent.setEllipsize(builder.contentEllipsize);
        }

        if (builder.positiveText == null && builder.negativeText == null) {
            dialogBottomDivider.setVisibility(View.GONE);
            dialogBtnLayout.setVisibility(View.GONE);
        }

        if (builder.positiveText != null) {
            dialogPositive.setVisibility(View.VISIBLE);
            dialogPositive.setText(builder.positiveText);
        } else {
            dialogPositive.setVisibility(View.GONE);
        }

        if (builder.positiveTextColor != -1) {
            dialogPositive.setTextColor(getContext().getResources().getColor(builder.positiveTextColor));
        }

        if (builder.negativeText != null) {
            dialogNegative.setVisibility(View.VISIBLE);
            dialogNegative.setText(builder.negativeText);
        } else {
            dialogNegative.setVisibility(View.GONE);
        }

        if (builder.negativeTextColor != -1) {
            dialogNegative.setTextColor(getContext().getResources().getColor(builder.negativeTextColor));
        }

        if (builder.positiveText != null && builder.negativeText != null) {
            dialogBtnDivide.setVisibility(View.VISIBLE);
        } else {
            dialogBtnDivide.setVisibility(View.GONE);
        }

        dialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (builder.onPositiveCallback != null) {
                    builder.onPositiveCallback.onClick(CustomDialog.this, DIALOG_ACTION_POSITIVE);
                }
                if (builder.autoDismiss) {
                    dismiss();
                }
            }
        });

        dialogNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (builder.onNegativeCallback != null) {
                    builder.onNegativeCallback.onClick(CustomDialog.this, DIALOG_ACTION_NEGATIVE);
                }
                if (builder.autoDismiss) {
                    dismiss();
                }
            }
        });
    }

    @Override
    public void dismiss() {
        if (input != null) {
            // 必须在dismiss前隐藏键盘，不然永远无法隐藏
            DialogUtil.hideKeyboard(this);
        }
        super.dismiss();
    }

    private void initMessageLayout() {
        setContentView(R.layout.style1_dialog_message);
    }

    private void initInputLayout() {
        setContentView(R.layout.style1_dialog_input);
        // 输入框
        EditText dialogInput = findViewById(R.id.dialogInput);
        EditText dialogSecondInput = findViewById(R.id.dialogSecondInput);
        TextView dialogWarning = findViewById(R.id.dialogWarning);
        this.input = dialogInput;
        this.secondInput = dialogSecondInput;
        this.inputWarning = dialogWarning;
        if (builder.inputHeight > 0) {
            ViewGroup.LayoutParams layoutParams = dialogInput.getLayoutParams();
            layoutParams.height = builder.inputHeight;
        }
        if (builder.hint != null) {
            dialogInput.setHint(builder.hint);
        }

        if (builder.secondHint != null) {
            dialogSecondInput.setHint(builder.secondHint);
        }

        if (builder.inputValue != null) {
            dialogInput.setText(builder.inputValue);
        }

        if (builder.secondInputValue != null) {
            dialogSecondInput.setText(builder.secondInputValue);
        }

        if (builder.secondInputVisible) {
            dialogSecondInput.setVisibility(View.VISIBLE);
        } else {
            dialogSecondInput.setVisibility(View.GONE);
        }

        if (builder.inputType != -1) {
            //设置EditText输入类型，但是代码设置，需要调用setSingleLine为true才能生效
            dialogInput.setInputType(builder.inputType);
            dialogInput.setSingleLine(true);
        }
        if (builder.inputFocus) {
            DialogUtil.showKeyboard(this);
        } else {
            DialogUtil.hideKeyboard(this);
        }
    }

    private void initImageLayout() {
        setContentView(R.layout.style1_dialog_image);
        ImageView dialogIcon = findViewById(R.id.dialogIcon);
        if (builder.iconResId != -1) {
            dialogIcon.setImageResource(builder.iconResId);
        } else {
            dialogIcon.setImageResource(R.drawable.public_dialog_default_image_icon);
        }
    }

    private void initSingleChoiceLayout() {
        setContentView(R.layout.style1_dialog_choice);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final BaseQuickAdapter<String, BaseViewHolder> itemAdapter = new BaseQuickAdapter<String, BaseViewHolder>(R.layout.style1_item_single_choice, Arrays.asList(builder.items)) {
            @Override
            protected void convert(BaseViewHolder helper, String item) {
                // 标题
                helper.setText(R.id.singleName, item);
                // 选中&未选中
                if (builder.selectedIndex > -1) {
                    helper.getView(R.id.singleChoice).setSelected(builder.selectedIndex == getIndexByString(item));
                } else {
                    helper.getView(R.id.singleChoice).setSelected(0 == getIndexByString(item));
                }
            }
        };
        itemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                // 已选择不需再刷新
                if (builder.selectedIndex == position) return;
                String item = (String) adapter.getItem(position);
                // 刷新UI
                builder.selectedIndex = position;
                itemAdapter.notifyDataSetChanged();
                // 回调给外面
                if (builder.listCallbackSingleChoice != null) {
                    builder.listCallbackSingleChoice.onSelection(CustomDialog.this, view, position, item);
                }
                // 显示或者隐藏键盘
                if (builder.isNeedInput && position == getItemsLength() - 1) {
                    DialogUtil.showKeyboard(CustomDialog.this);
                } else {
                    DialogUtil.hideKeyboard(CustomDialog.this);
                }
            }
        });
        recyclerView.setAdapter(itemAdapter);
        // “其他”选项的时候需要输入自定义内容
        EditText dialogInput = findViewById(R.id.dialogInput);
        this.input = dialogInput;
        // 输入框
        dialogInput.setVisibility(builder.isNeedInput ? View.VISIBLE : View.GONE);
        if (builder.isNeedInput) DialogUtil.hideKeyboard(this);// 默认不显示键盘
    }

    private void initMultiChoiceLayout() {
        setContentView(R.layout.style1_dialog_choice);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final BaseQuickAdapter<String, BaseViewHolder> itemAdapter = new BaseQuickAdapter<String, BaseViewHolder>(R.layout.style1_item_single_choice, Arrays.asList(builder.items)) {
            @Override
            protected void convert(BaseViewHolder helper, String item) {
                // 标题
                helper.setText(R.id.singleName, item);
                // 选中
                if (builder.selectedIndices != null && builder.selectedIndices.length > 0) {
                    helper.getView(R.id.singleChoice).setSelected(isSelectedByPosition(helper.getLayoutPosition()) != -1);
                } else {
                    helper.getView(R.id.singleChoice).setSelected(false);
                }
            }
        };
        itemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                // 处理选择与不选择的逻辑
                if (builder.selectedIndices != null && builder.selectedIndices.length > 0) {
                    if (selectedIndiceList == null) selectedIndiceList = new ArrayList<>();
                    if (isSelectedByPosition(position) != -1) {
                        // 已选择->未选择
                        selectedIndiceList.remove((Integer) position);
                    } else {
                        // 未选择->已选择
                        selectedIndiceList.add((Integer) position);
                    }
                } else {
                    if (selectedIndiceList == null) selectedIndiceList = new ArrayList<>();
                    else selectedIndiceList.clear();
                    selectedIndiceList.add((Integer) position);
                }
                // 刷新UI
                builder.selectedIndices = new Integer[selectedIndiceList.size()];
                selectedIndiceList.toArray(builder.selectedIndices);
                itemAdapter.notifyDataSetChanged();
                // 回调给外面
                if (builder.listCallbackMultiChoice != null) {
                    String[] text;
                    if (builder.selectedIndices != null && builder.selectedIndices.length > 0) {
                        text = new String[builder.selectedIndices.length];
                        for (int i = 0; i < builder.selectedIndices.length; i++) {
                            text[i] = builder.items[builder.selectedIndices[i]];
                        }
                    } else text = new String[]{};
                    builder.listCallbackMultiChoice.onSelection(CustomDialog.this, builder.selectedIndices, text);
                }
                // 显示或者隐藏键盘
                if (builder.isNeedInput && position == getItemsLength() - 1) {
                    DialogUtil.showKeyboard(CustomDialog.this);
                } else {
                    DialogUtil.hideKeyboard(CustomDialog.this);
                }

            }
        });
        recyclerView.setAdapter(itemAdapter);
        // “其他”选项的时候需要输入自定义内容
        EditText dialogInput = findViewById(R.id.dialogInput);
        this.input = dialogInput;
        // 输入框
        dialogInput.setVisibility(builder.isNeedInput ? View.VISIBLE : View.GONE);
        if (builder.isNeedInput) DialogUtil.hideKeyboard(this);// 默认不显示键盘
        // 数组转换成List，List有增删功能
        if (builder.selectedIndices != null && builder.selectedIndices.length > 0) {
            List<Integer> asList = Arrays.asList(builder.selectedIndices);
            selectedIndiceList = new ArrayList<>(asList);
        }
    }

    private void initListLayout() {
        setContentView(R.layout.style1_dialog_list);
        MaxHeightRecyclerView recyclerView = findViewById(R.id.recyclerView);
        if (builder.listMaxHeight > 0)
            recyclerView.setMaxHeight(ScreenUtil.dp2px(getContext(), builder.listMaxHeight));
        if (builder.adapter != null) recyclerView.setAdapter(builder.adapter);
        if (builder.layoutManager != null) recyclerView.setLayoutManager(builder.layoutManager);
    }

    private void initCustomLayout() {
        setContentView(R.layout.style1_dialog_custom);
        FrameLayout dialogCustom = findViewById(R.id.dialogCustom);
        if (builder.customLayoutId != -1) {
            View customView = LayoutInflater.from(getContext()).inflate(builder.customLayoutId, dialogCustom, false);
            if (customView == null) return;
            if (builder.onCustomViewCallback != null) {
                builder.onCustomViewCallback.onInitCustomView(CustomDialog.this, customView);
            }
            dialogCustom.addView(customView);
        }
    }

    private int getItemsLength() {
        if (builder.items != null && builder.items.length > 0) {
            return builder.items.length;
        }
        return 0;
    }

    /**
     * 是否被选中
     *
     * @return -1表示未选中
     */
    private int isSelectedByPosition(int position) {
        int index = -1;
        if (builder.selectedIndices != null && builder.selectedIndices.length > 0) {
            for (int i = 0; i < builder.selectedIndices.length; i++) {
                if (position == builder.selectedIndices[i]) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    private int getIndexByString(String content) {
        int index = -1;
        if (!TextUtils.isEmpty(content)) {
            if (builder.items != null && builder.items.length > 0) {
                for (int i = 0; i < builder.items.length; i++) {
                    if (content.equals(builder.items[i])) {
                        index = i;
                        break;
                    }
                }
            }
        }
        return index;
    }

    public static class Builder {
        protected Context context;
        protected CharSequence title;
        protected CharSequence content;
        protected CharSequence positiveText;
        protected CharSequence negativeText;
        @ColorRes
        protected int positiveTextColor = -1;
        @ColorRes
        protected int negativeTextColor = -1;
        protected boolean cancelable = true;
        protected boolean canceledOnTouchOutside = true;
        protected int dialogType = DIALOG_TYPE_MESSAGE;
        protected SingleButtonCallback onPositiveCallback;
        protected SingleButtonCallback onNegativeCallback;
        protected boolean autoDismiss = true;
        protected int titleLines = -1;
        protected TextUtils.TruncateAt titleEllipsize;
        protected int contentLines = -1;
        protected TextUtils.TruncateAt contentEllipsize;

        /*-----------------------------------INPUT BEGIN-----------------------------------*/
        protected CharSequence hint;
        protected CharSequence secondHint;
        protected CharSequence inputValue;
        protected CharSequence secondInputValue;
        protected boolean secondInputVisible = false;
        protected int inputHeight = -1;
        protected boolean inputFocus = false;
        protected int inputType = -1;
        /*-----------------------------------INPUT END-----------------------------------*/

        /*-----------------------------------IMAGE BEGIN-----------------------------------*/
        @DrawableRes
        protected int iconResId = -1;
        /*-----------------------------------IMAGE END-----------------------------------*/

        /*-----------------------------------CHOICE BEGIN-----------------------------------*/
        protected String[] items = null;
        protected boolean isNeedInput = false;
        protected int selectedIndex = -1;
        protected Integer[] selectedIndices = null;
        protected ListCallbackSingleChoice listCallbackSingleChoice;
        protected ListCallbackMultiChoice listCallbackMultiChoice;
        /*-----------------------------------CHOICE END-----------------------------------*/

        /*-----------------------------------LIST BEGIN-----------------------------------*/
        protected RecyclerView.Adapter<?> adapter;
        protected RecyclerView.LayoutManager layoutManager;
        protected int listMaxHeight = 0;
        /*-----------------------------------LIST END-----------------------------------*/

        /*-----------------------------------CUSTOM BEGIN-----------------------------------*/
        protected int customLayoutId = -1;
        protected CustomViewCallBack onCustomViewCallback;
        /*-----------------------------------CUSTOM END-----------------------------------*/

        /**
         * 标题
         *
         * @param titleRes 资源id
         */
        public Builder title(@StringRes int titleRes) {
            title(this.context.getText(titleRes));
            return this;
        }

        /**
         * 标题
         */
        public Builder title(CharSequence title) {
            this.title = title;
            return this;
        }

        /**
         * 行数和省略号
         *
         * @param where {@link android.text.TextUtils.TruncateAt#START}
         *              、{@link android.text.TextUtils.TruncateAt#MIDDLE}
         *              、{@link android.text.TextUtils.TruncateAt#END}
         *              、{@link android.text.TextUtils.TruncateAt#MARQUEE}
         */
        public Builder titleLinesAndEllipsize(int lines, TextUtils.TruncateAt where) {
            this.titleLines = lines;
            this.titleEllipsize = where;
            return this;
        }

        /**
         * 内容，一般提示性性质
         *
         * @param contentRes 资源id
         */
        public Builder content(@StringRes int contentRes) {
            content(this.context.getText(contentRes));
            return this;
        }

        /**
         * 内容，一般提示性性质
         */
        public Builder content(CharSequence content) {
            this.content = content;
            return this;
        }

        /**
         * 行数和省略号
         *
         * @param where {@link android.text.TextUtils.TruncateAt#START}
         *              、{@link android.text.TextUtils.TruncateAt#MIDDLE}
         *              、{@link android.text.TextUtils.TruncateAt#END}
         *              、{@link android.text.TextUtils.TruncateAt#MARQUEE}
         */
        public Builder contentLinesAndEllipsize(int lines, TextUtils.TruncateAt where) {
            this.contentLines = lines;
            this.contentEllipsize = where;
            return this;
        }


        /**
         * 确定按钮
         *
         * @param positiveRes 资源id
         */
        public Builder positiveText(@StringRes int positiveRes) {
            if (positiveRes == 0) {
                return this;
            }
            positiveText(this.context.getText(positiveRes));
            return this;
        }

        /**
         * 确定按钮
         */
        public Builder positiveText(CharSequence message) {
            this.positiveText = message;
            return this;
        }

        /**
         * 确定按钮颜色
         */
        public Builder positiveTextColor(@ColorRes int positiveTextColor) {
            this.positiveTextColor = positiveTextColor;
            return this;
        }

        /**
         * 取消按钮
         *
         * @param negativeRes 资源id
         */
        public Builder negativeText(@StringRes int negativeRes) {
            if (negativeRes == 0) {
                return this;
            }
            return negativeText(this.context.getText(negativeRes));
        }

        /**
         * 取消按钮
         */
        public Builder negativeText(CharSequence message) {
            this.negativeText = message;
            return this;
        }

        /**
         * 取消按钮颜色
         */
        public Builder negativeTextColor(@ColorRes int negativeTextColor) {
            this.negativeTextColor = negativeTextColor;
            return this;
        }

        public Builder cancelable(boolean cancelable) {
            this.cancelable = cancelable;
            this.canceledOnTouchOutside = cancelable;
            return this;
        }

        public Builder canceledOnTouchOutside(boolean canceledOnTouchOutside) {
            this.canceledOnTouchOutside = canceledOnTouchOutside;
            return this;
        }

        /**
         * 弹窗的方式，默认是DIALOG_TYPE_MESSAGE
         *
         * @param dialogType {@link CustomDialog#DIALOG_TYPE_MESSAGE}、{@link CustomDialog#DIALOG_TYPE_IMAGE}、{@link CustomDialog#DIALOG_TYPE_INPUT}
         */
        public Builder dialogType(int dialogType) {
            this.dialogType = dialogType;
            return this;
        }

        /**
         * 确定按钮的callback
         */
        public Builder onPositive(SingleButtonCallback callback) {
            this.onPositiveCallback = callback;
            return this;
        }

        /**
         * 取消按钮的callback
         */
        public Builder onNegative(SingleButtonCallback callback) {
            this.onNegativeCallback = callback;
            return this;
        }

        /**
         * 点击按钮是否自动触发dismiss，默认自动触发
         */
        public Builder autoDismiss(boolean autoDismiss) {
            this.autoDismiss = autoDismiss;
            return this;
        }

        /**
         * 设置输入框高度
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_INPUT}才生效</P>
         */
        public Builder inputHeight(int inputHeight) {
            this.inputHeight = inputHeight;
            return this;
        }

        /**
         * 设置输入框高度
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_INPUT}才生效</P>
         */
        public Builder inputFocus(boolean inputFocus) {
            this.inputFocus = inputFocus;
            return this;
        }

        /**
         * 设置输入方式
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_INPUT}才生效</P>
         */
        public Builder inputType(int inputType) {
            this.inputType = inputType;
            return this;
        }

        /**
         * 输入框提示内容
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_INPUT}才生效</P>
         *
         * @param hintRes 资源id
         */
        public Builder hint(@StringRes int hintRes) {
            hint(this.context.getText(hintRes));
            return this;
        }

        /**
         * 输入框提示内容
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_INPUT}才生效</P>
         */
        public Builder hint(CharSequence hint) {
            this.hint = hint;
            return this;
        }

        /**
         * 次级输入框提示内容
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_INPUT}才生效</P>
         *
         * @param secondHintRes 资源id
         */
        public Builder secondHint(@StringRes int secondHintRes) {
            secondHint(this.context.getText(secondHintRes));
            return this;
        }

        /**
         * 次级输入框提示内容
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_INPUT}才生效</P>
         */
        public Builder secondHint(CharSequence secondHint) {
            this.secondHint = secondHint;
            return this;
        }

        /**
         * 输入框输入内容
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_INPUT}才生效</P>
         *
         * @param inputValueRes 资源id
         */
        public Builder inputValue(@StringRes int inputValueRes) {
            inputValue(this.context.getText(inputValueRes));
            return this;
        }

        /**
         * 输入框输入内容
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_INPUT}才生效</P>
         */
        public Builder inputValue(CharSequence inputValue) {
            this.inputValue = inputValue;
            return this;
        }

        /**
         * 次级输入框输入内容
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_INPUT}才生效</P>
         *
         * @param secondInputValueRes 资源id
         */
        public Builder secondInputValue(@StringRes int secondInputValueRes) {
            secondInputValue(this.context.getText(secondInputValueRes));
            return this;
        }

        /**
         * 次级输入框输入内容
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_INPUT}才生效</P>
         */
        public Builder secondInputValue(CharSequence secondInputValue) {
            this.secondInputValue = secondInputValue;
            return this;
        }

        /**
         * 次级输入框是否显示
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_INPUT}才生效</P>
         */
        public Builder secondInputVisible(boolean secondInputVisible) {
            this.secondInputVisible = secondInputVisible;
            return this;
        }

        /**
         * 图片
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_IMAGE}才生效</P>
         *
         * @param iconResId
         */
        public Builder iconResId(@DrawableRes int iconResId) {
            this.iconResId = iconResId;
            return this;
        }

        /**
         * 列表
         */
        public Builder items(String[] items) {
            this.items = items;
            return this;
        }

        public Builder needInput(boolean isNeedInput) {
            this.isNeedInput = isNeedInput;
            return this;
        }

        /**
         * 默认单项选择
         */
        public Builder selectedIndexSingleChoice(int selectedIndex) {
            this.selectedIndex = selectedIndex;
            return this;
        }

        /**
         * 默认多项选择
         */
        public Builder selectedIndexMultiChoice(@Nullable Integer[] selectedIndices) {
            this.selectedIndices = selectedIndices;
            return this;
        }

        /**
         * 设置单选的回调
         * <P>单选：dialogType为{@link CustomDialog#DIALOG_TYPE_SINGLE_CHOICE}才生效</P>
         *
         * @param selectedIndex 选中的选择框
         * @param callback      单选回调
         */
        public Builder itemsCallbackSingleChoice(int selectedIndex, ListCallbackSingleChoice callback) {
            this.selectedIndex = selectedIndex;
            this.listCallbackSingleChoice = callback;
            this.listCallbackMultiChoice = null;
            return this;
        }

        /**
         * 设置多选的回调
         * <P>多选：dialogType为{@link CustomDialog#DIALOG_TYPE_MULTI_CHOICE}才生效</P>
         *
         * @param selectedIndices 选中的选择框
         * @param callback        多选回调
         */
        public Builder itemsCallbackMultiChoice(@Nullable Integer[] selectedIndices, ListCallbackMultiChoice callback) {
            this.selectedIndices = selectedIndices;
            this.listCallbackSingleChoice = null;
            this.listCallbackMultiChoice = callback;
            return this;
        }

        /**
         * 设置列表
         * <P>Sets a custom {@link RecyclerView.Adapter} for the dialog's list</P>
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_LIST}才生效</P>
         */
        public Builder adapter(RecyclerView.Adapter<?> adapter, @Nullable RecyclerView.LayoutManager layoutManager) {
            this.adapter = adapter;
            this.layoutManager = layoutManager;
            return this;
        }

        /**
         * 设置列表最大高度，单位dp
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_LIST}才生效</P>
         */
        public Builder listMaxHeight(int maxHeight) {
            this.listMaxHeight = maxHeight;
            return this;
        }

        /**
         * 设置自定义布局
         * <P>dialogType为{@link CustomDialog#DIALOG_TYPE_CUSTOM}才生效</P>
         */
        public Builder setCustomView(int customLayoutId, CustomViewCallBack onCustomViewCallback) {
            this.customLayoutId = customLayoutId;
            this.onCustomViewCallback = onCustomViewCallback;
            return this;
        }

        /**
         * 初始化一些样式
         */
        public Builder(Context context) {
            this.context = context;
        }

        @UiThread
        public CustomDialog build() {
            return new CustomDialog(this);
        }

        @UiThread
        public CustomDialog show() {
            CustomDialog dialog = build();
            dialog.show();
            return dialog;
        }
    }
}
