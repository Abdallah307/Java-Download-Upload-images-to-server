package sample;
import javafx.beans.value.ObservableValue;
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
import java.awt.image.*;
import javax.imageio.*;



public class Controller implements Initializable {

    @FXML
    AnchorPane loginScene;
    @FXML
    TextField userName;
    @FXML
    TextField password;
    @FXML
    Button loginButton;
    @FXML
    AnchorPane appScene;
    @FXML
    ChoiceBox chooseCombo;

    @FXML
    Button downloadButton;
    @FXML
    Button chooseUpload;
    @FXML
    ImageView uploadImage;
    @FXML
    Button uploadButton;
    @FXML
    ImageView imageview;
    @FXML
    RadioButton phpRadio;
    @FXML
    RadioButton servletRadio;
    @FXML
    Button logoutButton;



    private File chosenImage;
    private String dataStr="";
    private ArrayList<String> myUrls = new ArrayList<>();
    private String contentStr = "application/x-www-form-urlencoded";
    private ToggleGroup group;
    private URL myURL;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chooseCombo.getSelectionModel().selectFirst();


        group = new ToggleGroup();
        phpRadio.setToggleGroup(group);
        servletRadio.setToggleGroup(group);

        phpRadio.setUserData("php");
        servletRadio.setUserData("servlet");
        servletRadio.setSelected(true);



    }

    public void getDownloadableImages(){
        OutputStream os;
        InputStream is;
        try {
            if(group.getSelectedToggle().getUserData().equals("servlet")){
                myURL = new URL("http://localhost:8000/Network2Server_war_exploded/retrieve");

            }
            else{
                myURL = new URL("http://localhost/downloadable.php");
            }
            URLConnection myConn = myURL.openConnection();
            myConn.setDoOutput(true);
            myConn.setDoInput(true);
            myConn.setRequestProperty ("Content-Type", contentStr);
            myConn.setUseCaches(false);


            dataStr = "T1=67&T2=88";
            BufferedOutputStream out = new BufferedOutputStream(myConn.getOutputStream());

            out.write(dataStr.getBytes());
            out.close();
            String  SS="";
            int b=-1;


            is =  myConn.getInputStream();
            while ((b = is.read())!= -1){
                if ((char)b =='\r') SS +="\n";
                else SS =SS+ (char) b;;
            }
            String newS = SS.substring(1,SS.length()-2);
            String availableImages[] = newS.split(",");
            for(String ff : availableImages){
                if(ff!="")
                chooseCombo.getItems().add(ff);
            }

        }catch (Exception e){
            System.out.println(e.toString());

        }
    }

    public void login(ActionEvent event) {

        OutputStream os;
        InputStream is;
        System.out.println(group.getSelectedToggle().getUserData());
        boolean isPHP = false;
        String username = userName.getText();
        String pass = password.getText();
        String authStringEnc = encodeCredentials(username, pass);
        dataStr="username="+username+"&password="+pass;

        try {
            if(group.getSelectedToggle().getUserData().equals("servlet")){
                myURL = new URL("http://localhost:8000/Network2Server_war_exploded/login");
                isPHP = false;
            }
            else{
                myURL = new URL("http://localhost/login.php");
                isPHP = true;
            }
            URLConnection myConn = myURL.openConnection();
            myConn.setRequestProperty("Authorization", "Basic " + authStringEnc);
            myConn.setDoOutput(true);
            myConn.setDoInput(true);
            myConn.setRequestProperty ("Content-Type", contentStr);
            myConn.setUseCaches(false);

            

            BufferedOutputStream out = new BufferedOutputStream(myConn.getOutputStream());

            out.write(dataStr.getBytes());
            out.close();
            String  SS="";
            int b=-1;

            String t ="";
            is =  myConn.getInputStream();
            while ((b = is.read())!= -1){
                if ((char)b =='\r') SS +="\n";
                else SS =SS+ (char) b;;
            }
            System.out.println(SS);

            if(!isPHP){
                if(SS.intern() == "Authorized\n"){
                    loginScene.setVisible(false);
                    appScene.setVisible(true);
                    getDownloadableImages();
                }
                else{
                    Alert alertInformation = new Alert(Alert.AlertType.INFORMATION);
                    alertInformation.setTitle("Login");
                    alertInformation.setHeaderText("Status");
                    alertInformation.setContentText("Failed");
                    alertInformation.showAndWait();
                }
            }
            else{
                if(SS.intern() == "Authorized"){
                    loginScene.setVisible(false);
                    appScene.setVisible(true);
                    getDownloadableImages();
                }
                else{
                    Alert alertInformation = new Alert(Alert.AlertType.INFORMATION);
                    alertInformation.setTitle("Login");
                    alertInformation.setHeaderText("Status");
                    alertInformation.setContentText("Failed");
                    alertInformation.showAndWait();
                }
            }


        }catch (Exception e){
            System.out.println(e.toString());

        }

    }

    public void downloadImageFromServer() throws IOException {
        String fileName = "";
        fileName = chooseCombo.getSelectionModel().getSelectedItem().toString();
        dataStr="file="+fileName;

        try{
            InputStream inputStream;
            if(group.getSelectedToggle().getUserData().equals("servlet")){
                myURL = new URL("http://localhost:8000/Network2Server_war_exploded/download");
            }
            else{
                myURL = new URL("http://localhost/download.php");
            }

            URLConnection myConn = myURL.openConnection();
            myConn.setDoOutput(true);
            myConn.setDoInput(true);
            myConn.setRequestProperty("body", fileName);
            myConn.setRequestProperty ("Content-Type", contentStr);
            BufferedOutputStream out = new BufferedOutputStream(myConn.getOutputStream());
            out.write(dataStr.getBytes());
            out.close();

            OutputStream outputStream = new FileOutputStream(fileName);
            byte[] buffer = new byte[2048];
            int length = 0;
            inputStream = myConn.getInputStream();
            while ((length = inputStream.read(buffer)) != -1) {
                //System.out.println("Buffer Read of length: " + length);
                outputStream.write(buffer, 0, length);
            }
            Alert alertInformation = new Alert(Alert.AlertType.INFORMATION);
            alertInformation.setTitle("Download image");
            alertInformation.setHeaderText("Status");
            alertInformation.setContentText("Downloaded Successfully");
            alertInformation.showAndWait();


            inputStream.close();
            outputStream.close();
            FileInputStream ins = new FileInputStream("/home/abdallah/Desktop/network2Client/"+fileName);
            Image image = new Image(ins);
            imageview.setImage(image);



        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public void uploadImageToServer(ActionEvent event) throws IOException {
        String charset = "UTF-8";
        File uploadFile1 = chosenImage;
        String requestURL;

        if(group.getSelectedToggle().getUserData().equals("servlet")){
            requestURL = "http://localhost:8000/Network2Server_war_exploded/upload";
        }
        else{
            requestURL = "http://localhost/up.php";
        }


        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, charset);
            multipart.addFilePart("fileToUpload", uploadFile1);
            List<String> response = multipart.finish();
            System.out.println("SERVER REPLIED:");
            for (String line : response) {
                System.out.println(line);
            }
        } catch (IOException ex) {
            System.err.println(ex);

        }
    }

    public void fileChooserUpload(ActionEvent event) throws FileNotFoundException {
         chosenImage = getChosenImage();
    }

    public File getChosenImage() throws FileNotFoundException {
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
        System.out.println("auth string: " + authString);
        byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        return authStringEnc;
    }





}
