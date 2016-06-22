package br.edu.ifspsaocarlos.sdm.mensageirosdm.adapter;


import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.R;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Contact;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private List<Contact> contactList;

    public ContactAdapter(List<Contact> contactList) {
        this.contactList = contactList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_contact, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contact c = contactList.get(position);

        holder.mTextViewName.setText(c.getName());
        holder.mTextViewMessage.setText(c.getMessage());
        holder.mTextViewTime.setText(c.getTime());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewName;
        public TextView mTextViewMessage;
        public TextView mTextViewTime;


        public ViewHolder(View v) {
            super(v);
            mTextViewName = (TextView) v.findViewById(R.id.text_name);
            mTextViewMessage = (TextView) v.findViewById(R.id.text_message);
            mTextViewTime = (TextView) v.findViewById(R.id.text_time);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("SDM", "onClick: " + getAdapterPosition());
                }
            });
        }
    }
}