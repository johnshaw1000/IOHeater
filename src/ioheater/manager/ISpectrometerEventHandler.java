/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ioheater.manager;

/**
 * Event raised for a state change on the interface kit.
 * @author jshaw
 */
public interface ISpectrometerEventHandler {

    /**
     * Input state changed to "Low".
     */
    void spectrometerProximityOn();

    /**
     * Input state changed to "High".
     */
    void spectrometerProximityOff();

    /**
     * Output state changed to "Low".
     */
    void pumpStateOff();

    /**
     * Output state changed to "High".
     */
    void pumpStateOn();
}
