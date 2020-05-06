/**
 * @author: Ishan Parikh
 * Notes about the program:
 * ^This is a cruise control program used to simulate the idea of cruise control^
 *
 * all velocities are positive doubles up to the tenths place and recorded in MPH (miles per hour)
 * The GraphScene includes options to alter P, I, and D values used in self regulation, the self-regulation parameters I've chosen are P: 1.0, I: 0, D: 0.2
 * P, I and D modifiers increase and decrease by a factor of 0.1 and the min values are 0 and max values are 100 (for limitations sake)
 *
 **/
package sample.FXMLControllers;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class GraphScene implements Initializable {
    @FXML protected Label Curr_Veloc;
    @FXML protected Spinner<Double> Aim_Veloc = new Spinner<>();
    @FXML protected Spinner<Double> P = new Spinner<>();
    @FXML protected Spinner<Double> I = new Spinner<>();
    @FXML protected Spinner<Double> D = new Spinner<>();

    Car Hyundai_lite = new Car(1.0, 1.0);
    //Pid vars
    protected double KP = 1;
    protected double KI = 1;
    protected double KD = 1;
    private final int timeChange = 1000;
    private double prevError = 0; //starts at zero, therefore
    private double accumError = 0; //there is no error at the start
    private
    /**
     * @Param KP, KI, KD are coefficients used in the PID equation
     * These are the params used in PID modification
     * timeChange is delta t, or the rate of change of time, this is static, but you can change it if wished
     **/

    Timer sentinel = new Timer();
    TimerTask sent = new TimerTask() {
        @Override
        public void run() {
            if(!pvEqualsSP()){
                updatePID();
            }
            Hyundai_lite.accel_move();
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Curr_Veloc.setText("" + (Math.round(Hyundai_lite.getCurrentVeloc()*10)/10.0));
        SpinnerValueFactory<Double> PID= new SpinnerValueFactory<Double>() {
            @Override
            public void decrement(int steps) {
                for(int i = 0; i < steps; i++)
                    if(!(getValue() <= 0)) setValue(Math.round((getValue()-0.1)*10)/10.0);
            }

            @Override
            public void increment(int steps) {
                for(int i = 0; i<steps; i++)
                    if(!(getValue() >= 100)) setValue(Math.round((getValue()+0.1)*10)/10.0);
            }
        };
        PID.setValue(1.0);
        SpinnerValueFactory<Double> PJD= new SpinnerValueFactory<Double>() {
            @Override
            public void decrement(int steps) {
                for(int i = 0; i < steps; i++)
                    if(!(getValue() <= 0)) setValue(Math.round((getValue()-0.1)*10)/10.0);
            }

            @Override
            public void increment(int steps) {
                for(int i = 0; i<steps; i++)
                    if(!(getValue() >= 100)) setValue(Math.round((getValue()+0.1)*10)/10.0);
            }
        };
        PJD.setValue(0.3);
        SpinnerValueFactory<Double> PKD= new SpinnerValueFactory<Double>() {
            @Override
            public void decrement(int steps) {
                for(int i = 0; i < steps; i++)
                    if(!(getValue() <= 0)) setValue(Math.round((getValue()-0.1)*10)/10.0);
            }

            @Override
            public void increment(int steps) {
                for(int i = 0; i<steps; i++)
                    if(!(getValue() >= 100)) setValue(Math.round((getValue()+0.1)*10)/10.0);
            }
        };
        PKD.setValue(0.2);
        P.setValueFactory(PID);
        I.setValueFactory(PJD);
        D.setValueFactory(PKD);
        SpinnerValueFactory<Double> factory = new SpinnerValueFactory<Double>() {

            @Override
            public void decrement(int steps) {
                for(int i = 0; i < steps; i++)
                    if((getValue().intValue() > 0)) setValue(Math.round((getValue()-1)*10)/10.0);
            }

            @Override
            public void increment(int steps) {
                for(int i = 0; i < steps; i++)
                    if((getValue().intValue() <= 100)) setValue(Math.round((getValue()+1)*10)/10.0);
            }
        };
        factory.setValue(Hyundai_lite.aimVeloc);
        Aim_Veloc.setValueFactory(factory);
        updateStats();
        sentinel.scheduleAtFixedRate(sent, 0, timeChange);
    }

    @FXML
    private void updateStats() {
        Platform.runLater(() ->{
            KP = P.getValue();
            KI = I.getValue();
            KD = D.getValue();
            Hyundai_lite.setAimVeloc(Aim_Veloc.getValue());
        });
    }

    @FXML
    private void updatePID(){ //utilizes the PID formula
        double error = Hyundai_lite.getAimVeloc() - Hyundai_lite.getCurrentVeloc(); // calculates the current Error
        this.accumError += timeChange *  error / 1000.0;  //this is akin to taking the integral
        double PIDAdjust = (KP*error) +(KI*accumError*(timeChange/1000.0))+(KD*(error-prevError)/(timeChange/1000.0));
        Hyundai_lite.setAcceleration(PIDAdjust); //sets adjustment
        this.prevError = error; // calculates the "new" prev error
        Platform.runLater(() ->{
            Curr_Veloc.setText(""+(Math.round(Hyundai_lite.getCurrentVeloc()*10)/10.0));
        });

    }

    public void setListeners(Stage stage){
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                sentinel.cancel();
                sentinel.purge();
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public boolean pvEqualsSP(){
        double pv = Math.round(Hyundai_lite.getCurrentVeloc()*100.0);
        double sp = Math.round(Hyundai_lite.getAimVeloc()*100.0);
        return pv == sp;
    }

    static class Car {
        private double currentVeloc;
        private double aimVeloc;
        private double acceleration = 0; //moves ___ miles per second

        Car(double initVeloc, double aimVeloc) {
            this.currentVeloc = initVeloc;
            this.aimVeloc = aimVeloc;
        }

        public void setAcceleration(double acceleration) {
            this.acceleration = acceleration;
        }

        public void setAimVeloc(double aimVeloc) {
            this.aimVeloc = aimVeloc;
        }

        public void accel_move() {
            this.currentVeloc += acceleration;
        }

        public double getCurrentVeloc() {
            return currentVeloc;
        }

        public double getAimVeloc() {
            return aimVeloc;
        }

    }
}