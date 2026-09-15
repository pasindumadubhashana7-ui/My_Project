package Project;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class rent {
    private int ID;
    private user user;
    private car car;
    private LocalDateTime datetime;
    private int hours;
    private double total;
    private String status;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public rent(){
        datetime = LocalDateTime.now();
    }
    public int getID(){
        return ID;
    }
    public void setID(int ID){
        this.ID = ID;
    }
    public user getUser(){
        return user;
    }
    public void setUser (user user){
        this.user = user;
    }
    public car getCar(){
        return car;
    }
    public void setCar (car car){
        this.car = car;
    }
    public String getDatetime(){
        return formatter.format(datetime);
    }
    public void setDateTime(String datetimeString){
        this.datetime=LocalDateTime.parse(datetimeString, formatter);
    }
    public int getHours(){
        return hours;
    }
    public void setHours(int hours){
        this.hours = hours;
    }
    public double getTotal(){
        return total;
    }
    public void setTotal(double total){
        this.total = total;
    }
    public String getstatus(){
        return status;
    }
    public void setstatus(String status){
        this.status = status;
    }
}
