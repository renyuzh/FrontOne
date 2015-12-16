package com.onebus.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.onebus.R;
import com.onebus.model.Notice;

/**
 * 通知公告Adapter类
 * 
 * @author hkq
 * @version 1.0
 * @created 2015-7-20
 */
public class ListViewNewsAdapter extends BaseAdapter {

	private Context mContext;// 运行上下文
	private List<Notice> listItems;// 数据集合

	/**
	 * 实例化Adapter
	 * 
	 * @param context
	 * @param data
	 * @param resource
	 */
	public ListViewNewsAdapter(List<Notice> data, Context context) {
		this.mContext = context;
		this.listItems = data;
	}

	public int getCount() {
		return listItems.size();
	}

	public Object getItem(int position) {
		return listItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	/**
	 * ListView Item设置
	 */
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = LayoutInflater.from(mContext);
		convertView = inflater.inflate(R.layout.news_listitem, null);

		ImageView image = (ImageView) convertView
				.findViewById(R.id.news_listitem_flag);
		TextView title = (TextView) convertView
				.findViewById(R.id.news_listitem_title);
		TextView author = (TextView) convertView
				.findViewById(R.id.news_listitem_author);
		TextView time = (TextView) convertView
				.findViewById(R.id.news_listitem_date);

		// 设置文字和图片
		Notice news = listItems.get(position);
		if (news.isNewType()) {

			image.setVisibility(View.VISIBLE);

		} else {

			image.setVisibility(View.GONE);

		}

		title.setText(news.getTitle());
		author.setText(news.getAuthor());
		time.setText(news.getPubDate());

		return convertView;
	}
}