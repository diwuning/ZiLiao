package com.nmpa.nmpaapp.apply;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.utils.RxBus;
import com.nmpa.nmpaapp.widget.CustomGridView;
import com.nmpa.nmpaapp.widget.NestedExpandListView;
import java.util.List;

public class DialogListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "DialogListAdapter";
    private Context mContext;
    private List<TypeBean> workGroupList;

    public DialogListAdapter(Context mContext, List<TypeBean> workGroupList) {
      this.mContext = mContext;
        this.workGroupList = workGroupList;
    }


    @Override
    public int getGroupCount() {
        return workGroupList.size();
    }

    @Override
    public int getChildrenCount(int i) {
      return 1;
    }

    @Override
    public Object getGroup(int i) {
        return workGroupList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return workGroupList.get(i).getChildren().get(i1);
    }
    
    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        GroupHolder groupHolder = null;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.expandlist_dialog_group,null);
            groupHolder = new GroupHolder();
            groupHolder.groupName = view.findViewById(R.id.groupName);
            groupHolder.iv_groupImg = view.findViewById(R.id.iv_groupImg);
            view.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) view.getTag();
        }
        
        groupHolder.groupName.setText(workGroupList.get(i).getName());
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        ChildHolder childHolder = null;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.expandlist_dialog_child,null);
            childHolder = new ChildHolder();
            childHolder.gv_child = view.findViewById(R.id.gv_child);
            view.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) view.getTag();
        }
//        childHolder.gv_child.setFocusable(false);
        List<TypeBean> groupList = workGroupList.get(i).getChildren();

        DialogChildAdapter childAdapter = new DialogChildAdapter(mContext,groupList);
        childHolder.gv_child.setAdapter(childAdapter);
        childHolder.gv_child.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick  i="+i+",l="+l+","+adapterView.getParent().getParent());
                NestedExpandListView nelv = (NestedExpandListView) adapterView.getParent().getParent();
                for (int j = 0;j<nelv.getChildCount();j++) {
                  if ((((LinearLayout)nelv.getChildAt(j)).getChildAt(1)) instanceof LinearLayout) {
                        ((TextView)((LinearLayout)((LinearLayout)nelv.getChildAt(j)).getChildAt(1)).getChildAt(1)).setSelected(false);
                    }
                }
                CustomGridView gridView = (CustomGridView) adapterView;
                for (int j=0;j< gridView.getChildCount();j++) {
                  ((LinearLayout)gridView.getChildAt(j)).getChildAt(1).setSelected(false);
                }
                LinearLayout linearLayout = (LinearLayout) view;
                linearLayout.getChildAt(1).setSelected(true);
                RxBus.getDefault().post(groupList.get(i));
            }
        });
        return view;
    }
    
    @Override
    public boolean isChildSelectable(int i, int i1) {
        //设为true时，每个子列表上下有横线
        return true;
    }

    class GroupHolder {
        ImageView iv_groupImg;
        TextView groupName;
    }
    
    class ChildHolder {
        CustomGridView gv_child;
    }
}
