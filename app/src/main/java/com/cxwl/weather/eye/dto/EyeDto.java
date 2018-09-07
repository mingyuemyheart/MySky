package com.cxwl.weather.eye.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class EyeDto implements Parcelable{

	public String videoThumbUrl;//视频列表缩略图url
	public String location;
	public String fGroupId;//设备组id
	public String fId;//设备id
	public String fGroupIp;//设备组ip
	public String fGroupName;//设备组名称
	public String fNumber;//设备编号
	public String StatusUrl;//判断内外网地址
	public String erectTime;//架设时间
	public String streamPrivate;//内网视频流地址
	public String streamPublic;//公网视频流地址
	public String pictureThumbUrl;//图片结合缩略图url
	public String pictureUrl;//图片集合url
	public String pictureTime;//图片时间
	public String lat;
	public String lng;
	public String forePosition;//预位置
	public String facilityUrlTes;

	public String time;//逐小时
	public float temperature;//温度
	public float quality;//空气质量
	public float humidity;//湿度
	public float pressure;//气压
	public float windSpeed;//风速
	public String windDir;//风向
	public float precipitation;//雨量
	public float precipitationLevel;//雨量级别
	public float ultraviolet;//紫外线
	public float x = 0;//x轴坐标点
	public float y = 0;//y轴坐标点

	public EyeDto() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.videoThumbUrl);
		dest.writeString(this.location);
		dest.writeString(this.fGroupId);
		dest.writeString(this.fId);
		dest.writeString(this.fGroupIp);
		dest.writeString(this.fGroupName);
		dest.writeString(this.fNumber);
		dest.writeString(this.StatusUrl);
		dest.writeString(this.erectTime);
		dest.writeString(this.streamPrivate);
		dest.writeString(this.streamPublic);
		dest.writeString(this.pictureThumbUrl);
		dest.writeString(this.pictureUrl);
		dest.writeString(this.pictureTime);
		dest.writeString(this.lat);
		dest.writeString(this.lng);
		dest.writeString(this.forePosition);
		dest.writeString(this.facilityUrlTes);
		dest.writeString(this.time);
		dest.writeFloat(this.temperature);
		dest.writeFloat(this.quality);
		dest.writeFloat(this.humidity);
		dest.writeFloat(this.pressure);
		dest.writeFloat(this.windSpeed);
		dest.writeString(this.windDir);
		dest.writeFloat(this.precipitation);
		dest.writeFloat(this.precipitationLevel);
		dest.writeFloat(this.ultraviolet);
		dest.writeFloat(this.x);
		dest.writeFloat(this.y);
	}

	protected EyeDto(Parcel in) {
		this.videoThumbUrl = in.readString();
		this.location = in.readString();
		this.fGroupId = in.readString();
		this.fId = in.readString();
		this.fGroupIp = in.readString();
		this.fGroupName = in.readString();
		this.fNumber = in.readString();
		this.StatusUrl = in.readString();
		this.erectTime = in.readString();
		this.streamPrivate = in.readString();
		this.streamPublic = in.readString();
		this.pictureThumbUrl = in.readString();
		this.pictureUrl = in.readString();
		this.pictureTime = in.readString();
		this.lat = in.readString();
		this.lng = in.readString();
		this.forePosition = in.readString();
		this.facilityUrlTes = in.readString();
		this.time = in.readString();
		this.temperature = in.readFloat();
		this.quality = in.readFloat();
		this.humidity = in.readFloat();
		this.pressure = in.readFloat();
		this.windSpeed = in.readFloat();
		this.windDir = in.readString();
		this.precipitation = in.readFloat();
		this.precipitationLevel = in.readFloat();
		this.ultraviolet = in.readFloat();
		this.x = in.readFloat();
		this.y = in.readFloat();
	}

	public static final Creator<EyeDto> CREATOR = new Creator<EyeDto>() {
		@Override
		public EyeDto createFromParcel(Parcel source) {
			return new EyeDto(source);
		}

		@Override
		public EyeDto[] newArray(int size) {
			return new EyeDto[size];
		}
	};
}
