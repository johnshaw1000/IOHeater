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
public interface IInterfaceKitStateEventHandler {

    /**
     * Input state changed to "Low".
     */
    void inputStateChangedLow();

    /**
     * Input state changed to "High".
     */
    void inputStateChangedHigh();

    /**
     * Output state changed to "Low".
     */
    void outputStateChangedLow();

    /**
     * Output state changed to "High".
     */
    void outputStateChangedHigh();
}
