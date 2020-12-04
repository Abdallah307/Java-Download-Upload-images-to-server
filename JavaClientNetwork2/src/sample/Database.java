package sample;

import java.sql.*;
class Database{
    private Connection connection;

       Database(){
           try {
               Class.forName("com.mysql.jdbc.Driver");
               connection=DriverManager.getConnection("jdbc:mysql://localhost:3306/network2","","");
           }
           catch(Exception e){
               System.out.println(e);
           }


       }

       public void checkCredentials(String username, String password){
           try{

               Statement stmt=connection.createStatement();
               ResultSet rs=stmt.executeQuery("select * from users");
               while(rs.next()){

                   System.out.println(rs.getString(1)+"  "+rs.getString(2));

               }
               connection.close();
           }
           catch(Exception e){
               System.out.println(e);
           }
       }

}