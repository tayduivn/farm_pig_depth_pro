package com.farm.innovation.biz.insurance;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.farm.innovation.base.BaseActivity;
import com.farm.innovation.base.FarmAppConfig;
import com.farm.innovation.bean.UploadImageObject;
import com.farm.innovation.biz.iterm.Model;
import com.farm.innovation.login.IDCardValidate;
import com.farm.innovation.login.Utils;
import com.farm.innovation.utils.AVOSCloudUtils;
import com.farm.innovation.utils.FileUtils;
import com.farm.innovation.utils.HttpRespObject;
import com.farm.innovation.utils.HttpUtils;
import com.farm.innovation.utils.OkHttp3Util;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.farm.innovation.utils.ValidatorUtils;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.R;

import org.json.JSONObject;
import org.tensorflow.demo.FarmGlobal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.farm.innovation.utils.ImageUtils.fileToBitmap;
import static com.farm.innovation.utils.MyTextUtil.isEmojiCharacter;
import static com.farm.innovation.utils.ValidatorUtils.isMobileNO;
import static com.farm.innovation.utils.ValidatorUtils.isPhone;


/**
 * Created by Luolu on 2018/9/19.
 * InnovationAI
 * luolu@innovationai.cn
 */
public class EnterpriseBaodanActivity extends BaseActivity {
    public static String TAG = "EnterpriseBaodanActivity";

    ImageView ivCancel;

    TextView tvTitle;

    TextView tvExit;

    RelativeLayout rlTitle;

    EditText editTextBaodanedPeople;

    RadioButton radioButtonIDCard;

    RadioButton radioBttonBusinessLicens;
    /*   @BindView(R.id.certificateTypeRadioGroup)
       RadioGroup certificateTypeRadioGroup;*/

    EditText editTextBaodanIDCard;

    ImageView imageIDCardZhengUpload;

    TextView tvIdPositive;

    TextView textViewIDCardZhengPath;

    ImageView imageIDCardFanUpload;

    TextView textViewIdCardNegative;

    ConstraintLayout idCardNegativePhotoConstraintLayout;

    TextView idCardFanPath;

    EditText openBankCardName;

    EditText bankCardNumber;

    ImageView bankCard;

    TextView bankPath;

    EditText phoneNumber;

    Button btnNext;

    Button btnFinish;

    Button btnnew;


    private int certificateType = 1;
    private static final int REQUESTCODE_TAKE = 11111;        // 相机拍照标记
    private static final int REQUESTCODE_CUTTING = 22222;    // 图片裁切标记
    private static String IMAGE_FILE_NAME = "";// 头像文件名称
    private String imageType;
    private Uri uritempFile;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private String str_idcard_zheng = "";
    private String str_idcard_fan = "";
    private String str_bank = "";
    private String mTempToubaoNumber;
    private int mid;
    private int userId;
    private File fileURLPath;
    private File tempFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void initView() {
        super.initView();

        ivCancel = (ImageView) findViewById(R.id.iv_cancel);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvExit = (TextView) findViewById(R.id.tv_exit);
        rlTitle = (RelativeLayout) findViewById(R.id.rl_title);
        editTextBaodanedPeople = (EditText) findViewById(R.id.editTextBaodanedPeople);
        radioButtonIDCard = (RadioButton) findViewById(R.id.radioButtonIDCard);
        radioBttonBusinessLicens = (RadioButton) findViewById(R.id.radioBttonBusinessLicens);
        editTextBaodanIDCard = (EditText) findViewById(R.id.editTextBaodanIDCard);
        imageIDCardZhengUpload = (ImageView) findViewById(R.id.imageIDCardZhengUpload);
        tvIdPositive = (TextView) findViewById(R.id.textViewIDPositive);
        textViewIDCardZhengPath = (TextView) findViewById(R.id.textViewIDCardZhengPath);
        imageIDCardFanUpload = (ImageView) findViewById(R.id.imageIDCardFanUpload);
        textViewIdCardNegative = (TextView) findViewById(R.id.textViewIdCardNegative);
        idCardNegativePhotoConstraintLayout = (ConstraintLayout) findViewById(R.id.id_card_negative_photo_constraint_layout);
        idCardFanPath = (TextView) findViewById(R.id.idCardFanPath);
        openBankCardName = (EditText) findViewById(R.id.openBankCardName);
        bankCardNumber = (EditText) findViewById(R.id.bankCardNumber);
        bankCard = (ImageView) findViewById(R.id.bankCard);
        bankPath = (TextView) findViewById(R.id.bank_path);
        phoneNumber = (EditText) findViewById(R.id.phoneNumber);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnFinish = (Button) findViewById(R.id.btnFinish);
        btnnew = (Button) findViewById(R.id.btnnew);
        findViewById(R.id.btnnew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.btnFinish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.btnNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.bankCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.imageIDCardFanUpload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
        findViewById(R.id.imageIDCardZhengUpload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked((View) v);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.farm_activity_baodan_enterprise;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void initData() {
        XXPermissions.with(this)
                //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                //.permission(Permission.SYSTEM_ALERT_WINDOW, Permission.REQUEST_INSTALL_PACKAGES) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.Group.STORAGE, Permission.Group.LOCATION) //不指定权限则自动获取清单中的危险权限
                .permission(Permission.CAMERA)
                .request(new OnPermission() {

                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            // toastUtils.showLong(MyApplication.getApplication(), "获取权限成功");
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            Toast.makeText(FarmAppConfig.getApplication(), "被永久拒绝授权，请手动授予权限", Toast.LENGTH_SHORT).show();
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(FarmAppConfig.getApplication());
                        } else {
                            Toast.makeText(FarmAppConfig.getApplication(), "获取权限失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        IMAGE_FILE_NAME = stampToDate(System.currentTimeMillis()) + ".jpg";

        FarmGlobal.model = Model.BUILD.value();
        tvTitle.setText("新建企业保单");
        SharedPreferences pref = EnterpriseBaodanActivity.this.getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        userId = pref.getInt("uid", 0);

        ivCancel.setVisibility(View.VISIBLE);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getCurrentLocationLatLng();

        RadioGroup certificateTypeRadioGroup = findViewById(R.id.certificateTypeRadioGroup);
        certificateTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonIDCard) {
                certificateType = 1;
                idCardNegativePhotoConstraintLayout.setVisibility(View.VISIBLE);
                tvIdPositive.setText(getString(R.string.idPostive));
                imageIDCardZhengUpload.setImageResource(R.color.video_bg);
                imageIDCardFanUpload.setImageResource(R.color.video_bg);
                str_idcard_zheng = "";
                str_idcard_fan = "";
                //更新照片中文�?

            } else if (checkedId == R.id.radioBttonBusinessLicens) {
                certificateType = 2;
                idCardNegativePhotoConstraintLayout.setVisibility(View.INVISIBLE);
                tvIdPositive.setText(R.string.businessLicense);
                imageIDCardZhengUpload.setImageResource(R.color.video_bg);
                str_idcard_fan = "";
                // imageIDCardZhengUpload.setImageDrawable(null);
                //更新照片中文�?

            } else {
            }
        });
    }

    public String str = "";


    public void onViewClicked(View view) {
        int i1 = view.getId();
        if (i1 == R.id.imageIDCardZhengUpload) {
            photograph("idcard_zheng");

        } else if (i1 == R.id.imageIDCardFanUpload) {
            photograph("idcard_fan");

        } else if (i1 == R.id.bankCard) {
            photograph("bank");

        } else if (i1 == R.id.btnNext) {
        } else if (i1 == R.id.btnFinish) {
            str = "btnFinish";
            btnFinish.setEnabled(false);

            String etBdPeople = editTextBaodanedPeople.getText().toString();

            boolean isEmo = false;
            for (int i = 0; i < etBdPeople.length(); i++) {
                isEmo = isEmojiCharacter(etBdPeople.charAt(i));
                if (isEmo) {
                    break;
                }
            }
            if (isEmo) {
                Toast.makeText(EnterpriseBaodanActivity.this, "被保险人姓名不能包含特殊字符", Toast.LENGTH_LONG).show();
                btnFinish.setEnabled(true);
                return;
            }

            if ("".equals(etBdPeople)) {
                Toast.makeText(EnterpriseBaodanActivity.this, "请输入被保险人姓名！", Toast.LENGTH_LONG).show();
                btnFinish.setEnabled(true);
                return;
            }
            if (certificateType == 0) {
                Toast.makeText(EnterpriseBaodanActivity.this, "请选择证件类型", Toast.LENGTH_LONG).show();
                btnFinish.setEnabled(true);
                return;
            }
            if (certificateType == 1) {
                String strIDcard = IDCardValidate.validateIDcardNumber(editTextBaodanIDCard.getText().toString().trim(), true);
                if (!(strIDcard.length() == 15 || strIDcard.length() == 18)) {
                    Toast.makeText(getApplicationContext(), strIDcard, Toast.LENGTH_SHORT).show();
                    btnFinish.setEnabled(true);
                    return;
                }
            } else if (certificateType == 2) {
                if (!ValidatorUtils.isLicense18(editTextBaodanIDCard.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "请输入正确的统一社会信用代码", Toast.LENGTH_SHORT).show();
                    btnFinish.setEnabled(true);
                    return;
                }

            }

            if (certificateType == 1) {
                if ("".equals(str_idcard_zheng.trim())) {
                    Toast.makeText(EnterpriseBaodanActivity.this, "请上传身份证正面照片", Toast.LENGTH_LONG).show();
                    btnFinish.setEnabled(true);
                    return;
                } else if ("".equals(str_idcard_fan.trim())) {
                    Toast.makeText(EnterpriseBaodanActivity.this, "请上传身份证反面照片", Toast.LENGTH_LONG).show();
                    btnFinish.setEnabled(true);
                    return;
                }
            } else if (certificateType == 2) {
                if ("".equals(str_idcard_zheng.trim())) {
                    Toast.makeText(EnterpriseBaodanActivity.this, "请上传营业执照正面照片", Toast.LENGTH_LONG).show();
                    btnFinish.setEnabled(true);
                    return;
                }
            }
            if ("".equals(openBankCardName.getText().toString())) {
                Toast.makeText(EnterpriseBaodanActivity.this, "开户行为空", Toast.LENGTH_LONG).show();
                btnFinish.setEnabled(true);
                return;
            }
            if ("".equals(bankCardNumber.getText().toString())) {
                Toast.makeText(EnterpriseBaodanActivity.this, "银行账号为空", Toast.LENGTH_LONG).show();
                btnFinish.setEnabled(true);
                return;
            }
            if (str_bank.equals("")) {
                Toast.makeText(getApplicationContext(), "请上传银行卡正面照片", Toast.LENGTH_SHORT).show();
                btnFinish.setEnabled(true);
                return;
            }

            if ("".equals(phoneNumber.getText().toString())) {
                Toast.makeText(EnterpriseBaodanActivity.this, "联系方式为空", Toast.LENGTH_LONG).show();
                btnFinish.setEnabled(true);
                return;
            }

            if (!isMobileNO(phoneNumber.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "手机号格式有误", Toast.LENGTH_SHORT).show();
                btnFinish.setEnabled(true);
                return;
            } else if (isPhone(phoneNumber.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "电话号格式异常", Toast.LENGTH_SHORT).show();
                btnFinish.setEnabled(true);
                return;
            }
            String str_random1 = createCode();
            mTempToubaoNumber = stampToDate(System.currentTimeMillis()) + str_random1;
            createDan();


        } else if (i1 == R.id.btnnew) {
            str = "xinjian";
            btnnew.setEnabled(false);
//                if (isOPen(EnterpriseBaodanActivity.this)) {

            String etBdPeopl = editTextBaodanedPeople.getText().toString();
            boolean isEm = false;
            for (int i = 0; i < etBdPeopl.length(); i++) {
                isEm = isEmojiCharacter(etBdPeopl.charAt(i));
                if (isEm) {
                    break;
                }
            }
            if (isEm) {
                Toast.makeText(EnterpriseBaodanActivity.this, "被保险人姓名不能包含特殊字符", Toast.LENGTH_LONG).show();
                btnnew.setEnabled(true);
                return;
            }

            if ("".equals(editTextBaodanedPeople.getText().toString())) {
                Toast.makeText(EnterpriseBaodanActivity.this, "被保险人不能为空！", Toast.LENGTH_LONG).show();
                btnnew.setEnabled(true);
                return;
            }
            if (certificateType == 0) {
                Toast.makeText(EnterpriseBaodanActivity.this, "请选择证件类型", Toast.LENGTH_LONG).show();
                btnnew.setEnabled(true);
                return;
            }
            if (certificateType == 1) {
                String strIDcard = IDCardValidate.validateIDcardNumber(editTextBaodanIDCard.getText().toString().trim(), true);
                if (!(strIDcard.length() == 15 || strIDcard.length() == 18)) {
                    Toast.makeText(getApplicationContext(), strIDcard, Toast.LENGTH_SHORT).show();
                    btnnew.setEnabled(true);
                    return;
                }
            } else if (certificateType == 2) {
                if (!ValidatorUtils.isLicense18(editTextBaodanIDCard.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "请输入正确的统一社会信用代码", Toast.LENGTH_SHORT).show();
                    btnnew.setEnabled(true);
                    return;
                }

            }

            if (certificateType == 1) {
                if ("".equals(str_idcard_zheng.trim())) {
                    Toast.makeText(EnterpriseBaodanActivity.this, "请上传身份证正面照片", Toast.LENGTH_LONG).show();
                    btnnew.setEnabled(true);
                    return;
                } else if ("".equals(str_idcard_fan.trim())) {
                    Toast.makeText(EnterpriseBaodanActivity.this, "请上传身份证反面照片", Toast.LENGTH_LONG).show();
                    btnnew.setEnabled(true);
                    return;
                }
            } else if (certificateType == 2) {
                if ("".equals(str_idcard_zheng.trim())) {
                    Toast.makeText(EnterpriseBaodanActivity.this, "请上传营业执照正面照片", Toast.LENGTH_LONG).show();
                    btnnew.setEnabled(true);
                    return;
                }
            }
            if ("".equals(openBankCardName.getText().toString())) {
                Toast.makeText(EnterpriseBaodanActivity.this, "开户行为空", Toast.LENGTH_LONG).show();
                btnnew.setEnabled(true);
                return;
            }
            if ("".equals(bankCardNumber.getText().toString())) {
                Toast.makeText(EnterpriseBaodanActivity.this, "银行账号为空", Toast.LENGTH_LONG).show();
                btnnew.setEnabled(true);
                return;
            }
            if (str_bank.equals("")) {
                Toast.makeText(getApplicationContext(), "请上传银行卡正面照片", Toast.LENGTH_SHORT).show();
                btnnew.setEnabled(true);
                return;
            }
            if ("".equals(phoneNumber.getText().toString())) {
                Toast.makeText(EnterpriseBaodanActivity.this, "联系方式为空", Toast.LENGTH_LONG).show();
                btnnew.setEnabled(true);
                return;
            }

            if (!isMobileNO(phoneNumber.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "手机号格式有误", Toast.LENGTH_SHORT).show();
                btnnew.setEnabled(true);
                return;
            } else if (isPhone(phoneNumber.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "电话号格式异常", Toast.LENGTH_SHORT).show();
                btnnew.setEnabled(true);
                return;
            }

            String str_random3 = createCode();
            mTempToubaoNumber = stampToDate(System.currentTimeMillis()) + str_random3;
            createDan();


//                }
//                else {
//                    AlertDialogManager.showMessageDialog(EnterpriseBaodanActivity.this, "提示", getString(R.string.locationwarning), new AlertDialogManager.DialogInterface() {
//                        @Override
//                        public void onPositive() {
//                            Intent intent = new Intent();
//                            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                            startActivityForResult(intent, 1315);
//                        }
//
//                        @Override
//                        public void onNegative() {
//
//                        }
//                    });
//
//                }


        } else {
        }
    }


    private void createDan() {
        Map<String,String> map = new HashMap<>();
        map.put(HttpUtils.AppKeyAuthorization, "hopen");
        SharedPreferences pref = getSharedPreferences(Utils.USERINFO_SHAREFILE, Context.MODE_PRIVATE);
        int userid = pref.getInt("uid", 0);
        map.put(HttpUtils.id, userid + "");
        Map<String,String> mapbody = new HashMap<>();
        mapbody.put("baodanNo", mTempToubaoNumber);
        mapbody.put("baodanName", FarmerPreferencesUtils.getStringValue(HttpUtils.baodanName, FarmAppConfig.getApplication()));
        mapbody.put("baodanType", FarmerPreferencesUtils.getStringValue(HttpUtils.baodanType, FarmAppConfig.getApplication()));
        mapbody.put("animalType", String.valueOf(FarmerPreferencesUtils.getAnimalType(EnterpriseBaodanActivity.this)));
        mapbody.put("toubaoType", FarmerPreferencesUtils.getStringValue(HttpUtils.InsuranceType, FarmAppConfig.getApplication()));
        mapbody.put("baodanRate", FarmerPreferencesUtils.getStringValue(HttpUtils.insuranceRate, FarmAppConfig.getApplication()));
        mapbody.put("shiyangMethod", FarmerPreferencesUtils.getStringValue(HttpUtils.farmForm, FarmAppConfig.getApplication()));
        mapbody.put("toubaoCost", FarmerPreferencesUtils.getStringValue(HttpUtils.InsuranceCost, FarmAppConfig.getApplication()));
        mapbody.put("address", FarmerPreferencesUtils.getStringValue(HttpUtils.baodanApplyAddress, FarmAppConfig.getApplication()));
        mapbody.put("uid", userid + "");
        mapbody.put(HttpUtils.deptId, FarmerPreferencesUtils.getIntValue(HttpUtils.deptId, FarmAppConfig.getApplication()) + "");

        mapbody.put("longitude", String.valueOf(currentLat));
        mapbody.put("latitude", String.valueOf(currentLon));

        mapbody.put("cardType", String.valueOf(certificateType));
        mapbody.put("cardNo", editTextBaodanIDCard.getText().toString());
        mapbody.put("cardFront", str_idcard_zheng.trim());
        mapbody.put("cardBack", str_idcard_fan.trim());
        mapbody.put("name", editTextBaodanedPeople.getText().toString());
        mapbody.put("phone", phoneNumber.getText().toString());
        mapbody.put("bankNo", bankCardNumber.getText().toString());
        mapbody.put("bankName", openBankCardName.getText().toString());
        mapbody.put("bankFront", str_bank.trim());

        OkHttp3Util.doPost(HttpUtils.BaoDanadd, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i(TAG, e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnFinish.setEnabled(true);
                        btnnew.setEnabled(true);
                    }
                });

                AVOSCloudUtils.saveErrorMessage(e, EnterpriseBaodanActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnFinish.setEnabled(true);
                        btnnew.setEnabled(true);
                        try {
                            JSONObject jsonObject = new JSONObject(string);
                            int status = jsonObject.getInt("status");
                            String msg = jsonObject.getString("msg");
                            if (status == -1) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        showDialogError(msg);
                                    }
                                });
                            } else if (status == 0) {
                                Toast.makeText(EnterpriseBaodanActivity.this, msg, Toast.LENGTH_LONG).show();
                            } else if (status == 1) {
                                Toast.makeText(EnterpriseBaodanActivity.this, msg, Toast.LENGTH_LONG).show();
                                if ("btnFinish".equals(str)) {
                                    if (fileURLPath != null) {
                                        File file = new File(fileURLPath.getAbsolutePath());
                                        boolean deleteFile = FileUtils.deleteFile(file);
                                        if (deleteFile) {
                                            Log.i(TAG, "finish,调用系统拍照的fileURLPath删除成功");
                                        }
                                    }
                                    if (tempFile != null) {
                                        File file = new File(tempFile.getAbsolutePath());
                                        boolean deleteFile = FileUtils.deleteFile(file);
                                        if (deleteFile) {
                                            Log.i(TAG, "finish,调用系统拍照的tempFile删除成功");
                                        }
                                    }
                                    finish();
                                } else if ("xinjian".equals(str)) {
                                    JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                    mid = jsonObject1.getInt("id");

                                    FarmerPreferencesUtils.saveKeyValue(HttpUtils.id, mid + "", FarmAppConfig.getApplication());
                                    goToActivity(CreateYanActivity.class, null);
                                    finish();
                                    //createYan();
                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            AVOSCloudUtils.saveErrorMessage(e, EnterpriseBaodanActivity.class.getSimpleName());
                        }
                    }
                });


            }
        });
    }


    private void createYan() {
        Map<String, String> map = new HashMap<>();
        map.put(HttpUtils.AppKeyAuthorization, "hopen");
        Map<String, String> mapbody = new HashMap<>();
        mapbody.put("bdsId", String.valueOf(mid));
        mapbody.put("baodanNo", mTempToubaoNumber);
        mapbody.put("longitude", String.valueOf(currentLat));
        mapbody.put("latitude", String.valueOf(currentLon));

        mapbody.put("cardType", certificateType + "");
        mapbody.put("cardNo", editTextBaodanIDCard.getText().toString());
        mapbody.put("cardFront", str_idcard_zheng.trim());
        mapbody.put("cardBack", str_idcard_fan.trim());
        mapbody.put("name", editTextBaodanedPeople.getText().toString());
        mapbody.put("phone", phoneNumber.getText().toString());
        mapbody.put("bankNo", bankCardNumber.getText().toString());
        mapbody.put("bankName", openBankCardName.getText().toString());
        mapbody.put("bankFront", str_bank.trim());
        mapbody.put("uid", String.valueOf(userId));

        OkHttp3Util.doPost(HttpUtils.BaoDanaddyan, mapbody, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();
                Log.i(TAG, e.toString());
                AVOSCloudUtils.saveErrorMessage(e, EnterpriseBaodanActivity.class.getSimpleName());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, string);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(string);
                            int status = jsonObject.getInt("status");
                            String msg = jsonObject.getString("msg");
                            if (status == -1) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialog.dismiss();
                                        showDialogError(msg);
                                    }
                                });
                            } else if (status == 0) {
                                Toast.makeText(EnterpriseBaodanActivity.this, msg, Toast.LENGTH_LONG).show();
                            } else if (status == 1) {
                                Toast.makeText(EnterpriseBaodanActivity.this, msg, Toast.LENGTH_LONG).show();
                                if (fileURLPath != null) {
                                    File file = new File(fileURLPath.getAbsolutePath());
                                    boolean deleteFile = FileUtils.deleteFile(file);
                                    if (deleteFile) {
                                        Log.i(TAG, "finish,调用系统拍照的fileURLPath删除成功");
                                    }
                                }
                                if (tempFile != null) {
                                    File file = new File(tempFile.getAbsolutePath());
                                    boolean deleteFile = FileUtils.deleteFile(file);
                                    if (deleteFile) {
                                        Log.i(TAG, "finish,调用系统拍照的tempFile删除成功");
                                    }
                                }
                                FarmerPreferencesUtils.saveKeyValue(HttpUtils.id, mid + "", FarmAppConfig.getApplication());
                                goToActivity(CreateYanActivity.class, null);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            AVOSCloudUtils.saveErrorMessage(e, EnterpriseBaodanActivity.class.getSimpleName());
                        }
                    }
                });


            }
        });
    }

    /**
     * 拍照
     *
     * @param imageType 图片采集类型
     */
    private void photograph(String imageType) {
        this.imageType = imageType;
        tempFile = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //下面这句指定调用相机拍照后的照片存储的路径
        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(EnterpriseBaodanActivity.this, AppConfig.getAppContext().getPackageName().concat(".animalcounter.provider"), tempFile));
        startActivityForResult(takeIntent, REQUESTCODE_TAKE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_TAKE:// 调用相机拍照
//                crop(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME);
                if(tempFile.exists()){
                    try {
                        setPicToView(tempFile);
                    } catch (Exception e) {
//                    Toast.makeText(EnterpriseBaodanActivity.this, "图片处理异常，请重试。", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }


                break;
            case REQUESTCODE_CUTTING:// 取得裁剪后的图片
                if (data != null) {
                    try {
//                        setPicToView();
                    } catch (Exception e) {
                        Toast.makeText(EnterpriseBaodanActivity.this, "图片处理异常，请重试。", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(EnterpriseBaodanActivity.this, "图片处理异常，请重试。", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (fileURLPath != null) {
            boolean deleteFile = FileUtils.deleteFile(fileURLPath);
            if (deleteFile) {
                Log.i(TAG, "onDestroy,调用系统拍照的fileURLPath删除成功");
            }
        }
        if (tempFile != null) {
            boolean deleteFile = FileUtils.deleteFile(tempFile);
            if (deleteFile) {
                Log.i(TAG, "onDestroy,调用系统拍照的tempFile删除成功");
            }
        }
        mLocationClient.stopLocation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLocationClient != null) {
            mLocationClient.startLocation(); // 启动定位
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationClient != null) {
            mLocationClient.stopLocation();//停止定位
        }
    }


    /**
     * 裁剪图片方法实现
     */
    private void crop(String imagePath) {
        // TODO: 2018/8/24 By:LuoLu  "No Activity found to handle Intent"
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent intent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            intent.setDataAndType(getImageContentUri(new File(imagePath)), "image/*");
            intent.putExtra("crop", "false");
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            EnterpriseBaodanActivity.this.startActivityForResult(intent, REQUESTCODE_CUTTING);
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            String errorMessage = "你的设备不支持图片裁剪，请更换其他设备重试。";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * 转换 content:// uri
     *
     * @param imageFile 图片文件
     * @return url
     */
    private Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                return Uri.withAppendedPath(baseUri, "" + id);
            } else {
                if (imageFile.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * 保存裁剪之后的图片数据
     */
    private void setPicToView(File file) throws Exception {
        // 取得SDCard图片路径做显示
//        Bitmap photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(uritempFile));

        Bitmap photo = fileToBitmap(file.getAbsolutePath(), 4);

        String urlpath = FileUtils.saveFile(EnterpriseBaodanActivity.this, "temphead.jpg", photo);

        // TODO: 2018/8/21 By:LuoLu
        File newFile = new File(urlpath);
        Log.e("newFile", newFile.getPath());
        newFile.mkdirs();

        newFile = new File(newFile.getPath());
        Log.i("fileNew：", newFile.getPath());
        OutputStream out = new FileOutputStream(newFile.getPath());
        boolean flag = photo.compress(Bitmap.CompressFormat.JPEG, 30, out);
        Log.e("flag:", "图片压缩成功" + flag);

        fileURLPath = new File(urlpath);
        upload_zipImage(fileURLPath, userId, new BitmapDrawable(null, photo));
    }

    private void upload_zipImage(File zipFile_image, int uid, BitmapDrawable drawable) {
        UploadImageObject imgResp = HttpUtils.uploadImage(zipFile_image, uid);

        if (imgResp == null) {
            Toast.makeText(EnterpriseBaodanActivity.this, "图片上传失败，请检查您的网络。", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imgResp.status != HttpRespObject.STATUS_OK) {
            Toast.makeText(EnterpriseBaodanActivity.this, imgResp.msg, Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageType.contains("idcard_zheng")) {
            str_idcard_zheng = imgResp.upload_imagePath;
            imageIDCardZhengUpload.setImageDrawable(drawable);
        } else if (imageType.contains("idcard_fan")) {
            str_idcard_fan = imgResp.upload_imagePath;
            imageIDCardFanUpload.setImageDrawable(drawable);
        } else if (imageType.contains("bank")) {
            str_bank = imgResp.upload_imagePath;
            bankCard.setImageDrawable(drawable);
        }
    }

    private void getCurrentLocationLatLng() {
        //初始化定位
        mLocationClient = new AMapLocationClient(EnterpriseBaodanActivity.this);
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setOnceLocation(true);
        //设置定位间隔,单位毫秒
        mLocationOption.setInterval(3500);
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    private double currentLat;
    private double currentLon;
    private String str_address = "";
    private final AMapLocationListener mLocationListener = amapLocation -> {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消�?
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                currentLat = amapLocation.getLatitude();//获取纬度
                currentLon = amapLocation.getLongitude();//获取经度
                //  str_address = amapLocation.getAddress();
                str_address = mLocationClient.getLastKnownLocation().getAddress();
                amapLocation.getAccuracy();//获取精度信息
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表�?
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    };

}
