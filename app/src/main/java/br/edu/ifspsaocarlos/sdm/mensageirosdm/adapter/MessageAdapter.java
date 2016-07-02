package br.edu.ifspsaocarlos.sdm.mensageirosdm.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.R;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Message;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_message, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.mTextViewMessage.setText(message.getCorpo());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public Message getItem(int position) {
        return messageList.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewMessage;

        public ViewHolder(View v) {
            super(v);
            mTextViewMessage = (TextView) v.findViewById(R.id.text_message);
        }
    }
}
