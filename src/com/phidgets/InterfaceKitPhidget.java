/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.phidgets;

import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachListener;
import com.phidgets.event.ErrorListener;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.OutputChangeEvent;
import com.phidgets.event.OutputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

/**
 *
 * @author jshaw
 */
public class InterfaceKitPhidget extends Phidget {
    private SensorChangeListener sensorChangeListener;
    private OutputChangeListener outputChangeListener;
    private InputChangeListener inputChangeListener;
    private int sensorRawValuePrevious = 365;
    private int sensorRawValue = 365;
    private int sensorChangeTrigger = 10;
    private static InterfaceKitPhidget instance = null;
    private boolean inputState = false;
    private boolean outputState = false;
    
    /**
     * Constructor
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public InterfaceKitPhidget() {
        instance = this;
    }
    
    /**
     * The close method.
     * @throws PhidgetException
     */
    public void close() throws PhidgetException {
    }
    
    /**
     * The addAttachListener method.
     * @param al
     */
    public void addAttachListener(AttachListener al) {
    }
    
    /**
     *The addDetachListener method.
     * @param dl
     */
    public void addDetachListener(DetachListener dl) {
    }
    
    /**
     * The addErrorListener method.
     * @param el
     */
    public void addErrorListener(ErrorListener el) {
    }
    
    /**
     * The addInputChangeListener method.
     * @param icl
     */
    public void addInputChangeListener(InputChangeListener icl) {
        this.inputChangeListener = icl;
    }
    
    /**
     * The addOutputChangeListener method.
     * @param ocl
     */
    public void addOutputChangeListener(OutputChangeListener ocl) {
        this.outputChangeListener = ocl;
    }
    
    /**
     *
     * @param scl
     */
    public void addSensorChangeListener(SensorChangeListener scl) {
        this.sensorChangeListener = scl;
    }
    
    /**
     *
     * @throws PhidgetException
     */
    public void openAny() throws PhidgetException {
    }
    
    /**
     *
     * @throws PhidgetException
     */
    public void waitForAttachment() throws PhidgetException {
    }
    
    /**
     *
     * @return
     * @throws PhidgetException
     */
    public String getDeviceName() throws PhidgetException {
        return null;
    }
    
    /**
     *
     * @return
     * @throws PhidgetException
     */
    public int getInputCount() throws PhidgetException {
        return 0;
    }
    
    /**
     *
     * @param input
     * @return
     * @throws PhidgetException
     */
    public boolean getInputState(int input) throws PhidgetException {
        return this.inputState;
    }
    
    /**
     *
     * @param digPin
     * @return
     * @throws PhidgetException
     */
    public boolean getOutputState(int digPin) throws PhidgetException {
        return this.outputState;
    }
    
    /**
     *
     * @return
     * @throws PhidgetException
     */
    public int getOutputCount() throws PhidgetException {
        return 0;
    }
    
    /**
     *
     * @param digPin
     * @param value
     * @throws PhidgetException
     */
    public void setOutputState(int digPin, boolean value) throws PhidgetException {
        this.outputState = value;
        this.outputChangeListener.outputChanged(new OutputChangeEvent(this, digPin, value));
        
        this.inputState = value;
        this.inputChangeListener.inputChanged(new InputChangeEvent(this, digPin, value));
    }
    
    /**
     *
     * @param state
     * @return
     * @throws PhidgetException
     */
    public int getSensorRawValue(int state) throws PhidgetException {
        return this.sensorRawValue;
    }
    
    /**
     *
     * @param sensor
     * @param trigger
     * @throws PhidgetException
     */
    public void setSensorChangeTrigger(int sensor, int trigger) throws PhidgetException {
        if (sensor == 0) {
            this.sensorChangeTrigger = trigger;
            this.resetSensorRawValuePrevious();
        }
    }
    
    private void resetSensorRawValuePrevious() {
        this.sensorRawValuePrevious = this.sensorRawValue;
    }
    
    /* TEST METHODS ONLY ******************************************************/

    /**
     *
     * @return
     */
    
    
    public static InterfaceKitPhidget getInstance() {
        return instance;
    }

    /**
     *
     * @param val
     */
    public void setSensorRawValue(int val) {
        this.sensorRawValue = val;
        
        if (Math.abs(this.sensorRawValue - this.sensorRawValuePrevious) >= this.sensorChangeTrigger) {
            // Change event triggered
            this.resetSensorRawValuePrevious();
            this.sensorChangeListener.sensorChanged(new SensorChangeEvent(null, 0, this.sensorRawValue));
        }
    }
}
