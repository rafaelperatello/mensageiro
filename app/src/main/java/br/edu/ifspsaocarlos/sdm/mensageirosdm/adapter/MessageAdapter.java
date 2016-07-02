package br.edu.ifspsaocarlos.sdm.mensageirosdm.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import br.edu.ifspsaocarlos.sdm.mensageirosdm.R;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Contact;
import br.edu.ifspsaocarlos.sdm.mensageirosdm.model.Message;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messageList;
    private String userId;

    public MessageAdapter(List<Message> messageList, String userId) {
        this.messageList = messageList;
        this.userId = userId;
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

        RelativeLayout.LayoutParams paramsRelative = (RelativeLayout.LayoutParams) holder.mRelativeLayoutMessage.getLayoutParams();

        if (message.getOrigem_id().equals(userId)) {
            paramsRelative.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            paramsRelative.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsRelative.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            paramsRelative.removeRule(RelativeLayout.ALIGN_PARENT_START);

            holder.mTextViewMessage.setGravity(Gravity.RIGHT);
            holder.mTextViewMessage.setBackgroundResource(R.drawable.message_sent);

        } else {
            paramsRelative.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            paramsRelative.removeRule(RelativeLayout.ALIGN_PARENT_END);
            paramsRelative.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            paramsRelative.addRule(RelativeLayout.ALIGN_PARENT_START);

            holder.mTextViewMessage.setGravity(Gravity.LEFT);
            holder.mTextViewMessage.setBackgroundResource(R.drawable.message_received);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public Message getItem(int position) {
        return messageList.get(position);
    }

    public void addItem(Message message) {
        messageList.add(message);
        notifyItemChanged(messageList.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewMessage;
        public RelativeLayout mRelativeLayoutMessage;

        public ViewHolder(View v) {
            super(v);
            mTextViewMessage = (TextView) v.findViewById(R.id.text_message);
            mRelativeLayoutMessage = (RelativeLayout) v.findViewById(R.id.relative_message_container);
        }
    }
}
