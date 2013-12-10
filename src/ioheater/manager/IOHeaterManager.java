/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ioheater.manager;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.Phidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.ErrorEvent;
import com.phidgets.event.ErrorListener;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.OutputChangeEvent;
import com.phidgets.event.OutputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author jshaw
 */
public class IOHeaterManager implements AttachListener,
        DetachListener, ErrorListener, InputChangeListener,
        OutputChangeListener, SensorChangeListener {

    private static final Logger logger = Logger.getLogger("ioheater.manager.ioheatermanager");
    private InterfaceKitPhidget ik;
    private IHeaterStateEventHandler heaterStateEventHandler;
    private ITemperatureChangeEventHandler temperatureChangeEventHandler;
    private IInterfaceKitStateEventHandler interfaceKitStateEventHandler;
    private static final int TEMPERATURE_SENSOR_ANALOGUE_PIN = 0;
    private static final int TEMPERATURE_SENSOR_TRIGGER_DEFAULT = 10;
    private static final int HEATER_DIGITAL_INPUT_PIN = 1;
    private static final int HEATER_DIGITAL_OUTPUT_PIN = 2;
    private float targetTemperature = 0;
    private boolean isTemperatureManagementActive = false;

    private IOHeaterManager() {
    }

    public IOHeaterManager(IHeaterStateEventHandler heaterStateEventHandler,
                ITemperatureChangeEventHandler temperatureChangeEventHandler,
                IInterfaceKitStateEventHandler interfaceKitStateEventHandler) {
        this.heaterStateEventHandler = heaterStateEventHandler;
        this.temperatureChangeEventHandler = temperatureChangeEventHandler;
        this.interfaceKitStateEventHandler = interfaceKitStateEventHandler;
    }

    public void close() {
        try {
            ik.close();
            ik = null;
        } catch (PhidgetException ex) {
            logger.log(Level.WARNING, "Exception error in close", ex);
        }
    }

    @Override
    public void attached(AttachEvent ae) {
        logger.log(Level.WARNING, "attachment of {0}", ae);
    }
    
    @Override
    public void detached(DetachEvent ae) {
        logger.log(Level.WARNING, "detachment of {0}", ae);
    }

    @Override
    public void error(ErrorEvent ee) {
        logger.info(ee.toString());
    }

    @Override
    public void inputChanged(InputChangeEvent ie) {
        try {
            if (ik.getInputState(ie.getIndex())) {
                this.interfaceKitStateEventHandler.inputStateChangedHigh();
            } else {
                this.interfaceKitStateEventHandler.inputStateChangedLow();
            }
        } catch (PhidgetException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        
        startNewTrigger(ie.getIndex());
    }
    
    @Override
    public void outputChanged(OutputChangeEvent oe) {
        try {
            if (ik.getOutputState(HEATER_DIGITAL_OUTPUT_PIN)) {
                this.interfaceKitStateEventHandler.outputStateChangedHigh();
            } else {
                this.interfaceKitStateEventHandler.outputStateChangedLow();
            }
        } catch (PhidgetException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    @Override
    public void sensorChanged(SensorChangeEvent se){
        logger.info(String.format("SensorChangeEvent, value = %d", se.getValue()));
        try {
            temperatureChanged(se.getValue());
        } catch (IOHeaterException e) {
            if (!e.isLogged()) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                e.setLogged();
            }
        }
    }

    public void runPhidget() {
        //Example of enabling logging.
        //Phidget.enableLogging(Phidget.PHIDGET_LOG_VERBOSE, null);
        
        logger.info(getImplementationLibraryVersion());
        try {
            ik = new InterfaceKitPhidget();

            ik.addAttachListener(this);
            ik.addDetachListener(this);
            ik.addErrorListener(this);
            ik.addInputChangeListener(this);
            ik.addOutputChangeListener(this);
            ik.addSensorChangeListener(this);

            ik.openAny();
            logger.info("waiting for InterfaceKit attachment...");
            ik.waitForAttachment();
            logger.info("InterfaceKit attached.");

            logger.info(ik.getDeviceName());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // re-add back in the interrupt bit
                Thread.currentThread().interrupt();
            }

            if (ik.getInputCount() > 8) {
                logger.log(Level.WARNING, "input(7,8) = ({0},{1})", new Object[]{ik.getInputState(7), ik.getInputState(8)});
            }

            this.initialiseHeater();
            
            ik.setSensorChangeTrigger(TEMPERATURE_SENSOR_ANALOGUE_PIN, TEMPERATURE_SENSOR_TRIGGER_DEFAULT);
        } catch (PhidgetException ex) {
            logger.log(Level.WARNING, "Exception error in runPhidget", ex);
        }
    }
    
    public void setSensorChangeTrigger(int val) throws IOHeaterException {
        try {
            ik.setSensorChangeTrigger(TEMPERATURE_SENSOR_ANALOGUE_PIN, val);
        } catch (PhidgetException pe) {
            logger.log(Level.SEVERE, "Exception in setSensorChangeTrigger", pe);
            IOHeaterException e = new IOHeaterException("Unable to set sensor change trigger", pe);
            throw e;
        }
    }
    
    public void temperatureChanged(int sensorValue) throws IOHeaterException {
        logger.info(String.format("Sensor value is %d", sensorValue));
        this.temperatureChangeEventHandler.temperatureChanged(convertRawSensorValue(sensorValue));
        manageHeater();
    }
    
    private float convertRawSensorValue(int rawValue) {
        return (float)((rawValue * 0.22222) - 61.11);
    }

    public float getTemperature() {
        int sensorValue = 0;
        try {
            // Get the data from analog input
            sensorValue = ik.getSensorRawValue(TEMPERATURE_SENSOR_ANALOGUE_PIN);
        } catch (PhidgetException ex) {
            logger.log(Level.WARNING, "Exception error in runPhidget", ex);
        }

        return convertRawSensorValue(sensorValue);
    }

    public static String getImplementationLibraryVersion() {
        return Phidget.getLibraryVersion();
    }

    public void startTemperatureManagement(float targetTemperature) throws IOHeaterException {
        this.targetTemperature = targetTemperature;
        this.isTemperatureManagementActive = true;
        this.manageHeater();
    }
    
    private void manageHeater() throws IOHeaterException {
        if (this.isTemperatureManagementActive) {
            if (this.getTemperature() >= this.targetTemperature) {
                this.stopHeater();
            } else {
                this.startHeater();
            }
        }
    }

    public void stopTemperatureManagement() throws IOHeaterException {
        this.isTemperatureManagementActive = false;
        stopHeater();
    }
    
    private void startHeater() throws IOHeaterException {
        if (!this.isHeaterOn()) {
            this.setDigitalOutput(HEATER_DIGITAL_INPUT_PIN, true);
            this.heaterStateEventHandler.heaterStarted();
        }
    }
    
    private void stopHeater() throws IOHeaterException {
        if (this.isHeaterOn()) {
            this.setDigitalOutput(HEATER_DIGITAL_INPUT_PIN, false);
            this.heaterStateEventHandler.heaterStopped();
        }
    }

    private void setDigitalOutput(int digPin, boolean digState) {
        try {
            ik.setOutputState(digPin, digState);
        } catch (PhidgetException ex) {
            logger.log(Level.WARNING, "Exception error in changeOutput", ex);
        }
    }

    public boolean isHeaterOn() throws IOHeaterException {
        return getDigitalOutput(1);
    }

    private boolean getDigitalOutput(int digPin) throws IOHeaterException {
        try {
            return ik.getOutputState(digPin);
        } catch (PhidgetException pe) {
            logger.log(Level.SEVERE, "Unable to get digital output", pe);
            throw new IOHeaterException("Unable to get digital output", pe);
        }
    }

    private boolean getDigitalInput(int digPin) {
        boolean digState = false;
        try {
            digState = ik.getInputState(digPin);
        } catch (PhidgetException ex) {
            logger.log(Level.WARNING, "Exception error in changeOutput", ex);
        }
        return (digState);
    }

    private void startNewTrigger(final int digPin) {
        if (triggerTask != null) {
            triggerTask.cancel(true);
        }
        triggerTask = triggerExecutor.submit(new Runnable() {
            @Override
            public void run() {
                changeOutput(digPin);
            }
        });
    }

    private boolean changeOutput(int digPin) {
        boolean pinStatus = this.getDigitalInput(digPin);
        if(pinStatus) {
            this.heaterStateEventHandler.heaterStarted();
        }
        else {
            this.heaterStateEventHandler.heaterStopped();
        }
        return (pinStatus);
    }
    
    private void initialiseHeater() {
        try {
            this.stopHeater();
        } catch (IOHeaterException e) {
            if (!e.isLogged()) {
                logger.log(Level.WARNING, "Unable to stop heater during initialise", e);
            }
        }
    }

    private final ExecutorService triggerExecutor = Executors.newSingleThreadExecutor();
    private Future<?> triggerTask;
}