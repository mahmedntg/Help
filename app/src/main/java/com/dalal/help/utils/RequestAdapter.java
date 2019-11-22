package com.dalal.help.utils;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dalal.help.R;
import com.dalal.help.ui.requests.RequestsFragment;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private List<Request> requestList;
    private RequestsFragment fragment;


    public RequestAdapter(List<Request> requests, RequestsFragment fragment) {
        this.requestList = requests;
        this.fragment = fragment;
    }

    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.request_item, parent, false);

        return new RequestViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(RequestViewHolder viewHolder, int position) {
        Request model = requestList.get(position);
        User user = fragment.getUser();
        viewHolder.setDelete(user);
        viewHolder.setName(TextUtils.isEmpty(model.getName()) ? String.valueOf(model.getAmount()) : model.getName());
        viewHolder.setStatus(model.getStatus());
        viewHolder.setServiceType(model.getServiceType());

        viewHolder.position = position;
    }


    @Override
    public int getItemCount() {
        return requestList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public int position;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView textView = (TextView) mView.findViewById(R.id.serviceName);
            textView.setText(name);
        }


        public void setStatus(String status) {
            TextView textView = (TextView) mView.findViewById(R.id.status);
            textView.setText(status);
        }

        public void setServiceType(String category) {
            TextView textView = (TextView) mView.findViewById(R.id.serviceType);
            textView.setText(category);
        }

        public void setDelete(User user) {
            TextView textView = (TextView) mView.findViewById(R.id.delete);
            if (user.getType().equalsIgnoreCase("donator")) {
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fragment.deleteItem(position, requestList.get(position).getKey());
                    }
                });
            } else textView.setVisibility(View.GONE);
        }

    }


}
