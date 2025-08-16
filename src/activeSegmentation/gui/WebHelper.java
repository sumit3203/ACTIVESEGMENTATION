package activeSegmentation.gui;

import java.util.List;
 
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
//import javafx.application.Platform;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import ij.IJ;
 
public class WebHelper extends Application {
    private Scene scene;
    
    private static String webhlp="";
   
    private ABrowser browser;

	private static String cssfile="";
     
    @Override 
    public void start(Stage stage) {
        stage.setTitle("Help Browser");
        IJ.log("browser ... ");
        browser = new ABrowser(webhlp);
        scene = new Scene(browser, 750, 500, Color.web("#666970"));
        stage.setScene(scene);

        scene.getStylesheets().add(cssfile);

        // Load and inject JavaScript after the page is loaded
        /*
        WebEngine engine = browser.getEngine();
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Read the JS file as a String
                try (InputStream in = getClass().getResourceAsStream("/tex-chtml-full.js")) {
                    if (in != null) {
                        String js = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        engine.executeScript(js);
                    } else {
                        System.err.println("JS file not found!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });*/

        stage.show();
    }
    
    @Override
	public void init() {
    	Parameters params =getParameters();
    	List<String> lst=params.getRaw();
    	//System.out.println(lst);
    	
    	if (!lst.isEmpty()) {
    		 IJ.log("init "+lst.get(0));
    		setWebHelp(lst.get(0));
    	}
    }
    
    /**
     * 
     * @param args
     */
    public static void main(String[] args){
        launch("/help.html");         
    }

    /**
     * 
     * @return
     */
	public String getWebHelp() {
		return webhlp;
	}

	/**
	 * 
	 * @param webhlp
	 */
	public void setWebHelp(String webhlp) {
		
		String hlpfile=  WebHelper.class.getResource(webhlp).toExternalForm();
		IJ.log("local "+hlpfile);
		this.webhlp = hlpfile;
		String ker= webhlp.substring(0, webhlp.length()-4);
		//System.out.println(ker+"css");
		 cssfile=WebHelper.class.getResource(ker+"css").toExternalForm();
       // scene.getStylesheets().add(cssfile);        
        IJ.log("local "+cssfile);
	}



}
