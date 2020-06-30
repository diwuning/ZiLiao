package com.nmpa.nmpaapp.apply;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.modules.home.adapter.ChildAdapter;
import com.nmpa.nmpaapp.modules.home.bean.WorkBean;
import com.nmpa.nmpaapp.widget.CustomGridView;
import java.util.ArrayList;
import java.util.List;

public class ArrayApplyAdapter extends ArrayAdapter<TypeBean> {
    private static final String TAG = "ArrayApplyAdapter";
    private int mResource;
    private Context mContext;
    public ArrayApplyAdapter(@NonNull Context context, int resource, @NonNull List<TypeBean> objects) {
      super(context, resource, objects);
        mResource = resource;
        mContext = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TypeBean typeBean = getItem(position);
        View view = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        ImageView imageView = view.findViewById(R.id.iv_type);
        imageView.setImageResource(R.drawable.ic_banner_selected);
        TextView textView = view.findViewById(R.id.tv_type);
        textView.setText(typeBean.getName());
        CustomGridView gv_type = view.findViewById(R.id.gv_child);
        List<WorkBean> groupList = new ArrayList<>();
        for (int i=0;i<2;i++) {
          WorkBean workBean = new WorkBean();
            workBean.setImgPath(String.valueOf(R.drawable.icon_dang));
            workBean.setName("类型名"+i);
            groupList.add(workBean);
        }

        ChildAdapter childAdapter = new ChildAdapter(mContext,groupList);
        gv_type.setAdapter(childAdapter);
        gv_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG,"3333333333333333");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.e(TAG,"44444444444");
            }
        });
        
        gv_type.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG,"sdfsdfsdf");
                listener.onChildItemClick(i);
            }
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(position);
            }
        });
        return view;
    };
    public interface onAItemClickListener{
        void onItemClick(int position);
        void onChildItemClick(int position);
    }
    
    private onAItemClickListener listener;
    public void setOnItemClickListener (onAItemClickListener onItemClickListener) {
        listener = onItemClickListener;
    }
}
