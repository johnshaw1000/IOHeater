/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ioheater.manager;

/**
 *
 * @author jshaw
 */
public interface IInterfaceKitStateEventHandler {
    void inputStateChangedLow();
    void inputStateChangedHigh();
    void outputStateChangedLow();
    void outputStateChangedHigh();
}
