package innovation.biz.iterm;


import com.xiangchuangtec.luolu.animalcounter.MyApplication;

import innovation.biz.classifier.PigKeyPointsDetectTFlite;
import innovation.biz.classifier.PigRotationPrediction;
import innovation.media.Model;
import innovation.utils.FileUtils;
import innovation.utils.PreferencesUtils;
import innovation.utils.Rot2AngleType;

import org.tensorflow.demo.CameraConnectionFragment;
import org.tensorflow.demo.DetectorActivity;
import org.tensorflow.demo.Global;
import org.tensorflow.demo.env.Logger;

import java.io.File;

/**
 * Created by Luolu on 2018/10/30.
 * InnovationAI
 * luolu@innovationai.cn
 */
public class AnimalClassifierResultIterm {
    private final static float ADD_PIC_RATE = 1.6f;
    private static final Logger LOGGER = new Logger(AnimalClassifierResultIterm.class.getName());
    public static void pigAngleCalculateTFlite(PostureItem postureItem) {
        int type;
        int maxLeft = PreferencesUtils.getMaxPics(PreferencesUtils.FACE_ANGLE_MAX_LEFT, MyApplication.getAppContext());
        int maxMiddle = PreferencesUtils.getMaxPics(PreferencesUtils.FACE_ANGLE_MAX_MIDDLE, MyApplication.getAppContext());
        int maxRight = PreferencesUtils.getMaxPics(PreferencesUtils.FACE_ANGLE_MAX_RIGHT, MyApplication.getAppContext());
        DetectorActivity.AngleTrackType = 10;
        PigFaceKeyPointsItem pigFaceKeyPointsItem = PigFaceKeyPointsItem.getInstance();
        type = Rot2AngleType.getPigAngleType((float) postureItem.rot_x, (float) postureItem.rot_y);
        String imagefilename = "";
        String imageSrcFileName = "";
        String txtfilename = "";

        // 理赔标志
        boolean isLiPei = (Global.model == Model.VERIFY.value());


        boolean addImgFlag = false;

        // 判断图片角度
        if((PigRotationPrediction.pigPredictAngleType == 1  || isLiPei)
                && PigKeyPointsDetectTFlite.pigKeypointsK1 == true) {
            DetectorActivity.AngleTrackType = 1;
            type = 1;
            // 未达到上限时增加图片
            if(DetectorActivity.type1Count < maxLeft * ADD_PIC_RATE) {
                DetectorActivity.type1Count++;
                addImgFlag = true;
            }else{
                //左脸达到上限发消息 关闭左脸提示
                CameraConnectionFragment.collectNumberHandler.sendEmptyMessage(3);
            }
        } else if((PigRotationPrediction.pigPredictAngleType == 2  || isLiPei)
                && PigKeyPointsDetectTFlite.pigKeypointsK2 == true){
            DetectorActivity.AngleTrackType = 2;
            type = 2;
            // 未达到上限时增加图片
            if(DetectorActivity.type2Count < maxMiddle * ADD_PIC_RATE) {
                DetectorActivity.type2Count++;
                addImgFlag = true;
            }
        } else if((PigRotationPrediction.pigPredictAngleType == 3  || isLiPei)
                && PigKeyPointsDetectTFlite.pigKeypointsK3 == true) {
            DetectorActivity.AngleTrackType = 3;
            type = 3;
            // 未达到上限时增加图片
            if(DetectorActivity.type3Count < maxRight * ADD_PIC_RATE) {
                DetectorActivity.type3Count++;
                addImgFlag = true;
            }else{
                //右脸达到上限发消息 关闭右脸提示
                CameraConnectionFragment.collectNumberHandler.sendEmptyMessage(4);
            }
        } else {
            // 啥也不干
            type = 10;
            return;
        }


        imagefilename = Global.mediaPayItem.getBitmapFileName(type);
        txtfilename = Global.mediaPayItem.getTxtFileNme(type);

        //保存角度信息
        String contenType = imagefilename.substring(imagefilename.lastIndexOf("/") + 1);
        contenType += ":";
        contenType += "rot_x = " + postureItem.rot_x + "; ";
        contenType += "rot_y = " + postureItem.rot_y + "; ";
        contenType += "rot_z = " + postureItem.rot_z + "; ";
        contenType += "box_x0 = " + postureItem.modelX0 + "; ";
        contenType += "box_y0 = " + postureItem.modelY0 + "; ";
        contenType += "box_x1 = " + postureItem.modelX1 + "; ";
        contenType += "box_y1 = " + postureItem.modelY1 + "; ";
        contenType += "score = " + postureItem.modelDetectedScore + "; ";
        contenType += "point0 = " + pigFaceKeyPointsItem.getPointFloat0().toString() + "; ";
        contenType += "point1 = " + pigFaceKeyPointsItem.getPointFloat1().toString() + "; ";
        contenType += "point2 = " + pigFaceKeyPointsItem.getPointFloat2().toString() + "; ";
        contenType += "point3 = " + pigFaceKeyPointsItem.getPointFloat3().toString() + "; ";
        contenType += "point4 = " + pigFaceKeyPointsItem.getPointFloat4().toString() + "; ";
        contenType += "point5 = " + pigFaceKeyPointsItem.getPointFloat5().toString() + "; ";
        contenType += "point6 = " + pigFaceKeyPointsItem.getPointFloat6().toString() + "; ";
        contenType += "point7 = " + pigFaceKeyPointsItem.getPointFloat7().toString() + "; ";
        contenType += "point8 = " + pigFaceKeyPointsItem.getPointFloat8().toString() + "; ";
        contenType += "point9 = " + pigFaceKeyPointsItem.getPointFloat9().toString() + "; ";
        contenType += "point10 = " + pigFaceKeyPointsItem.getPointFloat10().toString() + "; ";
        LOGGER.i("pigFaceKeyPointsItem:" + pigFaceKeyPointsItem.toString());

        // 符合角度且未达到上限时增加图片
        if (addImgFlag) {
            FileUtils.saveInfoToTxtFile(txtfilename, contenType + "angle:" + type);
            //保存图片
            File tmpimagefile = new File(imagefilename);
            // File tmpImageSrcFileName = new File(imageSrcFileName);
            // save clip scale bitmap
            FileUtils.saveBitmapToFile(postureItem.clipBitmap, tmpimagefile);
            // 保存src图片
            // FileUtils.saveBitmapToFile(postureItem.srcBitmap, tmpImageSrcFileName);
            DetectorActivity.tracker.getCountOfCurrentImage(DetectorActivity.type1Count,
                    DetectorActivity.type2Count, DetectorActivity.type3Count);
        }

        if (DetectorActivity.type1Count >= maxLeft && DetectorActivity.type2Count >= maxMiddle && DetectorActivity.type3Count >= maxRight) {
            CameraConnectionFragment.collectNumberHandler.sendEmptyMessage(1);
        }
    }

}