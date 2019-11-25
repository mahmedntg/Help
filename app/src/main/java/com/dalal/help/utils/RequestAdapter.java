package com.dalal.help.utils;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dalal.help.R;
import com.dalal.help.ui.requestdetails.RequestDetailsFragment;
import com.dalal.help.ui.requests.RequestsFragment;

import java.util.List;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private List<Request> requestList;
    private RequestsFragment fragment;
    private User user;


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
        user = fragment.getUser();
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

    public class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        public int position;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView textView = (TextView) mView.findViewById(R.id.serviceName);
            textView.setOnClickListener(this);
            textView.setText(name);
        }


        public void setStatus(String status) {
            TextView textView = (TextView) mView.findViewById(R.id.status);
            textView.setOnClickListener(this);
            textView.setText(status);
        }

        public void setServiceType(String serviceType) {
            TextView textView = (TextView) mView.findViewById(R.id.serviceType);
            textView.setOnClickListener(this);
            textView.setText(serviceType);
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

        @Override
        public void onClick(View v) {
            Bundle data = new Bundle();
            data.putSerializable("request", requestList.get(position));

            RequestDetailsFragment requestDetailsFragment = new RequestDetailsFragment();
            requestDetailsFragment.setArguments(data);
            FragmentTransaction fragmentTransaction = fragment.getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment, requestDetailsFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        }

    }


}
