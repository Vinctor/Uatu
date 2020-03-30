package com.vinctor.uatu.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.vinctor.uatu.room.Constant;
import com.vinctor.uatu.room.ThreadPool;
import com.vinctor.uatu.room.TraceRecord;
import com.vinctor.uatu.room.TraceRecordDao;
import com.vinctor.uatu.room.UatuDatabase;
import com.vinctor.uatuLib.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private RecyclerView recyclerView;
    private MethodTracrListAdapter methodTracrListAdapter;
    CheckSqlHandler handler;
    private Toolbar toolbar;
    private UatuDatabase database;
    private String currentFilterText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_lib);
        initView();
        initData();
    }

    private void initData() {
        database = UatuDatabase.getInstance();
        if (database == null) {
            return;
        }
        handler = new CheckSqlHandler(methodTracrListAdapter);
    }

    private void handlerCheckResult(List<TraceRecord> traceRecords) {
        List<MethodTraceBean> list = new ArrayList<>();
        for (TraceRecord traceRecord : traceRecords) {
            MethodTraceBean methodTraceBean = new MethodTraceBean(traceRecord);
            list.add(methodTraceBean);
        }
        Message message = Message.obtain();
        message.what = Constant.CHECK_ALL_RECORD;
        message.obj = list;
        handler.sendMessage(message);
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        methodTracrListAdapter = new MethodTracrListAdapter(this);
        recyclerView.setAdapter(methodTracrListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.uatu_main_menu, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(true);
        EditText inputView = searchView.findViewById(R.id.search_src_text);
        inputView.setFilters(new InputFilter[]{new SearchFilter()});
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.clear) {
            ThreadPool
                    .getSqlThreadPool()
                    .execTask(new Runnable() {
                        @Override
                        public void run() {
                            int aLong = database.getTraceRecoedDao().deleteAll();
                            if (aLong > 0) {
                                handlerCheckResult(new ArrayList<TraceRecord>());
                            }
                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        currentFilterText = newText;
        getRecordsByFilter();
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (TextUtils.isEmpty(currentFilterText)) {
            getAllRecords();
        } else {
            getRecordsByFilter();
        }
    }

    private void getAllRecords() {
        final TraceRecordDao traceRecoedDao = database.getTraceRecoedDao();
        ThreadPool.getSqlThreadPool().execTask(new Runnable() {
            @Override
            public void run() {
                List<TraceRecord> traceRecords = traceRecoedDao.getAll();
                handlerCheckResult(traceRecords);
            }
        });
    }

    private void getRecordsByFilter() {
        ThreadPool
                .getSqlThreadPool()
                .execTask(new Runnable() {
                    @Override
                    public void run() {
                        TraceRecordDao traceRecoedDao = database.getTraceRecoedDao();
                        List<TraceRecord> result;
                        if (currentFilterText.contains(".")) {
                            String[] strings = currentFilterText.split("\\.");
                            int length = strings.length;
                            String className = strings[0];
                            String methodName = length > 1 ? strings[1] : "";
                            result = traceRecoedDao.getAllByClassAndMethod(className, methodName);
                        } else {
                            result = traceRecoedDao.getAllByClassOrMethod(currentFilterText);
                        }
                        handlerCheckResult(result);
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }


    static class CheckSqlHandler extends Handler {
        WeakReference<MethodTracrListAdapter> methodTracrListAdapterWrapper;

        public CheckSqlHandler(MethodTracrListAdapter methodTracrListAdapter) {
            this.methodTracrListAdapterWrapper = new WeakReference<>(methodTracrListAdapter);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (methodTracrListAdapterWrapper != null) {
                MethodTracrListAdapter methodTracrListAdapter = this.methodTracrListAdapterWrapper.get();
                if (methodTracrListAdapter != null) {
                    List<MethodTraceBean> list = (List<MethodTraceBean>) msg.obj;
                    methodTracrListAdapter.setList(list);
                }
            }
        }
    }

    class SearchFilter implements InputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String origin = dest.toString();
            String input = source.toString();
            if (origin.contains(".")) {
                if (input.contains(".")) {
                    return "";
                }
            }
            return null;
        }
    }
}
