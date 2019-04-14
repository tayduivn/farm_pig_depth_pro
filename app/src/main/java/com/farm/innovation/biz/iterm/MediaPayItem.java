package com.farm.innovation.biz.iterm;

import android.content.Context;

import com.farm.innovation.biz.classifier.CowFaceDetectTFlite;
import com.farm.innovation.biz.classifier.DonkeyFaceDetectTFlite;
import com.farm.innovation.biz.classifier.PigFaceDetectTFlite;
import com.farm.innovation.utils.ConstUtils;
import com.farm.innovation.utils.FileUtils;
import com.farm.innovation.utils.PreferencesUtils;
import com.farm.innovation.utils.StorageUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.tensorflow.demo.FarmGlobal;


/**
 * Created by haojie on 2018/3/14. 理赔用相关文件接口
 */

public class MediaPayItem {
    private static final String TXT_SUFFIX = ".txt";
    private static final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    /**
     * The name of path and bitmap
     */
    private String mDirName = "理赔";//storage/emulated/0/innovation/animal/投保
    private File mDir = null;//storage/emulated/0/innovation/animal/投保

    private String mCurrentName = mDirName + "/" + "Current"; //当前捕捉猪的文件（图片+视频+编号.txt）
    private String mBitmapName = mCurrentName + "/" + "图片";
    private String mVideoName = mCurrentName + "/" + "视频";
    private String mNumTxtName = mCurrentName + "/" + "number";


    private String mZipImageName = mDirName + "/" + "ZipImage"; //保存所有图片zip文件
    private String mZipVideoName = mDirName + "/" + "ZipVideo"; //保存所有视频zip文件
    private File mZipImageDir = null;//storage/emulated/0/innovation/animal/理赔/ZipImage
    private File mZipVideoDir = null;//storage/emulated/0/innovation/animal/理赔/ZipVideo

    private File mCurrentDir = null;//storage/emulated/0/innovation/animal/理赔/Current
    private File mBimmapDir = null;//storage/emulated/0/innovation/animal/理赔/Current/图片
    private File mVideoDir = null;//storage/emulated/0/innovation/animal/理赔/Current/视频
    private File mNumberFile = null;//storage/emulated/0/innovation/animal/理赔/Current/视频

    Context mContext;

    public MediaPayItem(Context context) {
        mContext = context;
        currentInit();
    }


    public void currentInit()
    {
        //创建投保目录
        if (mDir == null) {
            mDir = new File(StorageUtils.getExternalCacheDir(mContext), mDirName);
            if (!mDir.exists()) {
                mDir.mkdirs();
            }
        }

        //创建Current目录
        if (mCurrentDir == null) {
            mCurrentDir = new File(StorageUtils.getExternalCacheDir(mContext), mCurrentName);
            if (!mCurrentDir.exists()) {
                mCurrentDir.mkdirs();
            }
        }
        //创建图片目录
        if (mBimmapDir == null) {
            mBimmapDir = new File(StorageUtils.getExternalCacheDir(mContext), mBitmapName);
            if (!mBimmapDir.exists()) {
                mBimmapDir.mkdirs();
            }
            //创建图片下角度分类目录
            File mtmpDir;
            String tmpstr = "";
            for(int i = 0; i < 4; i++)
            {
                tmpstr = getAnglePrefix(i);
                mtmpDir = new File(StorageUtils.getExternalCacheDir(mContext), mBitmapName+"/"+tmpstr);
                if (!mtmpDir.exists()) {
                    mtmpDir.mkdirs();
                }
            }
        }

        //创建视频目录
        if (mVideoDir == null) {
            mVideoDir = new File(StorageUtils.getExternalCacheDir(mContext), mVideoName);
            if (!mVideoDir.exists()) {
                mVideoDir.mkdirs();
            }
        }


        //创建图片zip目录
        if (mZipImageDir == null) {
            mZipImageDir = new File(StorageUtils.getExternalCacheDir(mContext), mZipImageName);
            if (!mZipImageDir.exists()) {
                mZipImageDir.mkdirs();
            }
        }
        //创建视频zip目录
        if (mZipVideoDir == null) {
            mZipVideoDir = new File(StorageUtils.getExternalCacheDir(mContext), mZipVideoName);
            if (!mZipVideoDir.exists()) {
                mZipVideoDir.mkdirs();
            }
        }
        //创建标号文件
//        mNumberFile = new File(StorageUtils.getExternalCacheDir(mContext), mNumTxtName + TXT_SUFFIX);
//        try {
//            if (!mNumberFile.exists()) {
//                mNumberFile.createNewFile();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    public void currentDel()    {

        FileUtils.deleteFile(mCurrentDir);
        mCurrentDir = null;
        mBimmapDir = null;
        mVideoDir = null;
        mNumberFile = null;
    }

    //获得存储视频的video文件名
    public String getVideoFileName() {
        String strVideofile = null;
        File mtmpBimmapDir = null;
        if (mtmpBimmapDir == null) {
            mtmpBimmapDir = new File(StorageUtils.getExternalCacheDir(mContext), mVideoName);
            if (!mtmpBimmapDir.exists()) {
                mtmpBimmapDir.mkdirs();
            }
        }
        SimpleDateFormat tmpSimpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS", Locale.getDefault());
        String fname = tmpSimpleDateFormat.format(new Date(System.currentTimeMillis()));
        strVideofile = new File(mtmpBimmapDir, fname).getAbsolutePath() + FarmGlobal.VIDEO_SUFFIX;///storage/emulated/0/innovation/animal/Current/视频/20180301105200.mp4

        return strVideofile;
    }


    //获得图片目录下角度分类目录前缀名
    private String getAnglePrefix(int type)
    {
        String prestr = "";
        if(type < 4)
            prestr = "Angle-0" + type;
        else
            prestr = "Angle-10";

        return prestr;
    }
    //获得存储角度图片的image文件名
    public String getBitmapFileName(int type) {
        String prestr = getAnglePrefix(type);
        File mtmpBimmapDir = null;
        if (mtmpBimmapDir == null) {
            mtmpBimmapDir = new File(StorageUtils.getExternalCacheDir(mContext), mBitmapName + "/" + prestr);
            if (!mtmpBimmapDir.exists()) {
                mtmpBimmapDir.mkdirs();
            }
        }
        SimpleDateFormat tmpSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        String fname = tmpSimpleDateFormat.format(new Date(System.currentTimeMillis()));
        String strBitmapfile = null;
        int animalType =  PreferencesUtils.getAnimalType(mContext);
        if (animalType == ConstUtils.ANIMAL_TYPE_PIG){
            strBitmapfile= new File(mtmpBimmapDir, PigFaceDetectTFlite.srcPigBitmapName).getAbsolutePath();
        }else if (animalType == ConstUtils.ANIMAL_TYPE_CATTLE){
            strBitmapfile= new File(mtmpBimmapDir, CowFaceDetectTFlite.srcCowBitmapName).getAbsolutePath();
        }else if (animalType == ConstUtils.ANIMAL_TYPE_DONKEY){
            strBitmapfile= new File(mtmpBimmapDir, DonkeyFaceDetectTFlite.srcDonkeyBitmapName).getAbsolutePath();
        }else {
            strBitmapfile= new File(mtmpBimmapDir, "other"+fname).getAbsolutePath() + FarmGlobal.IMAGE_SUFFIX;
        }
        return strBitmapfile;
    }
    //存原图路径
    public String getOriBitmapFileName(){
        File mtmpBimmapDir = null;
        if (mtmpBimmapDir == null) {
            mtmpBimmapDir = new File(StorageUtils.getExternalCacheDir(mContext), mBitmapName + "/Ori/Info");
            if (!mtmpBimmapDir.exists()) {
                mtmpBimmapDir.mkdirs();
            }
        }
        SimpleDateFormat tmpSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        String fname = tmpSimpleDateFormat.format(new Date(System.currentTimeMillis()));
        String strBitmapfile = null;
        int animalType =  PreferencesUtils.getAnimalType(mContext);

        if (animalType == ConstUtils.ANIMAL_TYPE_PIG){
            String pigPath = PigFaceDetectTFlite.srcPigBitmapName.equals("")?fname: PigFaceDetectTFlite.srcPigBitmapName;
            strBitmapfile= new File(mtmpBimmapDir, pigPath).getAbsolutePath();
        }else if (animalType == ConstUtils.ANIMAL_TYPE_CATTLE){
            String cowPath = CowFaceDetectTFlite.srcCowBitmapName.equals("")?fname: CowFaceDetectTFlite.srcCowBitmapName;
            strBitmapfile= new File(mtmpBimmapDir, cowPath).getAbsolutePath();
        }else if (animalType == ConstUtils.ANIMAL_TYPE_DONKEY){
            String donkeyPath = DonkeyFaceDetectTFlite.srcDonkeyBitmapName.equals("")?fname: DonkeyFaceDetectTFlite.srcDonkeyBitmapName;
            strBitmapfile= new File(mtmpBimmapDir, donkeyPath).getAbsolutePath();
        }else {
            strBitmapfile= new File(mtmpBimmapDir, "other"+fname).getAbsolutePath() + FarmGlobal.IMAGE_SUFFIX;
        }
        return strBitmapfile;
    }

    public String getOriInfoBitmapFileName(String subPath){
        File mtmpBimmapDir = null;
        if (mtmpBimmapDir == null) {
            mtmpBimmapDir = new File(StorageUtils.getExternalCacheDir(mContext), mBitmapName + "/Ori/Info" + subPath);
            if (!mtmpBimmapDir.exists()) {
                mtmpBimmapDir.mkdirs();
            }
        }
        SimpleDateFormat tmpSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        String fname = tmpSimpleDateFormat.format(new Date(System.currentTimeMillis()));
        String strBitmapfile = null;
        int animalType =  PreferencesUtils.getAnimalType(mContext);

        if (animalType == ConstUtils.ANIMAL_TYPE_PIG){
            String pigPath = PigFaceDetectTFlite.srcPigBitmapName.equals("")?fname: PigFaceDetectTFlite.srcPigBitmapName;
            strBitmapfile= new File(mtmpBimmapDir, pigPath).getAbsolutePath();
        }else if (animalType == ConstUtils.ANIMAL_TYPE_CATTLE){
            String cowPath = CowFaceDetectTFlite.srcCowBitmapName.equals("")?fname: CowFaceDetectTFlite.srcCowBitmapName;
            strBitmapfile= new File(mtmpBimmapDir, cowPath).getAbsolutePath();
        }else if (animalType == ConstUtils.ANIMAL_TYPE_DONKEY){
            String donkeyPath = DonkeyFaceDetectTFlite.srcDonkeyBitmapName.equals("")?fname: DonkeyFaceDetectTFlite.srcDonkeyBitmapName;
            strBitmapfile= new File(mtmpBimmapDir, donkeyPath).getAbsolutePath();
        }else {
            strBitmapfile= new File(mtmpBimmapDir, "other"+fname).getAbsolutePath() + FarmGlobal.IMAGE_SUFFIX;
        }
        return strBitmapfile;
    }

    public String getOriInfoTXTFileName() {
        File mtmpBimmapDir = null;
        if (mtmpBimmapDir == null) {
            mtmpBimmapDir = new File(StorageUtils.getExternalCacheDir(mContext), mBitmapName + "/Ori/Info");
            if (!mtmpBimmapDir.exists()) {
                mtmpBimmapDir.mkdirs();
            }
        }
        String strTxtfile = new File(mtmpBimmapDir, "infoTXT").getAbsolutePath() + TXT_SUFFIX;
        return strTxtfile;
    }

    public String getUnsuccessInfoTXTFileName() {
        File mtmpBimmapDir = null;
        if (mtmpBimmapDir == null) {
            mtmpBimmapDir = new File(StorageUtils.getExternalCacheDir(mContext), mBitmapName + "/Ori/Info");
            if (!mtmpBimmapDir.exists()) {
                mtmpBimmapDir.mkdirs();
            }
        }
        String strTxtfile = new File(mtmpBimmapDir, "unsuccessTXT").getAbsolutePath() + TXT_SUFFIX;
        return strTxtfile;
    }


    //获得存储角度信息的txt文件名
    public String getTxtFileNme(int type) {
        String prestr = getAnglePrefix(type);
        File mtmpBimmapDir = null;
        if (mtmpBimmapDir == null) {
            mtmpBimmapDir = new File(StorageUtils.getExternalCacheDir(mContext), mBitmapName + "/" + prestr);
            if (!mtmpBimmapDir.exists()) {
                mtmpBimmapDir.mkdirs();
            }
        }
        String strTxtfile = new File(mtmpBimmapDir, prestr).getAbsolutePath() + TXT_SUFFIX;
        return strTxtfile;
    }

    public String getCurrentDir() {
        if (mCurrentDir == null) {
            mCurrentDir = new File(StorageUtils.getExternalCacheDir(mContext), mCurrentName);
            if (!mCurrentDir.exists()) {
                mCurrentDir.mkdirs();
            }
        }
        return mCurrentDir.getAbsolutePath();
    }

    public String getImageDir() {
        if (mBimmapDir == null) {
            mBimmapDir = new File(StorageUtils.getExternalCacheDir(mContext), mBitmapName);
            if (!mBimmapDir.exists()) {
                mBimmapDir.mkdirs();
            }
        }
        return mBimmapDir.getAbsolutePath();
    }

    public String getVideoDir() {
        if (mVideoDir == null) {
            mVideoDir = new File(StorageUtils.getExternalCacheDir(mContext), mVideoName);
            if (!mVideoDir.exists()) {
                mVideoDir.mkdirs();
            }
        }
        return mVideoDir.getAbsolutePath();
    }

    public String getZipImageDir() {
        if (mZipImageDir == null) {
            mZipImageDir = new File(StorageUtils.getExternalCacheDir(mContext), mZipImageName);
            if (!mZipImageDir.exists()) {
                mZipImageDir.mkdirs();
            }
        }
        return mZipImageDir.getAbsolutePath();
    }

    public String getZipVideoDir() {
        if (mZipVideoDir == null) {
            mZipVideoDir = new File(StorageUtils.getExternalCacheDir(mContext), mZipVideoName);
            if (!mZipVideoDir.exists()) {
                mZipVideoDir.mkdirs();
            }
        }
        return mZipVideoDir.getAbsolutePath();
    }


    //取标号文件
    public File getNumberFile() {
        if (mNumberFile == null) {
            mNumberFile = new File(StorageUtils.getExternalCacheDir(mContext), mNumTxtName+ TXT_SUFFIX);
            try {
                if (!mNumberFile.exists()) {
                    mNumberFile.createNewFile();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mNumberFile;
    }

    //获得压缩文件名
    public String getZipFileName() {
        SimpleDateFormat tmpSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmmss_SSS", Locale.getDefault());
//        String name =  "Time-" + tmpSimpleDateFormat.format(new Date(System.currentTimeMillis()));
        String name = String.valueOf(System.currentTimeMillis());
        FarmGlobal.ZipFileName = name;
        return name;
    }

}