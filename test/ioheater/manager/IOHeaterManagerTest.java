/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ioheater.manager;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jshaw
 */
public class IOHeaterManagerTest implements IHeaterStateEventHandler,
        ITemperatureChangeEventHandler, ISpectrometerEventHandler {
    
    private boolean heaterManagerOn = false;
    private boolean heaterOn = false;
    private float temperature = 0;
    private String inputState = "Unknown";
    private String outputState = "Unknown";
    
    @Override
    public void heaterStopped() {
        heaterOn = false;
    }
    
    @Override
    public void heaterStarted() {
        heaterOn = true;
    }
    
    /**
     *
     * @param temperature
     */
    @Override
    public void temperatureChanged(float temperature) {
        this.temperature = temperature;
    }
    
    @Override
    public void spectrometerProximityOn() {
        this.inputState = "Low";
    }
    
    @Override
    public void spectrometerProximityOff() {
        this.inputState = "High";
    }
    
    @Override
    public void pumpStateOff() {
        this.outputState = "Low";
    }
    
    @Override
    public void pumpStateOn() {
        this.outputState = "High";
    }

    @Override
    public void heaterManagerStopped() {
        this.heaterManagerOn = false;
    }
    
    /**
     *
     */
    public IOHeaterManagerTest() {
    }
    
    /**
     *
     */
    @BeforeClass
    public static void setUpClass() {
    }
    
    /**
     *
     */
    @AfterClass
    public static void tearDownClass() {
    }
    
    /**
     *
     */
    @Before
    public void setUp() {
        heaterOn = false;
    }
    
    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     *
     */
    @Test
    public void initialise() {
        // Arrange
        IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);
        
        // Act
        
        // Assert
        Assert.assertNotNull(unitUnderTest);
        Assert.assertFalse(this.heaterOn);
    }
    
    /**
     *
     */
    @Test
    public void getImplementationLibraryVersion() {
        // Arrange
        String expected = "Fake";
                
        // Act
        String actual = IOHeaterManager.getImplementationLibraryVersion();
                
        // Assert
        Assert.assertEquals(expected, actual);
    }
    
    /**
     *
     */
    @Test
    public void runPhidget() {
        // Arrange
        IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);
        
        // Act
        unitUnderTest.runPhidget();
        
        // Assert
        Assert.assertFalse(this.heaterOn);
    }
    
    /**
     *
     */
    @Test
    public void getTemperature37() {
        // Arrange
        IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);
        unitUnderTest.runPhidget();
        InterfaceKitPhidget.getInstance().setSensorRawValue(441);
        
        // Act
        float expected = 37;
        float actual = unitUnderTest.getTemperature();
        
        // Assert
        Assert.assertEquals(expected, actual, 1);
    }

    /**
     *
     */
    @Test
    public void getTemperature32_4() {
        // Arrange
        IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);
        unitUnderTest.runPhidget();
        InterfaceKitPhidget.getInstance().setSensorRawValue(421);
        
        // Act
        float expected = 32.4f;
        float actual = unitUnderTest.getTemperature();
        
        // Assert
        Assert.assertEquals(expected, actual, 1);
    }

    /**
     *
     */
    @Test
    public void getTemperature29() {
        // Arrange
        IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);
        unitUnderTest.runPhidget();
        InterfaceKitPhidget.getInstance().setSensorRawValue(405);
        
        // Act
        float expected = 29;
        float actual = unitUnderTest.getTemperature();
        
        // Assert
        Assert.assertEquals(expected, actual, 1);
    }

    /**
     *
     */
    @Test
    public void startTemperatureManagement37() {
        try {
            // Arrange
            IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);
            unitUnderTest.runPhidget();
            InterfaceKitPhidget.getInstance();

            // Act
            InterfaceKitPhidget.getInstance().setSensorRawValue(441);
            unitUnderTest.startTemperatureManagement(37.0f);
            InterfaceKitPhidget.getInstance().setSensorChangeTrigger(1, 10000);
            InterfaceKitPhidget.getInstance().setSensorRawValue(703);
            InterfaceKitPhidget.getInstance().setSensorChangeTrigger(1, 10);
            InterfaceKitPhidget.getInstance().setSensorRawValue(713);

            // Assert
            Assert.assertFalse(this.heaterOn);
        } catch (IOHeaterException e) {
            Assert.fail(String.format("Test failed with exception: %s", e.getMessage()));
        } catch (PhidgetException e) {
            Assert.fail(String.format("Test failed with exception: %s", e.getMessage()));
        }
    }

    /**
     *
     */
    @Test
    public void startTemperatureManagement29() {
        try {
            // Arrange
            IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);
            unitUnderTest.runPhidget();
            unitUnderTest.setSensorChangeTrigger(10);
            InterfaceKitPhidget.getInstance();
            InterfaceKitPhidget.getInstance().setSensorRawValue(390);

            // Act
            unitUnderTest.startTemperatureManagement(29.0f);
            InterfaceKitPhidget.getInstance().setSensorRawValue(400);
            InterfaceKitPhidget.getInstance().setSensorRawValue(410);
            InterfaceKitPhidget.getInstance().setSensorRawValue(400);
            InterfaceKitPhidget.getInstance().setSensorRawValue(410);
            InterfaceKitPhidget.getInstance().setSensorRawValue(400);
            InterfaceKitPhidget.getInstance().setSensorRawValue(410);
            InterfaceKitPhidget.getInstance().setSensorRawValue(400);
            InterfaceKitPhidget.getInstance().setSensorRawValue(410);
            InterfaceKitPhidget.getInstance().setSensorRawValue(400);

            // Assert
            Assert.assertTrue(this.heaterOn);
        } catch (IOHeaterException e) {
            Assert.fail(String.format("Test failed with exception: %s", e.getMessage()));
        }
    }

    /**
     *
     */
    @Test
    public void startTemperatureManagement29_1() {
        try {
            // Arrange
            IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);
            unitUnderTest.runPhidget();
            unitUnderTest.setSensorChangeTrigger(1);
            InterfaceKitPhidget.getInstance();
            InterfaceKitPhidget.getInstance().setSensorRawValue(389);

            // Act
            unitUnderTest.startTemperatureManagement(29.0f);
            InterfaceKitPhidget.getInstance().setSensorRawValue(392);
            InterfaceKitPhidget.getInstance().setSensorRawValue(395);
            InterfaceKitPhidget.getInstance().setSensorRawValue(398);
            InterfaceKitPhidget.getInstance().setSensorRawValue(401);
            InterfaceKitPhidget.getInstance().setSensorRawValue(404);
            InterfaceKitPhidget.getInstance().setSensorRawValue(407);
            InterfaceKitPhidget.getInstance().setSensorRawValue(405);
            InterfaceKitPhidget.getInstance().setSensorRawValue(403);
            InterfaceKitPhidget.getInstance().setSensorRawValue(401);

            // Assert
            Assert.assertTrue(this.heaterOn);
        } catch (IOHeaterException e) {
            Assert.fail(String.format("Test failed with exception: %s", e.getMessage()));
        }
    }

    /**
     *
     */
    @Test
    public void startTemperatureManagement29_2() {
        try {
            // Arrange
            IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);

            unitUnderTest.runPhidget();
            InterfaceKitPhidget.getInstance();
            InterfaceKitPhidget.getInstance().setSensorRawValue(400);
            unitUnderTest.setSensorChangeTrigger(100);

            // Act
            unitUnderTest.startTemperatureManagement(29.0f);
            InterfaceKitPhidget.getInstance().setSensorRawValue(420);
            InterfaceKitPhidget.getInstance().setSensorRawValue(440);
            InterfaceKitPhidget.getInstance().setSensorRawValue(460);
            InterfaceKitPhidget.getInstance().setSensorRawValue(480);

            // Assert
            Assert.assertTrue(this.heaterOn);
        } catch (IOHeaterException e) {
            Assert.fail(String.format("Test failed with exception: %s", e.getMessage()));
        }
    }

    /**
     *
     */
    @Test
    public void startTemperatureManagement29_3() {
        try {
            // Arrange
            IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);
            unitUnderTest.runPhidget();
            unitUnderTest.setSensorChangeTrigger(100);
            InterfaceKitPhidget.getInstance();
            InterfaceKitPhidget.getInstance().setSensorRawValue(400);

            // Act
            unitUnderTest.startTemperatureManagement(29.0f);
            InterfaceKitPhidget.getInstance().setSensorRawValue(420);
            InterfaceKitPhidget.getInstance().setSensorRawValue(440);
            InterfaceKitPhidget.getInstance().setSensorRawValue(460);
            InterfaceKitPhidget.getInstance().setSensorRawValue(480);
            InterfaceKitPhidget.getInstance().setSensorRawValue(500);

            // Assert
            Assert.assertFalse(this.heaterOn);
        } catch (IOHeaterException e) {
            Assert.fail(String.format("Test failed with exception: %s", e.getMessage()));
        }
    }

    /**
     *
     */
    @Test
    public void startTemperatureManagement29_4() {
        try {
            // Arrange
            IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);
            unitUnderTest.runPhidget();
            InterfaceKitPhidget.getInstance();
            InterfaceKitPhidget.getInstance().setSensorRawValue(360);
            unitUnderTest.setSensorChangeTrigger(100);

            // Act
            unitUnderTest.startTemperatureManagement(29.0f);
            InterfaceKitPhidget.getInstance().setSensorRawValue(380);
            InterfaceKitPhidget.getInstance().setSensorRawValue(400);
            InterfaceKitPhidget.getInstance().setSensorRawValue(420);
            InterfaceKitPhidget.getInstance().setSensorRawValue(440);
            InterfaceKitPhidget.getInstance().setSensorRawValue(460);
            InterfaceKitPhidget.getInstance().setSensorRawValue(450);
            InterfaceKitPhidget.getInstance().setSensorRawValue(440);
            InterfaceKitPhidget.getInstance().setSensorRawValue(430);
            InterfaceKitPhidget.getInstance().setSensorRawValue(420);
            InterfaceKitPhidget.getInstance().setSensorRawValue(410);
            InterfaceKitPhidget.getInstance().setSensorRawValue(400);
            InterfaceKitPhidget.getInstance().setSensorRawValue(390);

            // Assert
            Assert.assertFalse(this.heaterOn);
        } catch (IOHeaterException e) {
            Assert.fail(String.format("Test failed with exception: %s", e.getMessage()));
        }
    }

    /**
     *
     */
    @Test
    public void startTemperatureManagement29_5() {
        try {
            // Arrange
            IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);
            unitUnderTest.runPhidget();
            InterfaceKitPhidget.getInstance();
            InterfaceKitPhidget.getInstance().setSensorRawValue(380);
            unitUnderTest.setSensorChangeTrigger(100);

            // Act
            unitUnderTest.startTemperatureManagement(29.0f);
            InterfaceKitPhidget.getInstance().setSensorRawValue(420);
            InterfaceKitPhidget.getInstance().setSensorRawValue(440);
            InterfaceKitPhidget.getInstance().setSensorRawValue(460);
            InterfaceKitPhidget.getInstance().setSensorRawValue(500);
            InterfaceKitPhidget.getInstance().setSensorRawValue(490);
            InterfaceKitPhidget.getInstance().setSensorRawValue(480);
            InterfaceKitPhidget.getInstance().setSensorRawValue(470);
            InterfaceKitPhidget.getInstance().setSensorRawValue(460);
            InterfaceKitPhidget.getInstance().setSensorRawValue(440);
            InterfaceKitPhidget.getInstance().setSensorRawValue(430);
            InterfaceKitPhidget.getInstance().setSensorRawValue(420);
            InterfaceKitPhidget.getInstance().setSensorRawValue(410);
            InterfaceKitPhidget.getInstance().setSensorRawValue(400);

            // Assert
            Assert.assertTrue(this.heaterOn);
        } catch (IOHeaterException e) {
            Assert.fail(String.format("Test failed with exception: %s", e.getMessage()));
        }
    }

    /**
     *
     */
    @Test
    public void stopTemperatureManagement() {
        try {
            // Arrange
            IOHeaterManager unitUnderTest = new IOHeaterManager(this, this, this);
            unitUnderTest.runPhidget();
            unitUnderTest.setSensorChangeTrigger(10);
            InterfaceKitPhidget.getInstance();
            InterfaceKitPhidget.getInstance().setSensorRawValue(600);

            // Act
            unitUnderTest.startTemperatureManagement(29.0f);
            InterfaceKitPhidget.getInstance().setSensorRawValue(620);
            InterfaceKitPhidget.getInstance().setSensorRawValue(605);
            unitUnderTest.stopTemperatureManagement();

            // Assert
            Assert.assertFalse(this.heaterOn);
        } catch (IOHeaterException e) {
            Assert.fail(String.format("Test failed with exception: %s", e.getMessage()));
        }
    }
}
