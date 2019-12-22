public class MemorandumActivity extends BaseActivity implements OnItemLongClickListener, OnItemClickListener {

    private List<MemoBean> list = new ArrayList<MemoBean>();
    private MemoAdapter adapter;
    private DbHelper helper;
    private SQLiteDatabase database;
    private DatePickerDialog datePickerDialog;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_randum);
        Button button = (Button) findViewById(R.id.add_memo);
        ListView listView = (ListView) findViewById(R.id.list_view);
        helper = new DbHelper(MemorandumActivity.this);
        database = helper.getReadableDatabase();
        adapter = new MemoAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        queryMemo();
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTime(1);
            }
        });
    }

    public void selectTime(final int type){
        Calendar cal = Calendar.getInstance();
        // 显示年月日选择对话框
        datePickerDialog = new DatePickerDialog(MemorandumActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 显示时间选择对话框
                datePickerDialog.dismiss();
                addAlarm(type,year, monthOfYear, dayOfMonth);
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // 查询备忘录
    private void queryMemo() {
        list.clear();
        Cursor cursor = database.query("memo", null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                MemoBean memoBean = new MemoBean(time, title, content, id);
                list.add(memoBean);
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();

    }

    /**
     * 显示添加备忘内容对话框
     * type     1--新增  2--修改
     */
    public void showDialog(final int type, final int year, final int monthOfYear, final int dayOfMonth, final String t) {
        View view = getLayoutInflater().inflate(R.layout.half_dialog_view, null);
        final EditText editText = (EditText) view.findViewById(R.id.dialog_edit);
        final EditText dialogTitle = (EditText) view.findViewById(R.id.dialog_title);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        if (type==1){
            dialog.setTitle("请输入内容");// 设置对话框的标题
        }else {
            dialog.setTitle("修改备忘录");// 设置对话框的标题
        }
        dialog.setView(view).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String content = editText.getText().toString();
                String title = dialogTitle.getText().toString();

                String month = monthOfYear + 1 > 10 ? String.valueOf(monthOfYear + 1) : "0" + (monthOfYear + 1);
                String day = dayOfMonth > 10 ? String.valueOf(dayOfMonth) : "0" + dayOfMonth;
                // 保存
                String time = year + "年" + month + "月" + day + "日" + t;
                ContentValues values = new ContentValues();
                values.put("time", time);
                values.put("title", title);
                values.put("content", content);
                database = helper.getWritableDatabase();
                if (type==1){
                    database.insert("memo", null, values);
                }else {
                    database.update("memo", values, "id=?",new String[]{String.valueOf(id)});
                }
                dialog.dismiss();
                queryMemo();
            }
        }).create();
        dialog.show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("删除")// 设置对话框的标题
                .setMessage("真的要删除吗?")// 设置对话框的内容
                // 设置对话框的按钮
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.delete("memo", "id=?", new String[]{list.get(position).getId() + ""});
                        dialog.dismiss();
                        queryMemo();
                    }
                }).create();
        dialog.show();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id1) {
        id = list.get(position).getId();
        selectTime(2);
    }

    private Calendar calendar = Calendar.getInstance();
    private TimePickerDialog dialog;

    // 添加时钟
    // 显示时间选择对话框
    private void addAlarm(final int type, final int year, final int monthOfYear, final int dayOfMonth) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);

        dialog = new TimePickerDialog(MemorandumActivity.this, new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // 设置时间
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                // 广播跳转
                Intent intent = new Intent(MemorandumActivity.this, CallAlarmReceiver.class);
                // 启动一个广播
                PendingIntent sender = PendingIntent.getBroadcast(MemorandumActivity.this, 0, intent, 0);
                // 创建闹钟
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
//				am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 0, sender);
                String tmpS = format(hourOfDay) + ":" + format(minute);

                // SharedPreferences保存数据，并提交
                SharedPreferences time1Share = getPreferences(0);
                SharedPreferences.Editor editor = time1Share.edit();
                editor.putString("TIME1", tmpS);
                editor.commit();

                dialog.dismiss();
                showDialog(type, year, monthOfYear, dayOfMonth, tmpS);
            }
        }, mHour, mMinute, true);
        dialog.show();
    }

    private String format(int x) {
        String s = "" + x;
        if (s.length() == 1)
            s = "0" + s;
        return s;
    }
}
