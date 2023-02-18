package test;

import java.util.List;

import activeSegmentation.gui.ABrowser;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.application.Platform;
import ij.IJ;
 
@SuppressWarnings("restriction")
public class WebHelperTest extends Application {
    private Scene scene;
    
    private String webhlp="";
   
    private ABrowser browser;
     
    @Override 
    public void start(Stage stage) {
        // create the scene
        stage.setTitle("Help Browser");
        IJ.log("browser ... ");
        browser= new ABrowser(webhlp);
        scene = new Scene(browser, 750, 500, Color.web("#666970"));
        stage.setScene(scene);
        Platform.setImplicitExit(true);
        String cssurl= WebHelperTest.class.getResource("/help.css").toExternalForm();
        IJ.log("local resource "+cssurl);
        scene.getStylesheets().add(cssurl);        
        stage.show();
    }
    
    @Override
	public void init() {
    	Parameters params =this.getParameters();
    	List<String> lst=params.getRaw();
    	//System.out.println(lst);
    	
    	if (!lst.isEmpty()) {
    		IJ.log("init "+lst.get(0));
    		setWebHelp(lst.get(0));
    	}
    }
 
    public static void main(String[] args){
        launch("help.html");
         
    }

	public String getWebHelp() {
		return webhlp;
	}

	public void setWebHelp(String webhlp) {
		
		String hlpfile=  WebHelperTest.class.getResource(webhlp).toExternalForm();
		IJ.log("local resource "+hlpfile);
		this.webhlp = hlpfile;
	}



}
