package cn.lemon.view.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.lemon.view.R;


/**
 * Created by linlongxin on 2015/12/19.
 */
public abstract class RecyclerAdapter<T> extends RecyclerView.Adapter<BaseViewHolder<T>> {

    private static final String TAG = "RecyclerAdapter";
    private boolean allowLog = true;  //改成false关闭日志

    private static final int HEADER_TYPE = 111;
    private static final int FOOTER_TYPE = 222;
    protected static final int STATUS_TYPE = 333;
    protected int mViewCount = 0;

    private boolean hasHeader = false;
    private boolean hasFooter = false;
    public boolean isShowNoMore = false;   //停止加载
    public boolean loadMoreAble = false;   //是否可加载更多

    protected Action mLoadMoreAction;

    private List<T> mData = new ArrayList<>();

    private View headerView;
    private View footerView;
    protected View mStatusView;
    protected LinearLayout mLoadMoreView;
    public TextView mNoMoreView;

    private Context mContext;

    public void colseLog() {
        allowLog = false;
    }

    public RecyclerAdapter(Context context) {
        mContext = context;
        initStatusView(context);
    }

    public RecyclerAdapter(Context context, T[] data) {
        this(context, Arrays.asList(data));
    }

    public RecyclerAdapter(Context context, List<T> data) {
        mContext = context;
        initStatusView(context);
        this.mData = data;
        mViewCount += data.size();
        notifyDataSetChanged();
    }

    public void initStatusView(Context context) {
        mStatusView = LayoutInflater.from(context).inflate(R.layout.view_status_last, null);
        mStatusView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mLoadMoreView = (LinearLayout) mStatusView.findViewById(R.id.load_more_view);
        mNoMoreView = (TextView) mStatusView.findViewById(R.id.no_more_view);
        mViewCount++;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER_TYPE) {
            return new BaseViewHolder(headerView);
        } else if (viewType == FOOTER_TYPE) {
            return new BaseViewHolder(footerView);
        } else if (viewType == STATUS_TYPE) {
            return new BaseViewHolder(mStatusView);
        } else
            return onCreateBaseViewHolder(parent, viewType);
    }

    public abstract BaseViewHolder<T> onCreateBaseViewHolder(ViewGroup parent, int viewType);

    /* ViewHolder 绑定数据，这里的 position 和 getItemViewType() 方法的 position 不一样
        这里的 position 指当前可见的 item 的 position 的位置。
        注意 ：每个 ViewHolder 绑定数据时值调用此方法一次
     */
    @Override
    public void onBindViewHolder(BaseViewHolder<T> holder, int position) {
        log("onBindViewHolder  viewCount : " + mViewCount + " position : " + position);
        if (position == 0) {
            return;
        } else if (!hasHeader && !hasFooter && position < mData.size()) { //没有Header和Footer
            holder.setData(mData.get(position));
        } else if (hasHeader && !hasFooter && position > 0 && position < mViewCount - 1) { //有Header没有Footer
            holder.setData(mData.get(position - 1));
        } else if (!hasHeader && position < mViewCount - 2) { //没有Header，有Footer
            holder.setData(mData.get(position));
        } else if (position > 0 && position < mViewCount - 2) { //都有
            holder.setData(mData.get(position - 1));
        }

        int positionEnd;
        if (hasHeader && hasFooter) {
            positionEnd = mViewCount - 3;
        } else if (hasHeader && !hasFooter) {
            positionEnd = mViewCount - 2;
        } else if (!hasHeader && hasFooter) {
            positionEnd = mViewCount - 2;
        } else {
            positionEnd = mViewCount - 1;
        }
        if (loadMoreAble && !isShowNoMore && position == positionEnd) {
            mLoadMoreView.setVisibility(View.VISIBLE);
            if (mLoadMoreAction != null) {
                mLoadMoreAction.onAction();
            }
        }
    }

    /**
     * ViewHolder 更新 Item 的位置选择 ViewType , 和 UI 是同步的
     */
    @Override
    public int getItemViewType(int position) {
        if (hasHeader && position == 0) {   //header
            return HEADER_TYPE;
        }
        if (hasFooter && position == mViewCount - 2) {  //footer
            return FOOTER_TYPE;
        }
        if (position == mViewCount - 1) {  //最后View的状态
            return STATUS_TYPE;
        }

        return super.getItemViewType(position);
    }

    /**
     * 包含了 Header , Footer , 状态显示 Item
     */
    @Override
    public int getItemCount() {
        return mViewCount;
    }

    public void showNoMore() {
        isShowNoMore = true;
        mLoadMoreView.post(new Runnable() {
            @Override
            public void run() {
                mLoadMoreView.setVisibility(View.GONE);
                mNoMoreView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void openLoadMore() {
        isShowNoMore = false;
        mLoadMoreView.post(new Runnable() {
            @Override
            public void run() {
                mLoadMoreView.setVisibility(View.VISIBLE);
                mNoMoreView.setVisibility(View.GONE);
            }
        });
    }

    public void setLoadMoreAction(Action action) {
        mLoadMoreAction = action;
    }

    public void add(T object) {
        if (!isShowNoMore) {
            mData.add(object);
            int position;
            if (hasFooter) {
                position = mViewCount - 2;
            } else {
                position = mViewCount - 1;
            }
            mViewCount++;
            notifyItemInserted(position);
        }
    }

    public void insert(T object, int position) {
        if (!isShowNoMore) {
            mData.add(position, object);
            mViewCount++;
            notifyItemInserted(position);
        }
    }

    public void addAll(List<T> data) {
        int size = data.size();
        if (!isShowNoMore && size > 0) {
            mData.addAll(data);
            int positionStart;
            if (hasFooter) {
                positionStart = mViewCount - 2;
            } else {
                positionStart = mViewCount - 1;
            }
            mViewCount += size;
            notifyItemRangeInserted(positionStart, size);
            log("addAll : startPosition : " + positionStart + "  itemCount : " + size);
        }
    }

    public void addAll(T[] objects) {
        addAll(Arrays.asList(objects));
    }

    public void replace(T object, int position) {
        mData.set(position, object);
        mViewCount++;
        notifyItemChanged(position);
    }

    //position start with 0
    public void remove(T object) {
        if (!mData.contains(object)) {
            return;
        }
        int position = mData.indexOf(object);
        mData.remove(object);
        if (hasHeader) {
            notifyItemRemoved(position + 1);
        } else {
            notifyItemRemoved(position);
        }
        mViewCount--;
    }

    //position start with 0
    public void remove(int position) {
        if (hasHeader) {
            if (position - 1 >= 0 && !mData.isEmpty()) {
                mData.remove(position - 1);
                notifyItemRemoved(position);
            } else if (position - 1 >= 0 && mData.isEmpty()) {
                return;
            } else {
                throw new IndexOutOfBoundsException("RecyclerView has header,position is should more than 0");
            }
        } else {
            if (mData.isEmpty()) {
                return;
            } else {
                mData.remove(position);
                notifyItemRemoved(position);
            }
        }
        mViewCount--;
    }

    public void clear() {
        if (mData == null || mData.size() == 0) {
            return;
        }
        mData.clear();
        mViewCount = 1;
        if (hasHeader) {
            mViewCount++;
        }
        if (hasFooter) {
            mViewCount++;
        }
        notifyDataSetChanged();

        isShowNoMore = false;
        mLoadMoreView.setVisibility(View.GONE);
        mNoMoreView.setVisibility(View.GONE);
    }


    public void setHeader(View header) {
        hasHeader = true;
        headerView = header;
        mViewCount++;
    }

    public void setHeader(@LayoutRes int res) {
        setHeader(LayoutInflater.from(mContext).inflate(res, null));
    }

    public View getHeader() {
        return headerView;
    }

    public View getFooter() {
        return footerView;
    }

    public void setFooter(View footer) {
        hasFooter = true;
        footerView = footer;
        mViewCount++;
    }

    public void setFooter(@LayoutRes int res) {
        setFooter(LayoutInflater.from(mContext).inflate(res, null));
    }

    public void removeHeader() {
        if (hasHeader) {
            hasHeader = false;
        }
    }

    public void removeFooter() {
        if (hasFooter) {
            hasFooter = false;
        }
    }

    public List<T> getData() {
        return mData;
    }

    public Context getContext() {
        return mContext;
    }

    public void log(String content) {
        if (allowLog) {
            Log.i(TAG, content);
        }
    }
}
