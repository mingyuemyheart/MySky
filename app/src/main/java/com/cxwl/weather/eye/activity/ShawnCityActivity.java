package com.cxwl.weather.eye.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cxwl.weather.eye.R;
import com.cxwl.weather.eye.adapter.ShawnCityNationAdapter;
import com.cxwl.weather.eye.adapter.ShawnCitySearchAdapter;
import com.cxwl.weather.eye.dto.EyeDto;

import java.util.ArrayList;
import java.util.List;

/**
 * 城市选择
 */
public class ShawnCityActivity extends ShawnBaseActivity implements OnClickListener{
	
	private Context mContext;
	private TextView tvProvinceBar,tvNationalBar;
	private LinearLayout llGroup,llGridView;
	private List<EyeDto> dataList = new ArrayList<>();

	//搜索城市后的结果列表
	private ListView listView;
	private ShawnCitySearchAdapter searchAdapter;
	private List<EyeDto> searchList = new ArrayList<>();

	//省内热门
	private GridView pGridView;
	private List<EyeDto> pList = new ArrayList<>();

	//全国热门
	private GridView nGridView;
	private List<EyeDto> nList = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_city);
		mContext = this;
		initWidget();
		initListView();
		initPGridView();
		initNGridView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		EditText etSearch = findViewById(R.id.etSearch);
		etSearch.addTextChangedListener(watcher);
		TextView tvProvince = findViewById(R.id.tvProvince);
		tvProvince.setOnClickListener(this);
		TextView tvNational = findViewById(R.id.tvNational);
		tvNational.setOnClickListener(this);
		tvProvinceBar = findViewById(R.id.tvProvinceBar);
		tvProvinceBar.setOnClickListener(this);
		tvNationalBar = findViewById(R.id.tvNationalBar);
		tvNationalBar.setOnClickListener(this);
		llGroup = findViewById(R.id.llGroup);
		llGridView = findViewById(R.id.llGridView);
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("选择城市");

		dataList.clear();
		dataList.addAll(getIntent().getExtras().<EyeDto>getParcelableArrayList("dataList"));

		pList.clear();
		nList.clear();
		for (EyeDto dto : dataList) {
			if (!TextUtils.isEmpty(dto.cityName)) {
				if (dto.cityName.startsWith("天津")) {
					if (dto.location.startsWith("铁塔") || dto.location.startsWith("气象频道")) {
						pList.add(dto);
					}
				}else {
					nList.add(dto);
				}
			}
		}
	}
	
	private TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			searchList.clear();
			if (arg0.toString().trim().equals("")) {
				listView.setVisibility(View.GONE);
				llGroup.setVisibility(View.VISIBLE);
				llGridView.setVisibility(View.VISIBLE);
			}else {
				listView.setVisibility(View.VISIBLE);
				llGridView.setVisibility(View.GONE);
				llGroup.setVisibility(View.GONE);
				getCityInfo(arg0.toString().trim());
			}

		}
	};

	/**
	 * 获取城市信息
	 */
	private void getCityInfo(String keyword) {
		searchList.clear();
		for (EyeDto dto : dataList) {
			String name = dto.provinceName+dto.cityName+dto.disName+dto.location;
			if (name.contains(keyword)) {
				searchList.add(dto);
			}
		}
		if (searchAdapter != null) {
			searchAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		listView = findViewById(R.id.listView);
		searchAdapter = new ShawnCitySearchAdapter(mContext, searchList);
		listView.setAdapter(searchAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentWeatherDetail(searchList.get(arg2));
			}
		});
	}
	
	/**
	 * 初始化省内热门gridview
	 */
	private void initPGridView() {
		pGridView = findViewById(R.id.pGridView);
		ShawnCityNationAdapter pAdapter = new ShawnCityNationAdapter(mContext, pList);
		pGridView.setAdapter(pAdapter);
		pGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				intentWeatherDetail(pList.get(arg2));
			}
		});
	}
	
	/**
	 * 初始化全国热门
	 */
	private void initNGridView() {
		nGridView = findViewById(R.id.nGridView);
		ShawnCityNationAdapter nAdapter = new ShawnCityNationAdapter(mContext, nList);
		nGridView.setAdapter(nAdapter);
		nGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentWeatherDetail(nList.get(arg2));
			}
		});
	}

	/**
	 * 迁移到天气详情界面
	 */
	private void intentWeatherDetail(EyeDto data) {
		Intent intent = new Intent(this, ShawnVideoDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("data", data);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
			case R.id.tvProvince:
			case R.id.tvProvinceBar:
				tvProvinceBar.setBackgroundColor(getResources().getColor(R.color.red));
				tvNationalBar.setBackgroundColor(Color.TRANSPARENT);
				pGridView.setVisibility(View.VISIBLE);
				nGridView.setVisibility(View.GONE);
				break;
			case R.id.tvNational:
			case R.id.tvNationalBar:
				tvProvinceBar.setBackgroundColor(Color.TRANSPARENT);
				tvNationalBar.setBackgroundColor(getResources().getColor(R.color.red));
				pGridView.setVisibility(View.GONE);
				nGridView.setVisibility(View.VISIBLE);
				break;

		default:
			break;
		}
	}
}
