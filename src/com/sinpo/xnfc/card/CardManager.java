/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.sinpo.xnfc.card;

import java.io.IOException;

import com.onebus.view.CardActivity;
import com.sinpo.xnfc.BusCard;
import com.sinpo.xnfc.card.pboc.PbocCard;
import com.sinpo.xnfc.tech.Iso7816;

import android.content.IntentFilter;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Parcelable;
import android.util.Log;

public final class CardManager {

	private static final String SP = "\n\n";

	public static String[][] TECHLISTS;
	public static IntentFilter[] FILTERS;

	static {
		try {
			TECHLISTS = new String[][] { { IsoDep.class.getName() },
					{ NfcV.class.getName() }, { NfcF.class.getName() }, };

			FILTERS = new IntentFilter[] { new IntentFilter(
					NfcAdapter.ACTION_TECH_DISCOVERED, "*/*") };
		} catch (Exception e) {
		}
	}

	public static String buildResult(String n, String i, String d, String x) {
		if (n == null)
			return null;
		
		
		/**
		 * 这里将信息加载到BusCard中
		 */
		CardActivity.mBusCard.setName(n);

		final StringBuilder s = new StringBuilder();
		
		s.append(n);

		if (d != null){
			s.append(SP).append(d);
			CardActivity.mBusCard.setBalance(d);
		}

		if (x != null){
			s.append(SP).append(x);
			
			//处理Log
			int logstartpos = 0;
			int startpos = x.indexOf("]", logstartpos);
			CardActivity.mBusCard.getConsumeLogList().clear();
			//循环遍历
			while(startpos!=-1){
				String oneLog = x.substring(logstartpos, startpos);
				Log.i("card log", oneLog);
				String time = oneLog.substring(1, 17);
				String cash = oneLog.substring(17, oneLog.indexOf("["));
				String districtCode = oneLog.substring(oneLog.indexOf("[")+1);
				BusCard.ConsumeLog nLog = new BusCard.ConsumeLog(districtCode, time, cash);
				CardActivity.mBusCard.getConsumeLogList().add(nLog);
				
				logstartpos = startpos+1;
				startpos=x.indexOf("]", logstartpos);
			}
			
			
		}
		
		if (i != null)
			s.append(SP).append(i);
		
		return s.toString();
	}

	public static String load(Parcelable parcelable, Resources res) {
		final Tag tag = (Tag) parcelable;
		
		
		final IsoDep isodep = IsoDep.get(tag);
		

		Log.d("NFCTAG", "ffff");//isodep.transceive("45".getBytes()).toString());

		
		if (isodep != null) {
			return PbocCard.load(isodep, res);
		}

		final NfcV nfcv = NfcV.get(tag);
		if (nfcv != null) {
			return VicinityCard.load(nfcv, res);
		}

		final NfcF nfcf = NfcF.get(tag);
		if (nfcf != null) {
			return OctopusCard.load(nfcf, res);
		}

		return null;
	}
}
