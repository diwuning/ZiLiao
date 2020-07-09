package com.nmpa.nmpaapp.modules.tree;

public interface OnTreeNodeClickListener {
    /**
     * 点击事件方法
     * @param node 节点
     * @param position 条目位置
     */
    void onClick(Node node, int position);
}
