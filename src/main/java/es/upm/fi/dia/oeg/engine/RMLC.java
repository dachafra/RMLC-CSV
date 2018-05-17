package es.upm.fi.dia.oeg.engine;

import org.json.JSONObject;

/**
 * Hello world!
 *
 */
public class RMLC
{
    public static void main( String[] args )
    {
        RMLCConfig rmlcConfig = new RMLCConfig();
        JSONObject config;
        if(args.length>0)
            config=rmlcConfig.getConfig(args[0]);
        else
            config=rmlcConfig.getConfig( "config.json");

        rmlcConfig.downloadAndUnzip();

        RMLCProcess rmlcProcess = new RMLCProcess(config);
        rmlcProcess.run();


    }
}
