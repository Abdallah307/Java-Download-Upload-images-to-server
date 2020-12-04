package sample;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
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
   // @FXML
    //Image downloadImage;
    @FXML
    Button downloadButton;
    @FXML
    Button chooseUpload;
    //@FXML
    //Image uploadImage;
    @FXML
    Button uploadButton;
    @FXML
    ImageView imageview;



    private String dataStr="";
    private ArrayList<String> myUrls = new ArrayList<>();
    private String contentStr = "application/x-www-form-urlencoded";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chooseCombo.getSelectionModel().selectFirst();
        getDownloadableImages();

    }

    public void getDownloadableImages(){
        OutputStream os;
        InputStream is;
        try {
            URL myURL = new URL("http://localhost:8000/Network2Server_war_exploded/retrieve");
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
                chooseCombo.getItems().add(ff);
            }

        }catch (Exception e){
            System.out.println(e.toString());

        }
    }

    public void login(ActionEvent event) {
        dataStr="";
        OutputStream os;
        InputStream is;

        String username = userName.getText();
        String pass = password.getText();
        String authStringEnc = encodeCredentials(username, pass);

        try {
            URL myURL = new URL("http://localhost:8000/Network2Server_war_exploded/login");
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

            if(SS.intern() == "Authorized\n"){
                loginScene.setVisible(false);
                appScene.setVisible(true);
            }
            else{
                Alert alertInformation = new Alert(Alert.AlertType.INFORMATION);
                alertInformation.setTitle("Login");
                alertInformation.setHeaderText("Status");
                alertInformation.setContentText("Failed");
                alertInformation.showAndWait();
            }

        }catch (Exception e){
            System.out.println(e.toString());

        }

    }

    public void downloadImageFromServer() throws IOException {
        String fileName = "";
        fileName = chooseCombo.getSelectionModel().getSelectedItem().toString();

        try{
            //String fileName = "im.jpg";
            String website = "http://localhost:8000/Network2Server_war_exploded/download";
            InputStream inputStream;
            URL url = new URL(website);
            URLConnection myConn = url.openConnection();
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
            FileInputStream ins = new FileInputStream("/home/abdallah/IdeaProjects/JavaClientNetwork2/"+fileName);
            Image image = new Image(ins);
            imageview.setImage(image);



        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public void uploadImageToServer(ActionEvent event) throws IOException {
        URL myURL = new URL("http://localhost:8000/Network2Server_war_exploded/upload");
        URLConnection myConn = myURL.openConnection();
        myConn.setDoOutput(true);
        myConn.setDoInput(true);
        myConn.setRequestProperty ("Content-Type", contentStr);
        myConn.setUseCaches(false);

        BufferedImage img = ImageIO.read(new File("/home/abdallah/IdeaProjects/JavaClientNetwork2/im3.jpg"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        baos.flush();

        byte[] bytes = baos.toByteArray();
        baos.close();
        BufferedOutputStream out = new BufferedOutputStream(myConn.getOutputStream());

        out.write(bytes);
        out.close();
    }



    public String encodeCredentials(String username, String pass){
        String authString = username + ":" + pass;
        System.out.println("auth string: " + authString);
        byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        return authStringEnc;
    }





}
