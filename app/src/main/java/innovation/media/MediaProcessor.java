package innovation.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hjq.toast.ToastUtils;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.model.bean.CommitLiBean;
import com.xiangchuang.risks.model.bean.GSCPigBean;
import com.xiangchuang.risks.model.bean.GsCommitBean;
import com.xiangchuang.risks.utils.AVOSCloudUtils;
import com.xiangchuang.risks.utils.AlertDialogManager;
import com.xiangchuangtec.luolu.animalcounter.PigAppConfig;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.GsonUtils;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.demo.DetectorActivity_pig;
import org.tensorflow.demo.Global;
import org.tensorflow.demo.ToubaoCowInfoActivity;
import org.tensorflow.demo.env.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipOutputStream;

import innovation.entry.GsonBean;
import innovation.entry.InnApplication;
import innovation.entry.NewBuildResultObject;
import innovation.location.LocationManager_new;
import innovation.login.RespObject;
import innovation.login.Utils;
import innovation.network_status.NetworkUtil;
import innovation.upload.UploadHelper;
import innovation.upload.UploadThread;
import innovation.utils.FileUtils;
import innovation.utils.HttpRespObject;
import innovation.utils.HttpUtils;
import innovation.utils.JsonHelper;
import innovation.utils.PigInnovationAiOpen;
import innovation.utils.Toast;
import innovation.utils.UploadObject;
import innovation.utils.ZipUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;
import static com.xiangchuang.risks.view.SelectFunctionActivity_new.gscPigBeans;
import static innovation.entry.InnApplication.getCowEarNumber;
import static innovation.entry.InnApplication.getCowType;
import static innovation.entry.InnApplication.getStringTouboaExtra;
import static innovation.entry.InnApplication.getlipeiTempNumber;
import static innovation.utils.FileUtils.fileToBase64;
import static org.tensorflow.demo.CameraConnectionFragment_pig.collectNumberHandler;

//import innovation.tensorflow.tracking.FaceDetector;

/**
 * Author by luolu, Date on 2018/8/16.
 * COMPANY：InnovationAI
 */

public class MediaProcessor {
    //调用搜索接口的次数
    private int searchSallmerCount = 0;

    private static final int MSG_PROCESSOR_ZIP = 101;
    private static final int MSG_PROCESSOR_UPLOAD_INSURE_ONE = 102;
    private static final int MSG_PROCESSOR_UPLOAD_PAY_ONE = 103;
    private static final int MSG_PROCESSOR_UPLOAD_ALL = 104;
    private static final int MSG_PROCESSOR_TEST = 105;
    private static final int MSG_PROCESSOR_UPLOAD_IMAGEONE = 106;

    private static final int MSG_UI_PROGRESS_EXTRACT_IMG = 1;
    private static final int MSG_UI_PROGRESS_DETECT_IMG = 2;
    private static final int MSG_UI_PROGRESS_ZIP_IMG = 3;
    private static final int MSG_UI_PROGRESS_ZIP_VIDEO = 33;
    private static final int MSG_UI_PROGRESS_ZIP_VIDEO_UPLOAD = 47;
    private static final int MSG_UI_PROGRESS_UPLOAD_IMG = 4;
    private static final int MSG_UI_FINISH_NO_DETECTED_IMG_EXIST = 6;
    private static final int MSG_UI_FINISH_ZIP_IMG_FAILED = 7;
    private static final int MSG_UI_FINISH_ZIP_VIDEO_FAILED = 8;
    private static final int MSG_UI_FINISH_UPLOAD_IMG_ONE_FAILED = 9;
    private static final int MSG_UI_FINISH_UPLOAD_IMG_ONE_SUCCESS = 10;
    private static final int MSG_UI_FINISH_MISSING_DETECT_IMG = 11;
    private static final int MSG_UI_PROGRESS_UPLOAD_IMG_ONE = 12;
    private static final int MSG_UI_PROGRESS_UPLOAD_ALL = 14;
    private static final int MSG_UI_FINISH_UPLOAD_ALL = 15;
    private static final int MSG_UI_FINISH_NOZIP = 16;
    private static final int MSG_UI_FINISH_ZIP_INSURE = 17;
    private static final int MSG_UI_FINISH_ZIP_PAY = 18;
    private static final int MSG_UI_FINISH_VERIFY = 19;
    private static final int MSG_UI_FINISH_BUILD = 21;
    private static final int MSG_UI_FINISH_ZIP_FILE_NULL = 22;
    private final Logger mLoggerPig = new Logger(MediaProcessor.class.getSimpleName());
    private static MediaProcessor sInstance;
    private final Context mContext;
    private Activity mActivity = null;
    //    private final FaceDetector mFaceDetector_new; //haojie add
    private ProgressDialog mProgressDialog;
    private InsureDialog mInsureDialog = null;
    private final Handler mProcessorHandler_new;
    private final Handler mUiHandler_new;
    private ReviewImageDialog mReviewDialogImage = null; //
    private ReviewVideoDialog mReviewDialogVideo = null; //
    private String errStr;
    private String cow_libid;
    private String obj_cow_verify;
    private String verification_status;
    private NewBuildResultObject pigBuildResp2;
    private NewBuildResultObject pigBuildRespResult;

    private String str_address = "";
    private final Gson gson;
    private GsonBean bean;
    private static String getPayYiji;
    private static String getPayErji;
    private static String getPaySanji;
    private String sheId;
    private String inspectNo;
    private String reason;

    private ZipOutputStream zipOutputStream;
    private String similarImgUrl;
    private int userLibId;
    private String msg;
    private List<CommitLiBean.DataBean.SimilarListBean> similarList;
    private String similarImgUrl1;
    private int animalId;
    private String lipeiId;
    private String lipeiNo;
    private String mmsg;
    private Bitmap bitmap;
    private String minsureNo;
    private String mpreCompensateTime;
    private String mjuanName;
    private String msheName;
    private String showmsg;
    private String similarityDegree;
    private String seqNo = "";
    private String compensateTime = "";
    private String pigType = "";

    private boolean isChongFu = false;


    public static MediaProcessor getInstance(Context context) {
        if (sInstance == null) {
            synchronized (MediaProcessor.class) {
                if (sInstance == null) {
                    sInstance = new MediaProcessor(context);
                }
            }
        }
        return sInstance;
    }

    public MediaProcessor(Context context) {
        mContext = context.getApplicationContext();
        final String[][] MODEL_PARAMS = {
                // animal type=0
                {"", ""},
                {"image:0", "exist:0,predict:0"},  // animal type=Global.ANIMAL_TYPE_PIG
                {"image:0", "exist:0,predict:0"},  // animal type=Global.ANIMAL_TYPE_CATTLE
                {"image:0", "exist:0,predict:0"}   // animal type=Global.ANIMAL_TYPE_DONKEY
        };
//        mFaceDetector_new = FaceDetector.create(mContext.getAssets(),
//                "file:///android_asset/" + ConstUtils.getPBFile(ANIMAL_TYPE),
//                784,
//                192,
//                160,
//                160,
//                MODEL_PARAMS[ANIMAL_TYPE][0],
//                MODEL_PARAMS[ANIMAL_TYPE][1]
//        );
        HandlerThread mProcessorThread = new HandlerThread("processor-thread");
        mProcessorThread.start();
        mProcessorHandler_new = new ProcessorHandler_new(mProcessorThread.getLooper());
        mUiHandler_new = new UiHandler_new(Looper.getMainLooper());
        //getCurrentLocationLatLng();
        gson = new Gson();
    }

    //haojie add
//    public FaceDetector getFaceDetector_new() {
//        return mFaceDetector_new;
//    }

    public void handleMediaResource_build(final Activity activity) {
        mActivity = activity;
        initDialogs(activity);
        str_address = LocationManager_new.getInstance(activity).str_address;
    }

    public void handleMediaResource_build(Activity activity, String sheId, String inspectNo, String reason) {
        mActivity = activity;
        initDialogs(activity);
        this.sheId = sheId;
        this.inspectNo = inspectNo;
        this.reason = reason;
        str_address = LocationManager_new.getInstance(activity).str_address;
    }

    public void handleMediaResource_destroy() {
        destroyDialogs();
    }

    private void showProgressDialog(Activity activity) {
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setTitle(R.string.dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIcon(R.drawable.cowface);
        mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "确定", mPOSITIVEClickListener);
        mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "取消", mNEGATIVEClickListener);
        mProgressDialog.setMessage("开始处理......");
        mProgressDialog.show();
        Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
        if (positive != null) {
            positive.setVisibility(View.GONE);
        }
        Button negative = mProgressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
        if (negative != null) {
            negative.setVisibility(View.GONE);
        }
    }

    private void saveLibId(String id) {
        SharedPreferences idinfo = mActivity.getSharedPreferences(
                Utils.LIBIDINFO_SHAREFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = idinfo.edit();
        editor.putString("libid", id);
        editor.apply();
    }

    private String getLibId() {
        //读取用户信息
        SharedPreferences pref_user = mActivity.getSharedPreferences(Utils.LIBIDINFO_SHAREFILE, Context.MODE_PRIVATE);
        return pref_user.getString("libid", "");
    }

    private void initDialogs(final Activity activity) {
        updateInsureDialog(activity);
    }

    private void destroyDialogs() {
        if (mInsureDialog != null) {
            mInsureDialog.dismiss();
        }
        mInsureDialog = null;
        if (mReviewDialogImage != null) {
            mReviewDialogImage.dismiss();
        }
        mReviewDialogImage = null;
        if (mReviewDialogVideo != null) {
            mReviewDialogVideo.dismiss();
        }
        mReviewDialogVideo = null;
    }

    private void initReviewDialog_image(final Activity activity) {
        String imageDri = "";
        if (Global.model == Model.BUILD.value()) {
            imageDri = Global.mediaInsureItem.getImageDir();///storage/emulated/0/innovation/animal/投保/Current/图片
        } else if (Global.model == Model.VERIFY.value()) {
            imageDri = Global.mediaPayItem.getImageDir();///storage/emulated/0/innovation/animal/理赔/Current/图片
        }
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        final View conView = layoutInflater.inflate(R.layout.review_dialog_layout, null);
        mReviewDialogImage = new ReviewImageDialog(activity, conView, imageDri);
        mReviewDialogImage.setTitle(R.string.dialog_title);
        mReviewDialogImage.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mReviewDialogImage.dismiss();
                updateInsureDialog(activity);
            }
            return false;
        });

    }

    private void initReviewDialog_video(final Activity activity) {
        String imageDri = "";
        if (Global.model == Model.BUILD.value()) {
            imageDri = Global.mediaInsureItem.getVideoDir();///storage/emulated/0/innovation/animal/投保/Current/视频
        } else if (Global.model == Model.VERIFY.value()) {
            imageDri = Global.mediaPayItem.getVideoDir();///storage/emulated/0/innovation/animal/理赔/Current/视频
        }
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        final View conView = layoutInflater.inflate(R.layout.review_dialog_layout, null);
        mReviewDialogVideo = new ReviewVideoDialog(activity, conView, imageDri);
        mReviewDialogVideo.setTitle(R.string.dialog_title);
        mReviewDialogVideo.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mReviewDialogVideo.dismiss();
                updateInsureDialog(activity);
            }
            return false;
        });
    }


    private void initInsureDialog(final Activity activity) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        final View conView = layoutInflater.inflate(R.layout.insure_dialog_layout, null); //6个角度加载
        mInsureDialog = new InsureDialog(activity, conView);
        final EditText meditText;
        meditText = conView.findViewById(R.id.insure_number);
        mInsureDialog.setTitle(R.string.dialog_title);

        View.OnClickListener listener_abort = v -> {
            mInsureDialog.dismiss();
            if (PigAppConfig.debugNub == 1) {
                collectNumberHandler.sendEmptyMessage(5);
            } else {
                String pignum = meditText.getText().toString().trim();
                if (pignum.length() > 0) {
                    saveLibId(pignum);
                }
                mActivity.startActivity(new Intent(mActivity, DetectorActivity_pig.class));
                reInitCurrentDir();
                collectNumberHandler.sendEmptyMessage(2);
                Log.i("initInsureDialog:", "listener_abort");
            }
        };
        View.OnClickListener listener_add = v -> {
            String pignum = meditText.getText().toString().trim();
            if (pignum.length() > 0) {
                saveLibId(pignum);
            }
            mInsureDialog.dismiss();
        };
        View.OnClickListener listener_upload_one = v -> {
            // TODO: 2018/8/7  本次上传按钮

            String pignum = meditText.getText().toString().trim();
            if (pignum.length() == 0) {
//                    mInsureDialog.setTextTips("请输入编号！！！");
//                    return;
                pignum = "110";
            }
            saveLibId(pignum);
            mInsureDialog.dismiss();
            showProgressDialog(activity);
            writeNumnerFile(pignum);
            dialogProcessUploadOneImage();
           /* if (InnApplication.isOfflineMode) {
                showProgressDialog(activity);
                dialogProcessUploadOneImage();
            } else {
                String pignum = meditText.getText().toString().trim();
                if (pignum.length() == 0) {
//                    mInsureDialog.setTextTips("请输入编号！！！");
//                    return;
                    pignum = "110";
                }
                saveLibId(pignum);
                mInsureDialog.dismiss();
                showProgressDialog(activity);
                writeNumnerFile(pignum);
                dialogProcessUploadOneImage();
//                dialogProcessUploadAll();
            }
*/
        };

        View.OnClickListener listener_upload_all = v -> {
            mInsureDialog.dismiss();
            //执行上传操作(不包括当前目录)
            dialogProcessUploadAll();
        };

        View.OnClickListener listener_next = v -> {
            //回录像界面继续捕捉下一头图片
            String pignum = meditText.getText().toString().trim();
            if (pignum.length() == 0) {
                mInsureDialog.setTextTips("请输入编号！！！");
                return;
            }
            mInsureDialog.dismiss();
            showProgressDialog(activity);
            writeNumnerFile(pignum);
            dialogProcessZip();
        };
        View.OnClickListener listener_seeimage = v -> {
            mInsureDialog.dismiss();
            updateReviewDialog_image(activity);
        };

        View.OnClickListener listener_seevideo = v -> {
            mInsureDialog.dismiss();
            updateReviewDialog_video(activity);
        };
        //取消停止录制
        View.OnClickListener listener_cancel = v -> {
            mInsureDialog.dismiss();
            PigAppConfig.during = 0;
            reInitCurrentDir();
            collectNumberHandler.sendEmptyMessage(2);
            mActivity.startActivity(new Intent(mActivity, DetectorActivity_pig.class));

        };
        mInsureDialog.setAbortButton("放弃", listener_abort);
        mInsureDialog.setAddeButton("补充", listener_add);
        mInsureDialog.setUploadOneButton("本次上传", listener_upload_one);
        mInsureDialog.setUploadAllButton("全部上传", listener_upload_all);
        mInsureDialog.setNexteButton("下一头", listener_next);
        mInsureDialog.setSeeImageButton("回看图片", listener_seeimage);
        mInsureDialog.setSeeVideoButton("回看视频", listener_seevideo);
        mInsureDialog.setCancelButton("取消", listener_cancel);
        String pignum = meditText.getText().toString().trim();
        if (pignum.length() > 0) {
            saveLibId(pignum);
        }
        //updateInsureDialog(activity);
    }

    private void preCommitForLiPei(File zipFile, String timesFlag) {
        Log.e("precommit:zipimage2", "path=" + zipFile.getAbsolutePath());

        Map<String, String> mapbody = new HashMap<>();
        mapbody.put("taskID", PigInnovationAiOpen.getGscTaskid());
        mapbody.put("type", String.valueOf(PigInnovationAiOpen.getCurType()));
        mapbody.put(Constants.timesFlag, timesFlag);
        mapbody.put(Constants.address, str_address);
        mapbody.put(Constants.COLLECT_TIME, PigAppConfig.during / 1000 + "");
        Activity appContext = (Activity) mActivity;

        OkHttp3Util.uploadPreFile(HttpUtils.GSC_PAY_LIBUPLOAD, zipFile, "a.zip", mapbody, null, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
//                            Log.e("lipeiprecommit", e.getLocalizedMessage());
                        appContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                showTimeOutDialog();
                            }
                        });
                        AVOSCloudUtils.saveErrorMessage(e, MediaProcessor.class.getSimpleName());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String s = response.body().string();
                        mActivity.runOnUiThread(()->{
                            mProgressDialog.dismiss();
                            if (response.isSuccessful()) {
                                Log.e("lipeicommit", "上传--" + s);
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(s);
                                    int status = jsonObject.getInt("status");
                                    String mymsg = jsonObject.getString("msg");
                                    appContext.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (-1 == status) {
                                                //上传过程中有异常终止了，此时用户需要重试
                                                showTimeOutDialog();
                                            } else if (0 == status) {
                                                //模型返回图片质量不合格，用户需要重新采集
                                                showErrorDialog(mymsg);
                                            } else if (1 == status)  {
                                                GsCommitBean bean = GsonUtils.getBean(s, GsCommitBean.class);
                                                Log.e(TAG, "bean: " + bean);

                                                GSCPigBean gscPigBean = new GSCPigBean();
                                                gscPigBean.name = fileName;
                                                gscPigBean.picture = filePath;
                                                gscPigBean.address = str_address;
                                                gscPigBean.latitude = LocationManager_new.getInstance(appContext).currentLat;
                                                gscPigBean.longitude = LocationManager_new.getInstance(appContext).currentLon;
                                                gscPigBean.time = System.currentTimeMillis();
                                                gscPigBean.pigHouseNumber = "";
                                                gscPigBean.photoPigsNumber = 1;
                                                gscPigBean.resultStatus = bean.getData().getBuildStatus();
                                                gscPigBean.videoFlag = "0";

                                                gscPigBeans.add(gscPigBean);
                                                ToastUtils.show("理赔申请成功");

                                                boolean result = FileUtils.deleteFile(zipFile);
                                                if (result) {
                                                    Log.i("lipeidetete:", "本地图片打包文件删除成功！！");
                                                }
                                                mActivity.finish();
                                            }
                                        }
                                    });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showRetryDialog("网络异常，请检查网络后重试。");
                                    AVOSCloudUtils.saveErrorMessage(e, MediaProcessor.class.getSimpleName());
                                }

                            } else {
                                showRetryDialog("网络异常，请检查网络后重试。");
                            }
                        });
                    }
                }
        );

    }

    /**
     * 上传失败重试
     *
     * @param msg
     */
    private void showRetryDialog(String msg) {

        AlertDialogManager.showMessageDialog(mActivity, "提示", msg, new AlertDialogManager.DialogInterface() {
            @Override
            public void onPositive() {
                if (!NetworkUtil.isNetworkConnect(mContext)) {
                    Toast.makeText(mActivity, "断网了，请联网后重试。", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgressDialog(mActivity);
                processUploadOne_Pay();
            }

            @Override
            public void onNegative() {
                ((Activity) PigAppConfig.getContext()).finish();
            }
        });
    }


    // 预理赔/理赔弹框
    private void showErrorDialog(String msg) {

        AlertDialogManager.showMessageDialogOne(mActivity, "提示", msg,
                new AlertDialogManager.DialogInterface() {
                    @Override
                    public void onPositive() {
                        PigAppConfig.during = 0;
                        mActivity.startActivity(new Intent(mActivity, DetectorActivity_pig.class));
                        collectNumberHandler.sendEmptyMessage(2);
                    }

                    @Override
                    public void onNegative() {
                    }
                });
    }

    private void showTimeOutDialog() {

        AlertDialogManager.showMessageDialog(mActivity, "提示", "连接超时，请检查网络后重试。", new AlertDialogManager.DialogInterface() {
            @Override
            public void onPositive() {
                if (!NetworkUtil.isNetworkConnect(mContext)) {
                    Toast.makeText(mActivity, "断网了，请联网后重试。", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgressDialog(mActivity);
                processUploadOne_Pay();
            }

            @Override
            public void onNegative() {
                ((Activity) PigAppConfig.getContext()).finish();
            }
        });
    }

    private void updateReviewDialog_image(final Activity activity) {
        if (mReviewDialogImage == null) {
            initReviewDialog_image(activity);
        }
        mReviewDialogImage.updateView();
        mReviewDialogImage.show();
    }

    private void updateReviewDialog_video(final Activity activity) {
        if (mReviewDialogVideo == null) {
            initReviewDialog_video(activity);
        }
        mReviewDialogVideo.updateView();
        mReviewDialogVideo.show();

    }

    private void updateInsureDialog(final Activity activity) {
        if (mInsureDialog == null) {
            initInsureDialog(activity);
        }
    }

    public void showInsureDialog() {
        if (mInsureDialog != null && !mInsureDialog.isShowing()) {
            boolean imagehave = testImageHave(); //图片目录是否存在图片文件
            Map<String, String> showMap = getCaptureAngles();//存在角度图片
            String libid = getLibId();
            boolean ziphave = testZipHave(); //是否存在待上传文件
            boolean videohave = testVideoHave(); //是否存在mp4文件
            mInsureDialog.updateView(showMap, imagehave, ziphave, libid, videohave);
            mInsureDialog.show();
        }
    }

    //判断图片目录下是否已经存在图片文件
    private boolean testImageHave() {
        boolean ifhave = false;
        //获取图片文件
        String imageDri = "";
        if (Global.model == Model.BUILD.value()) {
            imageDri = Global.mediaInsureItem.getImageDir();///storage/emulated/0/innovation/animal/投保/Current/图片
        } else if (Global.model == Model.VERIFY.value()) {
            imageDri = Global.mediaPayItem.getImageDir();///storage/emulated/0/innovation/animal/理赔/Current/图片
        }
        File imageDir_new = new File(imageDri);//图片目录下的文件
        File[] files_image = imageDir_new.listFiles();
        if (!imageDir_new.exists() || files_image.length == 0) {
            return false;
        }

        File tmpFile;
        boolean imagehave;
        for (File aFiles_image : files_image) {
            tmpFile = aFiles_image;//tmpFile===/storage/emulated/0/innovation/animal/Current/图片/1
            String abspath = tmpFile.getAbsolutePath();
            imagehave = testFileHave(abspath);
            if (!imagehave) {
                continue;
            } else {
                return true;
            }
        }
        return ifhave;

    }

    //判断图片目录下是否已经存在图片文件
    private boolean testVideoHave() {
        //获取图片文件
        String videoDri = "";
        if (Global.model == Model.BUILD.value()) {
            videoDri = Global.mediaInsureItem.getVideoDir();///storage/emulated/0/innovation/animal/投保/Current/视频
        } else if (Global.model == Model.VERIFY.value()) {
            videoDri = Global.mediaPayItem.getVideoDir();///storage/emulated/0/innovation/animal/理赔/Current/视频
        }

        List<String> list = FileUtils.GetFiles(videoDri, Global.VIDEO_MP4, true);
        return (list != null) && (list.size() > 0);
    }

    //判断图片目录下是否已经存在Zip文件
    private boolean testZipHave() {
        //投保目录
        String zipimageDri = Global.mediaInsureItem.getZipImageDir();///storage/emulated/0/innovation/animal/投保/ZipImge
        List<String> list_image = FileUtils.GetFiles(zipimageDri, "zip", true);
        if ((list_image != null) && (list_image.size() > 0)) {
            return true;
        }

        String zipvideoDri = Global.mediaInsureItem.getZipVideoDir();///storage/emulated/0/innovation/animal/投保/ZipVideo
        List<String> list_video = FileUtils.GetFiles(zipvideoDri, "zip", true);
        return (list_video != null) && (list_video.size() > 0);

    }

    //判断指定目录下是否已经存在指定类型的文件
    private boolean testFileHave(String filePath) {
        File file_parent = new File(filePath);
        List<String> list_all = FileUtils.GetFiles(filePath, Global.IMAGE_JPEG, true);
        return list_all != null && (!file_parent.exists() || list_all.size() == 0);
    }

    //判断指定目录下是否已经存在指定类型的文件
    private int testFileHaveCount(String filePath) {
        File file_parent = new File(filePath);
        List<String> list_all = FileUtils.GetFiles(filePath, Global.IMAGE_JPEG, true);
        if (list_all == null) {
            return 0;
        }
        if (!file_parent.exists()) {
            return 0;
        }
        return list_all.size();
    }

    //获得Dialog显示框中需要显示的角度图(已经捕获的角度图片)
    private Map<String, String> getCaptureAngles() {
        //ArrayList<HashMap<String,String>> missArray = new ArrayList<HashMap<String,String>>();
        Map<String, String> showMap = new TreeMap<>();//TreeMap方式创建可以对map进行升序排序
        //获取图片文件
        String imageDri = "";
        if (Global.model == Model.BUILD.value()) {
            imageDri = Global.mediaInsureItem.getImageDir();///storage/emulated/0/innovation/animal/投保/Current/图片
        } else if (Global.model == Model.VERIFY.value()) {
            imageDri = Global.mediaPayItem.getImageDir();///storage/emulated/0/innovation/animal/理赔/Current/图片
        }
        File imageDir_new = new File(imageDri);//图片目录下的文件
        File[] files_image = imageDir_new.listFiles();
        if (!imageDir_new.exists() || files_image.length == 0) {
            return showMap;
        }
        //角度类型图片不完整，提示 ，需加上，测试阶段暂不加（防止角度缺失，始终不上传图像）
        ArrayList<Integer> typelist = new ArrayList<>();
        typelist.add(1);
        typelist.add(2);
        typelist.add(3);
        typelist.add(4);
        File tmpFile;
        String tmptype;
        boolean ifneed;
        int imagecount;
        for (int i = 0; i < files_image.length; i++) {
            tmptype = i + "";
            ifneed = typelist.contains(i);
            if (!ifneed) {//不是要显示的角度
                continue;
            }
            tmpFile = files_image[i];//tmpFile===/storage/emulated/0/innovation/animal/Current/图片/1
            String abspath = tmpFile.getAbsolutePath();
            Log.e(TAG, "abspath: " + abspath);
            imagecount = testFileHaveCount(abspath);
            showMap.put(tmptype, imagecount + "");
        }
        return showMap;
    }

    //重新初始化Current文件
    public void reInitCurrentDir() {
        Log.i("reInitCurrentDir:", "重新初始化Current文件");
        if (Global.model == Model.BUILD.value()) {
            Global.mediaInsureItem.currentDel();
            Global.mediaInsureItem.currentInit();
        } else if (Global.model == Model.VERIFY.value()) {
            Global.mediaPayItem.currentDel();
            Global.mediaPayItem.currentInit();
        }
    }

    private void writeNumnerFile(String number) {
        File file_num = null;
        if (Global.model == Model.BUILD.value()) {
            file_num = Global.mediaInsureItem.getNumberFile();
            if (file_num.exists()) {
                FileUtils.deleteFile(file_num);
                file_num = Global.mediaInsureItem.getNumberFile();
            }
        } else if (Global.model == Model.VERIFY.value()) {
            file_num = Global.mediaPayItem.getNumberFile();
            if (file_num.exists()) {
                FileUtils.deleteFile(file_num);
                file_num = Global.mediaPayItem.getNumberFile();
            }
        }
        if (file_num != null) {
            String str_num = file_num.getAbsolutePath();
            FileUtils.saveInfoToTxtFile(str_num, number);
        }
    }

    private void UploadOneInsure() {
        String zipimageDir = Global.mediaInsureItem.getZipImageDir();//storage/emulated/0/innovation/animal/投保/ZipImage
        File file_current = new File(zipimageDir);
        File zipFile_image = new File(file_current.getParentFile(), Global.ZipFileName + ".zip");
        dialogProcessUploadOneInsure(zipFile_image);
        Log.d("UploadOneInsure", "processUpload_zipImage_one file name = " + zipFile_image.getAbsolutePath());
    }

    private void UploadOnePay() {
        String zipimageDir = Global.mediaPayItem.getZipImageDir();//storage/emulated/0/innovation/animal/理赔/ZipImage
        String zipVideoDir = Global.mediaPayItem.getZipVideoDir();//storage/emulated/0/innovation/animal/理赔/zipVideoDir
        File file_current = new File(zipimageDir);
        File file_currentVideo = new File(zipVideoDir);
        File zipFile_image = new File(file_current.getParentFile(), Global.ZipFileName + ".zip");
        File zipFile_video = new File(file_currentVideo.getParentFile(), Global.ZipFileName + ".zip");
        dialogProcessUploadOnePay(zipFile_image);
        dialogProcessUploadOnePay(zipFile_video);
        Log.d("UploadOnePay", "processUpload_zipImage_one file name = " + zipFile_image.getAbsolutePath());
        Log.d("UploadOnePay", "processUpload_zipVideo_one video file name = " + zipFile_video.getAbsolutePath());
    }

    //压缩图片和视频为zip文件
    private void dialogProcessZip() {
        Message msg = Message.obtain(mProcessorHandler_new, MSG_PROCESSOR_ZIP);
        mProcessorHandler_new.sendMessage(msg);
    }

    /*---------------------------------------------上传投保图片zip文件-----------------------------------------------*/
    private void dialogProcessUploadOneImage() {
        Message msg = Message.obtain(mProcessorHandler_new, MSG_PROCESSOR_UPLOAD_IMAGEONE);
        mProcessorHandler_new.sendMessage(msg);
    }

    //上传投保图片zip文件
    private void dialogProcessUploadOneInsure(File file) {
        Message msg = Message.obtain(mProcessorHandler_new, MSG_PROCESSOR_UPLOAD_INSURE_ONE, file);
        mProcessorHandler_new.sendMessage(msg);
    }

    //上传理赔图片zip文件
    private void dialogProcessUploadOnePay(File file) {
        Message msg = Message.obtain(mProcessorHandler_new, MSG_PROCESSOR_UPLOAD_PAY_ONE, file);
        mProcessorHandler_new.sendMessage(msg);
    }

    private void dialogProcessUploadAll() {
        Message msg = Message.obtain(mProcessorHandler_new, MSG_PROCESSOR_UPLOAD_ALL);
        mProcessorHandler_new.sendMessage(msg);
    }

    public void transerPayData(String s, String s1, String s2) {

        getPayYiji = s;
        getPayErji = s1;
        getPaySanji = s2;
        Log.d("Media yiji", s);
        Log.d("Media erji", s1);
        Log.d("Media sanji", s2);

    }

    String imgBase64 = "";
    String filePath = "";
    String fileName = "";

    private class ProcessorHandler_new extends Handler {
        ProcessorHandler_new(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            mLoggerPig.i("ProcessorHandler message: %d, obj: %s", msg.what, msg.obj);
            switch (msg.what) {
                case MSG_PROCESSOR_ZIP:
                    processZip(true);
                    break;
                case MSG_PROCESSOR_UPLOAD_INSURE_ONE:
                    processUploadOne_Insure();
                    break;
                case MSG_PROCESSOR_UPLOAD_PAY_ONE:
                    // processUploadOne_Pay();
                    break;
                case MSG_PROCESSOR_UPLOAD_ALL:
                    processUpload_all();
                    processUpload_allNew();
                    break;
                case MSG_PROCESSOR_UPLOAD_IMAGEONE:
                    processZip(false);

                    // 投保时处理
//                    if (Global.model == Model.BUILD.value()) {
//                        String zipImageDir = Global.mediaInsureItem.getZipImageDir();//storage/emulated/0/innovation/animal/ZipImage
//                        File file_current = new File(zipImageDir);
//
//                        if (InnApplication.isOfflineMode) {
//                            offLineUploadPic(file_current);
//                        } else {
//                            processUploadOne_Insure();
//                            File zipFile_image = new File(file_current.getParentFile(), Global.ZipFileName + ".zip");
//                            Log.d("MediaProcess.java", "processUpload_zipImage_one file name = " + zipFile_image.getAbsolutePath());
//                        }
//                    }

                    // 理赔时处理
//                    if (Global.model == Model.VERIFY.value()) {
//                    }
                    String zipimageDir = Global.mediaPayItem.getZipImageDir();//storage/emulated/0/innovation/animal/ZipImage
                    processUploadOne_Pay();

                    break;
                case MSG_PROCESSOR_TEST:
                    break;
                default:
                    break;
            }
        }

        public void publishProgress(int what) {
            mUiHandler_new.sendEmptyMessage(what);
        }

        private void publishProgress(int model, int status) {
            Message msg = Message.obtain(mUiHandler_new, MSG_UI_FINISH_UPLOAD_IMG_ONE_FAILED, model, status);
            mUiHandler_new.sendMessage(msg);
        }

        private void publishProgress(int what, int model, int status, Object obj) {
            Message msg = Message.obtain(mUiHandler_new, what, model, status, obj);
            mUiHandler_new.sendMessage(msg);
        }

        //压缩图片文件
        private void processZip(boolean ifCloseDialog) {
            File file_num = null;
            String namepre = "";
            String imageDri = "";
            String zipimageDri = "";
            String videoDri = "";
            String zipvideoDri = "";
            publishProgress(MSG_UI_PROGRESS_ZIP_IMG);//"压缩图片请等待";
            if (Global.model == Model.BUILD.value()) {
                //获取编号文件
                file_num = Global.mediaInsureItem.getNumberFile();
                namepre = Global.mediaInsureItem.getZipFileName();
                imageDri = Global.mediaInsureItem.getImageDir();//storage/emulated/0/innovation/animal/20180227//File detectDir = new File(item.getDetectedDir(mContext));
                zipimageDri = Global.mediaInsureItem.getZipImageDir();//storage/emulated/0/innovation/animal/ZipImage
                videoDri = Global.mediaInsureItem.getVideoDir();
                Global.mediaInsureItem.getZipVideoDir();//storage/emulated/0/innovation/animal/ZipVideo
                zipvideoDri = Global.mediaInsureItem.getZipVideoDir();
            } else if (Global.model == Model.VERIFY.value()) {
                //获取编号文件
                file_num = Global.mediaPayItem.getNumberFile();
                namepre = Global.mediaPayItem.getZipFileName();
                imageDri = Global.mediaPayItem.getImageDir();
                zipimageDri = Global.mediaPayItem.getZipImageDir();//storage/emulated/0/innovation/animal/ZipImage
                videoDri = Global.mediaPayItem.getVideoDir();
                Global.mediaPayItem.getZipVideoDir();//storage/emulated/0/innovation/animal/ZipVideo
                zipvideoDri = Global.mediaPayItem.getZipVideoDir();
            }
            //获取图片文件
            File imageDir_new = new File(imageDri);//图片目录下的文件

            File[] files_image = imageDir_new.listFiles();
            if (files_image == null) {
                publishProgress(MSG_UI_FINISH_ZIP_FILE_NULL);//"文件不存在
                return;
            }
            if (files_image.length == 0) {
                publishProgress(MSG_UI_FINISH_ZIP_FILE_NULL);//"文件不存在
                return;
            }

            File imgFile = new File(imageDri + "/Angle-02");
            //获取文件夹下制定后缀的文件集合
            File[] files = imgFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jpeg");
                }
            });
            //zip打包文件
            File file_current = new File(zipimageDri);

            if (files != null && files.length > 0) {
                File f = files[0];
                File fileGSCImg = new File(file_current, f.getName());
                filePath = fileGSCImg.getAbsolutePath();
                fileName = f.getName();
                FileUtils.copyFile(f.getAbsolutePath(), fileGSCImg.getAbsolutePath());
                imgBase64 = fileToBase64(f);
            } else {
                imgBase64 = "";
            }
//            Log.e(TAG, "imgBase64: " + imgBase64);
            // 4. zip recognized image
            //加入编号文件
            File[] fs_image = new File[files_image.length + 1];
            for (int i = 0; i < files_image.length; i++) {
                fs_image[i] = files_image[i];
            }
            fs_image[files_image.length] = file_num;

            //打包图片文件

            File zipFile_image = new File(file_current, namepre + ".zip");
            ZipUtil.zipFiles(fs_image, zipFile_image);
            if (!zipFile_image.exists()) {
                publishProgress(MSG_UI_FINISH_ZIP_IMG_FAILED);//"压缩图片出错，请重新录制";
                reInitCurrentDir();
                return;
            }

            // TODO: 2018/8/15 By:LuoLu  zip video
            if (Global.UPLOAD_VIDEO_FLAG == true) {
                publishProgress(MSG_UI_PROGRESS_ZIP_VIDEO);
                File videoDir_new = new File(videoDri);//视频目录下的文件
                File[] files_video = videoDir_new.listFiles();

                //20180425
                if (files_video == null) {
                    publishProgress(MSG_UI_FINISH_ZIP_FILE_NULL);
                    return;
                }
                if (files_video.length == 0) {
                    publishProgress(MSG_UI_FINISH_ZIP_FILE_NULL);
                    return;
                }

                File[] fs_video = new File[files_video.length + 1];
                for (int i = 0; i < files_video.length; i++) {
                    fs_video[i] = files_video[i];
                }
                fs_video[files_video.length] = file_num;

                file_current = new File(zipvideoDri);
                File zipFile_video = new File(file_current, Global.ZipFileName + ".zip");
                ZipUtil.zipFiles(fs_video, zipFile_video);
                if (!zipFile_video.exists()) {
                    publishProgress(MSG_UI_FINISH_ZIP_VIDEO_FAILED);
                    reInitCurrentDir();
                    return;
                }
            }

            File videoDir_new = new File(videoDri);//当前视频目录下的文件
            File imageDri_new = new File(imageDri);//当前图片目录下的文件
            boolean deleteCurrentVideoResult = FileUtils.deleteFile(videoDir_new);
            boolean deleteCurrentImageResult = FileUtils.deleteFile(imageDri_new);
            if (deleteCurrentVideoResult == true) {
                mLoggerPig.i("当前视频文件夹删除成功！");
            }
            if (deleteCurrentImageResult == true) {
                mLoggerPig.i("当前图片文件夹删除成功！");
            }


            reInitCurrentDir(); //20180425
            if (Global.model == Model.BUILD.value()) {
                if (ifCloseDialog) {
                    mProgressDialog.dismiss();
                }
            } else if (Global.model == Model.VERIFY.value()) {
                UploadOnePay();
            }
        }

        private void processUpload_all() {
            UploadThread.getInstance(mContext).upload_all();
        }

        private void processUpload_allNew() {
            UploadThread.getInstance(mContext).upload();
        }

        public void processUploadOne_Insure() {
            int model = Global.model;
            publishProgress(MSG_UI_PROGRESS_UPLOAD_IMG_ONE);
            // TODO: 2018/8/18 By:LuoLu  清空当前图片文件夹
            String imageDri = "";
            String videoDri = "";
            if (Global.model == Model.BUILD.value()) {
                //获取编号文件
                imageDri = Global.mediaInsureItem.getImageDir();
                videoDri = Global.mediaInsureItem.getVideoDir();
                Global.mediaInsureItem.getZipVideoDir();
            }
            File videoDir_new = new File(videoDri);//当前视频目录下的文件
            File imageDri_new = new File(imageDri);//当前图片目录下的文件
            boolean deleteCurrentVideoResult = FileUtils.deleteFile(videoDir_new);
            boolean deleteCurrentImageResult = FileUtils.deleteFile(imageDri_new);
            if (deleteCurrentVideoResult) {
                mLoggerPig.i("当前投保视频文件夹删除成功！");
            }
            if (deleteCurrentImageResult) {
                mLoggerPig.i("当前投保图片文件夹删除成功！");
            }

            // TODO: 2018/8/18 By:LuoLu  是否传视频设置
            if (Global.UPLOAD_VIDEO_FLAG) {
                // TODO: 2018/8/16 By:LuoLu  投保建库上传图片包和视频包
                String zipImageDir = Global.mediaInsureItem.getZipImageDir();
                String zipVideoDir = Global.mediaInsureItem.getZipVideoDir();
                Log.i("zipVideoDir:", zipVideoDir);
                File file_zip = new File(zipImageDir);
                File file_zipVideo = new File(zipVideoDir);
                String fname_image = Global.ZipFileName + ".zip";
                Log.i("fname_video:", fname_image);
                File zipFile_image2 = new File(file_zip, fname_image); //要上传的文件
                File zipFile_video2 = new File(file_zipVideo, fname_image); //要上传的视频文件
                if (!zipFile_image2.exists()) {
                    Log.i("zipFile_image2:", "压缩图片文件夹不存在！！");
                    Toast.makeText(mActivity, "压缩图片文件夹为空！", Toast.LENGTH_SHORT).show();
                }
                if (!zipFile_video2.exists()) {
                    Log.i("zipVideo:", "压缩视频文件夹不存在！！");
                    Toast.makeText(mActivity, "压缩视频文件夹为空！", Toast.LENGTH_SHORT).show();
                }
                //读编号信息
                String fname_num = "number.txt";
                String content = null;
                String contentVideo = null;
                try {
                    content = FileUtils.getZipFileContent(zipFile_image2, fname_num);
                    contentVideo = FileUtils.getZipFileContent(zipFile_video2, fname_num);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                if (TextUtils.isEmpty(contentVideo)) {
                    return;
                }
                //读取用户信息
                SharedPreferences pref_user = mActivity.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
                int userId = 0;
                // TODO: 2018/8/20 By:LuoLu
                if (pref_user == null) {
                    mHandler.sendEmptyMessage(400);

                }
                userId = pref_user.getInt("uid", 0);

                UploadObject imgResp = upload_zipImage(model, zipFile_image2, userId, content);
                int status;
                if (imgResp == null || imgResp.status != HttpRespObject.STATUS_OK) {
                    status = imgResp == null ? -1 : imgResp.status;
                    publishProgress(model, status);
                    return;
                }
                //获取ib_id
                int lib_id = imgResp.upload_libId;
                String piginfo = imgResp.upload_pigInfo;
                if (imgResp == null || imgResp.status != HttpRespObject.STATUS_OK) {
                    status = imgResp == null ? -1 : imgResp.status;
                    publishProgress(model, status);
                    return;
                }
//            FileUtils.chageFileName(zipVideoDir, Global.ZipFileName, lib_id);
                // TODO: 2018/8/15 By:LuoLu
                publishProgress(MSG_UI_PROGRESS_ZIP_VIDEO_UPLOAD);
                UploadObject imgRespVideo = upload_zipVideo(model, lib_id, zipFile_video2, userId, content);

                if (imgRespVideo == null || imgRespVideo.status != HttpRespObject.STATUS_OK) {
                    status = imgRespVideo == null ? -1 : imgRespVideo.status;
                    publishProgress(model, status);
                    return;
                }
                if (emptyPigInfo(piginfo)) {
                    JSONObject jo = new JSONObject();
                    JsonHelper.putInt(jo, Utils.Upload.LIB_ID, imgResp.upload_libId);
                    JSONObject jo_env = new JSONObject();
                    try {
                        jo_env = new JSONObject(UploadHelper.getEnvInfo(mContext, null));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonHelper.putJsonObject(jo, Utils.Upload.LIB_ENVINFO, jo_env);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分", Locale.getDefault());
                    JsonHelper.putString(jo, Utils.Upload.LIB_CREATE_TIME, sdf.format(new Date(System.currentTimeMillis())));
                    imgResp.upload_pigInfo = jo.toString();
                }
                publishProgress(MSG_UI_FINISH_BUILD, model, 0, imgResp.upload_pigInfo);
            } else {
                // TODO: 2018/8/16 By:LuoLu  投保建库，上传图片包
                String zipImageDir = Global.mediaInsureItem.getZipImageDir();
                File file_zip = new File(zipImageDir);
                String fname_image = Global.ZipFileName + ".zip";
                Log.i("fname_video:", fname_image);
                File zipFile_image2 = new File(file_zip, fname_image); //要上传的文件
                if (!zipFile_image2.exists()) {
                    Log.i("zipFile_image2:", "压缩图片文件夹不存在！！");
                    return;
                }
                //读编号信息
                String fname_num = "number.txt";
                String content = null;
                try {
                    content = FileUtils.getZipFileContent(zipFile_image2, fname_num);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (TextUtils.isEmpty(content)) {
                    return;
                }

                //读取用户信息
                SharedPreferences pref_user = mActivity.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
//                int userId = pref_user.getInt("uid", 0);
                int userId = 0;
                // TODO: 2018/8/20 By:LuoLu
                if (pref_user == null) {
                    mHandler.sendEmptyMessage(400);

                }
                userId = pref_user.getInt("uid", 0);
                UploadObject imgResp = upload_zipImage(model, zipFile_image2, userId, content);
                int status;
                if (imgResp == null || imgResp.status != HttpRespObject.STATUS_OK) {
                    status = imgResp == null ? -1 : imgResp.status;
                    publishProgress(model, status);
                    return;
                }
                //获取ib_id
                int lib_id = imgResp.upload_libId;
                String piginfo = imgResp.upload_pigInfo;
                if (imgResp == null || imgResp.status != HttpRespObject.STATUS_OK) {
                    status = imgResp == null ? -1 : imgResp.status;
                    publishProgress(model, status);
                    return;
                }
                if (emptyPigInfo(piginfo)) {
                    JSONObject jo = new JSONObject();
                    JsonHelper.putInt(jo, Utils.Upload.LIB_ID, imgResp.upload_libId);
                    JSONObject jo_env = new JSONObject();
                    try {
                        jo_env = new JSONObject(UploadHelper.getEnvInfo(mContext, null));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonHelper.putJsonObject(jo, Utils.Upload.LIB_ENVINFO, jo_env);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分", Locale.getDefault());
                    JsonHelper.putString(jo, Utils.Upload.LIB_CREATE_TIME, sdf.format(new Date(System.currentTimeMillis())));
                    imgResp.upload_pigInfo = jo.toString();
                }
                publishProgress(MSG_UI_FINISH_BUILD, model, 0, imgResp.upload_pigInfo);
            }
        }


        private UploadObject upload_zipImage(int model, File zipFile_image, int uid, String libMum) {
            UploadObject imgResp = HttpUtils.uploadImages(mContext, model, zipFile_image, uid, libMum);
            if (imgResp == null || imgResp.status != HttpRespObject.STATUS_OK) {
                int status = imgResp == null ? -1 : imgResp.status;
                mLoggerPig.e("upload images failed, status: %d", status);
//                processUploadOne_Insure();
                return imgResp;
            }
            //boolean resultStatus = FileUtils.deleteFile(zipFile_image);
           /* if (resultStatus == true) {
                mLoggerPig.i("本地图片打包文件删除成功！！");
            }*/
            return imgResp;
        }

        // TODO: 2018/8/15 By:LuoLu  upload video
        private UploadObject upload_zipVideo(int model, int lib_id, File videoZipFile, int uid, String libMum) {
            UploadObject imgResp = HttpUtils.uploadVideo(mContext, model, lib_id, videoZipFile, uid, libMum);
            if (imgResp == null || imgResp.status != HttpRespObject.STATUS_OK) {
                int status = imgResp == null ? -1 : imgResp.status;
                mLoggerPig.e("upload video failed, status: %d", status);
                return imgResp;
            }
            boolean result = FileUtils.deleteFile(videoZipFile);
            if (result == true) {
                mLoggerPig.i("本地视频打包文件删除成功！！");
            }
            return imgResp;
        }
    }


    private void processUploadOne_Pay() {
        // TODO: 2018/8/18 By:LuoLu  清空当前图片文件夹
        String imageDri = "";
        String videoDri = "";
        if (Global.model == Model.VERIFY.value()) {
            //获取编号文件
            imageDri = Global.mediaPayItem.getImageDir();
            videoDri = Global.mediaPayItem.getVideoDir();
            Global.mediaPayItem.getZipVideoDir();//storage/emulated/0/innovation/animal/ZipVideo
        }
        File videoDirNew = new File(videoDri);//当前视频目录下的文件
        File imageDriNew = new File(imageDri);//当前图片目录下的文件
        boolean deleteCurrentVideoResult = FileUtils.deleteFile(videoDirNew);
        boolean deleteCurrentImageResult = FileUtils.deleteFile(imageDriNew);
        if (deleteCurrentVideoResult) {
            mLoggerPig.i("当前理赔视频文件夹删除成功！");
        }
        if (deleteCurrentImageResult) {
            mLoggerPig.i("当前理赔图片文件夹删除成功！");
        }


        int model = Model.VERIFY.value();
        //publishProgress(MSG_UI_PROGRESS_UPLOAD_IMG_ONE);

        /* 处理图片文件 start*/
        String zipImageDir = Global.mediaPayItem.getZipImageDir();
        File fileZip = new File(zipImageDir);
        String fnameImage = Global.ZipFileName + ".zip";
        Log.e(TAG, "processUploadOne_Pay: " + fnameImage);
        File zipFileImage2 = new File(fileZip, fnameImage); //要上传的图片ZIP文件
        /* 处理图片文件 end*/

        /* 处理视频文件 start */
        String zipVideoDir = Global.mediaPayItem.getZipVideoDir();
        Log.i("zipVideoDir:", zipVideoDir);
        File fileZipVideo = new File(zipVideoDir);
        String fnameVideo = Global.ZipFileName + ".zip";
        File zipFileVideo2 = new File(fileZipVideo, fnameVideo); //要上传的视频ZIP文件
        Log.i("zipVideo=====:", zipFileVideo2.getAbsolutePath());
        /* 处理视频文件 end */

        //如果图片文件不存在直接返回
        if (!zipFileImage2.exists()) {
            Log.i("zipImage:", "压缩图片文件夹不存在！！");
            Toast.makeText(mActivity, "压缩视频文件夹为空。", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!zipFileVideo2.exists()) {
            Log.i("zipVideo:", "压缩视频文件夹不存在！！");
            Toast.makeText(mActivity, "压缩视频文件夹为空。", Toast.LENGTH_SHORT).show();
        }

        String timesflag = zipFileVideo2.getName();

        //理赔提交
        preCommitForLiPei(zipFileImage2, timesflag);
    }

    private static boolean emptyPigInfo(String pigInfo) {
        if (TextUtils.isEmpty(pigInfo)) {
            return true;
        }
        JSONObject jo = null;
        try {
            jo = new JSONObject(pigInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo == null;
    }

    private class UiHandler_new extends Handler {
        UiHandler_new(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            mLoggerPig.i("UiHandler message: %d", msg.what);
            final String showMessage;
            switch (msg.what) {
                case MSG_UI_PROGRESS_EXTRACT_IMG:
                    showMessage = "正在提取图片......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_PROGRESS_DETECT_IMG:
                    showMessage = "正在识别图片(" + msg.arg2 + ")......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_PROGRESS_ZIP_IMG:
                    showMessage = "压缩图片，请等待......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_PROGRESS_ZIP_VIDEO:
                    showMessage = "压缩视频，请等待......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_PROGRESS_ZIP_VIDEO_UPLOAD:
                    showMessage = "正在上传视频，请等待......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_FINISH_ZIP_INSURE:
                    showMessage = "压缩图片完成，是否立即上传";
                    mProgressDialog.setMessage(showMessage);
                    // updateProgressDialogTwoButton(showMessage);
                    break;
                case MSG_UI_FINISH_ZIP_PAY:
                    UploadOnePay();
                    break;
                case MSG_UI_PROGRESS_UPLOAD_IMG:
                    showMessage = msg.arg1 == Model.BUILD.value() ? "正在投保......" : "正在理赔......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_FINISH_NO_DETECTED_IMG_EXIST:
                    showMessage = "没有识别出所要的图片, 请重新录制";
                    updateProgressDialogOneButton(showMessage);
                    break;
                case MSG_UI_FINISH_MISSING_DETECT_IMG:
                    String info = (String) msg.obj;
                    showMessage = "缺少角度类型：" + info + " 的图片, 请重新录制";
                    updateProgressDialogOneButton(showMessage);
                    break;
                case MSG_UI_FINISH_ZIP_IMG_FAILED:
                    showMessage = "压缩图片出错，请重新录制";
                    updateProgressDialogOneButton(showMessage);
                    break;
                case MSG_UI_FINISH_ZIP_VIDEO_FAILED:
                    showMessage = "压缩视频出错，请重新录制";
                    updateProgressDialogOneButton(showMessage);
                    break;
                case MSG_UI_FINISH_ZIP_FILE_NULL:
                    showMessage = "待压缩的文件不存在，请重新录制";
                    updateProgressDialogOneButton(showMessage);
                    break;
                case MSG_UI_PROGRESS_UPLOAD_IMG_ONE:
                    showMessage = "正在上传建库的图片......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_FINISH_NOZIP:
                    showMessage = "没有需要上传的文件......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialogOneButton(showMessage);
                    break;
                case MSG_UI_FINISH_UPLOAD_IMG_ONE_FAILED:
                    showMessage = createUploadImgFailedMsg(msg.arg1, msg.arg2);
                    updateProgressDialogTwoButton(showMessage);
                    break;
                case MSG_UI_FINISH_UPLOAD_IMG_ONE_SUCCESS:
                    showMessage = "本次图片上传成功......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialogOneButton(showMessage);
                    break;
                case MSG_UI_PROGRESS_UPLOAD_ALL:
                    showMessage = "开始上传全部文件......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialog(showMessage);
                    break;
                case MSG_UI_FINISH_UPLOAD_ALL:
                    showMessage = "全部文件上传完成......";
                    mProgressDialog.setMessage(showMessage);
                    updateProgressDialogOneButton(showMessage);
                    break;
                //理赔
                case MSG_UI_FINISH_VERIFY:
                    final Context context = mProgressDialog.getContext();
//                    mProgressDialog.dismiss();
//                    mProgressDialog = null;
                    obj_cow_verify = (String) msg.obj;
                    final LipeiDialog dialogVerify = new LipeiDialog(context);
                    View.OnClickListener listener_new = v -> {
                        dialogVerify.dismiss();
                        if (Global.model == Model.VERIFY.value()) {
                            try {
                                String showMessage1 = "查询验证结果，请等待......";
                                mProgressDialog.setMessage(showMessage1);
                                updateProgressDialog(showMessage1);
                                JSONObject jo = new JSONObject(obj_cow_verify);
                                cow_libid = JsonHelper.getString(jo, Utils.Upload.LIB_ID);
                                str_address = str_address.equals("") ? "未知位置" : str_address;
//                                pigBuildResp2 = HttpUtils.upload_build(String.valueOf(Model.VERIFY.value()), cow_libid, mcowear_number.getText().toString(), mNumberEdit.getText().toString(), mContext);

                                Log.i("Mplipei cow_libid:", cow_libid);
                                Log.i("Mlipei getCowEarNumber:", getCowEarNumber);
                                Log.i("lp getlipeiTempNumber:", getlipeiTempNumber);
                                pigBuildResp2 = HttpUtils.upload_build(String.valueOf(Model.VERIFY.value()), cow_libid, getCowEarNumber, getlipeiTempNumber, mContext);
                                mHandler.sendEmptyMessage(2);
                            } catch (JSONException e) {

                                AlertDialogManager.showMessageDialogOne(mActivity, "提示", "查询失败", new AlertDialogManager.DialogInterface() {
                                    @Override
                                    public void onPositive() {
                                        mActivity.finish();
                                    }

                                    @Override
                                    public void onNegative() {

                                    }
                                });
                                e.printStackTrace();
                            }
                        }
                    };
                    if (msg.arg1 == Model.VERIFY.value()) {
                        dialogVerify.setTitle("理赔数据提交成功");
                        dialogVerify.setBtnLipeiNext("下一步", listener_new);
                    }
                    //dialogVerify.show();
                    //  Log.i(TAG, "理赔" + Model.VERIFY.value());
                    break;

                case MSG_UI_FINISH_BUILD:
                    Context context2 = mProgressDialog.getContext();
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                    final InfoDialog dialogBuild = new InfoDialog(context2, Model.BUILD.value(), (String) msg.obj);
                    final String str_obj = (String) msg.obj;
                    View.OnClickListener listener2 = v -> {
                        dialogBuild.dismiss();
                        Intent intent = new Intent(mActivity, ToubaoCowInfoActivity.class);
                        if (Global.model == Model.VERIFY.value()) {
//                                intent.putExtra("lipeiTempNumber", mNumberEdit.getText().toString());
                        }
                        if (Global.model == Model.BUILD.value()) {
//                                intent.putExtra("libid", mNumberEdit.getText().toString());
                            try {
                                JSONObject jo = new JSONObject(str_obj);
                                int libid = JsonHelper.getInt(jo, Utils.Upload.LIB_ID);

                                intent.putExtra("LipeiTempNumber", "");
                                intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                intent.putExtra("libid", String.valueOf(libid));
                                intent.putExtra("pagetype", "toubao");
                                intent.putExtra("cowinfo", "");
                                mActivity.startActivity(intent);
                                mActivity.finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    };
                    if (msg.arg1 == Model.BUILD.value()) {
                        if (InnApplication.isOfflineMode) {
                            dialogBuild.setTitle("离线数据采集成功");
                        } else {
                            dialogBuild.setTitle("采集数据提交成功");
                            dialogBuild.setInfo(msg.arg1, (String) msg.obj);
                        }
                        dialogBuild.setPositiveButton("下一步", listener2);
                    }
                    dialogBuild.show();
                    //Log.i(TAG, "投保" + Model.BUILD.value());
                    break;

                default:
                    showMessage = ".....";
                    mProgressDialog.setMessage(showMessage);
            }
        }

        private void updateProgressDialog(String showMessage) {
            Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setVisibility(View.GONE);
            }
            Button negative = mProgressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
            if (negative != null) {
                negative.setVisibility(View.GONE);
            }
            mProgressDialog.setMessage(showMessage);
        }

        private void updateProgressDialogOneButton(String showMessage) {
            Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
            if (positive != null) {
                positive.setVisibility(View.VISIBLE);
            }
            mProgressDialog.setMessage(showMessage);
        }

        private void updateProgressDialogTwoButton(String showMessage) {
            Button positive = mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE);
            positive.setText("重试");
            positive.setOnClickListener(view -> {
                if (Global.model == Model.BUILD.value()) {
                    UploadOneInsure();
                } else if (Global.model == Model.VERIFY.value()) {
                    UploadOnePay();
                }
            });
            if (positive != null) {
                positive.setVisibility(View.VISIBLE);
            }
            Button negative = mProgressDialog.getButton(ProgressDialog.BUTTON_NEGATIVE);
            negative.setText("重新采集");
            negative.setOnClickListener(view -> {

                mProgressDialog.dismiss();
                collectNumberHandler.sendEmptyMessage(2);
                Intent intent = new Intent(mActivity, DetectorActivity_pig.class);
                intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                mActivity.startActivity(intent);
            });
            if (negative != null) {
                negative.setVisibility(View.VISIBLE);
            }
            mProgressDialog.setMessage(showMessage);
        }


        private String createUploadImgFailedMsg(int model, int status) {
            String msg = model == Model.BUILD.value() ? "投保建库失败！" : "理赔建库失败！";
            switch (status) {
                case RespObject.STATUS_NET_ERR:
                    msg += "网络连接异常，请重试！";
                    break;
                case RespObject.STATUS_101:
                    msg += "上传失败，参数错误";
                    break;
                case RespObject.STATUS_102:
                    msg += "理赔失败，未找相关信息，请投保";
                    break;
                case RespObject.STATUS_103:
                    msg += "保存数据失败";
                    break;
                case RespObject.STATUS_104:
                    msg += "请登录后再拍摄视频";
                    break;
                case RespObject.STATUS_105:
                    msg += "返回数据为空";
                    break;
                case RespObject.STATUS_106:
                    msg += "操作不成功";
                    break;
                default:
                    msg += "";
            }
            return msg;
        }

    }

    private final DialogInterface.OnClickListener mPOSITIVEClickListener = (dialog, which) -> {
        dialog.dismiss();
        initDialogs(mActivity);
    };

    private final DialogInterface.OnClickListener mNEGATIVEClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            mProgressDialog = null;
        }
    };

    private String stampToDate(long timeMillis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
    }

    private String verification_status1;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.i("理赔库查询pigBuildRespResult", String.valueOf(pigBuildRespResult));
                    if (pigBuildRespResult != null) {
                        if (pigBuildRespResult.status == 1) {


                            Log.i("理赔库查询的pid", String.valueOf(pigBuildRespResult.build_result_pid));
                            Log.i("最后pigBuildRespResult", String.valueOf(pigBuildRespResult.data));
                            // TODO: 2018/8/7 By:LuoLu

                            verification_status1 = pigBuildRespResult.buildVertificaton;

                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                            if (pigBuildRespResult.buildStatus == 1) {
                                if (verification_status1.contains("Yes")) {
                                    if (verification_status.contains("Yes")) {
                                        String strs_similarity_toubao = pigBuildResp2.build_result_similarity;
                                        String strs_similarity = pigBuildRespResult.build_result_similarity;
                                        String str_images0;
                                        String str_images1;
                                        String str_images2;
                                        String[] strs_img0;
                                        String[] strs_img1;
                                        String[] strs_img2;

                                        String str_img0 = "";
                                        String str_img1 = "";
                                        String str_img2 = "";

                                        String[] strs_similarity_all_toubao;
                                        String last_similarity;
                                        if (strs_similarity.contains(",")) {
                                            String[] strsss_similarity = strs_similarity.split(",");
                                            if (strsss_similarity.length == 3) {
                                                String[] strs_similarity_all0 = String.valueOf(strsss_similarity[0]).split(":");
                                                String[] strs_similarity_all1 = String.valueOf(strsss_similarity[1]).split(":");
                                                String[] strs_similarity_all2 = String.valueOf(strsss_similarity[2]).split(":");

                                                str_images0 = JsonHelper.getString(pigBuildRespResult.build_result_images, String.valueOf(strs_similarity_all0[0]));
                                                str_images0 = str_images0.substring(1, str_images0.length() - 1);
                                                strs_img0 = str_images0.split(",");

                                                str_images1 = JsonHelper.getString(pigBuildRespResult.build_result_images, String.valueOf(strs_similarity_all1[0]));
                                                str_images1 = str_images1.substring(1, str_images1.length() - 1);
                                                strs_img1 = str_images1.split(",");

                                                str_images2 = JsonHelper.getString(pigBuildRespResult.build_result_images, String.valueOf(strs_similarity_all2[0]));
                                                str_images2 = str_images2.substring(1, str_images2.length() - 1);
                                                strs_img2 = str_images2.split(",");

                                                str_img0 = strs_img0[0].substring(1, strs_img0[0].length() - 1).replace("\\", "/");
                                                str_img1 = strs_img1[0].substring(1, strs_img1[0].length() - 1).replace("\\", "/");
                                                str_img2 = strs_img2[0].substring(1, strs_img2[0].length() - 1).replace("\\", "/");
                                            } else if (strsss_similarity.length == 2) {
                                                String[] strs_similarity_all0 = String.valueOf(strsss_similarity[0]).split(":");
                                                String[] strs_similarity_all1 = String.valueOf(strsss_similarity[1]).split(":");

                                                str_images0 = JsonHelper.getString(pigBuildRespResult.build_result_images, String.valueOf(strs_similarity_all0[0]));
                                                str_images0 = str_images0.substring(1, str_images0.length() - 1);
                                                strs_img0 = str_images0.split(",");

                                                str_images1 = JsonHelper.getString(pigBuildRespResult.build_result_images, String.valueOf(strs_similarity_all1[0]));
                                                str_images1 = str_images1.substring(1, str_images1.length() - 1);
                                                strs_img1 = str_images1.split(",");

                                                str_img0 = strs_img0[0].substring(1, strs_img0[0].length() - 1).replace("\\", "/");
                                                str_img2 = strs_img1[0].substring(1, strs_img1[0].length() - 1).replace("\\", "/");
                                            }
                                        } else {
                                            String[] strs_similarity_all = strs_similarity.split(":");

                                            str_images0 = JsonHelper.getString(pigBuildRespResult.build_result_images, String.valueOf(strs_similarity_all[0]));
                                            str_images0 = str_images0.substring(1, str_images0.length() - 1);
                                            strs_img0 = str_images0.split(",");

                                            str_img1 = strs_img0[0].substring(1, strs_img0[0].length() - 1).replace("\\", "/");
                                        }
                                        final LipeiResultDialog dialogLipeiResult = new LipeiResultDialog(mActivity);
                                        View.OnClickListener listener_new = v -> {
                                            dialogLipeiResult.dismiss();
                                            Toast.makeText(mActivity, "成功生成一条理赔单", Toast.LENGTH_SHORT).show();
                                            mActivity.finish();
                                        };
                                        View.OnClickListener listener_ReCollect = v -> {
                                            dialogLipeiResult.dismiss();
                                            collectNumberHandler.sendEmptyMessage(2);
                                            Intent intent = new Intent(mActivity, DetectorActivity_pig.class);
                                            intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                            intent.putExtra("LipeiTempNumber", getlipeiTempNumber);
                                            mActivity.startActivity(intent);
                                        };

                                        dialogLipeiResult.setTitle("验证结果");
                                        dialogLipeiResult.setBtnGoApplication("直接申请", listener_new);
                                        dialogLipeiResult.setBtnReCollect("重新拍摄", listener_ReCollect);

                                        if (strs_similarity == null) {
                                            dialogLipeiResult.setLipeiResultmessage("在历史理赔库中没有相似对象！");
                                        }
                                        //投保库中相似度
                                        if (strs_similarity_toubao.contains(",")) {
                                            String[] strsss_similarity_toubao = strs_similarity_toubao.split(",");
                                            strs_similarity_all_toubao = String.valueOf(strsss_similarity_toubao[0]).split(":");
                                        } else {
                                            strs_similarity_all_toubao = String.valueOf(strs_similarity_toubao).split(":");
                                        }

                                        if (strs_similarity.contains(",")) {
                                            String[] strsss_similarity = strs_similarity.split(",");
                                            String[] strs_similarity_all0 = String.valueOf(strsss_similarity[0]).split(":");
                                            double int_similarity_toubao = Double.parseDouble(String.valueOf(strs_similarity_all_toubao[1]).substring(0, String.valueOf(strs_similarity_all_toubao[1]).length() - 1));
                                            double int_similarity = Double.parseDouble(String.valueOf(strs_similarity_all0[1]).substring(0, String.valueOf(strs_similarity_all0[1]).length() - 1));
//                                        if (int_similarity_toubao > int_similarity) {
//                                            last_similarity = "在投保库有相似对象，相似度" + String.valueOf(strs_similarity_all_toubao[1]) + "。";
//                                        } else {
                                            last_similarity = "在历史理赔库有相似对象，相似度" + String.valueOf(strs_similarity_all0[1]) + "。";
//                                        }
                                            if (strsss_similarity.length == 2) {
                                                dialogLipeiResult.setImage1(str_img0);
                                                dialogLipeiResult.setImage3(str_img2);
                                                dialogLipeiResult.setLipeiResultmessage(last_similarity);
                                            } else if (strsss_similarity.length == 3) {
                                                dialogLipeiResult.setImage1(str_img0);
                                                dialogLipeiResult.setImage2(str_img1);
                                                dialogLipeiResult.setImage3(str_img2);
                                                dialogLipeiResult.setLipeiResultmessage(last_similarity);
                                            }
                                        } else {
                                            String[] strs_similarity_all0 = String.valueOf(strs_similarity).split(":");
                                            double int_similarity_toubao = Double.parseDouble(String.valueOf(strs_similarity_all_toubao[1]).substring(0, String.valueOf(strs_similarity_all_toubao[1]).length() - 1));
                                            double int_similarity = Double.parseDouble(String.valueOf(strs_similarity_all0[1]).substring(0, String.valueOf(strs_similarity_all0[1]).length() - 1));
//                                        if (int_similarity_toubao > int_similarity) {
//                                            last_similarity = "在投保库有相似对象，相似度" + String.valueOf(strs_similarity_all_toubao[1]) + "。";
//                                        } else {
                                            last_similarity = "在历史理赔库有相似对象，相似度" + String.valueOf(strs_similarity_all0[1]) + "。";
//                                        }
                                            dialogLipeiResult.setImage2(str_img1);
                                            dialogLipeiResult.setLipeiResultmessage(last_similarity);
                                        }
                                        dialogLipeiResult.show();
                                    } else if (verification_status.contains("No")) {
                                        String strs_similarity = pigBuildRespResult.build_result_similarity;
                                        String str_images0;
                                        String str_images1;
                                        String str_images2;
                                        String[] strs_img0;
                                        String[] strs_img1;
                                        String[] strs_img2;
                                        String str_img0 = "";
                                        String str_img1 = "";
                                        String str_img2 = "";
                                        if (strs_similarity.contains(",")) {
                                            String[] strsss_similarity = strs_similarity.split(",");
                                            if (strsss_similarity.length == 3) {
                                                String[] strs_similarity_all0 = String.valueOf(strsss_similarity[0]).split(":");
                                                String[] strs_similarity_all1 = String.valueOf(strsss_similarity[1]).split(":");
                                                String[] strs_similarity_all2 = String.valueOf(strsss_similarity[2]).split(":");
                                                str_images0 = JsonHelper.getString(pigBuildRespResult.build_result_images, String.valueOf(strs_similarity_all0[0]));
                                                str_images0 = str_images0.substring(1, str_images0.length() - 1);
                                                strs_img0 = str_images0.split(",");
                                                str_images1 = JsonHelper.getString(pigBuildRespResult.build_result_images, String.valueOf(strs_similarity_all1[0]));
                                                str_images1 = str_images1.substring(1, str_images1.length() - 1);
                                                strs_img1 = str_images1.split(",");
                                                str_images2 = JsonHelper.getString(pigBuildRespResult.build_result_images, String.valueOf(strs_similarity_all2[0]));
                                                str_images2 = str_images2.substring(1, str_images2.length() - 1);
                                                strs_img2 = str_images2.split(",");
                                                str_img0 = strs_img0[0].substring(1, strs_img0[0].length() - 1).replace("\\", "/");
                                                str_img1 = strs_img1[0].substring(1, strs_img1[0].length() - 1).replace("\\", "/");
                                                str_img2 = strs_img2[0].substring(1, strs_img2[0].length() - 1).replace("\\", "/");
                                            } else if (strsss_similarity.length == 2) {
                                                String[] strs_similarity_all0 = String.valueOf(strsss_similarity[0]).split(":");
                                                String[] strs_similarity_all1 = String.valueOf(strsss_similarity[1]).split(":");
                                                str_images0 = JsonHelper.getString(pigBuildRespResult.build_result_images, String.valueOf(strs_similarity_all0[0]));
                                                str_images0 = str_images0.substring(1, str_images0.length() - 1);
                                                strs_img0 = str_images0.split(",");
                                                str_images1 = JsonHelper.getString(pigBuildRespResult.build_result_images, String.valueOf(strs_similarity_all1[0]));
                                                str_images1 = str_images1.substring(1, str_images1.length() - 1);
                                                strs_img1 = str_images1.split(",");
                                                str_img0 = strs_img0[0].substring(1, strs_img0[0].length() - 1).replace("\\", "/");
                                                str_img2 = strs_img1[0].substring(1, strs_img1[0].length() - 1).replace("\\", "/");
                                            }
                                        } else {
                                            String[] strs_similarity_all = strs_similarity.split(":");
                                            str_images0 = JsonHelper.getString(pigBuildRespResult.build_result_images, String.valueOf(strs_similarity_all[0]));
                                            str_images0 = str_images0.substring(1, str_images0.length() - 1);
                                            strs_img0 = str_images0.split(",");
                                            str_img1 = strs_img0[0].substring(1, strs_img0[0].length() - 1).replace("\\", "/");
                                        }
                                        final LipeiResultDialog dialogLipeiResult = new LipeiResultDialog(mActivity);
                                        View.OnClickListener listener_new = v -> {
                                            dialogLipeiResult.dismiss();
                                            Toast.makeText(mActivity, "成功生成一条理赔单", Toast.LENGTH_SHORT).show();
                                            mActivity.finish();
                                        };
                                        View.OnClickListener listener_ReCollect = v -> {
                                            dialogLipeiResult.dismiss();
                                            collectNumberHandler.sendEmptyMessage(2);
                                            Intent intent = new Intent(mActivity, DetectorActivity_pig.class);
                                            intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                            intent.putExtra("LipeiTempNumber", getlipeiTempNumber);
                                            mActivity.startActivity(intent);

                                        };
                                        dialogLipeiResult.setTitle("验证结果");
                                        dialogLipeiResult.setBtnGoApplication("直接申请", listener_new);
                                        dialogLipeiResult.setBtnReCollect("重新拍摄", listener_ReCollect);

                                        if (strs_similarity == null) {
                                            dialogLipeiResult.setLipeiResultmessage("在历史理赔库中不存在此对象！！");
                                        }

                                        if (strs_similarity.contains(",")) {
                                            String[] strsss_similarity = strs_similarity.split(",");
                                            String[] strs_similarity_all0 = String.valueOf(strsss_similarity[0]).split(":");

                                            if (strsss_similarity.length == 2) {

                                                dialogLipeiResult.setImage1(str_img0);
                                                dialogLipeiResult.setImage3(str_img2);
                                                dialogLipeiResult.setLipeiResultmessage("在历史理赔库中有相似对象，相似度" + String.valueOf(strs_similarity_all0[1]) + "");
                                            } else if (strsss_similarity.length == 3) {
                                                dialogLipeiResult.setImage1(str_img0);
                                                dialogLipeiResult.setImage2(str_img1);
                                                dialogLipeiResult.setImage3(str_img2);
                                                dialogLipeiResult.setLipeiResultmessage("在历史理赔库中有相似对象，相似度" + String.valueOf(strs_similarity_all0[1]) + "");
                                            }
                                        } else {
                                            String[] strs_similarity_all0 = String.valueOf(strs_similarity).split(":");

                                            dialogLipeiResult.setImage2(str_img1);
                                            dialogLipeiResult.setLipeiResultmessage("在历史理赔库中有相似对象，相似度" + String.valueOf(strs_similarity_all0[1]) + "");
                                        }
                                        dialogLipeiResult.show();
                                    }
                                } else if (verification_status1.contains("No")) {
                                    if (verification_status.contains("Yes")) {

                                        String strs_similarity = pigBuildResp2.build_result_similarity;
                                        String str_images0;
                                        String str_images1;
                                        String str_images2;
                                        String[] strs_img0;
                                        String[] strs_img1;
                                        String[] strs_img2;

                                        String str_img0 = "";
                                        String str_img1 = "";
                                        String str_img2 = "";

                                        if (strs_similarity.contains(",")) {
                                            String[] strsss_similarity = strs_similarity.split(",");

                                            if (strsss_similarity.length == 3) {
                                                String[] strs_similarity_all0 = String.valueOf(strsss_similarity[0]).split(":");
                                                String[] strs_similarity_all1 = String.valueOf(strsss_similarity[1]).split(":");
                                                String[] strs_similarity_all2 = String.valueOf(strsss_similarity[2]).split(":");

                                                str_images0 = JsonHelper.getString(pigBuildResp2.build_result_images, String.valueOf(strs_similarity_all0[0]));
                                                str_images0 = str_images0.substring(1, str_images0.length() - 1);
                                                strs_img0 = str_images0.split(",");

                                                str_images1 = JsonHelper.getString(pigBuildResp2.build_result_images, String.valueOf(strs_similarity_all1[0]));
                                                str_images1 = str_images1.substring(1, str_images1.length() - 1);
                                                strs_img1 = str_images1.split(",");

                                                str_images2 = JsonHelper.getString(pigBuildResp2.build_result_images, String.valueOf(strs_similarity_all2[0]));
                                                str_images2 = str_images2.substring(1, str_images2.length() - 1);
                                                strs_img2 = str_images2.split(",");

                                                str_img0 = strs_img0[0].substring(1, strs_img0[0].length() - 1).replace("\\", "/");
                                                str_img1 = strs_img1[0].substring(1, strs_img1[0].length() - 1).replace("\\", "/");
                                                str_img2 = strs_img2[0].substring(1, strs_img2[0].length() - 1).replace("\\", "/");
                                            } else if (strsss_similarity.length == 2) {
                                                String[] strs_similarity_all0 = String.valueOf(strsss_similarity[0]).split(":");
                                                String[] strs_similarity_all1 = String.valueOf(strsss_similarity[1]).split(":");

                                                str_images0 = JsonHelper.getString(pigBuildResp2.build_result_images, String.valueOf(strs_similarity_all0[0]));
                                                str_images0 = str_images0.substring(1, str_images0.length() - 1);
                                                strs_img0 = str_images0.split(",");

                                                str_images1 = JsonHelper.getString(pigBuildResp2.build_result_images, String.valueOf(strs_similarity_all1[0]));
                                                str_images1 = str_images1.substring(1, str_images1.length() - 1);
                                                strs_img1 = str_images1.split(",");

                                                str_img0 = strs_img0[0].substring(1, strs_img0[0].length() - 1).replace("\\", "/");
                                                str_img2 = strs_img1[0].substring(1, strs_img1[0].length() - 1).replace("\\", "/");
                                            }
                                        } else {
                                            String[] strs_similarity_all = strs_similarity.split(":");

                                            str_images0 = JsonHelper.getString(pigBuildResp2.build_result_images, String.valueOf(strs_similarity_all[0]));
                                            str_images0 = str_images0.substring(1, str_images0.length() - 1);
                                            strs_img0 = str_images0.split(",");

                                            str_img1 = strs_img0[0].substring(1, strs_img0[0].length() - 1).replace("\\", "/");
                                        }

                                        final LipeiResultDialog dialogLipeiResult = new LipeiResultDialog(mActivity);
                                        View.OnClickListener listener_new = v -> {
                                            dialogLipeiResult.dismiss();
                                            Toast.makeText(mActivity, "成功生成一条理赔单", Toast.LENGTH_SHORT).show();
                                            mActivity.finish();
                                        };
                                        View.OnClickListener listener_ReCollect = v -> {
                                            dialogLipeiResult.dismiss();
                                            collectNumberHandler.sendEmptyMessage(2);
                                            Intent intent = new Intent(mActivity, DetectorActivity_pig.class);
                                            intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                            intent.putExtra("LipeiTempNumber", getlipeiTempNumber);
                                            mActivity.startActivity(intent);
                                        };
                                        dialogLipeiResult.setTitle("验证结果");
                                        dialogLipeiResult.setBtnGoApplication("直接申请", listener_new);
                                        dialogLipeiResult.setBtnReCollect("重新拍摄", listener_ReCollect);
                                        if (strs_similarity == null) {
                                            dialogLipeiResult.setLipeiResultmessage("在历史理赔库中没有相似对象！");
                                        }

                                        if (strs_similarity.contains(",")) {
                                            String[] strsss_similarity = strs_similarity.split(",");
                                            String[] strs_similarity_all0 = String.valueOf(strsss_similarity[0]).split(":");

                                            if (strsss_similarity.length == 2) {
                                                dialogLipeiResult.setImage1(str_img0);
                                                dialogLipeiResult.setImage3(str_img2);
                                                dialogLipeiResult.setLipeiResultmessage("在投保库中有相似对象，相似度" + String.valueOf(strs_similarity_all0[1]) + "。");
                                            } else if (strsss_similarity.length == 3) {
                                                dialogLipeiResult.setImage1(str_img0);
                                                dialogLipeiResult.setImage2(str_img1);
                                                dialogLipeiResult.setImage3(str_img2);
                                                dialogLipeiResult.setLipeiResultmessage("在投保库中有相似对象，相似度" + String.valueOf(strs_similarity_all0[1]) + "。");
                                            }
                                        } else {
                                            String[] strs_similarity_all0 = String.valueOf(strs_similarity).split(":");

                                            dialogLipeiResult.setImage2(str_img1);
                                            dialogLipeiResult.setLipeiResultmessage("在投保库中有相似对象，相似度" + String.valueOf(strs_similarity_all0[1]) + "。");
                                        }
                                        dialogLipeiResult.show();
                                    } else if (verification_status.contains("No")) {
                                        String strs_similarity = pigBuildResp2.build_result_similarity;
                                        String str_images0;
                                        String str_images1;
                                        String str_images2;
                                        String[] strs_img0;
                                        String[] strs_img1;
                                        String[] strs_img2;

                                        String str_img0 = "";
                                        String str_img1 = "";
                                        String str_img2 = "";

                                        if (strs_similarity.contains(",")) {
                                            String[] strsss_similarity = strs_similarity.split(",");

                                            if (strsss_similarity.length == 3) {
                                                String[] strs_similarity_all0 = String.valueOf(strsss_similarity[0]).split(":");
                                                String[] strs_similarity_all1 = String.valueOf(strsss_similarity[1]).split(":");
                                                String[] strs_similarity_all2 = String.valueOf(strsss_similarity[2]).split(":");
                                                str_images0 = JsonHelper.getString(pigBuildResp2.build_result_images, String.valueOf(strs_similarity_all0[0]));
                                                str_images0 = str_images0.substring(1, str_images0.length() - 1);
                                                strs_img0 = str_images0.split(",");
                                                str_images1 = JsonHelper.getString(pigBuildResp2.build_result_images, String.valueOf(strs_similarity_all1[0]));
                                                str_images1 = str_images1.substring(1, str_images1.length() - 1);
                                                strs_img1 = str_images1.split(",");
                                                str_images2 = JsonHelper.getString(pigBuildResp2.build_result_images, String.valueOf(strs_similarity_all2[0]));
                                                str_images2 = str_images2.substring(1, str_images2.length() - 1);
                                                strs_img2 = str_images2.split(",");
                                                str_img0 = strs_img0[0].substring(1, strs_img0[0].length() - 1).replace("\\", "/");
                                                str_img1 = strs_img1[0].substring(1, strs_img1[0].length() - 1).replace("\\", "/");
                                                str_img2 = strs_img2[0].substring(1, strs_img2[0].length() - 1).replace("\\", "/");
                                            } else if (strsss_similarity.length == 2) {
                                                String[] strs_similarity_all0 = String.valueOf(strsss_similarity[0]).split(":");
                                                String[] strs_similarity_all1 = String.valueOf(strsss_similarity[1]).split(":");

                                                str_images0 = JsonHelper.getString(pigBuildResp2.build_result_images, String.valueOf(strs_similarity_all0[0]));
                                                str_images0 = str_images0.substring(1, str_images0.length() - 1);
                                                strs_img0 = str_images0.split(",");

                                                str_images1 = JsonHelper.getString(pigBuildResp2.build_result_images, String.valueOf(strs_similarity_all1[0]));
                                                str_images1 = str_images1.substring(1, str_images1.length() - 1);
                                                strs_img1 = str_images1.split(",");

                                                str_img0 = strs_img0[0].substring(1, strs_img0[0].length() - 1).replace("\\", "/");
                                                str_img2 = strs_img1[0].substring(1, strs_img1[0].length() - 1).replace("\\", "/");
                                            }
                                        } else {
                                            String[] strs_similarity_all = strs_similarity.split(":");

                                            str_images0 = JsonHelper.getString(pigBuildResp2.build_result_images, String.valueOf(strs_similarity_all[0]));
                                            str_images0 = str_images0.substring(1, str_images0.length());
                                            strs_img0 = str_images0.split(",");

                                            str_img1 = strs_img0[0].substring(1, strs_img0[0].length() - 1).replace("\\", "/");
                                        }
                                        final LipeiResultDialog dialogLipeiResult = new LipeiResultDialog(mActivity);
                                        View.OnClickListener listener_new = v -> {
                                            dialogLipeiResult.dismiss();
                                            Toast.makeText(mActivity, "成功生成一条理赔单", Toast.LENGTH_SHORT).show();
                                            mActivity.finish();
                                        };
                                        View.OnClickListener listener_ReCollect = v -> {
                                            dialogLipeiResult.dismiss();
                                            collectNumberHandler.sendEmptyMessage(2);
                                            Intent intent = new Intent(mActivity, DetectorActivity_pig.class);
                                            intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                            intent.putExtra("LipeiTempNumber", getlipeiTempNumber);
                                            mActivity.startActivity(intent);
                                        };
                                        dialogLipeiResult.setTitle("验证结果");
                                        dialogLipeiResult.setBtnGoApplication("直接申请", listener_new);
                                        dialogLipeiResult.setBtnReCollect("重新拍摄", listener_ReCollect);

                                        if (strs_similarity == null) {
                                            dialogLipeiResult.setLipeiResultmessage("在历史理赔库中没有相似对象！");
                                        }

                                        if (strs_similarity.contains(",")) {
                                            String[] strsss_similarity = strs_similarity.split(",");

                                            if (strsss_similarity.length == 2) {
                                                dialogLipeiResult.setImage1(str_img0);
                                                dialogLipeiResult.setImage3(str_img2);
                                                dialogLipeiResult.setLipeiResultmessage("在投保库中、历史理赔库中均无此对象，系统推荐如下最相似的对象！");
                                            } else if (strsss_similarity.length == 3) {
                                                dialogLipeiResult.setImage1(str_img0);
                                                dialogLipeiResult.setImage2(str_img1);
                                                dialogLipeiResult.setImage3(str_img2);
                                                dialogLipeiResult.setLipeiResultmessage("在投保库中、历史理赔库中均无此对象，系统推荐如下最相似的对象！");
                                            }
                                        } else {
                                            dialogLipeiResult.setImage2(str_img1);
                                            dialogLipeiResult.setLipeiResultmessage("在投保库中、历史理赔库中均无此对象，系统推荐如下最相似的对象！");
                                        }
                                        dialogLipeiResult.show();
                                    }
                                }

                            } else {

                                AlertDialogManager.showMessageDialogOne(mActivity, "提示", "理赔失败", new AlertDialogManager.DialogInterface() {
                                    @Override
                                    public void onPositive() {
                                        mActivity.finish();
                                    }

                                    @Override
                                    public void onNegative() {

                                    }
                                });

                            }

                        } else if (pigBuildRespResult.status == 0) {

                            AlertDialogManager.showMessageDialogRetry(mActivity, "提示", pigBuildRespResult.msg, new AlertDialogManager.DialogInterface() {
                                @Override
                                public void onPositive() {
                                    collectNumberHandler.sendEmptyMessage(2);
                                    Intent intent = new Intent(mActivity, DetectorActivity_pig.class);
                                    intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                    intent.putExtra("LipeiTempNumber", getlipeiTempNumber);
                                    mActivity.startActivity(intent);
                                }

                                @Override
                                public void onNegative() {

                                }
                            });
                        } else if (pigBuildRespResult.status == -4) {
                            AlertDialogManager.showMessageDialogOne(mActivity, "提示", "服务端异常，请稍后再试！", new AlertDialogManager.DialogInterface() {
                                @Override
                                public void onPositive() {
                                    mActivity.finish();
                                }

                                @Override
                                public void onNegative() {

                                }
                            });
                        } else {
                            AlertDialogManager.showMessageDialogOne(mActivity, "提示", "参数校验异常！", new AlertDialogManager.DialogInterface() {
                                @Override
                                public void onPositive() {
                                    mActivity.finish();
                                }

                                @Override
                                public void onNegative() {

                                }
                            });
                        }

                    } else {
                        if (searchSallmerCount < 6) {
                            searchSallmerCount++;
                            mHandler.sendEmptyMessageDelayed(0, 10000);
                        } else {
                            Toast.makeText(mContext, "查询结果失败", Toast.LENGTH_SHORT).show();
                            searchSallmerCount = 0;
                            mActivity.finish();
                        }
                    }


                    break;
                case 2:  // TODO: 2018/8/4
                    if (pigBuildResp2 != null) {

                        if (pigBuildResp2.status == 1) {
                            if (pigBuildResp2.buildStatus == 1) {

                                Log.i("投保库查询获得的pid", pigBuildResp2.build_result_pid);
                                verification_status = pigBuildResp2.buildVertificaton;

                                //-->2.5新增猪/牛的信息 /pigInfo/addApp.

                                // 建库 /pigApp/cow
                                String stringCowearNumber = getCowEarNumber;
                                String strcowearsNumber = stampToDate(System.currentTimeMillis());
                                // TODO: 2018/8/7 By:LuoLu
                                //读取用户信息
                                SharedPreferences pref_user = mActivity.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
                                String username = pref_user.getString("fullname", "");

                                if (stringCowearNumber.equals("")) {
                                    stringCowearNumber = strcowearsNumber;
                                }
                                TreeMap<String, String> query = new TreeMap();
                                query.put("sheNo", "001");
                                query.put("juanNo", "001");
                                query.put("baodanNo", getlipeiTempNumber);
                                query.put("pigNo", stringCowearNumber);
                                query.put("type", "2");
                                query.put("address", str_address);
                                query.put("person", username);
                                query.put("pigType", getCowType);
                                query.put("libId", cow_libid);
                                query.put("seqNo", "");
                                query.put("lipeiNo", new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date(System.currentTimeMillis())));
                                query.put("pigInfo", obj_cow_verify);
                                query.put("pid", pigBuildResp2.build_result_pid);
                                query.put("yiji", getPayYiji);
                                query.put("erji", getPayErji);
                                query.put("sanji", getPaySanji);
                                query.put("pidseven", "");

                                // TODO: 2018/8/4
                                Set set = query.keySet();
                                for (Object aSet : set) {
                                    String key = (String) aSet;
                                    String value = query.get(key);
                                    Log.e("新增牲畜标号信息接口", "\nkey:" + key + "\nvalue:" + value);
                                }

                                // TODO: 2018/8/8 By:LuoLu  修改新增牛信息的接口处理

                                FormBody.Builder builder = new FormBody.Builder();
                                // Add Params to Builder
                                for (TreeMap.Entry<String, String> entry : query.entrySet()) {
                                    builder.add(entry.getKey(), entry.getValue());
                                }
                                // Create RequestBody
                                RequestBody formBody = builder.build();
                                int newCowStatus = 1;
                                try {
                                    String response = HttpUtils.post(HttpUtils.INSUR_ADDPIG_URL, formBody);
                                    bean = gson.fromJson(response, GsonBean.class);


                                    Log.i("transerPayData s:", getPayYiji);
                                    Log.i("transerPayData s1:", getPayErji);
                                    Log.i("transerPayData s2:", getPaySanji);


                                    Log.i("GsonBeen getStatus:", String.valueOf(bean.getStatus()));
                                    Log.i(HttpUtils.INSUR_ADDPIG_URL + "\n新增牲畜response:\n", response);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //新增牛，理赔库查询；
                                if (newCowStatus == bean.getStatus()) {
                                    pigBuildRespResult = HttpUtils.upload_build("3", cow_libid, getCowEarNumber, getlipeiTempNumber, mContext);
                                    mHandler.sendEmptyMessage(0);
                                } else if (bean.getStatus() == 0) {

                                    AlertDialogManager.showMessageDialogOne(mActivity, "提示", "服务端异常，请稍后再试！", new AlertDialogManager.DialogInterface() {
                                        @Override
                                        public void onPositive() {
                                            mActivity.finish();
                                        }

                                        @Override
                                        public void onNegative() {

                                        }
                                    });
                                } else {

                                    AlertDialogManager.showMessageDialogOne(mActivity, "提示", "新增对象失败！！", new AlertDialogManager.DialogInterface() {
                                        @Override
                                        public void onPositive() {
                                            mActivity.finish();
                                        }

                                        @Override
                                        public void onNegative() {

                                        }
                                    });
                                }
                            } else if (pigBuildResp2.buildStatus == 2) {
                                //改完
                                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

                                final View dialogView = LayoutInflater.from(mActivity).inflate(R.layout.dialog_common_two_layout, null);
                                TextView dialog_content_tv1 = dialogView.findViewById(R.id.dialog_content_tv1);
                                TextView dialog_ok_btn = dialogView.findViewById(R.id.dialog_ok_btn);
                                TextView dialog_cancel_btn = dialogView.findViewById(R.id.dialog_cancel_btn);
                                TextView dialog_tips_tv = dialogView.findViewById(R.id.dialog_tips_tv);

                                dialog_tips_tv.setText("提示");
                                dialog_content_tv1.setText("建库失败，图片质量不合格！");
                                dialog_ok_btn.setText("完成");
                                dialog_cancel_btn.setText("重新采集");

                                builder.setView(dialogView);

                                Dialog d = builder.create();

                                dialog_ok_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        d.dismiss();
                                        mActivity.finish();
                                    }
                                });

                                dialog_cancel_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        d.dismiss();
                                        collectNumberHandler.sendEmptyMessage(2);
                                        Intent intent = new Intent(mActivity, DetectorActivity_pig.class);
                                        intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                        intent.putExtra("LipeiTempNumber", getlipeiTempNumber);
                                        mActivity.startActivity(intent);
                                    }
                                });
                                d.setCancelable(false);
                                d.show();
                            } else if (pigBuildResp2.buildStatus == 0) {
                                //改完
                                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                                final View dialogView = LayoutInflater.from(mActivity).inflate(R.layout.dialog_common_two_layout, null);
                                TextView dialog_content_tv1 = dialogView.findViewById(R.id.dialog_content_tv1);
                                TextView dialog_ok_btn = dialogView.findViewById(R.id.dialog_ok_btn);
                                TextView dialog_cancel_btn = dialogView.findViewById(R.id.dialog_cancel_btn);
                                TextView dialog_tips_tv = dialogView.findViewById(R.id.dialog_tips_tv);

                                dialog_tips_tv.setText("提示");
                                dialog_content_tv1.setText(pigBuildResp2.msg);
                                dialog_ok_btn.setText("完成");
                                dialog_cancel_btn.setText("重新采集");

                                builder.setView(dialogView);

                                Dialog d = builder.create();
                                dialog_ok_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        d.dismiss();
                                        mActivity.finish();
                                    }
                                });

                                dialog_cancel_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        d.dismiss();
                                        collectNumberHandler.sendEmptyMessage(2);
                                        Intent intent = new Intent(mActivity, DetectorActivity_pig.class);
                                        intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                        intent.putExtra("LipeiTempNumber", getlipeiTempNumber);
                                        mActivity.startActivity(intent);
                                    }
                                });
                                d.setCancelable(false);
                                d.show();
                            }
                        } else {
                            AlertDialogManager.showMessageDialogRetry(mActivity, "提示", pigBuildRespResult.msg, new AlertDialogManager.DialogInterface() {
                                @Override
                                public void onPositive() {
                                    collectNumberHandler.sendEmptyMessage(2);
                                    Intent intent = new Intent(mActivity, DetectorActivity_pig.class);
                                    intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                    intent.putExtra("LipeiTempNumber", getlipeiTempNumber);
                                    mActivity.startActivity(intent);
                                }

                                @Override
                                public void onNegative() {

                                }
                            });
                        }
                    } else {
                        AlertDialogManager.showMessageDialogRetry(mActivity, "提示", pigBuildRespResult.msg, new AlertDialogManager.DialogInterface() {
                            @Override
                            public void onPositive() {
                                collectNumberHandler.sendEmptyMessage(2);
                                Intent intent = new Intent(mActivity, DetectorActivity_pig.class);
                                intent.putExtra("ToubaoTempNumber", getStringTouboaExtra);
                                intent.putExtra("LipeiTempNumber", getlipeiTempNumber);
                                mActivity.startActivity(intent);
                            }

                            @Override
                            public void onNegative() {

                            }
                        });
                    }
                    break;

                case 400:

                    AlertDialogManager.showMessageDialogOne(mActivity, "提示", "获取用户ID失败！", new AlertDialogManager.DialogInterface() {
                        @Override
                        public void onPositive() {
                            mActivity.finish();
                        }

                        @Override
                        public void onNegative() {

                        }
                    });

                    break;
                case 94:
                    break;
                default:
                    break;
            }

        }
    };


}
