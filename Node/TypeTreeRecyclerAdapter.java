package com.nmpa.nmpaapp.modules.office.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.modules.tree.Node;
import com.nmpa.nmpaapp.modules.tree.TreeRecyclerAdapter;

import java.util.List;

public class TypeTreeRecyclerAdapter extends TreeRecyclerAdapter {
    private static final String TAG = "TypeTreeRecyclerAdapter";
    private boolean isSingle;
    public TypeTreeRecyclerAdapter(RecyclerView mTree, Context context, List<Node> datas, int defaultExpandLevel, int iconExpand, int iconNoExpand,boolean isSingle) {
        super(mTree, context, datas, defaultExpandLevel, iconExpand, iconNoExpand);
        this.isSingle = isSingle;
    }

    public TypeTreeRecyclerAdapter(RecyclerView mTree, Context context, List<Node> datas, int defaultExpandLevel) {
        super(mTree, context, datas, defaultExpandLevel);
    }
    
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHoder(View.inflate(mContext, R.layout.tree_list_item,null));
    }

    @Override
    public void onBindViewHolder(final Node node, RecyclerView.ViewHolder holder, int position) {

        final MyHoder viewHolder = (MyHoder) holder;
        viewHolder.cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setChecked(node,viewHolder.cb.isChecked());
                setChildChecked(node,viewHolder.cb.isChecked());
                listener.onItemClick(position);
            }
        });
        viewHolder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (isSingle) {
                    if (b) { //如果checkbox的状态是选中的，那么除了被选中的那条数据，其他Node节点的checkbox状态都为false
                        for (int i = 0; i < mAllNodes.size(); i++) {
                            if (((Node) mAllNodes.get(i)).getId().equals(node.getId())) {
                                ((Node) mAllNodes.get(i)).setChecked(b);
                            } else {
                                ((Node) mAllNodes.get(i)).setChecked(false);
                            }
                        }

                    } else {//如果checkbox的状态是选中的，所有Node节点checkbox状态都为false
                        for (int i = 0; i < mAllNodes.size(); i++) {
                            if (((Node) mAllNodes.get(i)).getId().equals(node.getId())) {
                                ((Node) mAllNodes.get(i)).setChecked(b);
                            }
                        }
                    }
                } else {   ////如果checkbox是多选的，对应node节点的checkbox状态视用户的操作而定
                    for (int i = 0; i < mAllNodes.size(); i++) {
                        if (((Node) mAllNodes.get(i)).getId().equals(node.getId()))
                            ((Node) mAllNodes.get(i)).setChecked(b);

                    }
                }
            }
        });
        
        if (node.isChecked()){
            viewHolder.cb.setChecked(true);
        }else {
            viewHolder.cb.setChecked(false);
        }

        if (node.getIcon() == -1) {
            viewHolder.icon.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.icon.setImageResource(node.getIcon());
        }
        viewHolder.label.setText(node.getName());
    }

    public interface onAItemClickListener{
        void onItemClick(int position);
    }
    
    private onAItemClickListener listener;
    public void setOnItemClickListener (onAItemClickListener onItemClickListener) {
        listener = onItemClickListener;
    }

    class MyHoder extends RecyclerView.ViewHolder{
        public CheckBox cb;
        public TextView label;
        public ImageView icon;
        public MyHoder(View itemView) {
            super(itemView);
            cb = (CheckBox) itemView.findViewById(R.id.cb_select_tree);
            label = (TextView) itemView.findViewById(R.id.id_treenode_label);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }
    }
}
