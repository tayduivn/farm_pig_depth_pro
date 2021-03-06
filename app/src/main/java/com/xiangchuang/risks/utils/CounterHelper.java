package com.xiangchuang.risks.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.model.bean.GSCPigBean;
import com.xiangchuang.risks.model.bean.RecognitionResult;
import com.xiangchuang.risks.model.bean.WeightBean;
import com.xiangchuangtec.luolu.animalcounter.PigAppConfig;
import com.xiangchuangtec.luolu.animalcounter.Utils;
import com.xiangchuangtec.luolu.animalcounter.netutils.Constants;
import com.xiangchuangtec.luolu.animalcounter.netutils.OkHttp3Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xiangchuang.risks.view.SelectFunctionActivity_new;
import innovation.location.LocationManager_new;
import innovation.utils.ThreadPoolProxyFactory;
import innovation.utils.UIUtils;
import innovation.utils.ZipUtil;
import innovation.view.dialog.DialogHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
//import static SelectFunctionActivity_new.totalFarmPigs;

public final class CounterHelper {

    private static final Map<String, String> mHeaderMap = new HashMap<>();
    //    private static final String URL_TEST = "http://ai.innovationai.cn:1011/test";
    private static final String URL_TEST = "http://119.253.84.115:9018/count";

    //    private static final String URL_WEIGHT_TEST = "http://ai.innovationai.cn:1012/supervise";
    private static final String URL_WEIGHT_TEST = "http://119.253.84.115:9019/weight";


    static {
        mHeaderMap.put("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
    }

    public interface OnImageRecognitionListener {
        void onCompleted(int count, Bitmap bitmap, String time);
    }

    public interface OnImageRecognitionWeightListener {
        void onCompleted(float weight, float length, float area, int status, String errormsg);
    }

    public interface OnUploadResultListener {
        void onCompleted(boolean succeed, String resutl);
    }

    public static void uploadRecognitionResult(String sheId, String sheName, int duration,
                                               List<RecognitionResult> results,
                                               Context context, OnUploadResultListener listener) {

        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                List<GSCPigBean> list = new ArrayList<>();
                String path = FileUtils.createTempDir(context);
                File[] files = new File[results.size()];
                int totalCount = 0;
                int mAutoCount = 0;
                String locationString = "";
                try {
                    JSONArray arrays = new JSONArray();
                    for (RecognitionResult recognitionResult : results) {
                        String fileName = String.format("%s/%d.jpg", path, recognitionResult.index);
                        Log.e("CounterHelper", "fileName" + fileName);

                        if (recognitionResult.autoCount >= 0) {
                            saveBitmap(recognitionResult.bitmap, fileName);
                        } else {
                            resizeAndSaveBitmap(recognitionResult.bitmap, fileName);
                        }
                        files[recognitionResult.index] = new File(fileName);
                        JSONObject jsonObject = new JSONObject();
                        String[] split = fileName.split("/");
                        String picname = split[split.length - 1];
                        //经度 纬度 猪圈名字 图片名字 当前猪圈数
                        jsonObject.put("lat", recognitionResult.lat);
                        jsonObject.put("lon", recognitionResult.lon);
                        jsonObject.put("name", "猪圈" + (recognitionResult.index + 1));
                        jsonObject.put("picName", picname);
                        jsonObject.put("count", recognitionResult.count);
                        jsonObject.put("autoCount", recognitionResult.autoCount);
                        jsonObject.put("time", recognitionResult.time);
                        arrays.put(jsonObject);
                        totalCount += (recognitionResult.count < 0 ? 0 : recognitionResult.count);
                        mAutoCount += (recognitionResult.autoCount < 0 ? 0 : recognitionResult.autoCount);

                        GSCPigBean gscPigBean = new GSCPigBean();
                        gscPigBean.name = picname;
                        gscPigBean.picture = fileName;
                        gscPigBean.address = LocationManager_new.getInstance(context).str_address;
                        gscPigBean.latitude = recognitionResult.lat;
                        gscPigBean.longitude = recognitionResult.lon;
                        gscPigBean.time = System.currentTimeMillis();
                        gscPigBean.pigHouseNumber = sheId;
                        gscPigBean.photoPigsNumber = recognitionResult.count;
                        gscPigBean.pigType = SelectFunctionActivity_new.g_PigType;
                        list.add(gscPigBean);
                    }
                    for (GSCPigBean pbean : list) {
                        pbean.totalPigs = totalCount;
                    }
                    JSONObject root = new JSONObject();
                    root.put("pigsty", arrays);
                    locationString = root.toString();
                } catch (JSONException e) {
                    Log.e("uploadRecognitionResult", "JSONException: " + e.toString());
                    for (GSCPigBean pbean : list) {
                        pbean.resultStatus = 0;
                    }
                    SelectFunctionActivity_new.g_CaptivityMap.put(sheId, list);
                    listener.onCompleted(false, "");
                    return;
                }

                File zipFile = new File(path, "out.zip");
                ZipUtil.zipFiles(files, zipFile);
                {
                    Map<String, String> map = new HashMap<>();
                    map.put(Constants.AppKeyAuthorization, "hopen");
                    map.put(Constants.en_id, PigPreferencesUtils.getStringValue(Constants.en_id, context));

//                String url = "http://47.92.167.61:8081/numberCheck/app/sheCommit";
                    Map<String, String> param = new HashMap<>();
                    param.put("sheId", sheId);
                    param.put("name", sheName);
                    param.put("count", "" + totalCount);
                    param.put("autoCount", "" + mAutoCount);
                    param.put("location", locationString);
                    param.put("timeLength", "" + duration);
                    param.put("juanCnt", "" + results.size());

                    String type = PigPreferencesUtils.getStringValue(Constants.companyfleg, PigAppConfig.getAppContext());
                    String cuid;

                    if (type.equals("1")) {
                        cuid = PigPreferencesUtils.getStringValue(Constants.id, PigAppConfig.getAppContext());
                    } else {
                        cuid = PigPreferencesUtils.getIntValue(Constants.en_user_id, PigAppConfig.getAppContext()) + "";
                    }

                    param.put("createuser", cuid);
                    OkHttp3Util.uploadPreFile(Constants.SHECOMMIT, zipFile, "out.zip", param, map, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("uploadRecognitionResult", "IOException: " + e.toString());
                            for (GSCPigBean pbean : list) {
                                pbean.resultStatus = 0;
                            }
                            SelectFunctionActivity_new.g_CaptivityMap.put(sheId, list);
                            listener.onCompleted(false, "");
                            AVOSCloudUtils.saveErrorMessage(e, CounterHelper.class.getSimpleName());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.code() == 200) {
                                String resutl = response.body().string();
                                for (GSCPigBean pbean : list) {
                                    pbean.resultStatus = 1;
                                }
                                SelectFunctionActivity_new.g_CaptivityMap.put(sheId, list);
                                listener.onCompleted(true, resutl);
                            } else {
                                Log.e("uploadRecognitionResult", "response.code(): " + response.code());
                                for (GSCPigBean pbean : list) {
                                    pbean.resultStatus = 0;
                                }
                                SelectFunctionActivity_new.g_CaptivityMap.put(sheId, list);
                                listener.onCompleted(false, "保存成功");
                            }
                        }
                    });
                }
            }
        });
    }

    public static void recognitionFromNet(Context context, final Bitmap bitmap, final OnImageRecognitionListener listener) {

        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                Map param = new HashMap();
                param.put("imgBase64", getImgStr(bitmap));
                OkHttp3Util.doPostForWan(URL_TEST, param, mHeaderMap, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = simpleDateFormat.format(new Date());
                        listener.onCompleted(-1, null, time);
                        AVOSCloudUtils.saveErrorMessage(e, CounterHelper.class.getSimpleName());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        int count = -1;
                        Bitmap resultBitmap = null;
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = simpleDateFormat.format(new Date());
                        try {
                            if (response.code() == 200) {
                                String responseStr = response.body().string();
                                try {
                                    JsonParser parser = new JsonParser();  //创建JSON解析器
                                    JsonObject object = (JsonObject) parser.parse(responseStr);  //创建JsonObject对象

                                    // 点数结果
                                    count = object.get("Num").getAsInt();

                                    JsonArray array = object.get("Box").getAsJsonArray();    //得到为json的数组
                                    //resultBitmap = drawNewBitmap(resultImageFile.getAbsolutePath(), array, String.valueOf(count));


                                    // 给result.jpeg画框
                                    resultBitmap = drawNewBitmap(bitmap, array, String.valueOf(count), context, time);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    AVOSCloudUtils.saveErrorMessage(e, CounterHelper.class.getSimpleName());
                                }
                            }
                        } finally {
                            if (response.body() != null)
                                response.body().close();
                            listener.onCompleted(count, resultBitmap, time);
                        }
                    }
                });
            }
        });

    }

    /**
     * 死猪估重接口
     *
     * @param bitmap
     * @param insureNo
     * @param lipeiNo
     * @param listener
     */
    public static void recognitionWeightFromNet(final Bitmap bitmap, String insureNo, String lipeiNo, final OnImageRecognitionWeightListener listener) {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute((new Runnable() {
            @Override
            public void run() {
                Map param = new HashMap();
                param.put("picImage", getImgStr(bitmap));
                param.put("type", "1");
                param.put("baodan", insureNo);
                param.put("libno", lipeiNo);
                OkHttp3Util.doPostForWan(URL_WEIGHT_TEST, param, mHeaderMap, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        listener.onCompleted(-1, 0, 0, 0, "");
                        AVOSCloudUtils.saveErrorMessage(e, CounterHelper.class.getSimpleName());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        float weight = 0, length = 0, area = 0;
                        int status = 0;
                        int errorcode = 0;
                        WeightBean bean = null;
                        String msg = "";
                        try {
                            if (response.code() == 200) {
                                String responseStr = response.body().string();
                                try {
                                    Gson gson = new Gson();
                                    bean = gson.fromJson(responseStr, WeightBean.class);
                                    status = bean.getStatus();
                                    weight = bean.getWeight();
                                    msg = bean.getMsg();
                                    length = bean.getLength();
                                    area = bean.getArea();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    AVOSCloudUtils.saveErrorMessage(e, CounterHelper.class.getSimpleName());
                                }
                            }
                        } finally {
                            if (response.body() != null)
                                response.body().close();
                            listener.onCompleted(weight, length, area, status, msg);
                        }
                    }
                });
            }
        }));
    }

    private static String getErrorMsg(int errorcode) {
        String tipContent = "";
        if (errorcode == DialogHelper.ERROR_CODE_PIG_SMALL) {
            tipContent = "离猪远了，手机放低点，请重新拍！";
        } else if (errorcode == DialogHelper.ERROR_CODE_PIG_LARGE) {
            tipContent = "离猪近了，手机放高点，请重新拍！";
        } else if (errorcode == DialogHelper.ERROR_CODE_NO_FIND_RULER) {
            tipContent = "没尺子或光线太暗，请重新拍！";
        } else if (errorcode == DialogHelper.ERROR_CODE_RULER_SMALL) {
            tipContent = "离猪远了，手机放低点，请重新拍！";
        } else if (errorcode == DialogHelper.ERROR_CODE_RULER_LARGE) {
            tipContent = "离猪近了，手机放高点，请重新拍！";
        } else if (errorcode == DialogHelper.ERROR_CODE_RULER_LARGE_TWO) {
            tipContent = "状态异常，猪太小或手机离猪太近！";
        }
//        else if(errorcode == DialogHelper.ERROR_CODE_PICTURE_DECODE_ERROR) {
//            tipContent = "图片传输中解码出错,\n";
//        }
        return tipContent;
    }

    private static void saveBitmap(final Bitmap bitmap, final String filename) {
        final File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        Matrix matrix = new Matrix();
        matrix.postScale((float) 0.5, (float) 0.5);
        // 得到新的图片
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        try {
            final FileOutputStream out = new FileOutputStream(file);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (final Exception e) {
        }
    }

    public static int number = 1;

    private static Bitmap drawNewBitmap(Bitmap bitmap, JsonArray array, String text, Context context, String time) {
        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(copyBitmap);

        Paint rectPaint = new Paint();
        TextPaint textPaint = new TextPaint();

        Utils.setAttributes(PigAppConfig.getAppContext(), rectPaint, textPaint);
        rectPaint.setTextSize(1 / 2);

        Paint pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setAntiAlias(true);
        pointPaint.setAlpha(100);

        FileOutputStream out = null;
        try {
            final double width = canvas.getWidth();
            final double height = canvas.getHeight();

            int tempCount = 0;
            for (int i = 0; i < array.size(); i++) {
                tempCount++;
                JsonArray xyArray = array.get(i).getAsJsonArray();
//                xyArray.get(0).getAsDouble();
//                xyArray.get(1).getAsDouble();
//                xyArray.get(2).getAsDouble();
//                xyArray.get(3).getAsDouble();

                double x = width * xyArray.get(0).getAsDouble();
                double y = height * xyArray.get(1).getAsDouble();

                double w = width * xyArray.get(2).getAsDouble();
                double h = height * xyArray.get(3).getAsDouble();

                int left = (int) (x - w / 2);
                int top = (int) (y - h / 2);
                int right = (int) (x + w / 2);
                int bottom = (int) (y + h / 2);


                Log.i("xyaywidthheight", width + "," + height);
                Log.i("xyay", xyArray.toString());
                Log.i("xyayleftbottom", left + "," + top + "," + right + "," + bottom);

                //canvas.drawRect(left, top, right, bottom, rectPaint);
                canvas.drawCircle((float) x, (float) y, UIUtils.dp2px(context, 10f), pointPaint);
                textPaint.setColor(Color.YELLOW);
                canvas.drawText(tempCount + "", (float) x, (float) y + 12f, textPaint);
            }
//            canvas.drawText("圈" + number, 150,
//                    canvas.getHeight() - 100, textPaint);
//            canvas.drawText(text + "头", canvas.getWidth() - 200,
//                    canvas.getHeight() - 100, textPaint);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setColor(Color.RED);
            canvas.drawText("圈" + number, 80,
                    120, textPaint);
            canvas.drawText("识别" + text + "头", 300,
                    120, textPaint);

            textPaint.setColor(Color.WHITE);
            textPaint.setTextAlign(Paint.Align.LEFT);
//            float textSize = context.getResources().getDimensionPixelSize(R.dimen.sp_12);
//            textPaint.setTextSize(textSize);

//            StaticLayout layout3 = new StaticLayout("时间：" + time, textPaint, canvas.getWidth(),
//                    Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
////            canvas.drawText("纬度：" + LocationManager_new.getInstance(context).currentLat, 10, 60, textPaint);
//            canvas.translate(10, (float) (height - 40));
//            layout3.draw(canvas);

//            String cAddress = LocationManager_new.getInstance(context).str_address;
//            if (TextUtils.isEmpty(cAddress)) {
//                cAddress = PigPreferencesUtils.getStringValue(Constants.address, PigAppConfig.getAppContext());
//            }
//
//            StaticLayout layout2 = new StaticLayout("地址：" + cAddress, textPaint, canvas.getWidth(),
//                    Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
////            canvas.drawText("地址：" + LocationManager_new.getInstance(context).str_address, 10, (float) 90, textPaint);
//            canvas.translate(0, (float) (-(layout3.getHeight())));
//            layout2.draw(canvas);
//
//            StaticLayout layout1 = new StaticLayout("纬度：" + LocationManager_new.getInstance(context).currentLat, textPaint, canvas.getWidth(),
//                    Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
////            canvas.drawText("纬度：" + LocationManager_new.getInstance(context).currentLat, 10, 60, textPaint);
//            canvas.translate(0, (float) (-(layout2.getHeight())));
//            layout1.draw(canvas);
//
//            StaticLayout layout0 = new StaticLayout("经度：" + LocationManager_new.getInstance(context).currentLon, textPaint, canvas.getWidth(),
//                    Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
////            canvas.drawText("经度：" + LocationManager_new.getInstance(context).currentLon, 10, 30, textPaint);
//            canvas.translate(0, (float) (-(layout1.getHeight())));
//            layout0.draw(canvas);

//
//            canvas.drawText("经度：" + LocationManager_new.getInstance(context).currentLon, 10, (float) height-120, textPaint);
//            canvas.drawText("纬度：" + LocationManager_new.getInstance(context).currentLat, 10, (float) height-80, textPaint);
//            canvas.drawText("地址：" + LocationManager_new.getInstance(context).str_address, 10, (float) height-40, textPaint);
//            canvas.drawText("时间：" + time, 10, (float) height-5, textPaint);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return copyBitmap;
    }


    public static Bitmap drawModifierBitmap(Bitmap bitmap, String text) {
        Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(copyBitmap);

        Paint textPaint = new Paint();

        float textSize = PigAppConfig.getAppContext().getResources().getDimensionPixelSize(R.dimen.sp_14);
        textPaint.setTextSize(textSize);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.RED);

        canvas.drawText("修正为" + text + "头", canvas.getWidth() - 200,
                120, textPaint);
        return copyBitmap;
    }

    /**
     * 将图片转换成Base64编码
     *
     * @param bitmap 待处理图片
     * @return
     */
    private static String getImgStr(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 图片按比例压缩，以长边=1080为准
        // 图片质量提升为60.
        // 获得图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = 1f;

        int max = Math.max(width, height);
        if (max > 1080) {
            scale = 1080f / max;
        }
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        newbm.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = new ByteArrayInputStream(baos.toByteArray());
        byte[] data = null;
        //读取图片字节数组
        try {
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String base64Str = Base64.encodeToString(data, Base64.DEFAULT);
        Log.i("base64Str", base64Str);
        return base64Str;
    }


    private static Bitmap resizeAndSaveBitmap(final Bitmap bitmap, final String filename) {
        final File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        // 图片按比例压缩，以长边=1080为准
        // 图片质量提升为60.
        // 获得图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = 1f;

        int max = Math.max(width, height);
        if (max > 1080) {
            scale = 1080f / max;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的图片
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        try {
            final FileOutputStream out = new FileOutputStream(file);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (final Exception e) {

        }
        return newBitmap;
    }
}
