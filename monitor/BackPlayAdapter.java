package com.nmpa.nmpaapp.modules.monitor;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.android.arouter.launcher.ARouter;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.router.Page;
import java.util.List;

public class BackPlayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "CheckPlanAdapter";
    private Context mContext;
    private List<PlayBean> playBeans;
    
    public BackPlayAdapter(Context mContext, List<PlayBean> planBeans) {
        this.mContext = mContext;
        this.playBeans = planBeans;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.back_play_item,parent,false);
        PlanHolder planHolder = new PlanHolder(view);
        return planHolder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PlayBean planBean = playBeans.get(position);
        PlanHolder planHolder = (PlanHolder) holder;
//        planHolder.tv_bDate.setText(planBean.getRecordDate());
        planHolder.tv_bName.setText(planBean.getName());

        Log.d(TAG,"planBean.getName()="+planBean.getName());
        planHolder.ll_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ARouter.getInstance().build(Page.ACTIVITY_BACK_PLAYER).withString("path",planBean.getPath()).navigation();
            }
        });
    }

    @Override
    public int getItemCount() {
        return playBeans == null? 0: playBeans.size();
    }

    class PlanHolder extends RecyclerView.ViewHolder {
        private TextView tv_bDate,tv_bName;
        private LinearLayout ll_play;

        public PlanHolder(@NonNull View itemView) {
            super(itemView);
//            tv_bDate = itemView.findViewById(R.id.tv_bDate);
            tv_bName = itemView.findViewById(R.id.tv_bName);
            ll_play = itemView.findViewById(R.id.ll_play);
        }
    }

    public OnClickItemListener onClickItemListener;
    public void setOnItemClickListener(OnClickItemListener onClickBottomListener) {
        this.onClickItemListener = onClickBottomListener;
    }
    public interface OnClickItemListener{
        /**
         * 点击确定按钮事件
         */
        public void onItemClick(int position);
    }
}
