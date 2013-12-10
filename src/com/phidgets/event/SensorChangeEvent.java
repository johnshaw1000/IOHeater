/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.phidgets.event;

import com.phidgets.Phidget;

/**
 *
 * @author jshaw
 */
public class SensorChangeEvent {
    private int value;
    
    public SensorChangeEvent(Phidget source,
                         int index,
                         int value) {
        this.value = value;
    }
    
    public int getValue() {
        return this.value;
    }
}
