/*
Standalone Java Application for authentication and sending document for signature using DocuSign
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Base64; 
import java.net.HttpURLConnection;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;


import javax.net.ssl.HttpsURLConnection;


public class StandaloneApp {

	String accountid = "";
	String username = "";
	String password = "";

	public static void main(String[] args) throws Exception
	{
		StandaloneApp http = new StandaloneApp();
		try{
			http.authenticate();
			http.sendPost();
		}
		catch(Exception e){
			System.out.println("Invalid Password or Username");
		}

	}


    //For reading data from any file - pdf,doc,txt,etc
	private byte[] readFile(String path) throws IOException {
		InputStream is = StandaloneApp.class.getResourceAsStream("/" + path);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[1024];
		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		return buffer.toByteArray();
	} 



	private String getIntegrationKey() throws Exception
	{
		FileReader reader = new FileReader("application.properties");
		Properties p = new Properties();
		p.load(reader);
		String integration_key = p.getProperty("integration_key");
		return integration_key;

	}

	private String DocuSignAuthParameter(String integration_key)
	{
		JsonObject json = Json.createObjectBuilder()
			.add("Username",username)
			.add("Password",password)
			.add("IntegratorKey",integration_key)
			.build();

		return json.toString();
	}


	private void setRequestHeader(HttpURLConnection con) throws Exception{

		String integration_key = getIntegrationKey();
		String docuSignAuthParameter = DocuSignAuthParameter(integration_key);
		con.setRequestProperty("Accept","application/json");
		con.setRequestProperty("Accept-Language","en-US");
		con.setRequestProperty("Cache-Control","no-cache");
		con.setRequestProperty("Origin","https://apiexplorer.docusign.com");
		con.setRequestProperty("Referer","https://apiexplorer.docusign.com/");
		con.setRequestProperty("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/74.0.3729.169 Chrome/74.0.3729.169 Safari/537.36");
		con.setRequestProperty("X-DocuSign-Authentication",docuSignAuthParameter);
		con.setRequestProperty("Content-Type","application/json");
	}


	private String displayHTTPResponse(HttpURLConnection con) throws Exception{

		int count1 =0;
		String account_id="";
		System.out.println("HTTP RESPONSE : ");
		try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				count1++;
				if(count1 == 5 )
				{
					account_id = responseLine.trim().toString();
            
				}
				response.append(responseLine.trim());
			}
			System.out.println(response.toString());
			return account_id;
		}

	}

	private String prepareDocuSignEnvelopeBodyContent(String docBase64json,String document_type,String name,String email)
	{
		JsonObject json1 = Json.createObjectBuilder()
			.add("documentBase64",docBase64json)
			.add("documentId","2")
			.add("fileExtension",document_type)
			.add("name","Document")
			.build();

		JsonObject json2 = Json.createObjectBuilder()
			.add("email",email)
			.add("name",name)
			.add("recipientId","1")
			.build();

		JsonArray jsonArray1 = Json.createArrayBuilder()
			.add(json1)
			.build();

		JsonArray jsonArray2 = Json.createArrayBuilder()
			.add(json2)
			.build();

		JsonObject json = Json.createObjectBuilder()

			.add("documents",jsonArray1)
			.add("emailSubject","Sign the Document")
			.add("recipients",Json.createObjectBuilder()
				.add("signers",jsonArray2)
				.build()
			)
			.add("status","sent")
			.build();

		String resultJsonString = json.toString();
		return resultJsonString;
	}

	private void sendPost() throws Exception{

		java.io.Console console = System.console();
		System.out.println();
		String doc_name = console.readLine("Enter Document Name : ");
		String doc_type = console.readLine("Enter Document Type/Extension : ");
		String signer_email = console.readLine("Enter Signer's Registered Email ID : ");
		String signer_name = console.readLine("Enter Signer's Name : ");
		Base64.Encoder encoder = Base64.getEncoder(); ;
		byte[] buffer = readFile(doc_name);
		String docBase64 = new String(encoder.encodeToString(buffer));
		String url = "https://demo.docusign.net/restapi/v2/accounts/"+accountid+"/envelopes";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();		
		con.setRequestMethod("POST");		//HTTP request Header
		setRequestHeader(con);	
		String jsonInputString = prepareDocuSignEnvelopeBodyContent(docBase64, doc_type,signer_name, signer_email);		//HTTP request data in JSON
		con.setDoOutput(true);							//For writing data using OutputStream of HTTP
		try(OutputStream os = con.getOutputStream()) 
		{
			byte[] input = jsonInputString.getBytes("utf-8");
			os.write(input, 0, input.length);           
		}
        catch(Exception e)
		{
			System.out.println("Error");
		}
		displayHTTPResponse(con);				//For Reading HTTP response

	}
	private void authenticate() throws Exception{

		java.io.Console console = System.console();
		String usern = console.readLine("Enter Username : ");
		String passw = new String(console.readPassword("Enter Password : "));
		username = usern;
		password = passw;
		String account_id = "";
		String url = "https://demo.docusign.net/restapi/v2/login_information?login_settings=all";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");												//HTTP request Header
		setRequestHeader(con);
		account_id = displayHTTPResponse(con);										//For Reading HTTP response
		account_id = account_id.substring(14);
		account_id = account_id.substring(0, account_id.length()-1);
		account_id = account_id.substring(0, account_id.length()-1);
		accountid = account_id;
		System.out.println();
		System.out.println("Authentication Successful");
		System.out.println("Your Account ID : "+account_id);    
	}
}
