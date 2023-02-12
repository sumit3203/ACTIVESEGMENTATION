package activeSegmentation;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.application.Platform;

@SuppressWarnings("restriction")
public class ABrowser extends Region {
 
    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();
     
    public ABrowser(String resource) {
        //apply the styles
       getStyleClass().add("browser");
       try {
		///String hlpfile= WebHelper.class.getResource(resource).toExternalForm();
		   webEngine.load(resource);
	} catch (RuntimeException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       //add the web view to the scene
       getChildren().add(browser);
 
    }
    
    /*
    public void load(String url) {
    	 webEngine.load(url);
    }
     */
    
    @Override 
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
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