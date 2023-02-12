package test;

import java.util.List;
 
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.application.Platform;
import activeSegmentation.ABrowser;
 
@SuppressWarnings("restriction")
public class WebHelper extends Application {
    private Scene scene;
    
    private String webhlp="";
   
    private ABrowser browser;
     
    @Override 
    public void start(Stage stage) {
        // create the scene
        stage.setTitle("Help Browser");
        browser= new ABrowser(webhlp);
        scene = new Scene(browser, 750, 500, Color.web("#666970"));
        stage.setScene(scene);
        Platform.setImplicitExit(true);
      //  scene.getStylesheets().add("webviewsample/BrowserToolbar.css");        
        stage.show();
    }
    
    @Override
	public void init() {
    	//setWebHelp("help.html");
    	Parameters params =this.getParameters();
    	List<String> lst=params.getRaw();
    	System.out.println(lst);
    	if (!lst.isEmpty()) {
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
		String hlpfile= WebHelper.class.getResource(webhlp).toExternalForm();
		this.webhlp = hlpfile;
	}



}
