package com.innovation.pig.insurance.model;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiangchuang.risks.model.bean.SheListBean;
import com.innovation.pig.insurance.R;
import com.xiangchuang.risks.view.ShowPollingActivity_new;

import java.util.List;


public class PollingResultAdapter_new extends BaseAdapter {
    private Context context;
    private List<SheListBean.DataOffLineBaodanBean> recordList;
    private OnDetailClickListener mListener;
    private OnDetailitemClickListener onDetailitemClickListener;

    public interface OnDetailClickListener {
//        public void onClickView(int sheId);
        public void onClick(int position);
    }

    public PollingResultAdapter_new(Context context, List<SheListBean.DataOffLineBaodanBean> recordList,
                                    OnDetailClickListener listener) {
        this.context = context;
        this.recordList = recordList;
        mListener = listener;
    }


    @Override
    public int getCount() {
        return recordList.size();
    }

    @Override
    public Object getItem(int position) {
        return recordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(context, R.layout.result_list_item_new, null);
            viewHolder.polltitle = (TextView) convertView.findViewById(R.id.poll_title);
            viewHolder.pollcount = (TextView) convertView.findViewById(R.id.poll_count);
            viewHolder.pollDetail = (TextView) convertView.findViewById(R.id.poll_detail);
            viewHolder.pollDate = (TextView) convertView.findViewById(R.id.poll_date);
            viewHolder.poll_countHog = (TextView) convertView.findViewById(R.id.poll_countHog);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.polltitle.setText(recordList.get(position).getSheName()+"_"+recordList.get(position).getPigTypeName());
        int nums = recordList.get(position).getCount();
        Log.i("ShowPollingAdapter", nums + "");
        viewHolder.pollcount.setText(String.valueOf(nums));
        viewHolder.pollDetail.setTag(position);
        viewHolder.pollDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordList.get(position).getCount() == 0) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setIcon(R.drawable.pig_ic_launcher)
                            .setTitle("提示")
                            .setMessage("该猪舍猪只数量为0")
                            .setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(android.content.DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    dialog.setCancelable(false);
                    dialog.show();
                } else {
                    Integer position = (Integer) v.getTag();
//                    mListener.onClickView(Integer.valueOf(recordList.get(position).getSheId()));
                    mListener.onClick(position);
                }
            }
        });
        ShowPollingActivity_new activity= (ShowPollingActivity_new) context;
        viewHolder.poll_countHog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onDetailitemClickListener != null){
                    onDetailitemClickListener.onClick(position);
                }
            }
        });
        String time = recordList.get(position).getDianshuTime();
        if (time != null) {
            if (time.length() > 10)
                time = time.substring(0, 10);
            viewHolder.pollDate.setText(time);
        }

        return convertView;
    }
    public  void  setListener(OnDetailitemClickListener onDetailitemClickListener){
        this.onDetailitemClickListener=onDetailitemClickListener;
    };

    class ViewHolder {
        TextView polltitle, pollcount, pollDetail, pollDate,poll_countHog;
    }
    public interface OnDetailitemClickListener {
        public void onClick(int position);
    }

}
