package sample;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class MultipartUtility {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter outputWriter;


    public MultipartUtility(String requestURL, String charset)
            throws IOException {
        this.charset = charset;
        boundary = "===" + System.currentTimeMillis() + "===";
        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);

        outputStream = httpConn.getOutputStream();
        outputWriter = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
    }



    public void addFilePart(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        outputWriter.append("--" + boundary).append("\r\n");
        outputWriter.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append("\r\n");
        outputWriter.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append("\r\n");
        outputWriter.append("Content-Transfer-Encoding: binary").append("\r\n");
        outputWriter.append("\r\n");
        outputWriter.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();
        outputWriter.append("\r\n");
        outputWriter.flush();
    }


    public List<String> finish() throws IOException {
        List<String> response = new ArrayList<String>();

        outputWriter.append(LINE_FEED).flush();
        outputWriter.append("--" + boundary + "--").append(LINE_FEED);
        outputWriter.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            throw new IOException("status is not ok from server " + status);
        }

        return response;
    }
}