package com.gionee.note;

import java.io.File;

import amigo.app.AmigoActionBar;
import amigo.app.AmigoActivity;
import amigo.app.AmigoAlertDialog;
import amigo.widget.AmigoButton;
import amigo.widget.AmigoListView;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gionee.note.content.Constants;
import com.gionee.note.content.NoteApplication;
import com.gionee.note.utils.CommonUtils;
import com.gionee.note.utils.FileUtils;
import com.gionee.note.utils.Log;

public class ExportTypeSelectActivity extends AmigoActivity{

	private AmigoListView lvExportTo;
	// gn lilg 2012-12-28 modify for common controls begin
	//	private TextView btnNext;
	private AmigoButton btnNext;
	// gn lilg 2012-12-28 modify for common controls end

	// sd card 剩余最小可用空间为 1M
	private static final int MIN_AVAILABLE_STORE = 3;
	private static final int ZERO_STORE = 0;

	private static String[] GENRES = null;

	// gn lilg 2012-12-18 add for common controls begin
	private AmigoActionBar actionBar;
	// gn lilg 2012-12-18 add for common controls end

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("ExportTypeSelectActivity------onCreate() start!");

		CommonUtils.setTheme(this);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.export_type_select_layout_white);

		initData();

		initResources();

		Log.d("ExportTypeSelectActivity------onCreate() end!");
	}

	private void initData(){
		Log.i("ExportTypeSelectActivity------init data start!");

		// Gionee <lilg><2013-05-13> add for CR00808800 begin
		int sdcardState = FileUtils.checkSDCardState();
		if(sdcardState == FileUtils.ERROR_SDCARD_NOT_EXISTS_OR_UNAVAILABLE){
			// sd card not exists or unavailable
			Log.d("ExportTypeSelectActivity------sd card not exists or unavailable.");
			GENRES = new String[] { 
					getResources().getString(R.string.str_internal_memory)
			};
		}else{
			Log.d("ExportTypeSelectActivity------sd card exists");
			GENRES = new String[] { 
					getResources().getString(R.string.str_sdcard),
					getResources().getString(R.string.str_internal_memory)
			};
		}
		// Gionee <lilg><2013-05-13> add for CR00808800 end

		for(String item : GENRES){
			Log.i("ExportTypeSelectActivity------item: " + item);
		}
		Log.d("ExportTypeSelectActivity------init data end!");
	}

	private void initResources(){
		Log.i("ExportTypeSelectActivity------init resources start!");

		// gn lilg 2012-12-18 add for common controls begin
		actionBar = getAmigoActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setTitle(R.string.str_export_note);
		// Gionee <lilg><2013-05-24> modify for CR00809680 begin
		// actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.gn_com_title_bar));
		// Gionee <lilg><2013-05-24> modify for CR00809680 end
		// gn lilg 2012-12-18 add for common controls end

		lvExportTo = (AmigoListView) findViewById(R.id.lv_exportTo);

		lvExportTo.setAdapter(new ArrayAdapter<String>(this, R.layout.export_type_select_item_layout_white, GENRES));
		lvExportTo.setItemsCanFocus(false);
		lvExportTo.setChoiceMode(AmigoListView.CHOICE_MODE_SINGLE);
		lvExportTo.setItemChecked(ImportExportActivity.EXPORT_TYPE_SDCARD, true);

		lvExportTo.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				Log.d("ExportTypeSelectActivity------position: " + position + ",id: " + id);
				Log.d("ExportTypeSelectActivity------checkedItemPosition(): " + lvExportTo.getCheckedItemPosition());
			}
		});

		// gn lilg 2012-12-28 modify for common controls begin
		//		btnNext = (TextView) findViewById(R.id.btn_next);
		btnNext = (AmigoButton) findViewById(R.id.btn_next);
		// gn lilg 2012-12-28 modify for common controls end
		btnNext.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Log.i("ExportTypeSelectActivity------click button next!");

				// Gionee <lilg><2013-05-13> modify for CR00808800 begin
				if(GENRES.length == 1){
					Log.d("ExportTypeSelectActivity------GENRES length: " + GENRES.length);
					
					//首先判断外部SD卡是否存在
					File sdCard2Path = new File(FileUtils.PATH_SDCARD2);
					File internalMemoryPath = null;
					Log.d("ExportTypeSelectActivity------sd card total store: " + FileUtils.getTotalStore(sdCard2Path.getPath()));

					if(FileUtils.getTotalStore(sdCard2Path.getPath()) <= ZERO_STORE){
						// 不存在
						internalMemoryPath = new File(FileUtils.PATH_SDCARD);
						Log.d("ExportTypeSelectActivity------internalMemoryPath: " + FileUtils.PATH_SDCARD);
					}else{
						// 存在
						// Gionee <lilg><2014-09-26> modify for CR01390390 begin
						if(FileUtils.isGn2SdcardSwap()){
							internalMemoryPath = new File(FileUtils.PATH_SDCARD2);
						}else{
							internalMemoryPath = new File(FileUtils.PATH_SDCARD);
						}
						Log.d("ExportTypeSelectActivity------nternalMemoryPath: " + FileUtils.PATH_SDCARD2);
						// Gionee <lilg><2014-09-26> modify for CR01390390 end
					}
					
					if(FileUtils.getTotalStore(internalMemoryPath.getPath()) <= ZERO_STORE){
						Log.d("ExportTypeSelectActivity------internal memory exists: false");
						String msg = getResources().getString(R.string.import_note_no_internal); 
						showAlert(msg);
						return;
					}
					Log.d("ExportTypeSelectActivity------internal memory min available stort: " + FileUtils.getAvailableStore(internalMemoryPath.getPath()) + ", " + FileUtils.getAvailableStore(internalMemoryPath.getPath())/1024/1024);

					if((FileUtils.getAvailableStore(internalMemoryPath.getPath())/1024/1024) < MIN_AVAILABLE_STORE ){
						Log.d("ExportTypeSelectActivity------internal memory min available stort < 3M!");
						String msg = getResources().getString(R.string.check_internal_content); 
						showAlert(msg);
						return;
					}
					
					Intent intent = new Intent();
					intent.putExtra(ImportExportActivity.EXPORT_TYPE, 1);
					intent.setClass(ExportTypeSelectActivity.this, ExportItemSelectActivity.class);
					// CR00733764
					startActivityForResult(intent, CommonUtils.REQUEST_ExportTypeSelectActivity);
				}else if(GENRES.length == 2){
					Log.d("ExportTypeSelectActivity------GENRES length: " + GENRES.length);

					//判断SD卡是否存在
					if(lvExportTo.getCheckedItemPosition() == 0 ){
						File sdCard2Path = new File(FileUtils.PATH_SDCARD2);
						//外部SD卡
						Log.d("ExportTypeSelectActivity------sd card path exists: " + sdCard2Path.exists()+"sd card total store: " + FileUtils.getTotalStore(sdCard2Path.getPath()));

						if(FileUtils.getTotalStore(sdCard2Path.getPath()) <= ZERO_STORE){
							// 不存在或不可用
							Log.d("ExportTypeSelectActivity------SD卡不存在或不可用！");

							String msg = getResources().getString(R.string.import_note_no_sd); 
							showAlert(msg);
							return;
						}

						// Gionee <lilg><2014-09-26> modify for CR01390390 begin
						File sdCardPath = null;
						if(FileUtils.isGn2SdcardSwap()){
							sdCardPath = new File(FileUtils.PATH_SDCARD);
						}else{
							sdCardPath = sdCard2Path;
						}
						// Gionee <lilg><2014-09-26> modify for CR01390390 end
						
						Log.d("ExportTypeSelectActivity---sd card min available stort: " + FileUtils.getAvailableStore(sdCardPath.getPath()) + ", " +FileUtils.getAvailableStore(sdCardPath.getPath())/1024/1024);

						if((FileUtils.getAvailableStore(sdCardPath.getPath())/1024/1024) < MIN_AVAILABLE_STORE ){
							Log.d("ExportTypeSelectActivity------sd card min available stort < 3M!");

							String msg = getResources().getString(R.string.check_sd_content); 
							showAlert(msg);
							return;
						}

					}else if(lvExportTo.getCheckedItemPosition() == 1 ){
						//内部存储器(即内置SD卡)

						//首先判断外部SD卡是否存在
						File sdCard2Path = new File(FileUtils.PATH_SDCARD2);
						File internalMemoryPath = null;
						Log.d("ExportTypeSelectActivity------sd card total store: " + FileUtils.getTotalStore(sdCard2Path.getPath()));

						if(FileUtils.getTotalStore(sdCard2Path.getPath()) <= ZERO_STORE){
							// 不存在
							internalMemoryPath = new File(FileUtils.PATH_SDCARD);

							Log.d("ExportTypeSelectActivity------internalMemoryPath: " + FileUtils.PATH_SDCARD);

						}else{
							// 存在
							// Gionee <lilg><2014-09-26> modify for CR01390390 begin
							if(FileUtils.isGn2SdcardSwap()){
								internalMemoryPath = new File(FileUtils.PATH_SDCARD2);
							}else{
								internalMemoryPath = new File(FileUtils.PATH_SDCARD);
							}
							// Gionee <lilg><2014-09-26> modify for CR01390390 end
							Log.d("ExportTypeSelectActivity------nternalMemoryPath: " + FileUtils.PATH_SDCARD2);

						}


						if(FileUtils.getTotalStore(internalMemoryPath.getPath()) <= ZERO_STORE){
							Log.d("ExportTypeSelectActivity------internal memory exists: false");

							String msg = getResources().getString(R.string.import_note_no_internal); 
							showAlert(msg);
							return;
						}
						Log.d("ExportTypeSelectActivity------internal memory min available stort: " + FileUtils.getAvailableStore(internalMemoryPath.getPath()) + ", " + FileUtils.getAvailableStore(internalMemoryPath.getPath())/1024/1024);

						if((FileUtils.getAvailableStore(internalMemoryPath.getPath())/1024/1024) < MIN_AVAILABLE_STORE ){
							Log.d("ExportTypeSelectActivity------internal memory min available stort < 3M!");

							String msg = getResources().getString(R.string.check_internal_content); 
							showAlert(msg);
							return;
						}
					}else{
						Log.d("ExportTypeSelectActivity------typeSelect: " + lvExportTo.getCheckedItemPosition());
					}
					
					Intent intent = new Intent();
					intent.putExtra(ImportExportActivity.EXPORT_TYPE, lvExportTo.getCheckedItemPosition());
					intent.setClass(ExportTypeSelectActivity.this, ExportItemSelectActivity.class);
					// CR00733764
					startActivityForResult(intent, CommonUtils.REQUEST_ExportTypeSelectActivity);
				}else{
					Log.e("ExportTypeSelectActivity------GENRES length: " + GENRES.length);
				}
				// Gionee <lilg><2013-05-13> modify for CR00808800 end
			}
		});

	}
	// CR00733764	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CommonUtils.REQUEST_ExportTypeSelectActivity
				&& resultCode == CommonUtils.RESULT_ExportItemSelectActivity) {
			setResult(CommonUtils.RESULT_ExportTypeSelectActivity);
			finish();
		}
	}
	private void showAlert(String msg){
		Log.d("ExportTypeSelectActivity------show dialog start!");

		AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this,CommonUtils.getTheme());
		builder.setTitle(getString(R.string.promat));
		builder.setMessage(msg);
		builder.setPositiveButton(getString(R.string.Ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i("ExportTypeSelectActivity------click button confirm in dialog!");
			}
		});
		builder.setCancelable(false);
		builder.show();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i("ALLEditActivity------onOptionsItemSelected!");

		switch (item.getItemId()) {
		case android.R.id.home:
			Log.i("ALLEditActivity------click back button!");
			finish();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	//gionee 20121220 jiating modify for CR00747076 config begin
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.i("ExportTypeSelectActivity....onConfigurationChanged");
	}

	//gionee 20121220 jiating modify for CR00747076 config end

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("ExportTypeSelectActivity------onResume begin!");

		// Gionee <lilg><2013-04-10> add for note upgrade begin
		((NoteApplication) getApplication()).registerVersionCallback(this);
		// Gionee <lilg><2013-04-10> add for note upgrade end

		Log.d("ExportTypeSelectActivity------onResume end!");
	}

	@Override
	protected void onPause() {
		Log.i("ExportTypeSelectActivity------onPause begin!");

		// Gionee <lilg><2013-04-10> add for note upgrade begin
		((NoteApplication) getApplication()).unregisterVersionCallback(this);
		// Gionee <lilg><2013-04-10> add for note upgrade end

		Log.d("ExportTypeSelectActivity------onPause end!");
		super.onPause();
	}
}
