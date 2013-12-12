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
    
    /**
     *
     * @param source
     * @param index
     * @param state
     */
    public InputChangeEvent(Phidget source, int index, boolean state) {
        this.index = index;
        this.state = state;
        this.source = source;
    }
    
    /**
     *
     * @return
     */
    public int getIndex() {
        return this.index;
    }
    
    /**
     *
     * @return
     */
    public Phidget getSource() {
        return this.source;
    }
    
    /**
     *
     * @return
     */
    public boolean getState() {
        return this.state;
    }
}
