package com.my.dn.widget;

/**
 * Created by guary on 2017/9/28.
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.my.dn.R;
import com.my.dn.data.DiscoveredInfo;
import com.my.dn.data.Edge;
import com.my.dn.data.Node;
import com.my.dn.multitouch.CanvasNodeLongTouchListener;
import com.my.dn.util.CanvasData;
import com.my.dn.util.CanvasDrawer;
import com.my.dn.util.CanvasNode;
import com.my.dn.util.Utils;

public class DancingPanel extends ViewGroup {

    private static final String TAG = "DancingPanel";

    private int mMaxCompanySize;
    private int mMaxHumanSize;
    private int mMinCompanySize;
    private int mMinHumanSize;
    private int mCompanyStepSize;
    private int mHumanStepSize;
    private int mMaxHorizontalMargin;
    private int mMaxVerticalMargin;
    private int mMarginStepSize;
    private int mUsedCompanySize;
    private int mUsedHumanSize;
    private int mUsedHorizontalMargin;
    private int mUsedVerticalMargin;
    private float mCompanyHumanSizeRatio;
    private float mNodeScale;

    private DiscoveredInfo mDiscoverInfo;
    private List<Node> mLocalNodes; // local nodes从顶部开始展示
    private List<Node> mDiscoverNodes; // 非local nodes从底部开始展示
    private List<Edge> mEdges;

    private List<View> mLocalViews;
    private List<View> mDiscoveredViews;

    /*
        This is very important. Avoid onLayout when it's true. Otherwise all views
        will be return back its initial state.
     */
    private boolean isDiscoverCanvasDataInited = false;
    private boolean isEditMode = false;

    /*
     * 存储所有的种子节点View，按行记录
     */
    private List<List<View>> mAllLocalViews = new ArrayList<List<View>>();
    /*
     * 存储所有的发现节点View，按行记录
     */
    private List<List<View>> mAllDiscoverViews = new ArrayList<List<View>>();
    /*
     * 记录种子节点每一行的最大高度
     */
    private List<Integer> mLocalNodesLineHeight = new ArrayList<Integer>();
    /*
     * 记录发现节点每一行的最大高度
     */
    private List<Integer> mDiscoverNodesLineHeight = new ArrayList<Integer>();

    // 保存画布元素数据
    private CanvasData mCanvasData;
    private CanvasDrawer mDrawer = CanvasDrawer.geInstance();

    private CanvasNodeLongTouchListener.OnViewTouchedListener mOnViewTouchedListener = new CanvasNodeLongTouchListener.OnViewTouchedListener() {
        @Override
        public void onViewMoved(NodeView view, float deltaX, float deltaY) {
            CanvasNode canvasNode = mCanvasData.getCanvasNode(view.getNode().id);
            if (null != canvasNode) {
                int width = getWidth();
                int height = getHeight();
                Log.d(TAG, "onViewMoved width=" + width + " height=" + height);
                int radius = view.getMeasuredWidth() / 2; // view.getMeasuredHeight() / 2
                float newCenterX = canvasNode.Center.x + deltaX;
                float newCenterY = canvasNode.Center.y + deltaY;
                if (newCenterX - radius < 0 ||
                        newCenterX + radius > width ||
                        newCenterY - radius < 0 ||
                        newCenterY + radius > height) {
                    return;
                }

                canvasNode.Center.x = newCenterX;
                canvasNode.Center.y = newCenterY;

                int lc = (int) (canvasNode.Center.x - radius);
                int tc = (int) (canvasNode.Center.y - radius);
                int rc = (int) (canvasNode.Center.x + radius);
                int bc = (int) (canvasNode.Center.y + radius);
                MarginLayoutParams marginLP = (MarginLayoutParams) view.getLayoutParams();
                if (marginLP != null) {
                    marginLP.topMargin = tc;
                    marginLP.leftMargin = lc;
                }
                view.layout(lc, tc, rc, bc);

                mCanvasData.updateNode(canvasNode);
                invalidate();
            }
        }

        @Override
        public void onViewClicked(View v) {
            if (v instanceof NodeView) {
                if (isEditMode) {
                    deleteNode((NodeView) v);
                } else {
                    NodeView nv = (NodeView) v;
                    // TODO:
                }
            }
        }
    };

    public DancingPanel(Context context) {
        super(context);
        // 继承自ViewGroup的自定义控件，如果想调用onDraw()需要设置这个属性
        setWillNotDraw(false);
        initData();
    }

    public DancingPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 继承自ViewGroup的自定义控件，如果想调用onDraw()需要设置这个属性
        setWillNotDraw(false);
        initData();
    }

    public DancingPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 继承自ViewGroup的自定义控件，如果想调用onDraw()需要设置这个属性
        setWillNotDraw(false);
        initData();
    }

    private void initData() {
        mCompanyHumanSizeRatio = 2 / 3.0f;
        mMaxCompanySize = getContext().getResources().getDimensionPixelSize(R.dimen.max_company_node_width);
        mMinCompanySize = getContext().getResources().getDimensionPixelSize(R.dimen.min_company_node_width);
        mMaxHumanSize = (int) (mMaxCompanySize * mCompanyHumanSizeRatio);
        mMinHumanSize = (int) (mMinCompanySize * mCompanyHumanSizeRatio);
        mMaxHorizontalMargin = getContext().getResources().getDimensionPixelSize(R.dimen.max_node_horizontal_margin);
        mUsedHorizontalMargin = mMaxHorizontalMargin;
        mMaxVerticalMargin = getContext().getResources().getDimensionPixelSize(R.dimen.max_node_vertical_margin);
        mUsedVerticalMargin = mMaxVerticalMargin;
        mUsedCompanySize = mMaxCompanySize;
        mUsedHumanSize = mMaxHumanSize;
        mCompanyStepSize = getContext().getResources().getDimensionPixelSize(R.dimen.company_node_size_change_step);
        mHumanStepSize = (int) (mCompanyStepSize * mCompanyHumanSizeRatio);
        mMarginStepSize = getContext().getResources().getDimensionPixelSize(R.dimen.node_margin_change_step);
        mNodeScale = 1.0f;
    }

    private void computeSizesForAllNodes() {
        // 首先，判断最小size是否能布局放下，如果放不下，就从调整margin开始
        if (!isAllNodesFit(mMinCompanySize, mMinHumanSize, mMaxHorizontalMargin, mMaxVerticalMargin)) {
            mUsedCompanySize = mMinCompanySize;
            mUsedHumanSize = mMinHumanSize;
            mUsedHorizontalMargin = mMaxHorizontalMargin;
            mUsedVerticalMargin = mMaxVerticalMargin;
            do {
                mUsedHorizontalMargin -= mMarginStepSize;
                mUsedVerticalMargin -= mMarginStepSize;
            }
            while (!isAllNodesFit(mMinCompanySize, mMinHumanSize, mUsedHorizontalMargin, mUsedVerticalMargin));
        } else {
            mUsedCompanySize = mMaxCompanySize;
            mUsedHumanSize = mMaxHumanSize;
            mUsedHorizontalMargin = mMaxHorizontalMargin;
            mUsedVerticalMargin = mMaxVerticalMargin;
            // 如果最小size放的下，就从最大size开始往最小size判断，找到适合排布的最大size
            while (!isAllNodesFit(mUsedCompanySize, mUsedHumanSize, mMaxHorizontalMargin, mMaxVerticalMargin)) {
                mUsedCompanySize -= mCompanyStepSize;
                mUsedHumanSize -= mHumanStepSize;
            }
        }

        mNodeScale = mMaxCompanySize / (mUsedCompanySize * 1.0f);
        Log.d(TAG, "company size=" + mUsedCompanySize + " human size=" + mUsedHumanSize + " horizontal margin="
                + mUsedHorizontalMargin + " vertical margin=" + mUsedVerticalMargin);
        Log.d(TAG, "mNodeScale = " + mNodeScale);
    }

    private boolean isAllNodesFit(int companySize, int humanSize, int horizontalMargin, int verticalMargin) {
        int width = getWidth();
        int height = getHeight();
        // 首先确定种子节点占多少行
        int localNodeLines = 0;
        int totalLocalNodeWidth = 0;
        for (int i = 0; i < mLocalNodes.size(); i++) {
            Node node = mLocalNodes.get(i);
            if (node.isCompany()) {
                totalLocalNodeWidth += (companySize + horizontalMargin);
            } else {
                totalLocalNodeWidth += (humanSize + horizontalMargin);
            }
        }
        if (0 != totalLocalNodeWidth) {
            localNodeLines = totalLocalNodeWidth / width + 1;
        }

        // 其次判断发现节点占多少行
        int discoverNodeLines = 0;
        int totalDiscoverNodeWidth = 0;
        for (int i = 0; i < mDiscoverNodes.size(); i++) {
            Node node = mDiscoverNodes.get(i);
            if (node.isCompany()) {
                totalDiscoverNodeWidth += (companySize + horizontalMargin);
            } else {
                totalDiscoverNodeWidth += (humanSize + horizontalMargin);
            }
        }
        if (0 != totalDiscoverNodeWidth) {
            discoverNodeLines = totalDiscoverNodeWidth / width + 1;
        }

        int totalNodesHeight = (localNodeLines + discoverNodeLines) * (companySize + verticalMargin);
        Log.d(TAG, "totalNodesHeight=" + totalNodesHeight + " height=" + height);

        return totalNodesHeight < height;
    }

    public void showDiscoverInfo(DiscoveredInfo info) {
        Log.d(TAG, "showDiscoverInfo");

        if (null == info) {
            return;
        }

        this.isDiscoverCanvasDataInited = false;
        this.mDiscoverInfo = info;
        this.mLocalNodes = mDiscoverInfo.getLocalNodes();
        this.mDiscoverNodes = mDiscoverInfo.getDiscoveredNodes();
        computeSizesForAllNodes();
        this.mEdges = mDiscoverInfo.getEdges();
        mCanvasData = new CanvasData(mEdges);

        this.removeAllViews();
        mLocalViews = new ArrayList<>();
        mDiscoveredViews = new ArrayList<>();

        Log.d(TAG, "local node size=" + mLocalNodes.size());
        for (int i = 0; i < mLocalNodes.size(); i++) {
            Node node = mLocalNodes.get(i);
            Log.d(TAG, "add view for " + node.name);
            View viewAdded = addNodeView(true, node);
            viewAdded.setTag(node);
            mLocalViews.add(viewAdded);

            // add first, then updated in onLayout
            CanvasNode cn = new CanvasNode(node);
            mCanvasData.addNode(cn);
        }

        Log.d(TAG, "discovered node size=" + mDiscoverNodes.size());
        for (int i = 0; i < mDiscoverNodes.size(); i++) {
            Node node = mDiscoverNodes.get(i);
            Log.d(TAG, "add view for " + node.name);
            View viewAdded = addNodeView(false, node);
            viewAdded.setTag(node);
            mDiscoveredViews.add(viewAdded);

            // add first, then updated in onLayout
            CanvasNode cn = new CanvasNode(node);
            mCanvasData.addNode(cn);
        }
    }

    private View addNodeView(boolean isLocal, Node node) {
        ViewGroup.MarginLayoutParams layoutParams = null;
        if (node.isCompany()) {
            layoutParams = new ViewGroup.MarginLayoutParams(mUsedCompanySize, mUsedCompanySize);
            if (isLocal) {
                layoutParams.topMargin = mUsedVerticalMargin;
            } else {
                layoutParams.bottomMargin = mUsedVerticalMargin;
            }
        } else {
            layoutParams = new ViewGroup.MarginLayoutParams(mUsedHumanSize, mUsedHumanSize);
            if (isLocal) {
                layoutParams.topMargin = mUsedVerticalMargin;
            } else {
                layoutParams.bottomMargin = mUsedVerticalMargin;
            }
        }
        layoutParams.leftMargin = mUsedHorizontalMargin;

        NodeView nv = new NodeView(getContext(), node, isLocal);
        nv.setIsDiscoverView(!isLocal);
        nv.setOnTouchListener(new CanvasNodeLongTouchListener(nv, mOnViewTouchedListener));
        nv.setBackgroundResource(R.mipmap.local_node);
        addView(nv, layoutParams);
        return nv;
    }

    public CanvasData getCanvasData() {
        return this.mCanvasData;
    }

    public void enterEditMode() {
        isEditMode = true;
        if (null != mLocalViews) {
            for (int i = 0; i < mLocalViews.size(); i++) {
                NodeView view = (NodeView) mLocalViews.get(i);
                view.enterEditMode();
            }
        }

        if (null != mDiscoveredViews) {
            for (int i = 0; i < mDiscoveredViews.size(); i++) {
                NodeView view = (NodeView) mDiscoveredViews.get(i);
                view.enterEditMode();
            }
        }
    }

    public void exitEditMode() {
        isEditMode = false;
        if (null != mLocalViews) {
            for (int i = 0; i < mLocalViews.size(); i++) {
                NodeView view = (NodeView) mLocalViews.get(i);
                view.exitEditMode();
            }
        }

        if (null != mDiscoveredViews) {
            for (int i = 0; i < mDiscoveredViews.size(); i++) {
                NodeView view = (NodeView) mDiscoveredViews.get(i);
                view.exitEditMode();
            }
        }
    }

    public boolean isEditMode() {
        return this.isEditMode;
    }

    public void clearPanel() {
        if (null != mCanvasData) {
            if (null != mCanvasData.getEdges()) {
                mCanvasData.getEdges().clear();
            }
            if (null != mCanvasData.getNodesMap()) {
                mCanvasData.getNodesMap().clear();
            }
            mCanvasData = null;
        }
        this.removeAllViews();
        this.mLocalViews = null;
        this.mDiscoveredViews = null;
        this.mLocalNodes = null;
        this.mDiscoverNodes = null;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 获得它的父容器为它设置的测量模式和大小
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        // Log.d(TAG, sizeWidth + "," + sizeHeight);
        // 如果是warp_content情况下，记录宽和高
        int width = 0;
        int height = 0;
        // 记录每一行的宽度，width不断取最大宽度
        int lineWidth = 0;
        // 每一行的高度，累加至height
        int lineHeight = 0;

        int childCount = getChildCount();
        // 遍历每个子元素
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            // 测量每一个child的宽和高
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            // 得到child的lp
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            // 当前子控件实际占据的宽度
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            // 当前子控件实际占据的高度
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            // 如果加入当前child，则超出最大宽度，则得到目前最大宽度给width，累加height 然后开启新行
            if (lineWidth + childWidth > sizeWidth) {
                width = Math.max(lineWidth, childWidth);// 取最大的
                lineWidth = childWidth; // 重新开启新行，开始记录
                // 叠加当前高度，
                height += lineHeight;
                // 开启记录下一行的高度
                lineHeight = childHeight;
            } else
            // 否则累加值lineWidth,lineHeight取最大高度
            {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            // 如果是最后一个，则将当前记录的最大宽度和当前lineWidth做比较
            if (i == childCount - 1) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }
        }
        setMeasuredDimension((modeWidth == MeasureSpec.EXACTLY) ? sizeWidth : width,
                (modeHeight == MeasureSpec.EXACTLY) ? sizeHeight : height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout changed:" + changed + " l=" + l + " t=" + t + " r=" + r + " b=" + b);
        if (!isDiscoverCanvasDataInited) {
            if (null == mLocalViews || null == mDiscoveredViews) {
                return;
            } else {
                initLayout(l, t, r, b);
                isDiscoverCanvasDataInited = true;
            }
        }
    }

    private void randomLayoutAllChilds() {
        int childCount = this.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = this.getChildAt(i);
            randomLayoutView(view);
        }
    }

    private void randomLayoutView(View view) {
        Log.d(TAG, "randomLayoutView");
        int width = getWidth();
        int height = getHeight();
        int randomX = Utils.getRandom(width - mUsedCompanySize);
        int randomY = Utils.getRandom(height - mUsedCompanySize);
        Object tagObject = view.getTag();
        if (tagObject != null && tagObject instanceof Node) {
            Node node = (Node) tagObject;
            CanvasNode canvasNode = mCanvasData.getCanvasNode(node.id);
            updateCanvasNode(canvasNode.node.id, randomX, randomY, view.getMeasuredWidth(), view.getMeasuredHeight());
            view.layout(randomX, randomY, randomX + view.getMeasuredWidth(), randomY + view.getMeasuredHeight());
        }
    }

    private void initLayout(int l, int t, int r, int b) {
        mAllLocalViews.clear();
        mAllDiscoverViews.clear();
        mLocalNodesLineHeight.clear();
        mDiscoverNodesLineHeight.clear();

        int width = getWidth();
        int lineWidth = 0;
        int lineHeight = 0;
        // 存储每一行所有的childView
        List<View> lineViews = new ArrayList<>();
        // 遍历所有的孩子
        if (null == mLocalViews || null == mDiscoveredViews) {
            return;
        }

        // 从顶部添加种子结点
        for (int i = 0; i < mLocalViews.size(); i++) {
            View child = mLocalViews.get(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 如果已经需要换行
            if (childWidth + lp.leftMargin + lp.rightMargin + lineWidth > width) {
                // 记录这一行所有的View以及最大高度
                mLocalNodesLineHeight.add(lineHeight);
                // 将当前行的childView保存，然后开启新的ArrayList保存下一行的childView
                mAllLocalViews.add(lineViews);
                lineWidth = 0;// 重置行宽
                lineViews = new ArrayList<View>();
            }
            /**
             * 如果不需要换行，则累加
             */
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
            lineViews.add(child);
        }

        // 记录最后一行
        mLocalNodesLineHeight.add(lineHeight);
        mAllLocalViews.add(lineViews);

        int left = 0;
        int top = 0;
        // 得到总行数
        int lineNums = mAllLocalViews.size();
        for (int i = 0; i < lineNums; i++) {
            // 每一行的所有的views
            lineViews = mAllLocalViews.get(i);
            // 当前行的最大高度
            lineHeight = mLocalNodesLineHeight.get(i);
            // 遍历当前行所有的View
            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }

                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                // 计算childView的left,top,right,bottom
                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                // 如果一行中，既有公司圆圈，又有个人圆圈，需要再判断一下，个人小圆圈是否居中，因为之前设置的marginTop都是
                if (lp.topMargin + child.getMeasuredHeight() + lp.bottomMargin < lineHeight) {
                    int step = (lineHeight - lp.topMargin - child.getMeasuredHeight() - lp.bottomMargin) / 2;
                    tc = top + lp.topMargin + step;
                }
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();
                child.layout(lc, tc, rc, bc);
                left += child.getMeasuredWidth() + lp.rightMargin + lp.leftMargin;
                // update child's canvas node
                Object node = child.getTag();
                if (null != node && node instanceof Node) {
                    updateCanvasNode(((Node) node).id, lc, tc, child.getMeasuredWidth(), child.getMeasuredHeight());
                }
            }
            left = 0;
            top += lineHeight;
        }

        // 从底部添加发现的圆圈
        int bottom = getHeight();
        lineWidth = 0;
        lineHeight = 0;
        // 存储每一行所有的childView
        lineViews = new ArrayList<View>();
        for (int i = 0; i < mDiscoveredViews.size(); i++) {
            View child = mDiscoveredViews.get(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 如果已经需要换行
            if (childWidth + lp.leftMargin + lp.rightMargin + lineWidth > width) {
                // 记录这一行所有的View以及最大高度
                mDiscoverNodesLineHeight.add(lineHeight);
                // 将当前行的childView保存，然后开启新的ArrayList保存下一行的childView
                mAllDiscoverViews.add(lineViews);
                lineWidth = 0;// 重置行宽
                lineViews = new ArrayList<View>();
            }
            /**
             * 如果不需要换行，则累加
             */
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
            lineViews.add(child);
        }

        // 记录最后一行
        mDiscoverNodesLineHeight.add(lineHeight);
        mAllDiscoverViews.add(lineViews);

        left = 0;
        // 得到总行数
        lineNums = mAllDiscoverViews.size();
        for (int i = 0; i < lineNums; i++) {
            // 每一行的所有的views
            lineViews = mAllDiscoverViews.get(i);
            // 当前行的最大高度
            lineHeight = mDiscoverNodesLineHeight.get(i);

            // 遍历当前行所有的View
            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                // 计算childView的left,top,right,bottom
                int lc = left + lp.leftMargin;
                int tc = bottom - lp.bottomMargin - child.getMeasuredHeight();
                // 如果一行中，既有公司圆圈，又有个人圆圈，需要再判断一下，个人小圆圈是否居中，因为之前设置的marginTop都是
                if (lp.topMargin + child.getMeasuredHeight() + lp.bottomMargin < lineHeight) {
                    int step = (lineHeight - lp.topMargin - child.getMeasuredHeight() - lp.bottomMargin) / 2;
                    tc -= step;
                }
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                // Log.d(TAG, " , l = " + lc + " , t = " + tc + " , r =" + rc + " , b = " + bc);
                child.layout(lc, tc, rc, bc);
                left += child.getMeasuredWidth() + lp.rightMargin + lp.leftMargin;

                // update child's canvas node
                Object node = child.getTag();
                if (null != node && node instanceof Node) {
                    updateCanvasNode(((Node) node).id, lc, tc, child.getMeasuredWidth(), child.getMeasuredHeight());
                }
            }

            left = 0;
            bottom -= lineHeight;
        }
    }

    private void updateCanvasNode(String nodeId, int left, int top, int width, int height) {
        CanvasNode cn = mCanvasData.getCanvasNode(nodeId);
        if (null == cn) {
            return;
        }
        cn.Center.x = left + width / 2;
        cn.Center.y = top + height / 2;
        cn.radius = width / 2;
        mCanvasData.updateNode(cn);
    }

    public void deleteNode(String nodeId) {
        Log.d(TAG, "deleteNode nodeId:" + nodeId);

        if (null != mLocalViews) {
            for (int i = 0; i < mLocalViews.size(); i++) {
                NodeView nv = (NodeView) mLocalViews.get(i);
                if (nv.getNode().id.equals(nodeId)) {
                    deleteNode(nv);
                    return;
                }
            }
        }

        if (null != mDiscoveredViews) {
            for (int i = 0; i < mDiscoveredViews.size(); i++) {
                NodeView nv = (NodeView) mDiscoveredViews.get(i);
                if (nv.getNode().id.equals(nodeId)) {
                    deleteNode(nv);
                    return;
                }
            }
        }
    }

    private void deleteNode(NodeView nodeView) {
        Log.d(TAG, "deleteNode: nodeId:" + nodeView.getNode().id + " nodeName:" + nodeView.getNode().name);
        nodeView.setVisibility(View.INVISIBLE);
        Node node = nodeView.getNode();
        CanvasNode cn = mCanvasData.getCanvasNode(node.id);
        if (null != cn) {
            cn.isDelete = true;
            mCanvasData.updateNode(cn);
        }

        invalidate();
    }

    public void restoreNodes() {
        int childCount = this.getChildCount();
        boolean needRefresh = false;
        for (int i = 0; i < childCount; i++) {
            NodeView nv = (NodeView) this.getChildAt(i);
            if (nv.getVisibility() == View.INVISIBLE) {
                nv.setVisibility(View.VISIBLE);
                needRefresh = true;
            }

            CanvasNode cn = mCanvasData.getCanvasNode(nv.getNode().id);
            if (null != cn && cn.isDelete) {
                cn.isDelete = false;
                mCanvasData.updateNode(cn);
                needRefresh = true;
            }
        }

        if (needRefresh) {
            invalidate();
        }
    }

    public void saveNodes() {
        Log.d(TAG, "saveNodes");

        int childCount = getChildCount();
        List<Edge> edgeList = mCanvasData.getEdges();
        List<String> deleteIDList = new ArrayList<String>();
        List<NodeView> deleteNodeViewList = new ArrayList<NodeView>();
        for (int i = 0; i < childCount; i++) {
            NodeView nv = (NodeView) this.getChildAt(i);
            if (null != nv && nv.getVisibility() != View.VISIBLE) {
                Log.d(TAG, "add " + nv.getNode().name + " to delete list.");
                deleteNodeViewList.add(nv);
            }
        }

        for (int i = 0; i < deleteNodeViewList.size(); i++) {
            NodeView nv = deleteNodeViewList.get(i);
            this.removeView(nv);
            CanvasNode cn = mCanvasData.getCanvasNode(nv.getNode().id);
            if (null != cn && cn.isDelete) {
                mCanvasData.deleteNode(cn.node.id);
                deleteIDList.add(cn.node.id);
                if (mLocalViews.contains(nv)) {
                    Log.d(TAG, "remove view from Local Views");
                    mLocalViews.remove(nv);
                }
                if (mDiscoveredViews.contains(nv)) {
                    Log.d(TAG, "remove view from Discover Views");
                    mDiscoveredViews.remove(nv);
                }
            }
        }

        for (int i = 0; i < deleteIDList.size(); i++) {
            // remove related edge
            deleteRelatedEdge(edgeList, deleteIDList.get(i));
        }

        invalidate();
    }

    private void deleteRelatedEdge(List<Edge> edges, String id) {
        Log.d(TAG, "deleteRelatedEdge:" + id);

        if (null == edges || edges.size() == 0 || TextUtils.isEmpty(id)) {
            return;
        }

        List<Edge> deletedEdges = new ArrayList<Edge>();
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            if (id.equals(edge.source) || id.equals(edge.target)) {
                deletedEdges.add(edge);
            }
        }

        for (int i = 0; i < deletedEdges.size(); i++) {
            edges.remove(deletedEdges.get(i));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");
        super.onDraw(canvas);
        mDrawer.setDrawContent(mCanvasData, canvas);
        mDrawer.draw();
    }
}
