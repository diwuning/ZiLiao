package com.nmpa.nmpaapp.apply;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nmpa.nmpaapp.R;

import java.util.List;

public class DialogChildAdapter extends BaseAdapter {
    private static final String TAG = "DialogChildAdapter";
    private Context mContext;
    private List<TypeBean> workChilds;
    public DialogChildAdapter(Context mContext, List<TypeBean> workChilds) {
        this.mContext = mContext;
        this.workChilds = workChilds;
    }

    @Override
    public int getCount() {
        return workChilds ==null?0:workChilds.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }
    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ItemHolder itemHolder = null;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.expandlist_dialog_item,null);
            itemHolder = new ItemHolder();
            itemHolder.ll_item = view.findViewById(R.id.ll_item);
            itemHolder.iv_img = view.findViewById(R.id.iv_workIcon);
            itemHolder.tv_itemName = view.findViewById(R.id.tv_workName);
            view.setTag(itemHolder);
        } else {
            itemHolder = (ItemHolder) view.getTag();
        }
        TypeBean bean = workChilds.get(i);
        itemHolder.tv_itemName.setText(bean.getName());
        return view;
    }

    class ItemHolder {
        LinearLayout ll_item;
        ImageView iv_img;
        TextView tv_itemName;
    }
}
