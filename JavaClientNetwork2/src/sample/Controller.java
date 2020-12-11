package sample;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;




public class Controller implements Initializable {

    @FXML AnchorPane loginScene;
    @FXML TextField userName;
    @FXML PasswordField password;
    @FXML Button loginButton;
    @FXML AnchorPane appScene;
    @FXML ChoiceBox chooseCombo;

    @FXML Button downloadButton;
    @FXML Button chooseUpload;
    @FXML ImageView uploadImage;
    @FXML Button uploadButton;
    @FXML ImageView imageview;
    @FXML RadioButton phpRadio;
    @FXML RadioButton servletRadio;
    @FXML Button logoutButton;



    private File chosenImage;
    private String dataStr="";
    private ArrayList<String> myUrls = new ArrayList<>();
    private String contentStr = "application/x-www-form-urlencoded";
    private ToggleGroup group;
    private URL myURL;
    private String alertContent;
    private String boundary;
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter outputWriter;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chooseCombo.getSelectionModel().selectFirst();
        configureRadioButtons();
        alertContent = "Login failed please enter correct credentials!";
    }

    public void configureRadioButtons(){
        group = new ToggleGroup();
        phpRadio.setToggleGroup(group);
        servletRadio.setToggleGroup(group);

        phpRadio.setUserData("php");
        servletRadio.setUserData("servlet");
        servletRadio.setSelected(true);
    }

    public URLConnection establishConnection(URL url) throws IOException{
        URLConnection myConn = url.openConnection();
        myConn.setDoOutput(true);
        myConn.setDoInput(true);
        myConn.setRequestProperty ("Content-Type", contentStr);
        myConn.setUseCaches(false);
        return myConn;
    }

    public void addDownloadableImages(String response){
        String newS = response.substring(1,response.length()-2);
        String availableImages[] = newS.split(",");
        for(String ff : availableImages){
            if(ff!="")
                chooseCombo.getItems().add(ff);
        }
    }

    public void getDownloadableImages(){
        InputStream is;
        boolean isServlet = group.getSelectedToggle().getUserData().equals("servlet");

        try {
            if(isServlet){
                myURL = new URL("http://localhost:8000/Network2Server_war_exploded/retrieve");
            }
            else{
                myURL = new URL("http://localhost/downloadable.php");
            }

            URLConnection myConn = establishConnection(myURL);
            dataStr = "dummy=67";

            BufferedOutputStream out = new BufferedOutputStream(myConn.getOutputStream());
            out.write(dataStr.getBytes());
            out.close();
            String  response="";
            int b=-1;

            is =  myConn.getInputStream();
            while ((b = is.read())!= -1){
                if ((char)b =='\r') response +="\n";
                else response =response+ (char) b;;
            }

            addDownloadableImages(response);


        }catch (Exception e){
            System.out.println(e.toString());
        }
    }

    public void showAlert(String content,String headerText){
        Alert alertInformation = new Alert(Alert.AlertType.INFORMATION);
        alertInformation.setTitle("Information");
        alertInformation.setHeaderText(headerText);
        alertInformation.setContentText(content);
        alertInformation.showAndWait();
    }

    public void authorizeLogin(String response, boolean isPHP){
        if(!isPHP){
            if(response.intern() == "Authorized\n"){
                loginScene.setVisible(false);
                appScene.setVisible(true);
                getDownloadableImages();
            }
            else{
                String headerText = "Servlet Server";
                showAlert(alertContent, headerText);
            }
        }
        else{
            if(response.intern() == "Authorized"){
                loginScene.setVisible(false);
                appScene.setVisible(true);
                getDownloadableImages();
            }
            else{
                String headerText = "PHP Server";
                showAlert(alertContent, headerText);
            }
        }
    }

    public void login(ActionEvent event) {

        InputStream is;
        boolean isPHP = false;
        boolean isServlet = group.getSelectedToggle().getUserData().equals("servlet");
        String username = userName.getText();
        String pass = password.getText();
        String encryptedAuthenticationString = encodeCredentials(username, pass);
        dataStr="username="+username+"&password="+pass;

        try {
            if(isServlet){
                myURL = new URL("http://localhost:8000/Network2Server_war_exploded/login");
                isPHP = false;
            }
            else{
                myURL = new URL("http://localhost/login.php");
                isPHP = true;
            }
            URLConnection myConn = establishConnection(myURL);
            myConn.setRequestProperty("Authorization", "Basic " + encryptedAuthenticationString);

            BufferedOutputStream out = new BufferedOutputStream(myConn.getOutputStream());

            out.write(dataStr.getBytes());
            out.close();
            String  response="";
            int b=-1;

            String t ="";
            is =  myConn.getInputStream();
            while ((b = is.read())!= -1){
                if ((char)b =='\r') response +="\n";
                else response =response+ (char) b;;
            }

            authorizeLogin(response, isPHP);

        }catch (Exception e){
            showAlert("Check if the server is running", "Server Error");
        }

    }

    public void showDownloadedImage(String fileName){
        try{
            FileInputStream ins = new FileInputStream("/home/abdallah/Desktop/network2Client/"+fileName);
            Image image = new Image(ins);
            imageview.setImage(image);
        }
        catch (FileNotFoundException fne){
            showAlert("Can't show image not found","Not Found");
        }

    }

    public void downloadImageFromServer() throws IOException {

        try{
            String fileName = "";
            fileName = chooseCombo.getSelectionModel().getSelectedItem().toString();
            dataStr="file="+fileName;
            boolean isServlet = group.getSelectedToggle().getUserData().equals("servlet");
            InputStream inputStream;
            if(isServlet){
                myURL = new URL("http://localhost:8000/Network2Server_war_exploded/download");
            }
            else{
                myURL = new URL("http://localhost/download.php");
            }

            URLConnection myConn = establishConnection(myURL);
            myConn.setRequestProperty("body", fileName);
            BufferedOutputStream out = new BufferedOutputStream(myConn.getOutputStream());
            out.write(dataStr.getBytes());
            out.close();

            OutputStream outputStream = new FileOutputStream(fileName);
            byte[] buffer = new byte[2048];
            int length = 0;
            inputStream = myConn.getInputStream();
            while ((length = inputStream.read(buffer)) != -1) {

                outputStream.write(buffer, 0, length);
            }

            Alert alertInformation = new Alert(Alert.AlertType.INFORMATION);
            alertInformation.setTitle("Download image");
            alertInformation.setHeaderText("Status");
            alertInformation.setContentText("Downloaded Successfully");
            alertInformation.showAndWait();


            inputStream.close();
            outputStream.close();

            showDownloadedImage(fileName);

        }
        catch (ConnectException connectionException ){
            showAlert("Please Check if the server is running","Server Error");
        }
        catch(Exception e) {
            showAlert("Please choose an image to download","Not Chosen");
        }
    }

    public void setupUploadConnection(String Url) throws IOException {
        this.charset = "UTF-8";
        boundary = "===" + System.currentTimeMillis() + "===";
        URL url = new URL(Url);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

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

        outputWriter.append("\r\n").flush();
        outputWriter.append("--" + boundary + "--").append("\r\n");
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

    public void uploadImageToServer(ActionEvent event) throws IOException {
        File uploadFile1 = chosenImage;
        String url;
        boolean isServlet = group.getSelectedToggle().getUserData().equals("servlet");

        if(isServlet){
            url = "http://localhost:8000/Network2Server_war_exploded/upload";
        }
        else{
            url = "http://localhost/up.php";
        }


        try {
            setupUploadConnection(url);
            addFilePart("fileToUpload", uploadFile1);
            List<String> response = finish();
            showAlert(response.get(0),"Uploaded Successfully");
            getDownloadableImages();
        } catch (IOException ex) {
            showAlert("Please Check if the server is running","Server Error");

        }
    }

    public void fileChooserUpload(ActionEvent event) throws FileNotFoundException {
        try{
            chosenImage = getChosenImage();
        }
        catch (Exception fne){
            showAlert("You do not choose any file", "Not Chosen");
        }


    }

    public File getChosenImage() throws FileNotFoundException{

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png")
                );
        File file = fileChooser.showOpenDialog(null);
        FileInputStream fos = new FileInputStream(file);
        Image im = new Image(fos);
        uploadImage.setImage(im);
        return file;

    }

    public void logout(ActionEvent e){
        chooseCombo.getItems().clear();
        appScene.setVisible(false);
        loginScene.setVisible(true);
    }




    public String encodeCredentials(String username, String pass){
        String authString = username + ":" + pass;
        byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        return authStringEnc;
    }





}
