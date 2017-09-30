package com.my.dn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.my.dn.data.DiscoveredInfo;
import com.my.dn.data.Edge;
import com.my.dn.data.Node;
import com.my.dn.widget.DancingPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView mBtnEdit;
    private TextView mBtnCancel;
    private TextView mBtnSave;
    private ViewGroup mLayoutEdit;
    private DancingPanel mPanel;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mBtnEdit) {
                enterEditMode();
            } else if (v == mBtnCancel) {
                exitEditMode();
            } else if (v == mBtnSave) {
                doSave();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mBtnEdit = (TextView) findViewById(R.id.btn_edit);
        mBtnEdit.setOnClickListener(mOnClickListener);
        mBtnCancel = (TextView) findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(mOnClickListener);
        mBtnSave = (TextView) findViewById(R.id.btn_save);
        mBtnSave.setOnClickListener(mOnClickListener);
        mLayoutEdit = (ViewGroup) findViewById(R.id.layout_edit);
        constructPanelView();
    }

    private void constructPanelView() {
        mPanel = (DancingPanel) findViewById(R.id.sky_canvas);
        final DiscoveredInfo info = new DiscoveredInfo();
        // 构造Local nodes
        List<Node> localNodeList = new ArrayList<>();
        Node node = new Node();
        node.id = "1";
        node.name = "Peter";
        node.type = Node.HUMAN;
        localNodeList.add(node);

        node = new Node();
        node.id = "2";
        node.name = "IBM";
        node.type = Node.COMPANY;
        localNodeList.add(node);
        info.setLocalNodes(localNodeList);

        // 构造非Local Nodes
        List<Node> nodeList = new ArrayList<>();
        node = new Node();
        node.id = "3";
        node.name = "Google";
        node.type = Node.COMPANY;
        nodeList.add(node);

        node = new Node();
        node.id = "4";
        node.name = "Lawsen";
        node.type = Node.HUMAN;
        nodeList.add(node);
        info.setDiscoveredNodes(nodeList);

        // 构造边
        List<Edge> edges = new ArrayList<>();
        Edge edge = new Edge();
        edge.source = "1";
        edge.target = "2";
        edge.labels = Arrays.asList("employee");
        edges.add(edge);

        edge = new Edge();
        edge.source = "1";
        edge.target = "3";
        edge.labels = Arrays.asList("chairman");
        edges.add(edge);

        edge = new Edge();
        edge.source = "4";
        edge.target = "2";
        edge.labels = Arrays.asList("chairman");
        edges.add(edge);

        edge = new Edge();
        edge.source = "4";
        edge.target = "3";
        edge.labels = Arrays.asList("chairman");
        edges.add(edge);
        info.setEdges(edges);

        mPanel.post(new Runnable() {
            @Override
            public void run() {
                mPanel.showDiscoverInfo(info);
            }
        });
    }

    public void enterEditMode() {
        mLayoutEdit.setVisibility(View.VISIBLE);
        mBtnEdit.setVisibility(View.GONE);
        mPanel.enterEditMode();
    }

    public void exitEditMode() {
        mPanel.exitEditMode();
        mPanel.restoreNodes();
        mLayoutEdit.setVisibility(View.GONE);
        mBtnEdit.setVisibility(View.VISIBLE);
    }

    public void doSave() {
        mPanel.saveNodes();
        if (mPanel.isEditMode()) {
            mPanel.exitEditMode();
            exitEditMode();
        }
    }
}
