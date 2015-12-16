package com.onebus.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.onebus.R;
import com.onebus.model.Notice;
import com.onebus.widget.ListViewNewsAdapter;

public class NoticeActivity extends Activity {

	private String[] title = { "长乐路站点临时迁移的通告", "大唐芙蓉园南门站西迁200米", "车辆停运公告",
			"通宵1号线、2号线、3号线、4号线 这四条通宵公交临时停运", "更换520辆新型空调公交车",
			"新开通725路公交 桐芳巷小区至科技路西口", "北大街十字南北方向拟禁左", "首条商务快巴开辟西安公交快车道",
			"西安三条微型公交今开通", "西安一公交车早高峰门被挤爆" };
	private String[] time = { "10min前", "30min前", "1hours前", "2hours前",
			"3hours前", "24hours前", "60hours前", "80hours前", "100hours前",
			"110hours前" };
	private String[] body = {
			"2012年10月23日，由于市政公司对长乐西路（朝阳门――东二环）段路北侧道路拓宽改造进行围挡施工，届时，将会影响朝阳门、安仁坊公交站车辆的停靠。为了确保途径公交线路的正常运行，西安市公共交通总公司经实地勘察做如下调整：\n1、将原朝阳门所有公交站西移约200米至白马服饰广场大楼门前。\n2、将原安仁坊所有公交站东移至第四军医大学大门东侧。",
			"大唐芙蓉园南门公交枢纽站场将向西搬迁约200米，迁至芙蓉南路西口与芙蓉西路十字东北角。届时，21路、237路、609路、通宵4号线等公交线路将由此发车。\n同时，为方便芙蓉东路乘客出行，601路的线路进行了微调，由芙蓉南路缩至芙蓉东路与北池头二路十字，再经芙蓉东路延伸至曲江乐园。线路延伸段增设：中海东郡小区和曲江乐园公交站，票价不变。",
			"广大市民朋友：您们好！\n由于加气站加气设备故障，自昨天下午一直抢修到现在还没有排除。公交公司所有车辆没有一台能加上气，公交公司集中能运行的车辆投放到1路、12路，3、7、9、10、11路公交车暂时不能运行，给广大市民出行带来不便，表示歉意。",
			"为市民服务了近7年的4条通宵公交线路近日临时停运了，想乘坐通宵公交的市民别在公交站台等了。记者昨日从市公交总公司获悉，从2008年8月开始运行的西安4条通宵公交线路，近日由于多种因素暂时停止运营。",
			"近日，258路、48路、312路、16路、611路集中更换成了新型空调车。今年西安市共计划投入520部厦门金旅和苏州金龙混合型空调车，目前正在陆续投放，涉及20多条线路。按照调配计划，部分线路上的旧车将全部停运，车况好的车辆被调配到其他线路上补充运力。",
			"5月12日起，西安902路与新开通的725路进行分线运营。随着气温逐渐升高，部分中巴线路票价由原来的一元一票制恢复为两元一票制。\n分线后，原902线路南段仍为902路，线路改为上林苑一路——西安理工科技专修学院;北段新开725路，线路为桐芳巷小区——科技路西口。分线后两线票价均执行无人售票一元一票制，刷长安通不享受优惠。902路暂时执行原票价，约一周后改为无人售票一元一票制，刷长安通卡无优惠。此举对市民乘车出行带来方便，具体可对照站牌上的站点走向乘车。",
			"市交警支队决定对北大街十字南北方向实行时段性禁左，目前方案已确定，待标志标牌到位后正式实施。\n北大街十字禁左方案拟将北大街十字南口、北口的左转地标箭头更改为直行地标箭头；关闭南北方向的左转信号灯，直行灯时加长，信号周期不变。在北大街十字南口、北口紧挨护栏端头各设置5.5米高的立柱，立柱上端设置双面禁左标志牌，禁左标志牌下增设辅助标志，内容为6:00-23:00。\n为确保道路畅通，市交警支队秩序处规划科在北大街十字增设引导标志：在南口东侧机非绿化带二府街口处增设一块巨幅醒目的蓝底白字交通引导标志牌，内容为“北大街十字禁左，请直行到北门掉头”；在北口西侧的青年路、北曹家巷、糖坊街口三处的机非绿化带各增设一块同样规格交通引导标志牌，内容为“北大街十字禁左，请提前分流绕行”。同时对北大街十字口人行隔离护栏进行增补，引导行人过街走地铁通道。",
			"目前，公交总公司与长安大学正在西高新的大型社区和写字楼进行调研，计划在高新区大型社区和写字楼之间开辟首条商务快巴。\n“商务快巴将大型社区——写字楼连接起来，每天早7点至8点，晚5点至6点往返于大型社区和写字楼之间。”翟长卫说，商务快巴采用高档次空调公交车，保证乘客都有座位，实行月票、季票、年票等方式。在调研中，不少白领对商务大巴表示欢迎，很多人认为只要不拥挤、有座位，一天4元的车费他们都能接受，与开私家车相比，还是比较划算的，也免去了到处抢车位的烦恼。",
			"今起，我市三条微型公交线路161、162、163将正式开通。昨日记者获悉，长8.5米的中轿车设19个座位，可容纳40至50人，微型公交票价实行一元一票制，可刷卡并享受优惠，平均间隔暂定为6分钟。",
			"今天早上8点左右，西安市34路公交车在吉祥诚信商业街站拉载乘客后，由于乘客拥挤过度，后门玻璃被当场挤爆，所幸没有市民受伤。" };
	private String[] author = { "西安公交五公司", "西安公交四公司", "西安公交五公司", "西安公交三公司",
			"西安公交二公司", "西安公交一公司", "西安公交五公司", "西安公交一公司", "西安公交四公司", "西安公交三公司" };
	private boolean[] newType = { true, true, true, true, true, false, false,
			false, false, false };

	private List<Notice> notices;
	private ListViewNewsAdapter adapter;

	private ListView listView;
	private ImageView back;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notice);

		notices = new ArrayList<Notice>();
		initData();
		initView();

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finish();

			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Notice notice = notices.get(position);

				Intent intent = new Intent();
				intent.setClass(getApplicationContext(),
						NewsInformationActivity.class);
				intent.putExtra("title", notice.getTitle());
				intent.putExtra("author", notice.getAuthor());
				intent.putExtra("time", notice.getPubDate());
				intent.putExtra("body", notice.getBody());
				startActivity(intent);

			}
		});

	}

	private void initView() {

		back = (ImageView) findViewById(R.id.notice_btn_back);
		listView = (ListView) findViewById(R.id.notice_listView);
		adapter = new ListViewNewsAdapter(notices, this);
		listView.setAdapter(adapter);

	}

	private void initData() {

		int i;
		for (i = 0; i < 10; i++) {

			Notice notice = new Notice();
			notice.setTitle(title[i]);
			notice.setBody(body[i]);
			notice.setAuthor(author[i]);
			notice.setPubDate(time[i]);
			notice.setNewType(newType[i]);
			notices.add(notice);

		}

		// adapter.notifyDataSetChanged();

	}

}
