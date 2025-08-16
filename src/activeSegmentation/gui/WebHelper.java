package activeSegmentation.gui;

import java.util.List;
 
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
//import javafx.application.Platform;
import ij.IJ;
 
public class WebHelper extends Application {
    private Scene scene;
    
    private static String webhlp="";
   
    private ABrowser browser;

	private static String cssfile;
     
    @Override 
    public void start(Stage stage) {
 
        // create the scene
        stage.setTitle("Help Browser");
        IJ.log("browser ... ");
        browser= new ABrowser(webhlp);
        scene = new Scene(browser, 750, 500, Color.web("#666970"));
        stage.setScene(scene);

       // stage.setOnCloseRequest(e -> Platform.exit());
     //   stage.setOnCloseRequest(e -> stage.close());
        scene.getStylesheets().add(cssfile);        
      // IJ.log("local "+cssfile);
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
        launch("/help2.html");         
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
