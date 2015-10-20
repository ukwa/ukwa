import play.*;


public class Global extends GlobalSettings {
    
    @Override
	public void onStart(Application app) {
      Logger.info("Application startup...");
      controllers.Application.w3act.checkForUpdate();
    }
    
    @Override
    public void onStop(Application app) {
      Logger.info("Application shutdown...");
      controllers.Application.w3act.close();
    }     
}
