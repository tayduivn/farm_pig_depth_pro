/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.demo;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.RectF;

import java.io.File;
import java.util.List;

import innovation.biz.iterm.NewPigKeyPointAndRotationItem;
import innovation.biz.iterm.PostureItem;
import innovation.biz.iterm.PredictRotationIterm;
import innovation.utils.PointFloat;

/**
 * Generic interface for interacting with different recognition engines.
 */
public interface Classifier {
  /**
   * An immutable resultStatus returned by a Classifier describing what was recognized.
   */
  public class Recognition {
    /**
     * InSureCompanyBean unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private final String id;

    /**
     * Display name for the recognition.
     */
    private final String title;

    /**
     * InSureCompanyBean sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private final Float confidence;

    /** Optional location within the source image for the location of the recognized object. */
    private RectF location;
    private  List<Point> points;


    public Recognition(
            final String id, final String title, final Float confidence,
            final RectF location, List<Point> points) {
      this.id = id;
      this.title = title;
      this.confidence = confidence;
      this.location = location;
      this.points = points;
    }
    public Recognition(
            final String id, final String title, final Float confidence, final RectF location) {
      this.id = id;
      this.title = title;
      this.confidence = confidence;
      this.location = location;
    }

    public String getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }

    public Float getConfidence() {
      return confidence;
    }

    public RectF getLocation() {
      return new RectF(location);
    }

    public void setLocation(RectF location) {
      this.location = location;
    }
    public List<Point> getPoints() {
      return points;
    }

    public void setPoints(List<Point> points) {
      this.points = points;
    }

    @Override
    public String toString() {
      String resultString = "";
      if (id != null) {
        resultString += "[" + id + "] ";
      }

      if (title != null) {
        resultString += title + " ";
      }

      if (confidence != null) {
        resultString += String.format("(%.1f%%) ", confidence * 100.0f);
      }

      if (location != null) {
        resultString += location + " ";
      }

      return resultString.trim();
    }
  }


  public class RecognitionAndPostureItem {
    private List<Recognition> list;

    private PostureItem postureItem;
    private PredictRotationIterm predictRotationIterm;

    public PredictRotationIterm getPredictRotationIterm() {
      return predictRotationIterm;
    }

    public void setPredictRotationIterm(PredictRotationIterm predictRotationIterm) {
      this.predictRotationIterm = predictRotationIterm;
    }

    public List<Recognition> getList() {
      return list;
    }

    public void setList(List<Recognition> list) {
      this.list = list;
    }

    public PostureItem getPostureItem() {
      return postureItem;
    }

    public void setPostureItem(PostureItem postureItem) {
      this.postureItem = postureItem;
    }
  }
  NewPigKeyPointAndRotationItem pigRecognizePointImage(Bitmap bitmap, Bitmap originalBitmap);

  RecognitionAndPostureItem pigRecognitionAndPostureItem(Bitmap bitmap, Bitmap originalBitmap);
  RecognitionAndPostureItem pigRecognitionAndPostureItemTFlite(Bitmap bitmap, Bitmap originalBitmap);
  PredictRotationIterm pigRotationPredictionItemTFlite(Bitmap bitmap, Bitmap originalBitmap);

  List<PointFloat> recognizePointImage(Bitmap bitmap,Bitmap originalBitmap);


  void enableStatLogging(final boolean debug);

  String getStatString();

  void close();
}
