package com.example.uhf_tagaccess;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.rfid.api.ADReaderInterface;
import com.rfid.api.BluetoothCfg;
import com.rfid.api.GFunction;
import com.rfid.api.ISO18000p6CInterface;
import com.rfid.basetype.UINT32;
import com.rfid.def.ApiErrDefinition;
import com.rfid.def.RfidDef;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class MainActivity extends Activity implements OnClickListener
{
	private final int[] layRes = { R.id.tab_reader, R.id.tab_inventory,
			R.id.tab_TagAccess };
	private final String[] layTittle = { "RFID", "INVENTORY", "ACCESS" };
	private Spinner sn_commType = null;// Connector
	private Spinner sn_devName = null;// Device type
	private EditText ed_ipAddr = null;// IP
	private EditText ed_port = null;// Port
	private Spinner sn_bluetooth = null;// bluetooth
	private Spinner sn_comName = null;// com
	private Spinner sn_comBaud = null;
	private Spinner sn_comFrame = null;
	private Button btn_connect = null;// connect tag
	private Button btn_disconnect = null;// disconnect
	private Button btn_getDevInfo = null;// get device information
	private Button btn_startInventory = null;
	private Button btn_stopInventory = null;
	private Button btn_clearInventoryList = null;
	private TextView tv_inventoryInfo = null;
	private Spinner sp_access_epc_list = null;
	private Spinner sp_access_memory_bank = null;
	private Spinner sp_access_word_start = null;
	private Spinner sp_access_word_count = null;
	private EditText ed_access_access_pwb = null;
	private EditText ed_access_data = null;
	private Button btn_access_inventory = null;
	private Button btn_access_write = null;
	static ADReaderInterface m_reader = new ADReaderInterface();
	private Thread m_inventoryThread = null;
	private final static int INVENTORY_MSG = 1;
	private final static int INVENTORY_FAIL_MSG = 2;
	private final static int THREAD_END = 3;
	private final List<InventoryReport> inventoryList = new ArrayList<>();
	private InventoryAdapter inventoryAdapter = null;
	private final ArrayList<CharSequence> epcSpinnerList = new ArrayList<>();
	private ArrayAdapter<CharSequence> m_adaEpcSpinerList = null;
	private CheckBox chkInvAntenna1 = null;
	private CheckBox chkInvAntenna2 = null;
	private CheckBox chkInvAntenna3 = null;
	private CheckBox chkInvAntenna4 = null;
	private CheckBox chkInvAntenna5 = null;
	private CheckBox chkInvAntenna6 = null;
	private CheckBox chkInvAntenna7 = null;
	private CheckBox chkInvAntenna8 = null;
	private CheckBox chkInvAntenna9 = null;
	private CheckBox chkInvAntenna10 = null;
	private CheckBox chkInvAntenna11 = null;
	private CheckBox chkInvAntenna12 = null;
	private CheckBox chkInvAntenna13 = null;
	private CheckBox chkInvAntenna14 = null;
	private CheckBox chkInvAntenna15 = null;
	private CheckBox chkInvAntenna16 = null;


	private CheckBox chkAccessAntenna1 = null;
	private CheckBox chkAccessAntenna2 = null;
	private CheckBox chkAccessAntenna3 = null;
	private CheckBox chkAccessAntenna4 = null;

	private byte[] mAccessAntena = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TabHost myTabhost = (TabHost) findViewById(R.id.tabhost);

		myTabhost.setup();
		for (int i = 0; i < layRes.length; i++)
		{
			TabSpec myTab = myTabhost.newTabSpec("tab" + i);
			myTab.setIndicator(layTittle[i]);
			myTab.setContent(layRes[i]);
			myTabhost.addTab(myTab);
		}
		myTabhost.setCurrentTab(0);

		// Inventory list tittle
		ViewGroup InventorytableTitle = (ViewGroup) findViewById(R.id.inventorylist_title);
		InventorytableTitle.setBackgroundColor(Color.rgb(255, 100, 10));

		sn_commType = (Spinner) findViewById(R.id.sn_commType);
		sn_devName = (Spinner) findViewById(R.id.sn_devType);
		ed_ipAddr = (EditText) findViewById(R.id.ed_ipAddr);
		ed_port = (EditText) findViewById(R.id.ed_port);
		sn_bluetooth = (Spinner) findViewById(R.id.sn_blueName);
		sn_comName = (Spinner) findViewById(R.id.sn_comName);
		sn_comBaud = (Spinner) findViewById(R.id.sn_comBaud);
		sn_comFrame = (Spinner) findViewById(R.id.sn_comFrame);
		btn_connect = (Button) findViewById(R.id.btn_connect);
		btn_disconnect = (Button) findViewById(R.id.btn_disconnect);
		btn_getDevInfo = (Button) findViewById(R.id.btn_infor);
		btn_startInventory = (Button) findViewById(R.id.btn_startInventory);
		btn_stopInventory = (Button) findViewById(R.id.btn_stopInventory);
		btn_clearInventoryList = (Button) findViewById(R.id.btn_clearInventoryList);
		// inventory list
		ListView list_inventory_record = (ListView) findViewById(R.id.list_inventory_record);
		tv_inventoryInfo = (TextView) findViewById(R.id.tv_inventoryInfo);
		// sp_access_antenna = (Spinner) findViewById(R.id.sp_access_antenna);
		sp_access_epc_list = (Spinner) findViewById(R.id.sp_access_epc_list);
		sp_access_memory_bank = (Spinner) findViewById(R.id.sp_access_memory_bank);
		sp_access_word_start = (Spinner) findViewById(R.id.sp_access_word_start);
		sp_access_word_count = (Spinner) findViewById(R.id.sp_access_word_count);
		ed_access_access_pwb = (EditText) findViewById(R.id.ed_access_access_pwb);
		ed_access_data = (EditText) findViewById(R.id.ed_access_data);
		btn_access_inventory = (Button) findViewById(R.id.btn_access_inventory);
		btn_access_write = (Button) findViewById(R.id.btn_access_write);
		Button btn_access_read = (Button) findViewById(R.id.btn_access_read);
		chkInvAntenna1 = (CheckBox) findViewById(R.id.chk_inv_ant1);
		chkInvAntenna2 = (CheckBox) findViewById(R.id.chk_inv_ant2);
		chkInvAntenna3 = (CheckBox) findViewById(R.id.chk_inv_ant3);
		chkInvAntenna4 = (CheckBox) findViewById(R.id.chk_inv_ant4);
		chkInvAntenna5 = (CheckBox) findViewById(R.id.chk_inv_ant5);
		chkInvAntenna6 = (CheckBox) findViewById(R.id.chk_inv_ant6);
		chkInvAntenna7 = (CheckBox) findViewById(R.id.chk_inv_ant7);
		chkInvAntenna8 = (CheckBox) findViewById(R.id.chk_inv_ant8);

		chkInvAntenna9 = (CheckBox) findViewById(R.id.chk_inv_ant9);
		chkInvAntenna10 = (CheckBox) findViewById(R.id.chk_inv_ant10);
		chkInvAntenna11 = (CheckBox) findViewById(R.id.chk_inv_ant11);
		chkInvAntenna12 = (CheckBox) findViewById(R.id.chk_inv_ant12);
		chkInvAntenna13 = (CheckBox) findViewById(R.id.chk_inv_ant13);
		chkInvAntenna14 = (CheckBox) findViewById(R.id.chk_inv_ant14);
		chkInvAntenna15 = (CheckBox) findViewById(R.id.chk_inv_ant15);
		chkInvAntenna16 = (CheckBox) findViewById(R.id.chk_inv_ant16);

		chkAccessAntenna1 = (CheckBox) findViewById(R.id.chk_access_ant1);
		chkAccessAntenna2 = (CheckBox) findViewById(R.id.chk_access_ant2);
		chkAccessAntenna3 = (CheckBox) findViewById(R.id.chk_access_ant3);
		chkAccessAntenna4 = (CheckBox) findViewById(R.id.chk_access_ant4);

		m_adaEpcSpinerList = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_dropdown_item, epcSpinnerList);
		sp_access_epc_list.setAdapter(m_adaEpcSpinerList);

		sn_commType.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id)
			{
				CommTypeChange();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		btn_connect.setOnClickListener(this);
		btn_disconnect.setOnClickListener(this);
		btn_getDevInfo.setOnClickListener(this);
		btn_startInventory.setOnClickListener(this);
		btn_stopInventory.setOnClickListener(this);
		btn_clearInventoryList.setOnClickListener(this);
		btn_access_write.setOnClickListener(this);
		btn_access_read.setOnClickListener(this);
		btn_access_inventory.setOnClickListener(this);
		ArrayList<CharSequence> m_bluetoolNameList;
		ArrayAdapter<CharSequence> m_adaBluetoolName;
		m_bluetoolNameList = new ArrayList<>();
		ArrayList<BluetoothCfg> m_blueList = ADReaderInterface
				.GetPairBluetooth();
		if (m_blueList != null)
		{
			for (BluetoothCfg bluetoolCfg : m_blueList)
			{
				m_bluetoolNameList.add(bluetoolCfg.GetName());
			}
		}

		m_adaBluetoolName = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_dropdown_item,
				m_bluetoolNameList);
		sn_bluetooth.setAdapter(m_adaBluetoolName);

		// Get the Serial port
		ArrayList<CharSequence> m_comNameList = new ArrayList<>();
		String[] m_comList = ADReaderInterface.GetSerialPortPath();
		if(m_comList.length==0){
			//oast.makeText(this,"123",Toast.LENGTH_SHORT).show();
			String comName0= "/dev/ttyS0";
			String comName1= "/dev/ttyS1";
			String comName2= "/dev/ttyS2";

			m_comNameList.add("/dev/ttyS0");
			m_comNameList.add("/dev/ttyS1");
			m_comNameList.add("/dev/ttyS2");
		}else{
			Collections.addAll(m_comNameList, m_comList);
		}
		ArrayAdapter<CharSequence> m_adaComName = new ArrayAdapter<>(
				this, android.R.layout.simple_spinner_dropdown_item,
				m_comNameList);
		sn_comName.setAdapter(m_adaComName);


		ArrayList<CharSequence> antennaList = new ArrayList<CharSequence>();
		for (int i = 1; i <= 4; i++)
		{
			antennaList.add(i + "");
		}

		ArrayList<CharSequence> memBankList = new ArrayList<>();
		memBankList.add("00:RFU");
		memBankList.add("01:EPC");
		memBankList.add("02:TID");
		memBankList.add("03:User");
		sp_access_memory_bank.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_dropdown_item, memBankList));
		sp_access_memory_bank.setSelection(1);

		ArrayList<CharSequence> wordStartList = new ArrayList<>();
		for (int i = 0; i < 256; i++)
		{
			wordStartList.add(i + "");
		}
		sp_access_word_start.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_dropdown_item, wordStartList));
		sp_access_word_start.setSelection(2);

		ArrayList<CharSequence> wordStartCount = new ArrayList<>();
		for (int i = 0; i <= 255; i++)
		{
			wordStartCount.add(i + "");
		}
		sp_access_word_count.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_dropdown_item, wordStartCount));
		
		sp_access_word_count.setSelection(1);

		inventoryAdapter = new InventoryAdapter(this, inventoryList);
		list_inventory_record.setAdapter(inventoryAdapter);

		LoadActivityByHistory();
		AllControlVisible(false);
	}

	protected void onDestroy()
	{
		if (m_reader.isReaderOpen())
		{
			stopInventory();
			m_reader.RDR_Close();
		}

		super.onDestroy();
	}

	private void CommTypeChange()
	{
		LinearLayout bluetoothView = (LinearLayout) findViewById(R.id.group_bluetooth);
		RelativeLayout netView = (RelativeLayout) findViewById(R.id.group_net);
		RelativeLayout comView = (RelativeLayout) findViewById(R.id.group_com);
		switch (sn_commType.getSelectedItemPosition())
		{
		case 0:
			bluetoothView.setVisibility(View.VISIBLE);
			netView.setVisibility(View.GONE);
			comView.setVisibility(View.GONE);
			break;
		case 1:
			bluetoothView.setVisibility(View.GONE);
			netView.setVisibility(View.GONE);
			comView.setVisibility(View.VISIBLE);
			break;
		case 2:
			bluetoothView.setVisibility(View.GONE);
			netView.setVisibility(View.VISIBLE);
			comView.setVisibility(View.GONE);
			break;
		case 3:
			bluetoothView.setVisibility(View.GONE);
			netView.setVisibility(View.GONE);
			comView.setVisibility(View.GONE);
			break;
		case 4:
			bluetoothView.setVisibility(View.GONE);
			netView.setVisibility(View.GONE);
			comView.setVisibility(View.VISIBLE);
			break;
		default:
			bluetoothView.setVisibility(View.GONE);
			netView.setVisibility(View.GONE);
			comView.setVisibility(View.GONE);
			break;
		}
	}

	private void OpenDevice()
	{
		String conStr;
		String devName = sn_devName.getSelectedItem().toString();

		int mIdx = sn_commType.getSelectedItemPosition();
		if (mIdx == 0)
		{
			if (sn_bluetooth.getAdapter().isEmpty())
			{
				Toast.makeText(this, "Please specify the bluetooth.",
						Toast.LENGTH_LONG).show();
				return;
			}
			String bluetoolName = sn_bluetooth.getSelectedItem().toString();
			if (bluetoolName.equals(""))
			{
				Toast.makeText(this, "The bluetooth is null", Toast.LENGTH_LONG)
						.show();
				return;
			}
			conStr = String.format("RDType=%s;CommType=BLUETOOTH;Name=%s",
					devName, bluetoolName);
		} else if (mIdx == 1)// ����
		{

			if (sn_comName.getAdapter().isEmpty())
			{
				Toast.makeText(this, "Please specify a serial port.",
						Toast.LENGTH_LONG).show();
				return;
			}
			conStr = String
					.format("RDType=%s;CommType=COM;ComPath=%s;Baund=%s;Frame=%s;Addr=255",
							devName,
							sn_comName.getSelectedItem().toString(),
							sn_comBaud.getSelectedItem().toString(),
							sn_comFrame.getSelectedItem().toString());
		} else if (mIdx == 2)// (commTypeStr.equals(getString(R.string.tx_type_net)))//
								// ����
		{
			String sRemoteIp = ed_ipAddr.getText().toString();
			String sRemotePort = ed_port.getText().toString();
			conStr = String.format(
					"RDType=%s;CommType=NET;RemoteIp=%s;RemotePort=%s",
					devName, sRemoteIp, sRemotePort);
		} else if (mIdx == 3)// (commTypeStr.equals("USB"))
		{
			// ע�⣺ʹ��USB��ʽʱ��������Ҫö������USB�豸
			// Note: Before using USB, you must enumerate all USB devices first.
			int usbCnt = ADReaderInterface.EnumerateUsb(this);
			if (usbCnt <= 0)
			{
				Toast.makeText(this, "No USB device was found.",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (!ADReaderInterface.HasUsbPermission(""))
			{
				Toast.makeText(this,
						"No permission of operating the USB device.",
						Toast.LENGTH_SHORT).show();
				ADReaderInterface.RequestUsbPermission("");
				return;
			}

			conStr = String.format("RDType=%s;CommType=USB;Description=",
					devName);
		} else if (mIdx == 4)// (commTypeStr.equals(getString(R.string.tx_type_usb_com)))
		{
			// Attention: Only support Z-TEK
			// ע�⣺Ŀ¼ֻ֧��Z-TEK�ͺŵ�USBת������
			int mUsbCnt = ADReaderInterface.EnumerateZTEK(this, 0x0403, 0x6001);
			if (mUsbCnt <= 0)
			{
				Toast.makeText(this,
						"No permission of operating the USB device.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			conStr = String
					.format("RDType=%s;CommType=Z-TEK;port=1;Baund=%s;Frame=%s;Addr=255",
							devName, sn_comBaud.getSelectedItem().toString(),
							sn_comFrame.getSelectedItem().toString());

		} else
		{
			return;
		}
		int iret=m_reader.RDR_Open(conStr);
		if (iret == ApiErrDefinition.NO_ERROR)
		{
			// ///////////////////////////////////////////////////
			Toast.makeText(this, "Open the device successfully",
					Toast.LENGTH_SHORT).show();
			SaveActivity();
			AllControlVisible(true);
		} else
		{
			Toast.makeText(this, "It is failure to open the device.",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void AllControlVisible(boolean isConnect)
	{
		sn_devName.setEnabled(!isConnect);
		sn_commType.setEnabled(!isConnect);
		sn_bluetooth.setEnabled(!isConnect);
		ed_ipAddr.setEnabled(!isConnect);
		ed_port.setEnabled(!isConnect);
		sn_comName.setEnabled(!isConnect);
		sn_comBaud.setEnabled(!isConnect);
		sn_comFrame.setEnabled(!isConnect);
		btn_connect.setEnabled(!isConnect);
		btn_disconnect.setEnabled(isConnect);
		btn_getDevInfo.setEnabled(isConnect);
		btn_startInventory.setEnabled(isConnect);
		btn_stopInventory.setEnabled(false);
		btn_clearInventoryList.setEnabled(isConnect);
		// sp_access_antenna.setEnabled(isConnect);
		sp_access_epc_list.setEnabled(isConnect);
		sp_access_memory_bank.setEnabled(isConnect);
		sp_access_word_start.setEnabled(isConnect);
		sp_access_word_count.setEnabled(isConnect);
		ed_access_access_pwb.setEnabled(isConnect);
		ed_access_data.setEnabled(isConnect);
		btn_access_inventory.setEnabled(isConnect);
		btn_access_write.setEnabled(isConnect);

		chkInvAntenna1.setEnabled(isConnect);
		chkInvAntenna2.setEnabled(isConnect);
		chkInvAntenna3.setEnabled(isConnect);
		chkInvAntenna4.setEnabled(isConnect);
		chkInvAntenna5.setEnabled(isConnect);
		chkInvAntenna6.setEnabled(isConnect);
		chkInvAntenna7.setEnabled(isConnect);
		chkInvAntenna8.setEnabled(isConnect);

		chkInvAntenna9.setEnabled(isConnect);
		chkInvAntenna10.setEnabled(isConnect);
		chkInvAntenna11.setEnabled(isConnect);
		chkInvAntenna12.setEnabled(isConnect);
		chkInvAntenna13.setEnabled(isConnect);
		chkInvAntenna14.setEnabled(isConnect);
		chkInvAntenna15.setEnabled(isConnect);
		chkInvAntenna16.setEnabled(isConnect);

		chkAccessAntenna1.setEnabled(isConnect);
		chkAccessAntenna2.setEnabled(isConnect);
		chkAccessAntenna3.setEnabled(isConnect);
		chkAccessAntenna4.setEnabled(isConnect);
	}

	private void CloseDevice()
	{
		stopInventory();
		m_reader.RDR_Close();
		sn_devName.setEnabled(true);
		sn_commType.setEnabled(true);
		sn_bluetooth.setEnabled(true);
		ed_ipAddr.setEnabled(true);
		ed_port.setEnabled(true);
		sn_comName.setEnabled(true);
		sn_comBaud.setEnabled(true);
		sn_comFrame.setEnabled(true);
		btn_connect.setEnabled(true);
		btn_disconnect.setEnabled(false);
		btn_getDevInfo.setEnabled(false);
		AllControlVisible(false);
	}

	private void saveHistory(String sKey, String val)
	{
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(sKey, val);
		editor.apply();
	}

	private void saveHistory(String sKey, int val)
	{
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(sKey, val);
		editor.apply();
	}

	private int GetHistoryInt(String sKey)
	{
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_PRIVATE);
		return preferences.getInt(sKey, -1);
	}

	private String GetHistoryString(String sKey)
	{
		SharedPreferences preferences = this.getSharedPreferences(sKey,
				Context.MODE_PRIVATE);
		return preferences.getString(sKey, "");
	}

	private void SaveActivity()
	{
		int devItem = 0;
		int commItem = 0;
		int blueToolItem = 0;
		String ipStr = ed_ipAddr.getText().toString();
		String portStr = ed_port.getText().toString();
		int comNameItem = 0;
		int comBaudItem = 0;
		int comFrameItem = 0;

		if (!sn_devName.getAdapter().isEmpty())
		{
			devItem = sn_devName.getSelectedItemPosition();
		}
		if (!sn_commType.getAdapter().isEmpty())
		{
			commItem = sn_commType.getSelectedItemPosition();
		}
		if (!sn_bluetooth.getAdapter().isEmpty())
		{
			blueToolItem = sn_bluetooth.getSelectedItemPosition();
		}

		if (!sn_comName.getAdapter().isEmpty())
		{
			comNameItem = sn_comName.getSelectedItemPosition();
		}
		if (!sn_comBaud.getAdapter().isEmpty())
		{
			comBaudItem = sn_comBaud.getSelectedItemPosition();
		}
		if (!sn_comFrame.getAdapter().isEmpty())
		{
			comFrameItem = sn_comFrame.getSelectedItemPosition();
		}

		saveHistory("DEVNAME", devItem);
		saveHistory("COMMTYPE", commItem);
		saveHistory("COMBAUD", comBaudItem);
		saveHistory("COMFRAME", comFrameItem);
		saveHistory("BLUETOOL", blueToolItem);
		saveHistory("COMNAME", comNameItem);
		saveHistory("DEVIPADDR", ipStr);
		saveHistory("DEVPORT", portStr);
	}

	private void LoadActivityByHistory()
	{
		int devItem = GetHistoryInt("DEVNAME");
		if (devItem < sn_devName.getCount())
		{
			sn_devName.setSelection(devItem);
		}

		int commItem = GetHistoryInt("COMMTYPE");
		if (commItem < sn_commType.getCount())
		{
			sn_commType.setSelection(commItem);
		}

		int blueToolItem = GetHistoryInt("BLUETOOL");
		if (blueToolItem < sn_bluetooth.getCount())
		{
			sn_bluetooth.setSelection(blueToolItem);
		}

		int comNameItem = GetHistoryInt("COMNAME");
		if (comNameItem < sn_comName.getCount())
		{
			sn_comName.setSelection(comNameItem);
		}

		int comBaudItem = GetHistoryInt("COMBAUD");
		if (comBaudItem < sn_comBaud.getCount() && comBaudItem >= 0)
		{
			sn_comBaud.setSelection(comBaudItem);
		} else
		{
			sn_comBaud.setSelection(1);
		}

		int comFrameItem = GetHistoryInt("COMFRAME");
		if (comFrameItem < sn_comFrame.getCount() && comFrameItem >= 0)
		{
			sn_comFrame.setSelection(comFrameItem);
		} else
		{
			sn_comFrame.setSelection(0);
		}

		String sIp = GetHistoryString("DEVIPADDR");
		if (!sIp.equals(""))
		{
			ed_ipAddr.setText(sIp);
		}

		String sPort = GetHistoryString("DEVPORT");
		if (!sPort.equals(""))
		{
			ed_port.setText(sPort);
		}
	}

	private void GetInformation()
	{
		StringBuffer buffer = new StringBuffer();
		int iret = m_reader.RDR_GetReaderInfor(buffer);
		if (iret == ApiErrDefinition.NO_ERROR)
		{
			new AlertDialog.Builder(this).setTitle("")
					.setMessage(buffer.toString())
					.setPositiveButton("OK", null).show();
		} else
		{
			new AlertDialog.Builder(this)
					.setTitle("")
					.setMessage(
							"It is failure to get the information. Error="
									+ iret).setPositiveButton("OK", null)
					.show();
		}
	}

	private final Handler mHandler = new MyHandler(this);

	private static class MyHandler extends Handler
	{
		private final WeakReference<MainActivity> mActivity;

		public MyHandler(MainActivity activity)
		{
			mActivity = new WeakReference<>(activity);
		}

		@SuppressLint("DefaultLocale")
		public void handleMessage(Message msg)
		{
			MainActivity pt = mActivity.get();
			if (pt == null)
			{
				return;
			}
			switch (msg.what)
			{
			case INVENTORY_MSG:
				Vector<String> epcList = (Vector<String>) msg.obj;
				pt.inventoryList.clear();
				for (int i = 0; i < epcList.size(); i++)
				{
					boolean b_find = false;
					String strEPC = epcList.get(i);

					for (int j = 0; j < pt.inventoryList.size(); j++)
					{
						InventoryReport mReport = pt.inventoryList.get(j);
						if (mReport.getEpcStr().equals(strEPC))
						{
							mReport.setFindCnt(mReport.getFindCnt() + 1);
							b_find = true;
							break;
						}
					}

					if (!b_find)
					{
						pt.inventoryList.add(new InventoryReport(strEPC, 1));
					}
				}
				pt.tv_inventoryInfo.setText(String.format(
						"Tag count:%d  Loop:%d InventoryTime:%dms", pt.inventoryList.size(),
						msg.arg1, msg.arg2));

				pt.inventoryAdapter.notifyDataSetChanged();
				break;
			case INVENTORY_FAIL_MSG:
				break;
			case THREAD_END:// �߳̽���
				break;
			default:
				break;
			}
		}
	}

	private boolean b_inventoryThreadRun = false;
	private long inventoryTime=0;
	private long useTime=0;

	private class InventoryThrd implements Runnable
	{
		public void run()
		{

			b_inventoryThreadRun = true;

			Object InvenParamSpecList=ADReaderInterface.RDR_CreateInvenParamSpecList();
			if(InvenParamSpecList!=null){

				ADReaderInterface.RDR_SetInvenStopTrigger(InvenParamSpecList,(byte) RfidDef.INVEN_STOP_TRIGGER_TYPE_TIMEOUT,
						200,0);
			}
			ISO18000p6CInterface.ISO18000p6C_CreateInvenParam(InvenParamSpecList,(byte)0,(byte)0,RfidDef.ISO18000p6C_S0,
					RfidDef.ISO18000p6C_TARGET_A,RfidDef.ISO18000p6C_Dynamic_Q);

			int loopCnt = 0;
			byte AIType = RfidDef.AI_TYPE_NEW;
			while (b_inventoryThreadRun)
			{
				Vector<String> epcList = new Vector<>();
				inventoryTime=System.currentTimeMillis();
				int iret = m_reader.RDR_TagInventory(AIType, mAccessAntena, 0, InvenParamSpecList);
				if (iret == ApiErrDefinition.NO_ERROR || iret == -21)
				{
					Object TagDataReport = m_reader.RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
					while (TagDataReport != null)
					{
						long[] aip_id = new long[1];
						long[] tag_id = new long[1];
						long[] ant_id = new long[1];
						byte[] epcBuffer = new byte[255];
						long[] nSize = new long[1];
						nSize[0] = epcBuffer.length;

						iret = ISO18000p6CInterface
								.ISO18000p6C_ParseTagReportV2(TagDataReport,
										aip_id, tag_id, ant_id, epcBuffer,
										nSize);
						if (iret == ApiErrDefinition.NO_ERROR)
						{
							epcList.add(GFunction.encodeHexStr(epcBuffer,
									(int) nSize[0]));

						}
						TagDataReport = m_reader.RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT); // next
					}
					loopCnt++;
				}


				useTime=System.currentTimeMillis()-inventoryTime;

				Message msg = mHandler.obtainMessage();
				msg.what = INVENTORY_MSG;
				msg.obj = epcList;
				msg.arg1 = loopCnt;
				msg.arg2= (int)useTime;
				mHandler.sendMessage(msg);

			}
			b_inventoryThreadRun = false;
			m_reader.RDR_ResetCommuImmeTimeout();
			mHandler.sendEmptyMessage(THREAD_END);// �̵����
		}
	}

	private void startInventory()
	{
		int idx = 0;
		byte[] antSel = new byte[24];
		stopInventory();
		clearInventoryList();
		m_inventoryThread = new Thread(new InventoryThrd());
		btn_startInventory.setEnabled(false);
		btn_stopInventory.setEnabled(true);
		btn_clearInventoryList.setEnabled(false);

		if (chkInvAntenna1.isChecked())
		{
			antSel[idx++] = 1;
		}
		if (chkInvAntenna2.isChecked())
		{
			antSel[idx++] = 2;
		}
		if (chkInvAntenna3.isChecked())
		{
			antSel[idx++] = 3;
		}
		if (chkInvAntenna4.isChecked())
		{
			antSel[idx++] = 4;
		}
		if (chkInvAntenna5.isChecked())
		{
			antSel[idx++] = 5;
		}
		if (chkInvAntenna6.isChecked())
		{
			antSel[idx++] = 6;
		}
		if (chkInvAntenna7.isChecked())
		{
			antSel[idx++] = 7;
		}
		if (chkInvAntenna8.isChecked())
		{
			antSel[idx++] = 8;
		}

		if (chkInvAntenna9.isChecked())
		{
			antSel[idx++] = 9;
		}
		if (chkInvAntenna10.isChecked())
		{
			antSel[idx++] = 10;
		}
		if (chkInvAntenna11.isChecked())
		{
			antSel[idx++] = 11;
		}
		if (chkInvAntenna12.isChecked())
		{
			antSel[idx++] = 12;
		}
		if (chkInvAntenna13.isChecked())
		{
			antSel[idx++] = 13;
		}
		if (chkInvAntenna14.isChecked())
		{
			antSel[idx++] = 14;
		}
		if (chkInvAntenna15.isChecked())
		{
			antSel[idx++] = 15;
		}
		if (chkInvAntenna16.isChecked())
		{
			antSel[idx++] = 16;
		}

		if (idx > 0)
		{
			mAccessAntena = new byte[idx];
			System.arraycopy(antSel, 0, mAccessAntena, 0, idx);
		} else
		{
			mAccessAntena = null;
		}

		m_inventoryThread.start();
	}

	private void stopInventory()
	{
		if (m_inventoryThread != null && m_inventoryThread.isAlive())
		{
			b_inventoryThreadRun = false;
			m_reader.RDR_SetCommuImmeTimeout();
			try
			{
				m_inventoryThread.join();
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			m_inventoryThread = null;
		}
		btn_startInventory.setEnabled(true);
		btn_stopInventory.setEnabled(false);
		btn_clearInventoryList.setEnabled(true);
	}

	public static class InventoryReport
	{
		private String epcStr;
		private long findCnt = 0;

		public InventoryReport(String epc, long cnt)
		{
			super();
			this.setEpcStr(epc);
			this.setFindCnt(cnt);
		}

		public String getEpcStr()
		{
			return epcStr;
		}

		public void setEpcStr(String epc)
		{
			this.epcStr = epc;
		}

		public long getFindCnt()
		{
			return findCnt;
		}

		public void setFindCnt(long findCnt)
		{
			this.findCnt = findCnt;
		}
	}

	 public static class InventoryAdapter extends BaseAdapter
	{
		private final List<InventoryReport> list;
		private final LayoutInflater inflater;

		public InventoryAdapter(Context context, List<InventoryReport> list)
		{
			this.list = list;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount()
		{
			return list.size();
		}

		public Object getItem(int position)
		{
			return list.get(position);
		}

		public long getItemId(int position)
		{
			return position;
		}

		@SuppressLint("ResourceType")
		public View getView(int position, View convertView, ViewGroup parent)
		{
			InventoryReport inventoryReport = (InventoryReport) this
					.getItem(position);
			ViewHolder viewHolder;
			if (convertView == null)
			{
				viewHolder = new ViewHolder();
				convertView = inflater
						.inflate(R.xml.inventorylist_tittle, null);
				viewHolder.mTextEpc = (TextView) convertView
						.findViewById(R.id.tv_inventoryEpc);
				viewHolder.mTextFindCnt = (TextView) convertView
						.findViewById(R.id.tv_inventoryCnt);
				convertView.setTag(viewHolder);
			} else
			{
				viewHolder = (ViewHolder) convertView.getTag();
			}

			long mCnt = inventoryReport.getFindCnt();
			String strCnt = mCnt > 0 ? (mCnt + "") : "1";
			viewHolder.mTextEpc.setText(inventoryReport.getEpcStr());
			// viewHolder.mTextTagData.setText(inventoryReport.getTagDataStr());
			viewHolder.mTextFindCnt.setText(strCnt);

			return convertView;
		}

		private static class ViewHolder
		{
			public TextView mTextEpc;
			public TextView mTextFindCnt;
		}
	}

	@SuppressLint("SetTextI18n")
	private void clearInventoryList()
	{
		inventoryList.clear();
		tv_inventoryInfo.setText("Tag count:0  Loop:0");
		inventoryAdapter.notifyDataSetChanged();
	}

	private void updateAccessSelectAntennas()
	{
		int idx = 0;
		byte[] antSel = new byte[24];
		if (chkAccessAntenna1.isChecked())
		{
			antSel[idx] = 1;
			idx++;
		}
		if (chkAccessAntenna2.isChecked())
		{
			antSel[idx] = 2;
			idx++;
		}

		if (chkAccessAntenna3.isChecked())
		{
			antSel[idx] = 3;
			idx++;
		}
		if (chkAccessAntenna4.isChecked())
		{
			antSel[idx] = 4;
			idx++;
		}

		if (idx > 0)
		{
			mAccessAntena = new byte[idx];
			System.arraycopy(antSel, 0, mAccessAntena, 0, idx);
		}
		else
		{
			mAccessAntena = null;
		}
	}

	private void inventoryAccessEpc()
	{
		epcSpinnerList.clear();
		m_adaEpcSpinerList.notifyDataSetChanged();

		updateAccessSelectAntennas();

		int iret = m_reader.RDR_TagInventory(RfidDef.AI_TYPE_NEW,
				mAccessAntena, 1000, null); // OKOK
		if (iret != ApiErrDefinition.NO_ERROR)
		{
			Toast.makeText(this, "It is failure to inventory.",
					Toast.LENGTH_SHORT).show();
			return;
		}

		Object dnhReport = m_reader
				.RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
		while (dnhReport != null)
		{
			long[] aip_id = new long[1];
			long[] tag_id = new long[1];
			long[] ant_id = new long[1];
			byte[] epcData = new byte[255];
			long[] nSize = new long[1];
			nSize[0] = epcData.length;

			iret = ISO18000p6CInterface.ISO18000p6C_ParseTagReportV2(dnhReport,
					aip_id, tag_id, ant_id, epcData, nSize);
			if (iret != ApiErrDefinition.NO_ERROR)
			{
				Toast.makeText(this, "It is failure to inventory.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			String strEpc = GFunction.encodeHexStr(epcData, (int) nSize[0]);
			epcSpinnerList.add(strEpc);
			dnhReport = m_reader.RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
		}
		m_adaEpcSpinerList.notifyDataSetChanged();
	}

	private void ReadAccess()
	{
		ISO18000p6CInterface m_tag = new ISO18000p6CInterface();
		byte mMemType;
		switch (sp_access_memory_bank.getSelectedItemPosition())
		{
		case 0:
			mMemType = RfidDef.ISO18000p6C_MEM_BANK_RFU;
			break;
		case 1:
			mMemType = RfidDef.ISO18000p6C_MEM_BANK_EPC;
			break;
		case 2:
			mMemType = RfidDef.ISO18000p6C_MEM_BANK_TID;
			break;
		case 3:
			mMemType = RfidDef.ISO18000p6C_MEM_BANK_USER;
			break;
		default:
			return;
		}

		String epcStr = "";

		Object epcItem = sp_access_epc_list.getSelectedItem();
		if (epcItem != null)
		{
			epcStr = sp_access_epc_list.getSelectedItem().toString();
		}
		if (epcStr.equals(""))
		{
			Toast.makeText(this,
					"Please select a tag to write the data first.",
					Toast.LENGTH_LONG).show();
			return;
		}
		byte[] epcByBuf = GFunction.decodeHex(epcStr);

		String pwbStr = ed_access_access_pwb.getText().toString();
		long accessPwd = 0;
		if (!pwbStr.equals(""))
		{
			byte[] accessPwdBytes = GFunction.decodeHex(pwbStr);
			accessPwd = (accessPwdBytes[0] | (accessPwdBytes[1] << 8 & 0xff00)
					| (accessPwdBytes[2] << 16 & 0xff0000) | (accessPwdBytes[3] << 24 & 0xff000000));
		}

		int iret;
		long[] nSize = new long[1];

		int WordCnt = sp_access_word_count.getSelectedItemPosition();
		int WordPointer = sp_access_word_start.getSelectedItemPosition();

		
		byte[] dataByBuf = new byte[255];
		updateAccessSelectAntennas();
		iret = m_reader.RDR_SetMultiAccessAntennas(mAccessAntena, mAccessAntena.length);
		if (iret != ApiErrDefinition.NO_ERROR)
		{
			Toast.makeText(this, "It is failure to set the antennas.",
					Toast.LENGTH_LONG).show();
			return;
		}
		
		iret = m_tag.ISO18000p6C_Connect(m_reader, 0, epcByBuf,
				epcByBuf.length, accessPwd);
		if (iret != ApiErrDefinition.NO_ERROR)
		{
			Toast.makeText(this, "It is failure to write the data.",
					Toast.LENGTH_LONG).show();
			return;
		}

		
		nSize[0] = dataByBuf.length;
		iret = m_tag.ISO18000p6C_Read(mMemType, WordPointer, WordCnt,
				dataByBuf, nSize);

		m_tag.ISO18000p6C_Disconnect();
		if (iret == ApiErrDefinition.NO_ERROR)
		{
			String strTagdata = GFunction.encodeHexStr(dataByBuf,(int) nSize[0]);
			ed_access_data.setText(strTagdata);
			Toast.makeText(this, "read the data successfully.",
					Toast.LENGTH_LONG).show();
		} else
		{
			Toast.makeText(this, "It is failure in reading the data.",
					Toast.LENGTH_LONG).show();
		}
	}
	
	
	private void WriteAccess()
	{
		ISO18000p6CInterface m_tag = new ISO18000p6CInterface();
		byte mMemType;
		switch (sp_access_memory_bank.getSelectedItemPosition())
		{
		case 0:
			mMemType = RfidDef.ISO18000p6C_MEM_BANK_RFU;
			break;
		case 1:
			mMemType = RfidDef.ISO18000p6C_MEM_BANK_EPC;
			break;
		case 2:
			mMemType = RfidDef.ISO18000p6C_MEM_BANK_TID;
			break;
		case 3:
			mMemType = RfidDef.ISO18000p6C_MEM_BANK_USER;
			break;
		default:
			return;
		}

		String epcStr = "";

		Object epcItem = sp_access_epc_list.getSelectedItem();
		if (epcItem != null)
		{
			epcStr = sp_access_epc_list.getSelectedItem().toString();
		}
		if (epcStr.equals(""))
		{
			Toast.makeText(this,
					"Please select a tag to write the data first.",
					Toast.LENGTH_LONG).show();
			return;
		}
		byte[] epcByBuf = GFunction.decodeHex(epcStr);

		String pwbStr = ed_access_access_pwb.getText().toString();
		long accessPwd = 0;
		if (!pwbStr.equals(""))
		{
			byte[] accessPwdBytes = GFunction.decodeHex(pwbStr);
			accessPwd = (accessPwdBytes[0] | (accessPwdBytes[1] << 8 & 0xff00)
					| (accessPwdBytes[2] << 16 & 0xff0000) | (accessPwdBytes[3] << 24 & 0xff000000));
		}

		int iret;
		int WordCnt = sp_access_word_count.getSelectedItemPosition();
		int WordPointer = sp_access_word_start.getSelectedItemPosition();
		
		if (WordCnt<=0)
		{
			Toast.makeText(this,
					"The word pointer can not be 0.",
					Toast.LENGTH_LONG).show();
			return;
		}

		
		if (WordPointer < 2 && mMemType == RfidDef.ISO18000p6C_MEM_BANK_EPC)
		{
			Toast.makeText(this,
					"The word pointer of EPC memory must be more than 2.",
					Toast.LENGTH_LONG).show();
			return;
		}

		String dataStr = ed_access_data.getText().toString();
		if (dataStr.equals(""))
		{
			Toast.makeText(this, "Please injput the data first.",
					Toast.LENGTH_LONG).show();
			return;
		}
		byte[] dataByBuf = GFunction.decodeHex(dataStr);
		if (dataByBuf != null && dataByBuf.length != WordCnt * 2)
		{
			Toast.makeText(this, "Data error.", Toast.LENGTH_LONG).show();
			return;
		}

		updateAccessSelectAntennas();
		
		iret = m_reader.RDR_SetMultiAccessAntennas(mAccessAntena, mAccessAntena.length);
		if (iret != ApiErrDefinition.NO_ERROR)
		{
			Toast.makeText(this, "It is failure to set the antennas.",
					Toast.LENGTH_LONG).show();
			return;
		}

		iret = m_tag.ISO18000p6C_Connect(m_reader, 0, epcByBuf,
				epcByBuf.length, accessPwd);
		if (iret != ApiErrDefinition.NO_ERROR)
		{
			Toast.makeText(this, "It is failure to write the data.",
					Toast.LENGTH_LONG).show();
			return;
		}

		iret = m_tag.ISO18000p6C_Write(mMemType, WordPointer, WordCnt,
				dataByBuf, dataByBuf.length);

		m_tag.ISO18000p6C_Disconnect();

		if (iret == ApiErrDefinition.NO_ERROR)
		{
			Toast.makeText(this, "Write the data successfully.",
					Toast.LENGTH_LONG).show();
		} else
		{
			Toast.makeText(this, "It is failure in writing the data.",
					Toast.LENGTH_LONG).show();
		}
	}

	@SuppressLint("NonConstantResourceId")
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.btn_connect:
			OpenDevice();
			break;
		case R.id.btn_disconnect:
			CloseDevice();
			break;
		case R.id.btn_infor:
			GetInformation();
			break;
		case R.id.btn_startInventory:
			startInventory();
			break;
		case R.id.btn_stopInventory:
			stopInventory();
			break;
		case R.id.btn_clearInventoryList:
			clearInventoryList();
			break;

		case R.id.btn_access_inventory:
			inventoryAccessEpc();
			break;
		case R.id.btn_access_write:
			WriteAccess();
			break;

		case R.id.btn_access_read:
			ReadAccess();
			break;
		default:
			break;
		}
	}
}
