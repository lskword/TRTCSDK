package com.tencent.liteav.demo.trtc.widget.feature;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.demo.trtc.R;
import com.tencent.liteav.demo.trtc.sdkadapter.ConfigHelper;
import com.tencent.liteav.demo.trtc.sdkadapter.feature.VideoConfig;
import com.tencent.liteav.demo.trtc.utils.Utils;
import com.tencent.liteav.demo.trtc.widget.BaseSettingFragment;
import com.tencent.liteav.demo.trtc.widget.settingitem.BaseSettingItem;
import com.tencent.liteav.demo.trtc.widget.settingitem.CheckBoxSettingItem;
import com.tencent.liteav.demo.trtc.widget.settingitem.RadioButtonSettingItem;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.List;

/**
 * 混流相关配置
 *
 * @author guanyifeng
 */
public class SteamSettingFragment extends BaseSettingFragment implements View.OnClickListener {
    private static final String TAG = SteamSettingFragment.class.getName();

    private LinearLayout          mContentItem;
    private ImageView             mQrImg;
    private Button                mShare;
    private List<BaseSettingItem> mSettingItemList;
    private String                mPlayUrl;
    private RadioButtonSettingItem   mMixItem;
    private VideoConfig           mVideoConfig;

    @Override
    protected void initView(View itemView) {
        mContentItem = (LinearLayout) itemView.findViewById(R.id.item_content);
        mQrImg = (ImageView) itemView.findViewById(R.id.img_qr);
        mShare = (Button) itemView.findViewById(R.id.share);
        mShare.setOnClickListener(this);

        mSettingItemList = new ArrayList<>();
        mVideoConfig = ConfigHelper.getInstance().getVideoConfig();

        BaseSettingItem.ItemText itemText =
                new BaseSettingItem.ItemText("云端画面混流", "关闭","手动", "音频","预设");
        mMixItem = new RadioButtonSettingItem(getContext(), itemText,
                new RadioButtonSettingItem.SelectedListener() {
                    @Override
                    public void onSelected(int index) {
                        switch (index) {
                            case 0:
                                mVideoConfig.setCloudMixtureMode(TRTCCloudDef.TRTC_TranscodingConfigMode_Unknown);
                                break;
                            case 1:
                                mVideoConfig.setCloudMixtureMode(TRTCCloudDef.TRTC_TranscodingConfigMode_Manual);
                                break;
                            case 2:
                                mVideoConfig.setCloudMixtureMode(TRTCCloudDef.TRTC_TranscodingConfigMode_Template_PureAudio);
                                break;
                            case 3:
                                mVideoConfig.setCloudMixtureMode(TRTCCloudDef.TRTC_TranscodingConfigMode_Template_PresetLayout);
                                break;
                        }
                        if (mTRTCRemoteUserManager != null) mTRTCRemoteUserManager.updateCloudMixtureParams();
                    }
                });
        int mode = ConfigHelper.getInstance().getVideoConfig().getCloudMixtureMode();
        switch (mode) {
            case TRTCCloudDef.TRTC_TranscodingConfigMode_Unknown:
                mMixItem.setSelect(0);
                break;
            case TRTCCloudDef.TRTC_TranscodingConfigMode_Manual:
                mMixItem.setSelect(1);
                break;
            case TRTCCloudDef.TRTC_TranscodingConfigMode_Template_PureAudio:
                mMixItem.setSelect(2);
                break;
            case TRTCCloudDef.TRTC_TranscodingConfigMode_Template_PresetLayout:
                mMixItem.setSelect(3);
                break;
        }

        mSettingItemList.add(mMixItem);

        // 将这些view添加到对应的容器中
        for (BaseSettingItem item : mSettingItemList) {
            View view = item.getView();
            view.setPadding(0, SizeUtils.dp2px(5), 0, 0);
            mContentItem.addView(view);
        }

        updateQrView();
    }

    private void updateQrView() {
        mPlayUrl = getPlayUrl();
        if (mQrImg == null) {
            return;
        }
        if (TextUtils.isEmpty(mPlayUrl)) {
            mQrImg.setVisibility(View.GONE);
            mShare.setVisibility(View.GONE);
            return;
        } else {
            mQrImg.setVisibility(View.VISIBLE);
            mShare.setVisibility(View.VISIBLE);
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = Utils.createQRCodeBitmap(mPlayUrl, 400, 400);
                mQrImg.post(new Runnable() {
                    @Override
                    public void run() {
                        mQrImg.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    /**
     * 注意：该功能需要在控制台开启【旁路直播】功能，
     * 此功能是获取 CDN 直播地址，通过此功能，方便您能够在常见播放器中，播放音视频流。
     * 【*****】更多信息，您可以参考：https://cloud.tencent.com/document/product/647/16826
     *
     * @return 播放地址
     */
    private String getPlayUrl() {
        String playUrl;
        String steamId = mTRTCCloudManager.getDefaultPlayUrl();
        playUrl = "http://3891.liveplay.myqcloud.com/live/" + steamId + ".flv";
        return playUrl;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.trtc_fragment_mix_setting;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.share) {
            if (TextUtils.isEmpty(mPlayUrl)) {
                ToastUtils.showShort("播放地址不能为空");
                return;
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, mPlayUrl);
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "分享"));
        }
    }
}
