public class AddNoteActivity extends BaseActivity implements View.OnClickListener {
    private NoteDB noteDB;
    private SQLiteDatabase dbWriter;

//    private String value;
    private Button savebtn, cancelbtn;
    private EditText edtext;
    private ImageView c_img;
    private TextView tx_add;

    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcontent);
        initToolBar("添加记录",true);
        noteDB = new NoteDB(this);
        dbWriter = noteDB.getWritableDatabase();

        tx_add = (TextView) findViewById(R.id.add);
        tx_add.setOnClickListener(this);
        savebtn = (Button) findViewById(R.id.bt_save);
        savebtn.setOnClickListener(this);
        cancelbtn = (Button) findViewById(R.id.bt_cancle);
        cancelbtn.setOnClickListener(this);
        edtext = (EditText) findViewById(R.id.edtext);
        c_img = (ImageView) findViewById(R.id.c_img);

    }

    public void addDB() {
        ContentValues cv = new ContentValues();
        cv.put(NoteDB.CONTENT, edtext.getText().toString());
        cv.put(NoteDB.TIME, getTime());
        cv.put(NoteDB.PATH, imageUrl + "");
        cv.put(NoteDB.VIDEO, "");
        dbWriter.insert(NoteDB.TABLE_NAME, null, cv);
        showToast(AddNoteActivity.this,"添加成功");
        finish();
    }

    public String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date date = new Date();
        String str = format.format(date);   //  yyyy年MM月dd日 HH:mm:ss
        return str;
    }
