public class RegisterActivity extends BaseActivity implements OnClickListener {

	private EditText userName;
	private EditText psd;
	private EditText againPsd;
	private DbHelper helper;
	private SQLiteDatabase database;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.activity_register);
		initView();
		initToolBar("注册", true);
		// 创建数据库
		helper = new DbHelper(this);
		database = helper.getWritableDatabase();
	}



	/**
	 * 寻找布局控件
	 */
	private void initView() {
		userName = (EditText) findViewById(R.id.user_name);
		psd = (EditText) findViewById(R.id.psd);
		againPsd = (EditText) findViewById(R.id.again_psd);
		Button register = (Button) findViewById(R.id.register);
		register.setOnClickListener(this);
	}

	/**
	 * 注册
	 */
	@Override
	public void onClick(View v) {
		String name = userName.getText().toString();
		String password = psd.getText().toString();
		String againPassword = againPsd.getText().toString();
		if (TextUtils.isEmpty(name)) {
			Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
		} else if (TextUtils.isEmpty(password)) {
			Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
		} else if (TextUtils.isEmpty(againPassword)) {
			Toast.makeText(this, "确认密码不能为空", Toast.LENGTH_SHORT).show();
		} else if (!againPassword.equals(password)) {
			Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
		} else {
			register(name, password);
		}
	}

	/**
	 * 注册
	 *
	 * @param name
	 *            用户名
	 * @param password
	 *            密码
	 */
	private void register(String name, String password) {
		// 注册之前查询用户名是否已经注册
		if (!queryUserName(name)) {
			ContentValues values = new ContentValues();
			values.put("userName", name);
			values.put("password", password);
			// 保存用户名密码
			database.insert("user", null, values);
			Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
			finish();
		} else {
			Toast.makeText(this, "用户名已经存在！", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 根据用户名查询表
	 *
	 * @param name
	 */
	private boolean queryUserName(String name) {
		database = helper.getReadableDatabase();
		Cursor cursor = database.query("user", null, null, null, null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String userName = cursor.getString(cursor.getColumnIndex("userName"));
				if (name.equals(userName)) {
					// 用户名已经存在
					return true;
				}
			}
			cursor.close();
		}
		// 用户名不存在
		return false;
	}

}
