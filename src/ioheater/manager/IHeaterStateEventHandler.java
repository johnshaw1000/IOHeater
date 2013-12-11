/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ioheater.manager;

/**
 * Handler for HeaterStateEvent.
 * @author jshaw
 */
public interface IHeaterStateEventHandler {

    /**
     * Heater stopped event.
     */
    void heaterStopped();

    /**
     * Heater started event.
     */
    void heaterStarted();
}
