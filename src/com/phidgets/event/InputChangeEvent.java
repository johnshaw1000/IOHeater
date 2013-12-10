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
public class InputChangeEvent {
    private final int index;
    private final boolean state;
    private final Phidget source;
    
    public InputChangeEvent(Phidget source, int index, boolean state) {
        this.index = index;
        this.state = state;
        this.source = source;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public Phidget getSource() {
        return this.source;
    }
    
    public boolean getState() {
        return this.state;
    }
}
