package com.farm.innovation.bean;

import java.io.Serializable;

public class CattleBean implements Serializable {
//    public String name;// 		//图片名
//    public byte[] picture;//		//图片数据
    public double longitude;//	//经度
    public double latitude;//	//维度
    public String address;//	//地点
    public long time;//		//拍照时间戳
    public String zipPath;//图片数据
    public String message;//比较结果
    public int resultStatus;
    public int type;//0：投保，1：理赔
//    public PayInfoContrastResultBean data;
}
