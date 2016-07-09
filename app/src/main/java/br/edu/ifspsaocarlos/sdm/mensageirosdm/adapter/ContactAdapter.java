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
    private ContactAdapter.OnContactClickListener listener;

    public ContactAdapter(List<Contact> contactList, ContactAdapter.OnContactClickListener listener) {
        this.contactList = contactList;
        this.listener = listener;
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

        holder.mTextViewName.setText(c.getNome_completo());
        holder.mTextViewNickname.setText(c.getApelido());
        holder.listener = listener;
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public Contact getItem(int position) {
        return contactList.get(position);
    }

    public interface OnContactClickListener {
        void onContactClickListener(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewName;
        public TextView mTextViewNickname;
        public OnContactClickListener listener;

        public ViewHolder(View v) {
            super(v);
            mTextViewName = (TextView) v.findViewById(R.id.text_name);
            mTextViewNickname = (TextView) v.findViewById(R.id.text_nickname);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("SDM", "onClick: " + getAdapterPosition());

                    if (listener != null)
                        listener.onContactClickListener(getAdapterPosition());
                }
            });
        }
    }
}
