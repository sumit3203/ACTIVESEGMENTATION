package activeSegmentation.gui;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

//import java.net.MalformedURLException;
import java.net.*;

import ij.IJ;
import javafx.application.Platform;

public class ABrowser extends Region {
 
    private final WebView browser = new WebView();
    private final WebEngine webEngine = browser.getEngine();
     
    public ABrowser(String resource) {
        //apply the styles
       getStyleClass().add("browser");

       IJ.log("loading browser "+ resource);
       webEngine.load(resource);
 
       //add the web view to the scene
       getChildren().add(browser);
 
    }
    
    /*
    public void loadURL(final String url) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String tmp="";
				try {
					tmp = new java.net.URI(url).toASCIIString();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
 
            	webEngine.load(tmp);
            }
        });
    }
    */
    
    public void loadURL(final String url) {
        Platform.runLater(() -> {
            String tmp = "";
            try {
                tmp = new URI(url).toASCIIString();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            webEngine.load(tmp);
        });
    }
    
    @Override 
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }
 
    @Override 
    protected double computePrefWidth(double height) {
        return 750;
    }
 
    @Override 
    protected double computePrefHeight(double width) {
        return 500;
    }
    
 // JavaScript interface object
    public class JavaApp {
 
        public void exit() {
            Platform.exit();
        }
    }
}