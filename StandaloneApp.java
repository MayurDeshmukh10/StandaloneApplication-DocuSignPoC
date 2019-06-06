/*
Standalone Java Application for authentication and sending document for signature using DocuSign
*/

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.*;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import com.sun.jersey.core.util.Base64;   //For converting document data into base64 encoding

import javax.net.ssl.HttpsURLConnection;

public class StandaloneApp {

    public static void main(String[] args) throws Exception
    {
        StandaloneApp http = new StandaloneApp();

        http.sendPost();


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
  
    private void sendPost() throws Exception
    {
        
        FileReader reader = new FileReader("application.properties");
        Properties p = new Properties();
        p.load(reader);
        String document_name = p.getProperty("document_name");
        byte[] buffer = readFile(document_name);
        String docBase64 = new String(Base64.encode(buffer)); //Base64 encoding
        String docBase64json = "\""+docBase64+"\"";
        String document_type = "\""+p.getProperty("document_type")+"\"";
        String email = "\""+p.getProperty("signers_emailID").toString()+"\""; 
        String name ="\""+ p.getProperty("signers_name").toString()+"\"";
        String authorization_token = "Bearer "+p.getProperty("authorization_token").toString();

        String url = "https://demo.docusign.net/restapi/v2/accounts/8479610/envelopes";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //HTTP request Header
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept","application/json");
        con.setRequestProperty("Accept-Language","en-US");
        con.setRequestProperty("Cache-Control","no-cache");
        con.setRequestProperty("Origin","https://apiexplorer.docusign.com");
        con.setRequestProperty("Referer","https://apiexplorer.docusign.com/");
        con.setRequestProperty("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/74.0.3729.169 Chrome/74.0.3729.169 Safari/537.36");
        con.setRequestProperty("Authorization",authorization_token);
        con.setRequestProperty("Content-Length","52001");
        con.setRequestProperty("Content-Type","application/json");

        //HTTP request data in JSON
        String jsonInputString = "{\"documents\": [{ \"documentBase64\":"+docBase64json+",\"documentId\": \"2\",\"fileExtension\":"+document_type+",\"name\": \"Document\"}],\"emailSubject\": \"Sign Document\",\"recipients\": {\"signers\": [{\"email\":"+email+",\"name\":"+name+",\"recipientId\": \"1\"}]},\"status\": \"sent\" }";

        //For writing data using OutputStream of HTTP
        //System.out.println(jsonInputString);
        con.setDoOutput(true);
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);           
         }
         //For Reading HTTP response
	System.out.println("HTTP RESPONSE : ");
        try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        System.out.println(response.toString());
	System.out.println();
	System.out.println("SUCCESSFULLY AUTHENTICATED AND DOCUMENT SEND FOR SIGNATURE");
        }

    }
}
