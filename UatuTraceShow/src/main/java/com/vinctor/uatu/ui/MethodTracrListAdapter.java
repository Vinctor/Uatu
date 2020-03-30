package com.vinctor.uatu.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vinctor.uatuLib.R;

import java.util.ArrayList;
import java.util.List;

public class MethodTracrListAdapter extends RecyclerView.Adapter<MethodTracrListAdapter.MyHolder> {

    private final LayoutInflater layoutInflater;
    List<MethodTraceBean> list = new ArrayList<>();
    Context context;

    public MethodTracrListAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    public void setList(List<MethodTraceBean> mList) {
        list.clear();
        list.addAll(mList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.uatu_trace_list_item_layout, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        MethodTraceBean methodTraceBean = list.get(position);
        holder.className.setText(methodTraceBean.className);
        holder.methodName.setText(methodTraceBean.methodName);
        holder.startTs.setText(methodTraceBean.startTime);
        holder.threadName.setText(methodTraceBean.threadName);
        Long cost = methodTraceBean.cost;
        int color = 0xff5566CC;
        if (cost > 100) {
            color = 0xffBB7788;
        }
        if (color > 500) {
            color = 0xffEE0077;
        }
        if (cost > 1000) {
            color = 0xffff0000;
        }
        holder.timeCost.setTextColor(color);
        holder.timeCost.setText(cost + " ms");
        String desc = methodTraceBean.desc;
        if (TextUtils.isEmpty(desc)) {
            holder.signature.setVisibility(View.GONE);
        } else {
            String descResult =
                    new StringBuilder()
                            .append("(")
                            .append(desc)
                            .append(")")
                            .toString();
            holder.signature.setVisibility(View.VISIBLE);
            holder.signature.setText(descResult);
        }

        String argments = methodTraceBean.argments;
        if (TextUtils.isEmpty(argments)) {
            holder.argmentView.setVisibility(View.GONE);
        } else {
            holder.argmentView.setVisibility(View.VISIBLE);
            holder.argmentView.setText(argments);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {

        private final TextView methodName;
        private final TextView className;
        private final TextView signature;
        private final TextView startTs;
        private final TextView timeCost;
        private final TextView threadName;
        private final TextView argmentView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            methodName = itemView.findViewById(R.id.methodName);
            className = itemView.findViewById(R.id.className);
            signature = itemView.findViewById(R.id.signature);
            startTs = itemView.findViewById(R.id.startTs);
            timeCost = itemView.findViewById(R.id.timeCost);
            threadName = itemView.findViewById(R.id.threadName);
            argmentView = itemView.findViewById(R.id.args);
        }
    }
}
