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
    private ISpectrometerEventHandler spectrometerEventHandler;
    private static final int TEMPERATURE_SENSOR_ANALOGUE_PIN = 0;
    private static final int TEMPERATURE_SENSOR_TRIGGER_DEFAULT = 10;
    private static final int HEATER_DIGITAL_OUTPUT_PIN = 1;
    private static final int SPECTROMETER_PROXIMITY_DIGITAL_INPUT_PIN = 0;
    private static final int PUMP_DIGITAL_OUTPUT_PIN = 0;
    private float targetTemperature = 0;
    private boolean isTemperatureManagementActive = false;

    private IOHeaterManager() {
    }

    /**
     * Constructor.
     * @param heaterStateEventHandler
     * @param temperatureChangeEventHandler
     * @param interfaceKitStateEventHandler
     */
    public IOHeaterManager(IHeaterStateEventHandler heaterStateEventHandler,
                ITemperatureChangeEventHandler temperatureChangeEventHandler,
                ISpectrometerEventHandler interfaceKitStateEventHandler) {
        this.heaterStateEventHandler = heaterStateEventHandler;
        this.temperatureChangeEventHandler = temperatureChangeEventHandler;
        this.spectrometerEventHandler = interfaceKitStateEventHandler;
    }

    /**
     * Close the interface kit.
     */
    public void close() {
        try {
            ik.close();
            ik = null;
        } catch (PhidgetException ex) {
            logger.log(Level.WARNING, "Exception error in close", ex);
        }
    }

    /**
     * Interface kit attached event.
     * @param ae
     */
    @Override
    public void attached(AttachEvent ae) {
        logger.log(Level.WARNING, "attachment of {0}", ae);
    }
    
    /**
     * Interface kit detached event.
     * @param ae
     */
    @Override
    public void detached(DetachEvent ae) {
        logger.log(Level.WARNING, "detachment of {0}", ae);
    }

    /**
     * Interface kit error event.
     * @param ee
     */
    @Override
    public void error(ErrorEvent ee) {
        logger.info(ee.toString());
    }

    /**
     * Interface kit input state changed event.
     * @param ie
     */
    @Override
    public void inputChanged(InputChangeEvent ie) {
        try {
            if (this.isSpectrometerProximity()) {
                this.startPump();
                this.stopTemperatureManagement();
                this.spectrometerEventHandler.spectrometerProximityOn();
            } else {
                this.stopPump();
                this.spectrometerEventHandler.spectrometerProximityOff();
            }
        } catch (IOHeaterException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    /**
     * Interface kit output state changed event.
     * @param oe
     */
    @Override
    public void outputChanged(OutputChangeEvent oe) {
        try {
            if (oe.getIndex() == HEATER_DIGITAL_OUTPUT_PIN) {
                if (ik.getOutputState(HEATER_DIGITAL_OUTPUT_PIN)) {
                    this.heaterStateEventHandler.heaterStarted();
                } else {
                    this.heaterStateEventHandler.heaterStopped();
                }
            }
            
            if (oe.getIndex() == PUMP_DIGITAL_OUTPUT_PIN) {
                if (ik.getOutputState(PUMP_DIGITAL_OUTPUT_PIN)) {
                    this.spectrometerEventHandler.pumpStateOn();
                } else {
                    this.spectrometerEventHandler.pumpStateOff();
                }
            }
        } catch (PhidgetException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    /**
     * Interface kit sensor state change event.
     * @param se
     */
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

    /**
     * Run the interface kit.
     */
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
            this.initialisePump();
            this.initialiseProximity();
            
            ik.setSensorChangeTrigger(TEMPERATURE_SENSOR_ANALOGUE_PIN, TEMPERATURE_SENSOR_TRIGGER_DEFAULT);
        } catch (PhidgetException ex) {
            logger.log(Level.WARNING, "Exception error in runPhidget", ex);
        }
    }
    
    /**
     * Set the threshold for the sensor change trigger.
     * @param val
     * @throws IOHeaterException
     */
    public void setSensorChangeTrigger(int val) throws IOHeaterException {
        try {
            ik.setSensorChangeTrigger(TEMPERATURE_SENSOR_ANALOGUE_PIN, val);
        } catch (PhidgetException pe) {
            logger.log(Level.SEVERE, "Exception in setSensorChangeTrigger", pe);
            IOHeaterException e = new IOHeaterException("Unable to set sensor change trigger", pe);
            throw e;
        }
    }
    
    /**
     * Handle temperature change.
     * @param sensorValue
     * @throws IOHeaterException
     */
    public void temperatureChanged(int sensorValue) throws IOHeaterException {
        logger.info(String.format("Sensor value is %d", sensorValue));
        this.temperatureChangeEventHandler.temperatureChanged(convertRawSensorValue(sensorValue));
        manageHeater();
    }
    
    private float convertRawSensorValue(int rawValue) {
        return (float)((rawValue * 0.22222) - 61.11);
    }

    /**
     * Get the temperature from the interface kit.
     * @return
     */
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

    /**
     * Get the implementation library version.
     * @return
     */
    public static String getImplementationLibraryVersion() {
        return Phidget.getLibraryVersion();
    }

    /**
     *
     * @param targetTemperature
     * @throws IOHeaterException
     */
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

    /**
     *
     * @throws IOHeaterException
     */
    public void stopTemperatureManagement() throws IOHeaterException {
        this.isTemperatureManagementActive = false;
        stopHeater();
        this.heaterStateEventHandler.heaterManagerStopped();
    }
    
    private void startHeater() throws IOHeaterException {
        if (!this.isHeaterOn()) {
            this.setDigitalOutput(HEATER_DIGITAL_OUTPUT_PIN, true);
            //this.heaterStateEventHandler.heaterStarted();
        }
    }
    
    private void stopHeater() throws IOHeaterException {
        if (this.isHeaterOn()) {
            this.setDigitalOutput(HEATER_DIGITAL_OUTPUT_PIN, false);
            //this.heaterStateEventHandler.heaterStopped();
        }
    }

    private void setDigitalOutput(int digPin, boolean digState) {
        try {
            ik.setOutputState(digPin, digState);
        } catch (PhidgetException ex) {
            logger.log(Level.WARNING, "Exception error in changeOutput", ex);
        }
    }

    /**
     *
     * @return
     * @throws IOHeaterException
     */
    public boolean isHeaterOn() throws IOHeaterException {
        return getDigitalOutput(HEATER_DIGITAL_OUTPUT_PIN);
    }

    private boolean getDigitalOutput(int digPin) throws IOHeaterException {
        try {
            return ik.getOutputState(digPin);
        } catch (PhidgetException pe) {
            logger.log(Level.SEVERE, "Unable to get digital output", pe);
            throw new IOHeaterException("Unable to get digital output", pe);
        }
    }
    
    private boolean isSpectrometerProximity() {
        return this.getDigitalInput(SPECTROMETER_PROXIMITY_DIGITAL_INPUT_PIN);
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

    private void initialiseHeater() {
        try {
            this.stopHeater();
        } catch (IOHeaterException e) {
            if (!e.isLogged()) {
                logger.log(Level.WARNING, "Unable to stop heater during initialise", e);
            }
        }
    }
    
    private void initialisePump() {
        this.stopPump();
    }
    
    private void initialiseProximity() {
        if (this.isProximity()) {
            this.spectrometerEventHandler.spectrometerProximityOn();
        } else {
            this.spectrometerEventHandler.spectrometerProximityOff();
        }
    }
    
    private boolean isProximity() {
        return this.getDigitalInput(PUMP_DIGITAL_OUTPUT_PIN);
    }
    
    private void stopPump() {
        this.setDigitalOutput(PUMP_DIGITAL_OUTPUT_PIN, false);
    }
    
    private void startPump() {
        this.setDigitalOutput(PUMP_DIGITAL_OUTPUT_PIN, true);
    }
}
