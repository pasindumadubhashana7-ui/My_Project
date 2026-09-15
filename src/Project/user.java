package Project;

import java.util.Scanner;

public abstract class user{
    private String ID;
    private String fname;
    private String lname;
    private String email;
    private String phone;
    private String password;

public user(){};

public String getId(){
    return ID;
}
public void setId(String iD2){
this.ID=iD2;
}
public String getfname(){
    return fname;
}
public void setfname(String fname){
    this.fname=fname;
}
public String getlname(){
    return lname;
}
public void setlname(String lname){
    this.lname=lname;
}
public String getemail(){
    return email;
}
public void setemail(String email){
    this.email=email;
}
public String getphone(){
    return phone;
}
public void setphone(String phone){
    this.phone=phone;
}
public String getpassword(){
    return password;
}
public void setpassword(String password){
    this.password=password;
}

public abstract void showList(database databse, Scanner s);

public void setId(int iD2) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setId'");
}

protected abstract void setId1(String iD2);

}