package io.mrarm.irc.newui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.mrarm.irc.R;
import io.mrarm.irc.ServerConnectionInfo;
import io.mrarm.irc.ServerConnectionManager;

public class MessagesFragment extends Fragment
        implements SlideableFragmentToolbar.FragmentToolbarCallback {

    private static final String ARG_SERVER_UUID = "server_uuid";
    private static final String ARG_CHANNEL_NAME = "channel";

    private static final int LOAD_MORE_REMAINING_ITEM_COUNT = 10;

    private ServerConnectionInfo mConnection;
    private String mChannelName;
    private MessagesData mData;
    private MessagesUnreadData mUnreadData;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    public static MessagesFragment newInstance(ServerConnectionInfo server,
                                                   String channelName) {
        MessagesFragment fragment = new MessagesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SERVER_UUID, server.getUUID().toString());
        if (channelName != null)
            args.putString(ARG_CHANNEL_NAME, channelName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID connectionUUID = UUID.fromString(getArguments().getString(ARG_SERVER_UUID));
        mConnection = ServerConnectionManager.getInstance(getContext())
                .getConnection(connectionUUID);
        mChannelName = getArguments().getString(ARG_CHANNEL_NAME);

        mData = new MessagesData(getContext(), mConnection, mChannelName);
        mData.load(null);

        mUnreadData = new MessagesUnreadData(mConnection, mChannelName);
        mUnreadData.load();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mData.unload();
        mUnreadData.unload();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chat_messages_fragment, container, false);
        mRecyclerView = rootView.findViewById(R.id.messages);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        MessagesAdapter adapter = new MessagesAdapter(mData);
        adapter.setUnreadData(mUnreadData);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addOnScrollListener(new MessagesScrollListener());
        return rootView;
    }

    @Override
    public SlideableFragmentToolbar.ToolbarHolder onCreateToolbar(@NonNull LayoutInflater inflater,
                                                                  @Nullable ViewGroup container) {
        SlideableFragmentToolbar.TextToolbarHolder toolbar =
                new SlideableFragmentToolbar.TextToolbarHolder(this, container);
        toolbar.setTitle(mChannelName);
        return toolbar;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
    }

    private class MessagesScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            int firstVisible = mLayoutManager.findFirstVisibleItemPosition();
            int lastVisible = mLayoutManager.findLastVisibleItemPosition();
            if (firstVisible <= LOAD_MORE_REMAINING_ITEM_COUNT) {
                mData.loadMoreMessages(false);
            }
            if (lastVisible >= mLayoutManager.getItemCount() - LOAD_MORE_REMAINING_ITEM_COUNT) {
                mData.loadMoreMessages(true);
            }
        }

    }

}
