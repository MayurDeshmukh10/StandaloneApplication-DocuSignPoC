/*
Standalone Java Application for authentication and sending document for signature using DocuSign
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Base64; 
import java.net.HttpURLConnection;
import java.net.URL;


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
        String integration_key = "\""+p.getProperty("integration_key")+"\"";
        return integration_key;

    }


    private void setRequestHeader(HttpURLConnection con) throws Exception{

        String integration_key = getIntegrationKey();
        con.setRequestProperty("Accept","application/json");
        con.setRequestProperty("Accept-Language","en-US");
        con.setRequestProperty("Cache-Control","no-cache");
        con.setRequestProperty("Origin","https://apiexplorer.docusign.com");
        con.setRequestProperty("Referer","https://apiexplorer.docusign.com/");
        con.setRequestProperty("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/74.0.3729.169 Chrome/74.0.3729.169 Safari/537.36");
        con.setRequestProperty("X-DocuSign-Authentication","{ \"Username\":"+username+", \"Password\":"+password+", \"IntegratorKey\":"+integration_key+" }");
        con.setRequestProperty("Content-Type","application/json");
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
        //String docBase64 = new String(Base64.encode(buffer)); //Base64 encoding
        String docBase64json = "\""+docBase64+"\"";
        String document_type = "\""+doc_type+"\"";
        String email = "\""+signer_email+"\""; 
        String name ="\""+signer_name+"\"";
        

        String url = "https://demo.docusign.net/restapi/v2/accounts/"+accountid+"/envelopes";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //HTTP request Header
        con.setRequestMethod("POST");
        setRequestHeader(con);

        //HTTP request data in JSON
        String jsonInputString = "{\"documents\": [{ \"documentBase64\":"+docBase64json+",\"documentId\": \"2\",\"fileExtension\":"+document_type+",\"name\": \"Document\"}],\"emailSubject\": \"Sign Document\",\"recipients\": {\"signers\": [{\"email\":"+email+",\"name\":"+name+",\"recipientId\": \"1\"}]},\"status\": \"sent\" }";

        //For writing data using OutputStream of HTTP
        con.setDoOutput(true);
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);           
         }
         //For Reading HTTP response
	    displayHTTPResponse(con);

    }
    private void authenticate() throws Exception{

        java.io.Console console = System.console();
        String usern = console.readLine("Enter Username : ");
        String passw = new String(console.readPassword("Enter Password : "));
        username = "\""+usern+"\"";
        password = "\""+passw+"\"";
        String account_id = "";
        String url = "https://demo.docusign.net/restapi/v2/login_information?login_settings=all";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //HTTP request Header
        con.setRequestMethod("GET");
        setRequestHeader(con);

         //For Reading HTTP response
        account_id = displayHTTPResponse(con);
        account_id = account_id.substring(14);
        account_id = account_id.substring(0, account_id.length()-1);
        account_id = account_id.substring(0, account_id.length()-1);
        accountid = account_id;
	    System.out.println();
        System.out.println("Authentication Successful");
        System.out.println("Your Account ID : "+account_id);
        
    }
}
