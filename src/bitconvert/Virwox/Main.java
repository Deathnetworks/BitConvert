package bitconvert.Virwox;


import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bitconvert.Virwox.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements RadioGroup.OnCheckedChangeListener{
	
	//Radio Group Variables
	protected static int RadioGBPID = -1;	 
    protected static int RadioUSDID = -2;
    protected static int RadioEURID = -2;
    protected static RadioGroup RadioCurrencyGroup = null;        
    protected static RadioButton RadioGBP, RadioUSD, RadioEUR = null;
	
	//Variables
	int Amount = 0;
    int Total = 0;
    float SubTotal;
    static float SelectedBuy;
    static String SelectedCurrency;
            	
    //Persistent variables
    private SharedPreferences Variables;
    private static float BTCSell;
    private float GBPBuy, USDBuy, EURBuy;
    private String DateTime;
    private String Selected;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); 
        
        //Load stored variables
        Variables = getPreferences(MODE_PRIVATE);
        BTCSell = Variables.getFloat("BTC", 0);
        GBPBuy = Variables.getFloat("GBP", 0);
        USDBuy = Variables.getFloat("USD", 0);
        EURBuy = Variables.getFloat("EUR", 0);
        Selected = Variables.getString("Selected", "GBP");
        DateTime = Variables.getString("Date", "Sync for data");
        SelectedCurrency = Selected;
        
        //Declare RadioGroup
        RadioCurrencyGroup = (RadioGroup) findViewById(R.id.RadioCurrency); 
        //initialise listener
         
        
        RadioGBP = (RadioButton) findViewById(R.id.RadioGBP);
        RadioUSD = (RadioButton) findViewById(R.id.RadioUSD);
        RadioEUR = (RadioButton) findViewById(R.id.RadioEUR);
        
        //Set ID's
        RadioGBPID = RadioGBP.getId();
        RadioUSDID = RadioUSD.getId();
        RadioEURID = RadioEUR.getId();
                
        
        RadioUpdate(); //update the radio buttons with the stored variables
        Selected(); //set the SelectedBuy
        
        UpdateRates(); //update text views with stored data   
        RadioCurrencyGroup.setOnCheckedChangeListener(this);
        
    }
    
    void Selected(){
    	if (SelectedCurrency.equals("GBP")){
    		SelectedBuy = GBPBuy;
    	}
    	else if (SelectedCurrency.equals("USD")){
    		SelectedBuy = USDBuy;
    	} 
    	else if (SelectedCurrency.equals("EUR")){
    		SelectedBuy = EURBuy;
    	}
    	//Print new total text
    	TextView tv = (TextView)findViewById(R.id.Outputtxt);
    	//choose correct currency symbol
    	if (SelectedCurrency.equals("GBP")){
    		tv.setText("Total: £" + 0 + ".00");
    	}
    	else if (SelectedCurrency.equals("USD")){
    		tv.setText("Total: $" + 0 + ".00");
    	} 
    	else if (SelectedCurrency.equals("EUR")){
    		tv.setText("Total: " + 0 + ",00 €");
    	}
    }
    void RadioUpdate(){
    	if (SelectedCurrency.equals("GBP")){
    		RadioGBP.setChecked(true);
    	}
    	else if (SelectedCurrency.equals("USD")){
    		RadioUSD.setChecked(true);
    	} 
    	else if (SelectedCurrency.equals("EUR")){    		
    			RadioEUR.setChecked(true);
    	}
    }    
    public void Start(View Calculate){    	
    	//checks to see if data has been synchronised
    	if (SelectedBuy+BTCSell>0){ 
    		EditText editText = (EditText)findViewById(R.id.Amountinputtxt);
        	String editTextStr = editText.getText().toString();
        	//make sure user has entered a valid amount
        	if (!editTextStr.equals("") && !editTextStr.equals("0")){ //check for blank
        		Amount = Double.valueOf(editTextStr).intValue();
    			Calculate();        			
        		}
        	else {
        		Toast.makeText(this, "Please enter a valid amount" + "\n" + "of atleast 1 Bitcoin", Toast.LENGTH_SHORT).show();
        	}
        	
		}
    	else {
    		Toast.makeText(this, "Please synchronise data", Toast.LENGTH_SHORT).show();
    	}
    }
    
	public void SyncData(View Sync){
		//Checks for a viable network connection
		if (HaveNetworkConnection()){
    		try {
				GetInfo();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
    		SetInfo();
    		Selected();
    		UpdateRates();
    		Toast.makeText(this, "Data Synchronised", Toast.LENGTH_SHORT).show();
    	}
    	else {
    		Toast.makeText(this, "No internet connection found", Toast.LENGTH_SHORT).show();
    	}    	
    }
    
    void SetInfo(){
    	//Set current data and time
    	String currentTimeString = new SimpleDateFormat("hh:mm a").format(new Date());
    	String currentDateString = new SimpleDateFormat("dd/mm/yy").format(new Date());
    	DateTime = "Rates last updated " + currentDateString + " at " + currentTimeString;
    	//save variables
    	SharedPreferences.Editor edit = Variables.edit(); //open file to edit
    	edit.putFloat("BTC", BTCSell);
    	edit.putFloat("GBP", GBPBuy);
    	edit.putFloat("USD", USDBuy);
    	edit.putFloat("EUR", EURBuy);
    	edit.putString("Date", DateTime);
    	edit.putString("Selected", SelectedCurrency);
    	edit.commit(); //commit new data    	
    }
    
   	public void onCheckedChanged(RadioGroup group, int checkedId) {
   		if (RadioGBPID == checkedId){
   			SelectedCurrency = "GBP";
    	}
    	else if (RadioUSDID == checkedId){
    		SelectedCurrency = "USD";
    	} 
    	else if (RadioEURID == checkedId){
    		SelectedCurrency = "EUR";
    	}
   		
   		//save variable
    	SharedPreferences.Editor edit = Variables.edit(); //open file to edit    	
    	edit.putString("Selected", SelectedCurrency);
    	edit.commit(); //commit new data 
    	
    	//Update Interface
    	Selected();
    	UpdateRates();    	  	
    }      
  
    void UpdateRates(){
    	//Update the text displays containing the rates
    	TextView BTCtv = (TextView)findViewById(R.id.BTCRate);
    	BTCtv.setText(Integer.toString((int)BTCSell));
    	TextView CurrencyBTCRatetv = (TextView)findViewById(R.id.CurrencyRate);
    	CurrencyBTCRatetv.setText(Integer.toString((int)SelectedBuy));
    	
    	//determine and set current rate    	
    	CurrentRate();
    	
    	//set time since last update
    	TextView Datetv = (TextView)findViewById(R.id.SyncDate);
    	Datetv.setText(DateTime);
    	
    	//set the currency explanation text by location
    	TextView Currencytv = (TextView)findViewById(R.id.Ratetxt);    
    	Currencytv.setText("Current " + SelectedCurrency + "/SLL rate: ");
    	TextView CurrencyRatetv = (TextView)findViewById(R.id.CurrencySelectedText);    
    	CurrencyRatetv.setText("Current BTC/" + SelectedCurrency + " rate: ");
    }
    
    public void CurrentRate(){
		float holder;
		holder = (BTCSell/SelectedBuy);
		//protects against first install error's before any data is synced due to 0/0 issue
		if (BTCSell+SelectedBuy==0){
			holder=0;
		}
		BigDecimal payment = new BigDecimal(holder);
		//Initialise variable as UK default
		NumberFormat n = NumberFormat.getCurrencyInstance(Locale.UK);;
		//load correct currency format base on location		
    	if (SelectedCurrency.equals("USD")){
    		n = NumberFormat.getCurrencyInstance(Locale.US);
    	} 
    	else if (SelectedCurrency.equals("EUR")){
    		n = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    	}	     
	    double doublePayment = payment.doubleValue();
	    String s = n.format(doublePayment);
	    TextView tv = (TextView)findViewById(R.id.CurrentRatetxt);
        tv.setText(s);
	}
    
    public static String widgetrate()
    {
    	float holder;
		holder = (BTCSell/SelectedBuy);
		//protects against first install error's before any data is synced due to 0/0 issue
		if (BTCSell+SelectedBuy==0){
			holder=0;
		}
		BigDecimal payment = new BigDecimal(holder);
		//Initialise variable as UK default
		NumberFormat n = NumberFormat.getCurrencyInstance(Locale.UK);;
		//load correct currency format base on location		
    	if (SelectedCurrency.equals("USD")){
    		n = NumberFormat.getCurrencyInstance(Locale.US);
    	} 
    	else if (SelectedCurrency.equals("EUR")){
    		n = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    	}	     
	    double doublePayment = payment.doubleValue();
	    String s = n.format(doublePayment);
	    return s;
    }
    
    
    void Calculate(){
    	//load the correct variable to selected currency
    	
    	//check if user has selected to include fees in calculations
    	CheckBox VirwoxFeeChk = (CheckBox) findViewById(R.id.VirwoxFeeChk);
    	if (VirwoxFeeChk.isChecked()) {
    		withFee();
    		Total = (int) SubTotal;
    	}
    	else {
    		float holder;
            holder = BTCSell*Amount;
            Total = (int)(holder/SelectedBuy);
    	}
    	
    	//check to see if user is calculating paypal fee for withdrawal 
    	CheckBox PaypalFeeChk = (CheckBox) findViewById(R.id.PaypalFeeChk);
    	if (PaypalFeeChk.isChecked()) {            
    		Total = Total - 1; 
    	}
    	
    	//print total
    	TextView tv = (TextView)findViewById(R.id.Outputtxt);
    	//choose correct currency symbol
    	if (SelectedCurrency.equals("GBP")){
    		tv.setText("Total: £" + Total + ".00");
    	}
    	else if (SelectedCurrency.equals("USD")){
    		tv.setText("Total: $" + Total + ".00");
    	} 
    	else if (SelectedCurrency.equals("EUR")){
    		tv.setText("Total: " + Total + ",00 €");
    	}    	
    }
    
    void withFee(){
    	float BTCAmount;
    	float CurrencyAmount;
    	float WithFee; 
    	float BitCurrent;
    	BTCAmount = Amount*BTCSell;
    	BitCurrent = (float) (BTCAmount * 0.99) - 50;
    	SubTotal = BitCurrent/SelectedBuy;
    	CurrencyAmount = (((int)SubTotal)*SelectedBuy);
    	WithFee = (float) ((CurrencyAmount*1.025)+50);
    	while (WithFee>BTCAmount){
    		SubTotal--;
    		CurrencyAmount = (((int)SubTotal)*SelectedBuy);
        	WithFee = (float) ((CurrencyAmount*1.025)+50);  
        	
        	if (WithFee == 50){
        		WithFee=0;
        	}
    	}
    }
    
    private boolean HaveNetworkConnection()
    {
        boolean HaveConnectedWifi = false;
        boolean HaveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo)
        {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    HaveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    HaveConnectedMobile = true;
        }
        return HaveConnectedWifi || HaveConnectedMobile;
    }
	
	public void GetInfo() throws ClientProtocolException, IOException{
		//retrieve data
		JSONObject jArray = new JSONObject();
		
		try {
			HttpClient httpclient = new DefaultHttpClient();
						
			HttpGet httpget = new HttpGet("http://api.virwox.com/api/json.php?method=getBestPrices&symbols[0]=BTC/SLL&symbols[1]=GBP/SLL&symbols[2]=USD/SLL&symbols[3]=EUR/SLL");
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			
			byte buffer[] = new byte[1024] ;
			InputStream is = entity.getContent() ;
			int numBytes = is.read(buffer) ;
			is.close();
			
			String entityContents = new String(buffer,0,numBytes) ;
			
			try {
				jArray = new JSONObject(entityContents);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{}
    	}
    	finally{}
    	//parse info
    	try{
        	
    		JSONArray  result = jArray.getJSONArray("result");
    		//parse BTC>SLL
    		JSONObject e = result.getJSONObject(0);
    		HashMap<String, String> map = new HashMap<String, String>();
    		map.put("sell", e.getString("bestBuyPrice"));    		
    		BTCSell = Float.valueOf(map.get("sell")).floatValue();
    		
    		//Parse SLL>GBP
    		e = result.getJSONObject(1);
    		map.put("buyGBP", e.getString("bestSellPrice"));
    		GBPBuy = Float.valueOf(map.get("buyGBP")).floatValue();
    		
    		//Parse SLL>USD
    		e = result.getJSONObject(2);
    		map.put("buyUSD", e.getString("bestSellPrice"));
    		USDBuy = Float.valueOf(map.get("buyUSD")).floatValue();
    		
    		//Parse SLL>EUR
    		e = result.getJSONObject(3);
    		map.put("buyEUR", e.getString("bestSellPrice"));
    		EURBuy = Float.valueOf(map.get("buyEUR")).floatValue();
        }catch(JSONException e)        {
        	 Log.e("log_tag", "Error parsing data "+e.toString());
        }
    	
    	
	}
	
	public void TextClick(View TextClick){ //clear text box when user clicks to enter an amount
		EditText ClearText = (EditText)findViewById(R.id.Amountinputtxt);
    	ClearText.setText("");
	}
}