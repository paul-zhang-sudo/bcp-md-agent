package com.bsi.md.agent.sap;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import java.util.HashMap;
import java.util.Properties;

/**
 * sap provider类
 * @author fish
 */
public  class AgDestinationDataProvider implements DestinationDataProvider
{
        private DestinationDataEventListener eL;
        private HashMap<String, Properties> secureDBStorage = new HashMap<String, Properties>();
        
        public Properties getDestinationProperties(String destinationName)
        {
            try
            {
                Properties p = secureDBStorage.get(destinationName);
                if(p!=null)
                {
                    if(p.isEmpty())
                        throw new DataProviderException(DataProviderException.Reason.INVALID_CONFIGURATION, "destination configuration is incorrect", null);
                    return p;
                }
                return null;
            }
            catch(RuntimeException re)
            {
                throw new DataProviderException(DataProviderException.Reason.INTERNAL_ERROR, re);
            }
        }

        public void setDestinationDataEventListener(DestinationDataEventListener eventListener)
        {
            this.eL = eventListener;
        }

        public boolean supportsEvents()
        {
            return true;
        }

        void changeProperties(String destName, Properties properties)
        {
            synchronized(secureDBStorage)
            {
                if(properties==null)
                {
                    if(secureDBStorage.remove(destName)!=null)
                        eL.deleted(destName);
                }
                else 
                {
                    secureDBStorage.put(destName, properties);
                    eL.updated(destName); // create or updated
                }
            }
        }


    public void testConnect(String destName)
    {
        JCoDestination dest;
        try
        {
            dest = JCoDestinationManager.getDestination(destName);
            dest.ping();
            System.out.println("Destination " + destName + " works");
        }
        catch(JCoException e)
        {
            e.printStackTrace();
            System.out.println("Execution on destination " + destName+ " failed");
        }
    }
}
