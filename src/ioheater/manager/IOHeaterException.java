/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ioheater.manager;

import com.phidgets.PhidgetException;

/**
 *
 * @author jshaw
 */
public class IOHeaterException extends Exception {
    private boolean logged = false;
    private String friendlyMessage = null;
    
    IOHeaterException(String friendlyMessage, PhidgetException pe) {
        super(pe.getMessage());
        this.friendlyMessage = friendlyMessage;
    }
    
    @Override
    public String getMessage() {
        if (this.friendlyMessage != null) {
            return this.friendlyMessage;
        }
        
        return super.getMessage();
    }
    
    public boolean isLogged() {
        return this.logged;
    }
    
    public void setLogged() {
        this.logged = true;
    }
}
